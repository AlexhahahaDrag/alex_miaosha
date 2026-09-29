package com.alex.finance.gift.ai;

import com.alex.common.utils.date.DateUtils;
import com.alex.api.ai.api.AiAnalyzeApi;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.alex.api.ai.vo.AiAnalyzeResp;
import com.alex.api.finance.gift.event.vo.GiftEventTypeItemVo;
import com.alex.api.finance.gift.event.vo.GiftRecordRecommendAmountVo;
import com.alex.api.finance.gift.person.vo.GiftPersonRelationItemVo;
import com.alex.api.finance.gift.record.query.GiftRecordAiParseReq;
import com.alex.api.finance.gift.record.vo.GiftRecordAiParseVo;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.base.common.Result;
import com.alex.finance.gift.event.entity.GiftEventInfo;
import com.alex.finance.gift.event.mapper.GiftEventInfoMapper;
import com.alex.finance.gift.eventoption.entity.GiftEventTypeOption;
import com.alex.finance.gift.eventoption.mapper.GiftEventTypeOptionMapper;
import com.alex.finance.gift.person.entity.GiftPersonInfo;
import com.alex.finance.gift.person.mapper.GiftPersonInfoMapper;
import com.alex.finance.gift.personoption.entity.GiftPersonRelationOption;
import com.alex.finance.gift.personoption.mapper.GiftPersonRelationOptionMapper;
import com.alex.finance.gift.record.entity.GiftRecordInfo;
import com.alex.finance.gift.record.mapper.GiftRecordInfoMapper;
import com.alex.finance.gift.support.GiftDataScopeSupport;
import com.alex.finance.gift.support.GiftEventTypePresetSupport;
import com.alex.finance.gift.support.GiftRelationPresetSupport;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 礼尚往来 AI 智能助手服务：
 * 1. 自然语言快速记账智能解析与数据库实体匹配
 * 2. 礼金智能推荐（人情理由推理 + 场景化贺词/祝福语生成）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GiftAiService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Pattern AMOUNT_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:元|块|RMB|rmb)?");
    private static final Pattern PERSON_GIVE_PATTERN = Pattern.compile("(?:给|随给|包给|随了|送给|随礼给)\\s*([\\u4e00-\\u9fa5a-zA-Z0-9]{2,5}?)(?:给的|送的|随的|包的|的|结婚|大婚|喜宴|满月|生子|乔迁|搬家|做寿|生日|过寿|考上|升学|红包|礼金|\\s|$)");
    private static final Pattern PERSON_RECEIVE_PATTERN = Pattern.compile("(?:收到|收了|来自)\\s*([\\u4e00-\\u9fa5a-zA-Z0-9]{2,5}?)(?:给的|送的|随的|包的|的|结婚|大婚|喜宴|满月|生子|乔迁|搬家|做寿|生日|过寿|考上|升学|红包|礼金|\\s|$)");
    private static final Pattern PERSON_EVENT_PATTERN = Pattern.compile("([\\u4e00-\\u9fa5a-zA-Z0-9]{2,5}?)(?:结婚|大婚|喜宴|满月|生子|乔迁|搬家|做寿|生日|过寿|考上|升学)");

    private final AiAnalyzeApi aiAnalyzeApi;
    private final GiftPersonInfoMapper giftPersonInfoMapper;
    private final GiftPersonRelationOptionMapper giftPersonRelationOptionMapper;
    private final GiftEventTypeOptionMapper giftEventTypeOptionMapper;
    private final GiftEventInfoMapper giftEventInfoMapper;
    private final GiftRecordInfoMapper giftRecordInfoMapper;
    private final GiftEventTypePresetSupport giftEventTypePresetSupport;
    private final GiftRelationPresetSupport giftRelationPresetSupport;
    private final GiftDataScopeSupport giftDataScopeSupport;
    private final ObjectMapper objectMapper;

    /**
     * 自然语言记账智能解析
     */
    public GiftRecordAiParseVo parseQuickLedger(GiftRecordAiParseReq req) {
        if (req == null || !StringUtils.hasText(req.getContent())) {
            return new GiftRecordAiParseVo();
        }

        String rawText = req.getContent().trim();
        GiftRecordAiParseVo vo = new GiftRecordAiParseVo().setRawText(rawText);

        // 1. 调用 AI 进行结构化语义抽取
        boolean aiParsed = false;
        try {
            aiParsed = extractWithAi(rawText, vo);
        } catch (Exception e) {
            log.warn("AI parse quick ledger failed, fallback to local rule parser: {}", e.getMessage());
        }

        // 2. 若 AI 抽取失败或未获得关键信息，走本地规则与正则抽取保底
        if (!aiParsed || !StringUtils.hasText(vo.getPersonName()) || vo.getAmount() == null) {
            extractWithRegexRules(rawText, vo);
        }

        // 3. 关联数据库实体匹配（亲友库、关系词典、事由词典）
        matchDatabaseEntities(vo);

        // 4. 默认时间与方向兜底
        if (vo.getPayTime() == null) {
            vo.setPayTime(DateUtils.now());
        }
        if (!StringUtils.hasText(vo.getDirection())) {
            vo.setDirection("GIVE");
        }

        return vo;
    }

    /**
     * AI 调用抽取结构化记账字段
     */
    private boolean extractWithAi(String rawText, GiftRecordAiParseVo vo) {
        String prompt = "你是一个中国传统人情往来记账助手。请分析用户的自然语言记账描述，提取记账要素并返回纯 JSON 对象（不要使用任何 Markdown 代码块，不要包含 ```json 标签，直接返回 { 开头 } 结尾的 JSON 字符串）。\n"
                + "JSON 字段规范：\n"
                + "{\n"
                + "  \"personName\": \"亲友姓名或称谓（如：张三、李叔叔、王晓峰）\",\n"
                + "  \"relationName\": \"关系称呼（如：朋友、同事、同学、长辈、发小、亲属）\",\n"
                + "  \"direction\": \"流水方向：GIVE（送出/随礼）、RECEIVE（收到礼金）、RETURN（回礼）\",\n"
                + "  \"amount\": 数字金额（如 500、1000.00，纯数字无单位）,\n"
                + "  \"eventTypeName\": \"事由类型（如：婚礼、乔迁、满月、寿宴、升学、春节、白事、拜访）\",\n"
                + "  \"payTime\": \"记账时间（格式 yyyy-MM-dd HH:mm:ss，未指明请填 null）\",\n"
                + "  \"location\": \"地点或酒楼（未提及填 null）\",\n"
                + "  \"remark\": \"补充信息或祝福语（未提及填 null）\"\n"
                + "}\n\n"
                + "用户记账文本：\"" + rawText + "\"";

        AiAnalyzeReq aiReq = new AiAnalyzeReq();
        aiReq.setBizType("gift-ledger-parse");
        aiReq.setContent(prompt);
        aiReq.setDepth(1);

        Result<AiAnalyzeResp> result = aiAnalyzeApi.chat(aiReq);
        if (result != null && result.getData() != null && StringUtils.hasText(result.getData().getSummary())) {
            String summary = result.getData().getSummary().trim();
            // 剥离可能存在的 markdown 包裹
            if (summary.startsWith("```")) {
                int firstBrace = summary.indexOf("{");
                int lastBrace = summary.lastIndexOf("}");
                if (firstBrace >= 0 && lastBrace > firstBrace) {
                    summary = summary.substring(firstBrace, lastBrace + 1);
                }
            }
            try {
                JsonNode root = objectMapper.readTree(summary);
                if (root != null && root.isObject()) {
                    if (root.hasNonNull("personName")) {
                        vo.setPersonName(root.get("personName").asText().trim());
                    }
                    if (root.hasNonNull("relationName")) {
                        vo.setRelationName(root.get("relationName").asText().trim());
                    }
                    if (root.hasNonNull("direction")) {
                        String dir = root.get("direction").asText().trim().toUpperCase();
                        if ("GIVE".equals(dir) || "RECEIVE".equals(dir) || "RETURN".equals(dir)) {
                            vo.setDirection(dir);
                        }
                    }
                    if (root.hasNonNull("amount")) {
                        vo.setAmount(new BigDecimal(root.get("amount").asText()));
                    }
                    if (root.hasNonNull("eventTypeName")) {
                        vo.setEventTypeName(root.get("eventTypeName").asText().trim());
                    }
                    if (root.hasNonNull("location")) {
                        vo.setLocation(root.get("location").asText().trim());
                    }
                    if (root.hasNonNull("remark")) {
                        vo.setRemark(root.get("remark").asText().trim());
                    }
                    if (root.hasNonNull("payTime")) {
                        String timeStr = root.get("payTime").asText().trim();
                        try {
                            vo.setPayTime(LocalDateTime.parse(timeStr, DATE_TIME_FORMATTER));
                        } catch (Exception ignored) {
                        }
                    }
                    return true;
                }
            } catch (Exception e) {
                log.warn("Failed to parse AI JSON response: {}, raw: {}", e.getMessage(), summary);
            }
        }
        return false;
    }

    /**
     * 本地正则与规则提取兜底
     */
    private void extractWithRegexRules(String text, GiftRecordAiParseVo vo) {
        // 1. 流水方向
        if (!StringUtils.hasText(vo.getDirection())) {
            if (text.contains("收礼") || text.contains("收到") || text.contains("收了") || text.contains("给了我")
                    || text.contains("塞给我") || text.contains("随礼给我")) {
                vo.setDirection("RECEIVE");
            } else if (text.contains("回礼") || text.contains("还礼") || text.contains("回赠")) {
                vo.setDirection("RETURN");
            } else {
                vo.setDirection("GIVE");
            }
        }

        // 2. 金额提取
        if (vo.getAmount() == null) {
            Matcher m = AMOUNT_PATTERN.matcher(text);
            if (m.find()) {
                try {
                    vo.setAmount(new BigDecimal(m.group(1)));
                } catch (Exception ignored) {
                }
            }
        }

        // 3. 事由识别
        if (!StringUtils.hasText(vo.getEventTypeName())) {
            if (text.contains("婚") || text.contains("喜酒") || text.contains("结婚")) {
                vo.setEventTypeName("婚礼");
            } else if (text.contains("乔迁") || text.contains("搬家") || text.contains("入宅")) {
                vo.setEventTypeName("乔迁");
            } else if (text.contains("满月") || text.contains("生子") || text.contains("百天") || text.contains("百日")) {
                vo.setEventTypeName("满月");
            } else if (text.contains("升学") || text.contains("高考") || text.contains("大学")) {
                vo.setEventTypeName("升学");
            } else if (text.contains("寿") || text.contains("生日") || text.contains("做寿") || text.contains("庆生")) {
                vo.setEventTypeName("寿宴");
            } else if (text.contains("春节") || text.contains("拜年") || text.contains("压岁钱") || text.contains("过年")) {
                vo.setEventTypeName("春节");
            } else if (text.contains("中秋")) {
                vo.setEventTypeName("中秋");
            } else if (text.contains("端午")) {
                vo.setEventTypeName("端午");
            } else if (text.contains("白事") || text.contains("丧事") || text.contains("吊唁")) {
                vo.setEventTypeName("白事");
            } else if (text.contains("谢") || text.contains("感谢")) {
                vo.setEventTypeName("感谢");
            } else if (text.contains("看望") || text.contains("拜访")) {
                vo.setEventTypeName("拜访");
            } else {
                vo.setEventTypeName("其他");
            }
        }

        // 4. 关系识别
        if (!StringUtils.hasText(vo.getRelationName())) {
            if (text.contains("发小") || text.contains("闺蜜") || text.contains("死党") || text.contains("同学")
                    || text.contains("朋友") || text.contains("同窗") || text.contains("战友")) {
                vo.setRelationName("朋友");
            } else if (text.contains("同事") || text.contains("领导") || text.contains("老板") || text.contains("客户")) {
                vo.setRelationName("同事");
            } else if (text.contains("邻居") || text.contains("街坊")) {
                vo.setRelationName("邻里");
            } else if (text.contains("叔") || text.contains("舅") || text.contains("姨") || text.contains("姑")
                    || text.contains("伯") || text.contains("堂") || text.contains("表") || text.contains("亲戚")
                    || text.contains("长辈") || text.contains("父母") || text.contains("兄") || text.contains("弟")
                    || text.contains("姐") || text.contains("妹")) {
                vo.setRelationName("亲属");
            }
        }

        // 5. 姓名抽取
        if (!StringUtils.hasText(vo.getPersonName())) {
            Matcher m = "RECEIVE".equals(vo.getDirection()) ? PERSON_RECEIVE_PATTERN.matcher(text) : PERSON_GIVE_PATTERN.matcher(text);
            if (m.find()) {
                vo.setPersonName(m.group(1).trim());
            } else {
                Matcher mEvent = PERSON_EVENT_PATTERN.matcher(text);
                if (mEvent.find()) {
                    vo.setPersonName(mEvent.group(1).trim());
                }
            }
        }
    }

    /**
     * 关联系统数据库实体（匹配联系人、关系标签、事由类型）
     */
    private void matchDatabaseEntities(GiftRecordAiParseVo vo) {
        TUserVo loginUser = giftDataScopeSupport.requireLoginUser();
        Long orgId = giftDataScopeSupport.loginOrgId(loginUser);

        // 1. 匹配联系人
        if (StringUtils.hasText(vo.getPersonName())) {
            List<GiftPersonInfo> personList = giftPersonInfoMapper.selectList(new LambdaQueryWrapper<GiftPersonInfo>()
                    .eq(GiftPersonInfo::getIsDelete, 0)
                    .eq(orgId != null, GiftPersonInfo::getOrgId, orgId)
                    .isNull(orgId == null, GiftPersonInfo::getOrgId));

            GiftPersonInfo matchedPerson = null;
            if (personList != null && !personList.isEmpty()) {
                // 优先全字精确匹配
                for (GiftPersonInfo p : personList) {
                    if (vo.getPersonName().equalsIgnoreCase(p.getPersonName())) {
                        matchedPerson = p;
                        break;
                    }
                }
                // 其次模糊包含匹配
                if (matchedPerson == null) {
                    for (GiftPersonInfo p : personList) {
                        if (StringUtils.hasText(p.getPersonName()) &&
                                (p.getPersonName().contains(vo.getPersonName()) || vo.getPersonName().contains(p.getPersonName()))) {
                            matchedPerson = p;
                            break;
                        }
                    }
                }
            }

            if (matchedPerson != null) {
                vo.setPersonId(matchedPerson.getId());
                vo.setIsNewPerson(false);
                // 如果解析未获得关系，沿用人员已有关系
                if (!StringUtils.hasText(vo.getRelationType()) && StringUtils.hasText(matchedPerson.getRelationType())) {
                    vo.setRelationType(matchedPerson.getRelationType());
                }
            } else {
                vo.setPersonId(null);
                vo.setIsNewPerson(true);
            }
        } else {
            vo.setIsNewPerson(false);
        }

        // 2. 匹配关系类型
        matchRelationType(vo, orgId);

        // 3. 匹配事由类型
        matchEventType(vo, orgId);
    }

    private void matchRelationType(GiftRecordAiParseVo vo, Long orgId) {
        String relationSearch = StringUtils.hasText(vo.getRelationName()) ? vo.getRelationName() : vo.getRelationType();
        if (!StringUtils.hasText(relationSearch)) {
            vo.setRelationType("OTHER");
            vo.setRelationName("其他");
            return;
        }

        List<GiftPersonRelationOption> options = giftPersonRelationOptionMapper.selectList(new LambdaQueryWrapper<GiftPersonRelationOption>()
                .eq(GiftPersonRelationOption::getIsDelete, 0)
                .and(w -> w.eq(GiftPersonRelationOption::getUserId, 0L)
                        .or(orgId != null, ow -> ow.eq(GiftPersonRelationOption::getOrgId, orgId))));

        if (options != null) {
            for (GiftPersonRelationOption opt : options) {
                if (relationSearch.equalsIgnoreCase(opt.getRelationLabel()) || relationSearch.equalsIgnoreCase(opt.getRelationCode())) {
                    vo.setRelationType(opt.getRelationCode());
                    vo.setRelationName(opt.getRelationLabel());
                    return;
                }
            }
        }

        // 预设种子匹配兜底
        for (GiftPersonRelationItemVo item : giftRelationPresetSupport.listSystemPresets()) {
            if (relationSearch.equalsIgnoreCase(item.getName())) {
                vo.setRelationType(item.getName());
                vo.setRelationName(item.getName());
                return;
            }
        }

        vo.setRelationType("OTHER");
        vo.setRelationName(StringUtils.hasText(vo.getRelationName()) ? vo.getRelationName() : "其他");
    }

    private void matchEventType(GiftRecordAiParseVo vo, Long orgId) {
        String eventSearch = StringUtils.hasText(vo.getEventTypeName()) ? vo.getEventTypeName() : vo.getEventType();
        if (!StringUtils.hasText(eventSearch)) {
            vo.setEventType("OTHER");
            vo.setEventTypeName("其他");
            return;
        }

        List<GiftEventTypeOption> options = giftEventTypeOptionMapper.selectList(new LambdaQueryWrapper<GiftEventTypeOption>()
                .eq(GiftEventTypeOption::getIsDelete, 0)
                .and(w -> w.eq(GiftEventTypeOption::getUserId, 0L)
                        .or(orgId != null, ow -> ow.eq(GiftEventTypeOption::getOrgId, orgId))));

        if (options != null) {
            for (GiftEventTypeOption opt : options) {
                if (eventSearch.equalsIgnoreCase(opt.getEventLabel()) || eventSearch.equalsIgnoreCase(opt.getEventCode())
                        || (StringUtils.hasText(opt.getEventLabel()) && eventSearch.contains(opt.getEventLabel()))) {
                    vo.setEventType(opt.getEventCode());
                    vo.setEventTypeName(opt.getEventLabel());
                    return;
                }
            }
        }

        for (GiftEventTypeItemVo item : giftEventTypePresetSupport.listSystemPresets()) {
            if (eventSearch.equalsIgnoreCase(item.getName()) || eventSearch.equalsIgnoreCase(item.getEventCode())
                    || (StringUtils.hasText(item.getName()) && eventSearch.contains(item.getName()))) {
                vo.setEventType(item.getEventCode());
                vo.setEventTypeName(item.getName());
                return;
            }
        }

        vo.setEventType("OTHER");
        vo.setEventTypeName(StringUtils.hasText(vo.getEventTypeName()) ? vo.getEventTypeName() : "其他");
    }

    /**
     * 智能礼金推荐增强：AI 人情理由推理与场景化贺词/祝福语生成
     */
    public void enrichRecommendWithAi(GiftRecordRecommendAmountVo vo, Long personId, String eventType, String direction, Long orgId, Long userId) {
        String personName = "该亲友";
        String relationLabel = "亲友";
        BigDecimal lastAmount = null;
        BigDecimal totalGiven = BigDecimal.ZERO;
        BigDecimal totalReceived = BigDecimal.ZERO;

        if (personId != null) {
            GiftPersonInfo person = giftPersonInfoMapper.selectById(personId);
            if (person != null) {
                personName = person.getPersonName();
                if (StringUtils.hasText(person.getRelationType())) {
                    relationLabel = person.getRelationType();
                }
            }

            // 查询历史往来
            List<GiftRecordInfo> records = giftRecordInfoMapper.selectList(new LambdaQueryWrapper<GiftRecordInfo>()
                    .eq(GiftRecordInfo::getIsDelete, 0)
                    .and(w -> w.eq(GiftRecordInfo::getGiverPersonId, personId).or().eq(GiftRecordInfo::getReceiverPersonId, personId))
                    .orderByDesc(GiftRecordInfo::getPayTime));

            if (records != null && !records.isEmpty()) {
                lastAmount = records.get(0).getAmount();
                for (GiftRecordInfo r : records) {
                    if (r.getAmount() == null) {
                        continue;
                    }
                    if ("RECEIVE".equalsIgnoreCase(r.getDirection()) || personId.equals(r.getGiverPersonId())) {
                        totalReceived = totalReceived.add(r.getAmount());
                    } else {
                        totalGiven = totalGiven.add(r.getAmount());
                    }
                }
            }
        }

        // 调用 AI 进行礼金推荐理由与祝福语生成
        boolean aiSuccess = false;
        try {
            aiSuccess = generateAiRecommendation(vo, personName, relationLabel, eventType, direction, lastAmount, totalGiven, totalReceived);
        } catch (Exception e) {
            log.warn("AI recommendation enrichment failed, using rule fallback: {}", e.getMessage());
        }

        // 本地规则与文化礼节兜底
        if (!aiSuccess || !StringUtils.hasText(vo.getAiReasoning())) {
            fallbackRecommendation(vo, personName, relationLabel, eventType, direction, lastAmount, totalGiven, totalReceived);
        }
    }

    private boolean generateAiRecommendation(GiftRecordRecommendAmountVo vo, String personName, String relation,
                                              String eventType, String direction, BigDecimal lastAmount,
                                              BigDecimal totalGiven, BigDecimal totalReceived) {
        String prompt = "你是一位精通中国人情世故与传统礼尚往来文化的礼金顾问。请根据以下往来信息，给出随礼/回礼的推荐理由与一条得体温暖的贺词祝福语。\n"
                + "【往来背景】\n"
                + "- 对方姓名/称谓：" + personName + " (" + relation + ")\n"
                + "- 当前事由：" + (StringUtils.hasText(eventType) ? eventType : "礼尚往来") + "\n"
                + "- 往来方向：" + ("RETURN".equalsIgnoreCase(direction) ? "回礼" : "随礼/送礼") + "\n"
                + "- 往期最近一次金额：" + (lastAmount != null ? lastAmount + "元" : "暂无") + "\n"
                + "- 历史累计收礼：" + totalReceived + "元，历史累计随礼：" + totalGiven + "元\n"
                + "- 系统基准推荐金额：" + vo.getDefaultAmount() + "元，参考档位：" + vo.getRecommendations() + "\n\n"
                + "请返回纯 JSON 格式（不要使用 Markdown 代码块，不要包含 ```json 标签，直接以 { 开头）：\n"
                + "{\n"
                + "  \"aiReasoning\": \"精炼的礼金推荐理由（100字以内，结合人情世故、往来对等、吉利数字如6/8或双数展开说明）\",\n"
                + "  \"aiGreetingTip\": \"贴合该事由与关系的得体贺词/祝福语（30~60字）\"\n"
                + "}";

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setBizType("gift-recommend");
        req.setContent(prompt);
        req.setDepth(1);

        Result<AiAnalyzeResp> result = aiAnalyzeApi.chat(req);
        if (result != null && result.getData() != null && StringUtils.hasText(result.getData().getSummary())) {
            String summary = result.getData().getSummary().trim();
            if (summary.startsWith("```")) {
                int firstBrace = summary.indexOf("{");
                int lastBrace = summary.lastIndexOf("}");
                if (firstBrace >= 0 && lastBrace > firstBrace) {
                    summary = summary.substring(firstBrace, lastBrace + 1);
                }
            }
            try {
                JsonNode root = objectMapper.readTree(summary);
                if (root != null && root.isObject()) {
                    if (root.hasNonNull("aiReasoning")) {
                        vo.setAiReasoning(root.get("aiReasoning").asText().trim());
                    }
                    if (root.hasNonNull("aiGreetingTip")) {
                        vo.setAiGreetingTip(root.get("aiGreetingTip").asText().trim());
                    }
                    return true;
                }
            } catch (Exception e) {
                log.warn("Failed to parse AI recommendation JSON: {}, raw: {}", e.getMessage(), summary);
            }
        }
        return false;
    }

    private void fallbackRecommendation(GiftRecordRecommendAmountVo vo, String personName, String relation,
                                         String eventType, String direction, BigDecimal lastAmount,
                                         BigDecimal totalGiven, BigDecimal totalReceived) {
        String reasoning;
        if ("RETURN".equalsIgnoreCase(direction) && lastAmount != null && lastAmount.compareTo(BigDecimal.ZERO) > 0) {
            reasoning = String.format("按传统人情礼尚往来原则，对方往期曾随礼 %s 元。回礼讲究往来平衡、略有添彩，建议持平或适度上浮（如 +100~200元），兼顾通胀与长久情谊。", lastAmount.toPlainString());
        } else if (lastAmount != null && lastAmount.compareTo(BigDecimal.ZERO) > 0) {
            reasoning = String.format("参考与【%s】往期随礼金额 %s 元及当前【%s】事由的普遍档位，推荐选用吉利双数档位，既合乎礼数，亦显亲厚。", personName, lastAmount.toPlainString(), eventType != null ? eventType : "礼尚往来");
        } else {
            reasoning = String.format("基于【%s】事由的历史平均礼金与标准推荐档位，选用吉利整数，兼顾人情礼节与当代社交交往习惯。", eventType != null ? eventType : "礼尚往来");
        }
        vo.setAiReasoning(reasoning);

        // 贺词兜底
        String upperEvent = eventType != null ? eventType.toUpperCase() : "";
        String greeting;
        if (upperEvent.contains("WEDDING") || upperEvent.contains("婚礼") || upperEvent.contains("结婚")) {
            greeting = "良辰吉日，燕尔新婚！祝二位永结同心，百年好合，携手共赴幸福人生！";
        } else if (upperEvent.contains("BIRTH") || upperEvent.contains("满月") || upperEvent.contains("百日")) {
            greeting = "喜得贵子/千金，阖家欢乐！愿宝宝健康茁壮成长，一生平安顺遂！";
        } else if (upperEvent.contains("HOUSEWARMING") || upperEvent.contains("乔迁") || upperEvent.contains("搬家")) {
            greeting = "乔迁大吉，喜迁新居！愿华厦开新，福地呈祥，日子红红火火蒸蒸日上！";
        } else if (upperEvent.contains("EDUCATION") || upperEvent.contains("升学") || upperEvent.contains("高考")) {
            greeting = "金榜题名，前程似锦！祝学业有成，大展宏图，在新的征程扬帆远航！";
        } else if (upperEvent.contains("BIRTHDAY") || upperEvent.contains("寿宴") || upperEvent.contains("生日")) {
            greeting = "松柏常青，福寿安康！愿岁岁常欢愉，年年皆胜意，福如东海长流水！";
        } else if (upperEvent.contains("FESTIVAL_SPRING") || upperEvent.contains("春节") || upperEvent.contains("过年")) {
            greeting = "新春大吉，万事如意！祝阖家幸福，福暖四季，岁岁平安！";
        } else if (upperEvent.contains("FUNERAL") || upperEvent.contains("白事") || upperEvent.contains("丧事")) {
            greeting = "沉痛悼念，节哀顺变。望保重身体，逝者安息。";
        } else {
            greeting = "礼轻情意重，往来见真情。祝平安喜乐，万事顺意！";
        }
        vo.setAiGreetingTip(greeting);
    }
}
