package com.alex.finance.finance.service.impl;

import com.alex.api.ai.api.AiAnalyzeApi;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.alex.api.ai.vo.AiAnalyzeResp;
import com.alex.api.finance.vo.finance.UniversalFinanceAiParseReq;
import com.alex.api.finance.vo.finance.UniversalFinanceAiParseVo;
import com.alex.api.user.userInfo.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.base.common.Result;
import com.alex.common.utils.date.DateUtils;
import com.alex.finance.finance.service.UniversalFinanceAiService;
import com.alex.finance.prepaidCardInfoT.entity.PrepaidCardInfoT;
import com.alex.finance.prepaidCardInfoT.mapper.PrepaidCardInfoTMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * description: 通用智能记账服务实现类 (AI大模型抽取 + 规则正则词库降级兜底 + 预付卡/归属人联动)
 * author: alex
 * createDate: 2026-10-19
 * version: 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UniversalFinanceAiServiceImpl implements UniversalFinanceAiService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateUtils.FORMATTER_YYYY_MM_DD_HH_MM_SS;

    // 金额提取正则 (如 128元, 35.5块, 100.00, ¥88, ￥99)
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("(?<!\\d)(?:[¥￥$]|RMB|rmb)?\\s*(\\d+(?:\\.\\d{1,2})?)\\s*(?:元|块|RMB|rmb)?");

    // 小票单品条目提取正则 (如 "鲜牛奶 1 16.80" 或 "汉堡 x2 32.00")
    private static final Pattern RECEIPT_LINE_PATTERN = Pattern.compile("^([\\u4e00-\\u9fa5a-zA-Z0-9\\s（）()\\-+]{2,20}?)\\s+(?:[x*X])?\\s*(\\d+)?\\s*(?:[¥￥])?\\s*(\\d+(?:\\.\\d{1,2})?)$");

    @Autowired(required = false)
    private AiAnalyzeApi aiAnalyzeApi;

    @Autowired(required = false)
    private UserUtils userUtils;

    @Autowired(required = false)
    private PrepaidCardInfoTMapper prepaidCardInfoTMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public UniversalFinanceAiParseVo parseLedger(UniversalFinanceAiParseReq req) {
        if (req == null || StringUtils.isBlank(req.getText())) {
            return buildDefaultVo(req);
        }

        String rawText = req.getText().trim();
        String requestedScene = StringUtils.isNotBlank(req.getScene()) ? req.getScene().trim().toUpperCase() : "AUTO_DETECT";

        UniversalFinanceAiParseVo vo = new UniversalFinanceAiParseVo();
        boolean aiParsed = false;

        // 1. 优先尝试调用 AI 大模型抽取
        if (aiAnalyzeApi != null) {
            try {
                aiParsed = extractWithAi(rawText, requestedScene, vo);
            } catch (Exception e) {
                log.warn("UniversalFinanceAiService AI parse failed, fallback to rule engine: {}", e.getMessage());
            }
        }

        // 2. 若未成功使用 AI 抽取到核心要素（金额或分类），执行本地规则与正则兜底抽取
        if (!aiParsed || vo.getAmount() == null) {
            extractWithLocalRules(rawText, requestedScene, vo);
        }

        // 3. 补齐用户信息与预付卡实体关联匹配
        fillBelongToAndUser(req, vo);
        matchPrepaidCardIfApplicable(rawText, vo);

        // 4. 默认时间与兜底
        if (StringUtils.isBlank(vo.getInfoDate())) {
            vo.setInfoDate(DateUtils.now().format(DATE_TIME_FORMATTER));
        }
        if (StringUtils.isBlank(vo.getIncomeAndExpenses())) {
            vo.setIncomeAndExpenses("expense");
        }
        if (StringUtils.isBlank(vo.getTargetScene())) {
            vo.setTargetScene("DAILY_EXPENSE");
        }

        return vo;
    }

    /**
     * AI 调用抽取结构化字段
     */
    private boolean extractWithAi(String rawText, String requestedScene, UniversalFinanceAiParseVo vo) {
        String prompt = "你是一个全能个人财务与商业小票智能记账助理。请深入分析用户的记账描述、购物小票文本或预付卡消费充值记录，提取记账要素并返回严格纯 JSON 对象（不要使用 Markdown 代码块，不要包含 ```json 标签，直接返回 { 开头 } 结尾的 JSON 字符串）。\n\n"
                + "【场景判断】：\n"
                + "- DAILY_EXPENSE：日常收支（餐饮外卖、交通出行、日用百货、工资收入、理财等）\n"
                + "- SHOP_RECEIPT：购物小票/超市餐饮收银单（包含商品列表、单价、数量、总计）\n"
                + "- PREPAID_CARD：预付卡/储值卡/会员卡/礼品卡刷卡消费或充值\n\n"
                + "【JSON 规范】：\n"
                + "{\n"
                + "  \"targetScene\": \"DAILY_EXPENSE 或 SHOP_RECEIPT 或 PREPAID_CARD\",\n"
                + "  \"incomeAndExpenses\": \"expense(支出) 或 income(收入)\",\n"
                + "  \"amount\": 数字金额(如 128.50，纯数字无单位),\n"
                + "  \"name\": \"账目名称、商户名或消费项目(如：麦当劳、全家便利店、打车费、山姆会员卡充值)\",\n"
                + "  \"typeCode\": \"类别编码(建议从: catering(餐饮美食), traffic(交通出行), daily(日用百货), shopping(购物), recreation(休闲娱乐), medical(医疗保健), housing(居家物业), salary(工资薪水), financial(理财收益), other(其他) 中选择)\",\n"
                + "  \"typeName\": \"类别显示名(如：餐饮美食、交通出行、日用百货等)\",\n"
                + "  \"fromSource\": \"支付方式编码(wx(微信), zfb(支付宝), yhk(银行卡), xj(现金), prepaid_card(预付卡), mt(美团), other(其他))\",\n"
                + "  \"fromSourceName\": \"支付方式显示名称(如：微信、支付宝、银行卡、现金等)\",\n"
                + "  \"infoDate\": \"发生时间(格式 yyyy-MM-dd HH:mm:ss，如未说明年份请结合当前年份推断，未提及时间填 null)\",\n"
                + "  \"prepaidCardName\": \"若涉及预付卡，识别出的卡名称(如：星巴克星礼卡、山姆会员卡、洗车卡，否则 null)\",\n"
                + "  \"description\": \"摘要说明或备注\",\n"
                + "  \"receiptItems\": [\n"
                + "    {\"itemName\": \"商品名称\", \"unitPrice\": 12.00, \"quantity\": 1, \"subtotal\": 12.00}\n"
                + "  ]\n"
                + "}\n\n"
                + "指定场景提示：" + requestedScene + "\n"
                + "当前基准时间：" + DateUtils.now().format(DATE_TIME_FORMATTER) + "\n"
                + "用户记账文本：\"\"\"" + rawText + "\"\"\"";

        AiAnalyzeReq aiReq = new AiAnalyzeReq();
        aiReq.setBizType("universal-finance-parse");
        aiReq.setContent(prompt);
        aiReq.setDepth(1);

        Result<AiAnalyzeResp> result = aiAnalyzeApi.chat(aiReq);
        if (result != null && result.getData() != null && StringUtils.isNotBlank(result.getData().getSummary())) {
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
                    if (root.hasNonNull("targetScene")) {
                        vo.setTargetScene(root.get("targetScene").asText().trim());
                    }
                    if (root.hasNonNull("incomeAndExpenses")) {
                        vo.setIncomeAndExpenses(root.get("incomeAndExpenses").asText().trim());
                    }
                    if (root.hasNonNull("amount")) {
                        vo.setAmount(new BigDecimal(root.get("amount").asText()));
                    }
                    if (root.hasNonNull("name")) {
                        vo.setName(root.get("name").asText().trim());
                    }
                    if (root.hasNonNull("typeCode")) {
                        vo.setTypeCode(root.get("typeCode").asText().trim());
                    }
                    if (root.hasNonNull("typeName")) {
                        vo.setTypeName(root.get("typeName").asText().trim());
                    }
                    if (root.hasNonNull("fromSource")) {
                        vo.setFromSource(root.get("fromSource").asText().trim());
                    }
                    if (root.hasNonNull("fromSourceName")) {
                        vo.setFromSourceName(root.get("fromSourceName").asText().trim());
                    }
                    if (root.hasNonNull("infoDate")) {
                        vo.setInfoDate(root.get("infoDate").asText().trim());
                    }
                    if (root.hasNonNull("prepaidCardName")) {
                        vo.setPrepaidCardName(root.get("prepaidCardName").asText().trim());
                    }
                    if (root.hasNonNull("description")) {
                        vo.setDescription(root.get("description").asText().trim());
                    }
                    if (root.has("receiptItems") && root.get("receiptItems").isArray()) {
                        List<UniversalFinanceAiParseVo.ReceiptItemVo> items = new ArrayList<>();
                        for (JsonNode itemNode : root.get("receiptItems")) {
                            UniversalFinanceAiParseVo.ReceiptItemVo item = new UniversalFinanceAiParseVo.ReceiptItemVo();
                            if (itemNode.hasNonNull("itemName")) {
                                item.setItemName(itemNode.get("itemName").asText().trim());
                            }
                            if (itemNode.hasNonNull("unitPrice")) {
                                item.setUnitPrice(new BigDecimal(itemNode.get("unitPrice").asText()));
                            }
                            if (itemNode.hasNonNull("quantity")) {
                                item.setQuantity(itemNode.get("quantity").asInt(1));
                            }
                            if (itemNode.hasNonNull("subtotal")) {
                                item.setSubtotal(new BigDecimal(itemNode.get("subtotal").asText()));
                            }
                            items.add(item);
                        }
                        vo.setReceiptItems(items);
                    }
                    vo.setParseEngine("AI_DEEPSEEK");
                    vo.setReasoning("AI 大模型深度语义解析成功");
                    return true;
                }
            } catch (Exception e) {
                log.warn("Failed to parse AI JSON response: {}, raw: {}", e.getMessage(), summary);
            }
        }
        return false;
    }

    /**
     * 本地规则与正则抽取引擎 (100% 可用保底)
     */
    private void extractWithLocalRules(String rawText, String requestedScene, UniversalFinanceAiParseVo vo) {
        vo.setParseEngine("LOCAL_REGEX_FALLBACK");

        // 1. 判断场景
        String scene = requestedScene;
        if ("AUTO_DETECT".equals(scene)) {
            if (rawText.contains("小票") || rawText.contains("收银") || rawText.contains("单价") || rawText.contains("数量") || rawText.contains("合计")) {
                scene = "SHOP_RECEIPT";
            } else if (rawText.contains("卡") && (rawText.contains("充值") || rawText.contains("余额") || rawText.contains("刷卡") || rawText.contains("扣款"))) {
                scene = "PREPAID_CARD";
            } else {
                scene = "DAILY_EXPENSE";
            }
        }
        vo.setTargetScene(scene);

        // 2. 收支方向判断
        boolean isIncome = rawText.contains("收入") || rawText.contains("到账") || rawText.contains("入账")
                || rawText.contains("工资") || rawText.contains("奖金") || rawText.contains("利息")
                || rawText.contains("分红") || rawText.contains("收益") || rawText.contains("报销款");
        vo.setIncomeAndExpenses(isIncome ? "income" : "expense");

        // 3. 抽取金额
        if (vo.getAmount() == null) {
            Matcher m = AMOUNT_PATTERN.matcher(rawText);
            BigDecimal maxAmount = null;
            while (m.find()) {
                try {
                    BigDecimal val = new BigDecimal(m.group(1));
                    if (maxAmount == null || val.compareTo(maxAmount) > 0) {
                        maxAmount = val;
                    }
                } catch (Exception ignored) {
                }
            }
            vo.setAmount(maxAmount != null ? maxAmount : BigDecimal.ZERO);
        }

        // 4. 抽取支付渠道
        String fromSource = "wx";
        String fromSourceName = "微信";
        if (rawText.contains("支付宝") || rawText.contains("zfb") || rawText.contains("花呗")) {
            fromSource = "zfb";
            fromSourceName = "支付宝";
        } else if (rawText.contains("现金") || rawText.contains("现钞")) {
            fromSource = "xj";
            fromSourceName = "现金";
        } else if (rawText.contains("银行卡") || rawText.contains("招商") || rawText.contains("工行") || rawText.contains("建行") || rawText.contains("农行") || rawText.contains("刷卡")) {
            fromSource = "yhk";
            fromSourceName = "银行卡";
        } else if (rawText.contains("美团")) {
            fromSource = "mt";
            fromSourceName = "美团";
        } else if (rawText.contains("预付卡") || rawText.contains("储值卡") || rawText.contains("星礼卡") || rawText.contains("会员卡")) {
            fromSource = "prepaid_card";
            fromSourceName = "预付卡";
        }
        vo.setFromSource(fromSource);
        vo.setFromSourceName(fromSourceName);

        // 5. 分类映射
        matchCategory(rawText, isIncome, vo);

        // 6. 名称提取
        if (StringUtils.isBlank(vo.getName())) {
            vo.setName(extractName(rawText, vo.getTypeName()));
        }

        // 7. 时间抽取
        if (StringUtils.isBlank(vo.getInfoDate())) {
            vo.setInfoDate(extractDate(rawText));
        }

        // 8. 若为小票场景，解析单品条目
        if ("SHOP_RECEIPT".equals(scene) && (vo.getReceiptItems() == null || vo.getReceiptItems().isEmpty())) {
            List<UniversalFinanceAiParseVo.ReceiptItemVo> items = extractReceiptItems(rawText);
            if (!items.isEmpty()) {
                vo.setReceiptItems(items);
            }
        }

        vo.setReasoning("本地规则与正则引擎快速解析");
        vo.setDescription(rawText);
    }

    private void matchCategory(String text, boolean isIncome, UniversalFinanceAiParseVo vo) {
        if (isIncome) {
            if (text.contains("工资") || text.contains("薪水") || text.contains("薪酬")) {
                vo.setTypeCode("salary");
                vo.setTypeName("工资收入");
            } else if (text.contains("理财") || text.contains("基金") || text.contains("股票") || text.contains("收益") || text.contains("利息")) {
                vo.setTypeCode("financial");
                vo.setTypeName("理财收益");
            } else {
                vo.setTypeCode("other_income");
                vo.setTypeName("其他收入");
            }
            return;
        }

        // 支出场景分类
        if (text.contains("吃") || text.contains("饭") || text.contains("餐") || text.contains("外卖")
                || text.contains("咖啡") || text.contains("奶茶") || text.contains("肯德基") || text.contains("麦当劳")
                || text.contains("星巴克") || text.contains("火锅") || text.contains("烧烤") || text.contains("酒")) {
            vo.setTypeCode("catering");
            vo.setTypeName("餐饮美食");
        } else if (text.contains("打车") || text.contains("滴滴") || text.contains("地铁") || text.contains("公交")
                || text.contains("加油") || text.contains("停车") || text.contains("高铁") || text.contains("机票") || text.contains("出行")) {
            vo.setTypeCode("traffic");
            vo.setTypeName("交通出行");
        } else if (text.contains("超市") || text.contains("便利店") || text.contains("小票") || text.contains("买菜")
                || text.contains("纸巾") || text.contains("洗发") || text.contains("日用") || text.contains("山姆") || text.contains("沃尔玛")) {
            vo.setTypeCode("daily");
            vo.setTypeName("日用百货");
        } else if (text.contains("衣服") || text.contains("裤") || text.contains("鞋") || text.contains("护肤") || text.contains("口红") || text.contains("购物")) {
            vo.setTypeCode("shopping");
            vo.setTypeName("服饰美容");
        } else if (text.contains("电影") || text.contains("游戏") || text.contains("KTV") || text.contains("旅游") || text.contains("门票")) {
            vo.setTypeCode("recreation");
            vo.setTypeName("文娱休闲");
        } else if (text.contains("药") || text.contains("医院") || text.contains("挂号") || text.contains("诊所") || text.contains("体检")) {
            vo.setTypeCode("medical");
            vo.setTypeName("医疗保健");
        } else if (text.contains("房租") || text.contains("物业") || text.contains("水电") || text.contains("燃气") || text.contains("宽带")) {
            vo.setTypeCode("housing");
            vo.setTypeName("居家生活");
        } else {
            vo.setTypeCode("other");
            vo.setTypeName("其他开支");
        }
    }

    private String extractName(String text, String defaultTypeName) {
        String clean = text.replaceAll("(?<!\\d)(?:[¥￥$]|RMB|rmb)?\\s*(\\d+(?:\\.\\d{1,2})?)\\s*(?:元|块|RMB|rmb)?", "")
                .replaceAll("(微信|支付宝|现金|银行卡|支付|刷卡|付了|花了|充值|消费|昨天|今天|前天|中午|早上|晚上)", "")
                .trim();
        if (clean.length() >= 2 && clean.length() <= 20) {
            return clean;
        }
        return StringUtils.isNotBlank(defaultTypeName) ? defaultTypeName : "日常支出";
    }

    private String extractDate(String text) {
        LocalDateTime now = DateUtils.now();
        if (text.contains("前天")) {
            now = now.minusDays(2);
        } else if (text.contains("昨天")) {
            now = now.minusDays(1);
        } else if (text.contains("大前天")) {
            now = now.minusDays(3);
        }
        return now.format(DATE_TIME_FORMATTER);
    }

    private List<UniversalFinanceAiParseVo.ReceiptItemVo> extractReceiptItems(String text) {
        List<UniversalFinanceAiParseVo.ReceiptItemVo> list = new ArrayList<>();
        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            Matcher m = RECEIPT_LINE_PATTERN.matcher(trimmed);
            if (m.find()) {
                String name = m.group(1).trim();
                int qty = 1;
                if (m.group(2) != null) {
                    try {
                        qty = Integer.parseInt(m.group(2).trim());
                    } catch (Exception ignored) {
                    }
                }
                BigDecimal price = new BigDecimal(m.group(3));
                list.add(UniversalFinanceAiParseVo.ReceiptItemVo.builder()
                        .itemName(name)
                        .quantity(qty)
                        .unitPrice(price)
                        .subtotal(price.multiply(new BigDecimal(qty)))
                        .build());
            }
        }
        return list;
    }

    private void fillBelongToAndUser(UniversalFinanceAiParseReq req, UniversalFinanceAiParseVo vo) {
        Long belongTo = req.getBelongTo();
        if (belongTo == null && userUtils != null) {
            TUserVo loginUser = userUtils.getLoginUser();
            if (loginUser != null) {
                belongTo = loginUser.getId();
                vo.setBelongToName(StringUtils.isNotBlank(loginUser.getNickName()) ? loginUser.getNickName() : loginUser.getUsername());
            }
        }
        vo.setBelongTo(belongTo);
    }

    private void matchPrepaidCardIfApplicable(String text, UniversalFinanceAiParseVo vo) {
        if (prepaidCardInfoTMapper == null) {
            return;
        }
        try {
            Long userId = vo.getBelongTo();
            LambdaQueryWrapper<PrepaidCardInfoT> wrapper = new LambdaQueryWrapper<>();
            if (userId != null) {
                wrapper.eq(PrepaidCardInfoT::getUserId, userId);
            }
            wrapper.eq(PrepaidCardInfoT::getCardStatus, "正常");
            List<PrepaidCardInfoT> cards = prepaidCardInfoTMapper.selectList(wrapper);
            if (cards == null || cards.isEmpty()) {
                return;
            }

            for (PrepaidCardInfoT card : cards) {
                if (StringUtils.isNotBlank(card.getCardName()) && text.contains(card.getCardName())) {
                    vo.setPrepaidCardId(card.getCardId());
                    vo.setPrepaidCardName(card.getCardName());
                    vo.setPrepaidCardBalance(card.getCurrentBalance());
                    vo.setTargetScene("PREPAID_CARD");
                    vo.setFromSource("prepaid_card");
                    vo.setFromSourceName("预付卡");
                    break;
                }
            }
        } catch (Exception e) {
            log.warn("Match prepaid card failed: {}", e.getMessage());
        }
    }

    private UniversalFinanceAiParseVo buildDefaultVo(UniversalFinanceAiParseReq req) {
        UniversalFinanceAiParseVo vo = new UniversalFinanceAiParseVo();
        vo.setTargetScene("DAILY_EXPENSE");
        vo.setIncomeAndExpenses("expense");
        vo.setAmount(BigDecimal.ZERO);
        vo.setName("日常开销");
        vo.setTypeCode("other");
        vo.setTypeName("其他开支");
        vo.setFromSource("wx");
        vo.setFromSourceName("微信");
        vo.setInfoDate(DateUtils.now().format(DATE_TIME_FORMATTER));
        vo.setParseEngine("LOCAL_REGEX_FALLBACK");
        vo.setReasoning("输入为空，已赋予默认初始化结构");
        fillBelongToAndUser(req, vo);
        return vo;
    }
}
