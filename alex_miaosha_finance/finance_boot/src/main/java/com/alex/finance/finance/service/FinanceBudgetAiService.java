package com.alex.finance.finance.service;

import com.alex.api.ai.api.AiAnalyzeApi;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.alex.api.ai.vo.AiAnalyzeResp;
import com.alex.api.finance.vo.dict.DictInfoVo;
import com.alex.api.finance.vo.finance.*;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.base.common.Result;
import com.alex.base.constants.SysConf;
import com.alex.common.utils.date.DateUtils;
import com.alex.finance.dict.service.DictInfoService;
import com.alex.finance.finance.mapper.FinanceInfoMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * description: 零花钱月度预算 AI 智能测算、推荐与自然语言解析服务
 * author: alex
 * createDate: 2026-10-12
 * version: 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FinanceBudgetAiService {

    @Autowired(required = false)
    private AiAnalyzeApi aiAnalyzeApi;

    @Autowired(required = false)
    private FinanceInfoMapper financeInfoMapper;

    @Autowired(required = false)
    private DictInfoService dictInfoService;

    @Autowired(required = false)
    private UserUtils userUtils;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Pattern AMOUNT_PATTERN = Pattern.compile("(?<!\\d)(\\d+(?:\\.\\d+)?)\\s*(?:元|块|RMB|rmb)?");

    /**
     * AI 智能预算测算与画像推荐
     */
    public FinanceBudgetAiRecommendVo recommendBudget(FinanceBudgetAiRecommendReq req) {
        String targetMonth = resolveTargetMonth(req != null ? req.getBudgetMonth() : null);
        Long belongTo = resolveBelongTo(req != null ? req.getBelongTo() : null);

        // 1. 统计近 3 个月历史日常消费基准
        HistoricalExpenseProfile profile = analyzeHistoricalExpenses(targetMonth, belongTo);

        // 2. 构造基础兜底推荐模型 (保证 100% 规则引擎可用性)
        FinanceBudgetAiRecommendVo fallbackVo = buildFallbackRecommend(targetMonth, profile, req != null ? req.getTargetGoal() : null);

        if (aiAnalyzeApi == null) {
            return fallbackVo;
        }

        // 3. 尝试调用 AI 大模型（DeepSeek）进行深度推荐与个性化诊断
        try {
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("budgetMonth", targetMonth);
            ctx.put("historyMonthlyAvg", profile.getMonthlyAverage());
            ctx.put("topCategories", profile.getTopCategorySummary());
            ctx.put("targetGoal", req != null ? req.getTargetGoal() : "适度平衡");

            String prompt = String.format(
                    "作为家庭与个人理财智能规划专家，请根据用户的历史消费数据，为【%s】月份的日常零花钱预算提供科学测算推荐：\n" +
                    "- 历史月均支出基准：%s 元\n" +
                    "- 高频支出品类分布：%s\n" +
                    "- 偏好目标：%s\n" +
                    "请以标准 JSON 格式输出，包含以下字段：\n" +
                    "- moderateAmount: 适度型推荐金额（数字，基准合理缓冲）\n" +
                    "- conservativeAmount: 节约型推荐金额（数字，收紧弹性支出）\n" +
                    "- flexibleAmount: 宽松型推荐金额（数字，更充裕的预算）\n" +
                    "- recommendedAmount: 主推建议预算金额（数字）\n" +
                    "- recommendedScopeMode: 建议统计范畴（'all' 或 'custom'）\n" +
                    "- recommendedCategoryCodes: 重点管控分类列表（字符串数组）\n" +
                    "- reasoning: 测算推导理由（简明扼要中文分析）\n" +
                    "- healthAdvice: 消费健康诊断与建议（包含预估日均支出建议）\n" +
                    "只需返回纯JSON字符串，不要包含markdown格式标记。",
                    targetMonth,
                    profile.getMonthlyAverage().setScale(2, RoundingMode.HALF_UP),
                    profile.getTopCategorySummary(),
                    (req != null && StringUtils.isNotBlank(req.getTargetGoal())) ? req.getTargetGoal() : "适度平衡"
            );

            AiAnalyzeReq aiReq = new AiAnalyzeReq();
            aiReq.setBizType("finance-budget-ai-recommend");
            aiReq.setContent(prompt);
            aiReq.setContext(ctx);

            Result<AiAnalyzeResp> result = aiAnalyzeApi.chat(aiReq);
            if (result != null && result.getData() != null && StringUtils.isNotBlank(result.getData().getSummary())) {
                String text = cleanJsonMarkdown(result.getData().getSummary());
                JsonNode root = objectMapper.readTree(text);
                if (root != null) {
                    FinanceBudgetAiRecommendVo aiVo = FinanceBudgetAiRecommendVo.builder()
                            .budgetMonth(targetMonth)
                            .historyMonthlyAverage(profile.getMonthlyAverage())
                            .isFallback(false)
                            .build();

                    if (root.hasNonNull("recommendedAmount")) {
                        aiVo.setRecommendedAmount(new BigDecimal(root.get("recommendedAmount").asText()));
                    } else {
                        aiVo.setRecommendedAmount(fallbackVo.getRecommendedAmount());
                    }

                    if (root.hasNonNull("moderateAmount")) {
                        aiVo.setModerateAmount(new BigDecimal(root.get("moderateAmount").asText()));
                    } else {
                        aiVo.setModerateAmount(fallbackVo.getModerateAmount());
                    }

                    if (root.hasNonNull("conservativeAmount")) {
                        aiVo.setConservativeAmount(new BigDecimal(root.get("conservativeAmount").asText()));
                    } else {
                        aiVo.setConservativeAmount(fallbackVo.getConservativeAmount());
                    }

                    if (root.hasNonNull("flexibleAmount")) {
                        aiVo.setFlexibleAmount(new BigDecimal(root.get("flexibleAmount").asText()));
                    } else {
                        aiVo.setFlexibleAmount(fallbackVo.getFlexibleAmount());
                    }

                    if (root.hasNonNull("recommendedScopeMode")) {
                        aiVo.setRecommendedScopeMode(root.get("recommendedScopeMode").asText());
                    } else {
                        aiVo.setRecommendedScopeMode(fallbackVo.getRecommendedScopeMode());
                    }

                    if (root.hasNonNull("recommendedCategoryCodes") && root.get("recommendedCategoryCodes").isArray()) {
                        List<String> cats = new ArrayList<>();
                        for (JsonNode n : root.get("recommendedCategoryCodes")) {
                            cats.add(n.asText());
                        }
                        aiVo.setRecommendedCategoryCodes(!cats.isEmpty() ? cats : fallbackVo.getRecommendedCategoryCodes());
                    } else {
                        aiVo.setRecommendedCategoryCodes(fallbackVo.getRecommendedCategoryCodes());
                    }

                    if (root.hasNonNull("reasoning")) {
                        aiVo.setReasoning(root.get("reasoning").asText());
                    } else {
                        aiVo.setReasoning(fallbackVo.getReasoning());
                    }

                    if (root.hasNonNull("healthAdvice")) {
                        aiVo.setHealthAdvice(root.get("healthAdvice").asText());
                    } else {
                        aiVo.setHealthAdvice(fallbackVo.getHealthAdvice());
                    }

                    return aiVo;
                }
            }
        } catch (Exception ex) {
            log.warn("FinanceBudgetAiService recommend degraded to fallback: {}", ex.getMessage());
        }

        return fallbackVo;
    }

    /**
     * AI 自然语言一句话预算指令解析
     */
    public FinanceBudgetAiParseVo parseBudgetCommand(FinanceBudgetAiParseReq req) {
        String targetMonth = resolveTargetMonth(req != null ? req.getBudgetMonth() : null);
        String text = (req != null && StringUtils.isNotBlank(req.getText())) ? req.getText().trim() : "";

        // 1. 规则引擎构建基础降级结果
        FinanceBudgetAiParseVo fallbackVo = buildFallbackParse(text);

        if (StringUtils.isBlank(text) || aiAnalyzeApi == null) {
            return fallbackVo;
        }

        // 2. 调用大模型进行深度语义理解
        try {
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("text", text);
            ctx.put("budgetMonth", targetMonth);

            String prompt = String.format(
                    "请解析用户的零花钱/日常预算自然语言配置指令：\n" +
                    "用户输入：%s\n" +
                    "请以标准 JSON 格式输出，包含以下字段：\n" +
                    "- budgetAmount: 提取的月度预算金额（数字，若提及'不设上限'/'无限制'则为0，未提及金额则为null）\n" +
                    "- scopeMode: 统计范畴（'all':全量日常消费，'custom':指定专属分类）\n" +
                    "- categoryCodes: 匹配到的具体消费分类列表（字符串数组，如['餐饮美食', '交通出行']）\n" +
                    "- incomeAndExpenses: 收支方向（'expense':支出, 'income':收入，通常为'expense'）\n" +
                    "- parseSummary: 友好的中文解析摘要说明\n" +
                    "只需返回纯JSON字符串，不要包含markdown格式标记。",
                    text
            );

            AiAnalyzeReq aiReq = new AiAnalyzeReq();
            aiReq.setBizType("finance-budget-ai-parse");
            aiReq.setContent(prompt);
            aiReq.setContext(ctx);

            Result<AiAnalyzeResp> result = aiAnalyzeApi.chat(aiReq);
            if (result != null && result.getData() != null && StringUtils.isNotBlank(result.getData().getSummary())) {
                String clean = cleanJsonMarkdown(result.getData().getSummary());
                JsonNode root = objectMapper.readTree(clean);
                if (root != null) {
                    FinanceBudgetAiParseVo aiVo = FinanceBudgetAiParseVo.builder()
                            .isFallback(false)
                            .incomeAndExpenses("expense")
                            .build();

                    if (root.hasNonNull("budgetAmount")) {
                        aiVo.setBudgetAmount(new BigDecimal(root.get("budgetAmount").asText()));
                    } else {
                        aiVo.setBudgetAmount(fallbackVo.getBudgetAmount());
                    }

                    if (root.hasNonNull("scopeMode")) {
                        aiVo.setScopeMode(root.get("scopeMode").asText());
                    } else {
                        aiVo.setScopeMode(fallbackVo.getScopeMode());
                    }

                    if (root.hasNonNull("incomeAndExpenses")) {
                        aiVo.setIncomeAndExpenses(root.get("incomeAndExpenses").asText());
                    } else {
                        aiVo.setIncomeAndExpenses(fallbackVo.getIncomeAndExpenses());
                    }

                    if (root.hasNonNull("categoryCodes") && root.get("categoryCodes").isArray()) {
                        List<String> cats = new ArrayList<>();
                        for (JsonNode n : root.get("categoryCodes")) {
                            cats.add(n.asText());
                        }
                        aiVo.setCategoryCodes(!cats.isEmpty() ? cats : fallbackVo.getCategoryCodes());
                    } else {
                        aiVo.setCategoryCodes(fallbackVo.getCategoryCodes());
                    }

                    if (root.hasNonNull("parseSummary")) {
                        aiVo.setParseSummary(root.get("parseSummary").asText());
                    } else {
                        aiVo.setParseSummary(fallbackVo.getParseSummary());
                    }

                    return aiVo;
                }
            }
        } catch (Exception ex) {
            log.warn("FinanceBudgetAiService parse degraded to fallback: {}", ex.getMessage());
        }

        return fallbackVo;
    }

    /**
     * 规则引擎降级推荐生成器
     */
    public FinanceBudgetAiRecommendVo buildFallbackRecommend(String budgetMonth, HistoricalExpenseProfile profile, String targetGoal) {
        BigDecimal base = profile.getMonthlyAverage();
        boolean hasHistory = base.compareTo(BigDecimal.ZERO) > 0;

        BigDecimal moderate;
        BigDecimal conservative;
        BigDecimal flexible;

        if (hasHistory) {
            // 取百位数四舍五入整齐数
            moderate = roundToHundred(base);
            conservative = roundToHundred(base.multiply(new BigDecimal("0.80")));
            flexible = roundToHundred(base.multiply(new BigDecimal("1.25")));
        } else {
            // 冷启动预设档位
            moderate = new BigDecimal("3000.00");
            conservative = new BigDecimal("2000.00");
            flexible = new BigDecimal("5000.00");
        }

        BigDecimal recommended = moderate;
        if (StringUtils.isNotBlank(targetGoal)) {
            if (targetGoal.contains("节约") || targetGoal.contains("存钱") || targetGoal.contains("攒钱")) {
                recommended = conservative;
            } else if (targetGoal.contains("品质") || targetGoal.contains("宽松") || targetGoal.contains("享受")) {
                recommended = flexible;
            }
        }

        List<String> topCategories = profile.getTopCategories();
        if (topCategories.isEmpty()) {
            topCategories = Arrays.asList("餐饮美食", "日用百货", "交通出行");
        }

        String scopeMode = (hasHistory && !profile.getTopCategories().isEmpty() && profile.getTopRatio() >= 0.6)
                ? "custom"
                : "all";

        String reasoning;
        if (hasHistory) {
            reasoning = String.format("根据您近 3 个月的实际账单，月均支出基准为 ¥%s。其中 %s 占比突出（约占 %.0f%%），建议采用适度型预算（¥%s）以平衡生活与开支控制。",
                    base.setScale(0, RoundingMode.HALF_UP),
                    String.join("、", topCategories),
                    profile.getTopRatio() * 100,
                    recommended.setScale(0, RoundingMode.HALF_UP)
            );
        } else {
            reasoning = "检测到历史月度账单较少，已为您推荐典型家庭生活的标准适度预算（¥3,000），涵盖高频日常餐饮与生活所需。";
        }

        // 计算当月天数给出日均额度
        YearMonth ym = YearMonth.parse(budgetMonth);
        int days = ym.lengthOfMonth();
        BigDecimal daily = recommended.divide(new BigDecimal(days), 1, RoundingMode.HALF_UP);
        String healthAdvice = String.format("本月共 %d 天，按推荐预算每日合理可支配约为 ¥%s。保持在均线以内可有效避免月底透支。", days, daily);

        return FinanceBudgetAiRecommendVo.builder()
                .budgetMonth(budgetMonth)
                .recommendedAmount(recommended)
                .moderateAmount(moderate)
                .conservativeAmount(conservative)
                .flexibleAmount(flexible)
                .recommendedScopeMode(scopeMode)
                .recommendedCategoryCodes(topCategories)
                .reasoning(reasoning)
                .healthAdvice(healthAdvice)
                .historyMonthlyAverage(base)
                .isFallback(true)
                .build();
    }

    /**
     * 规则引擎自然语言解析
     */
    public FinanceBudgetAiParseVo buildFallbackParse(String text) {
        if (StringUtils.isBlank(text)) {
            return FinanceBudgetAiParseVo.builder()
                    .budgetAmount(new BigDecimal("3000.00"))
                    .scopeMode("all")
                    .incomeAndExpenses("expense")
                    .categoryCodes(Collections.emptyList())
                    .parseSummary("未输入明确内容，已默认填入标准日常预算 ¥3,000")
                    .isFallback(true)
                    .build();
        }

        BigDecimal amount = null;
        if (text.contains("不设限") || text.contains("不设上限") || text.contains("无限制")) {
            amount = BigDecimal.ZERO;
        } else {
            Matcher m = AMOUNT_PATTERN.matcher(text);
            if (m.find()) {
                try {
                    amount = new BigDecimal(m.group(1));
                } catch (Exception ignored) {
                }
            }
        }
        if (amount == null) {
            amount = new BigDecimal("2000.00");
        }

        List<String> matchedCategories = new ArrayList<>();
        Map<String, String[]> categoryKeywords = getCategoryKeywords();
        for (Map.Entry<String, String[]> entry : categoryKeywords.entrySet()) {
            for (String kw : entry.getValue()) {
                if (text.contains(kw)) {
                    matchedCategories.add(entry.getKey());
                    break;
                }
            }
        }

        boolean isCustom = !matchedCategories.isEmpty() || text.contains("只管") || text.contains("自选") || text.contains("仅限");
        String scopeMode = isCustom ? "custom" : "all";
        String direction = (text.contains("收入") || text.contains("进账")) ? "income" : "expense";

        String summary;
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            summary = "已识别为【不设上限】预算模式";
        } else {
            summary = String.format("已识别预算金额 ¥%s", amount.stripTrailingZeros().toPlainString());
        }
        if (isCustom && !matchedCategories.isEmpty()) {
            summary += String.format("，重点管控分类【%s】", String.join("、", matchedCategories));
        } else {
            summary += "，统计全部日常消费";
        }

        return FinanceBudgetAiParseVo.builder()
                .budgetAmount(amount)
                .scopeMode(scopeMode)
                .incomeAndExpenses(direction)
                .categoryCodes(matchedCategories)
                .parseSummary(summary)
                .isFallback(true)
                .build();
    }

    private HistoricalExpenseProfile analyzeHistoricalExpenses(String targetMonth, Long belongTo) {
        try {
            YearMonth ym = YearMonth.parse(targetMonth);
            LocalDate start = ym.minusMonths(3).atDay(1);
            LocalDate end = ym.minusMonths(1).atEndOfMonth();

            FinanceInfoVo query = new FinanceInfoVo();
            query.setInfoDateStart(start);
            query.setInfoDateEnd(end);
            query.setIncomeAndExpenses(SysConf.EXPENSE_STATUS);
            query.setIsValid("1");
            query.setBelongTo(belongTo);

            List<FinanceInfoVo> list = (financeInfoMapper != null) ? financeInfoMapper.getList(query) : Collections.emptyList();
            if (list == null || list.isEmpty()) {
                return new HistoricalExpenseProfile(BigDecimal.ZERO, Collections.emptyList(), 0.0, "暂无历史开销数据");
            }

            BigDecimal total = BigDecimal.ZERO;
            Map<String, BigDecimal> catMap = new HashMap<>();
            Set<String> distinctMonths = new HashSet<>();

            for (FinanceInfoVo item : list) {
                if (item.getAmount() == null) continue;
                String typeCode = item.getTypeCode();
                if (StringUtils.isBlank(typeCode) || "转账".equals(typeCode) || "内部转账".equals(typeCode)) {
                    continue;
                }
                total = total.add(item.getAmount());
                catMap.merge(typeCode, item.getAmount(), BigDecimal::add);
                if (item.getInfoDate() != null) {
                    distinctMonths.add(item.getInfoDate().toString().substring(0, 7));
                }
            }

            int months = Math.max(1, distinctMonths.size());
            BigDecimal avg = total.divide(new BigDecimal(months), 2, RoundingMode.HALF_UP);

            List<Map.Entry<String, BigDecimal>> sorted = catMap.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .collect(Collectors.toList());

            List<String> topCats = sorted.stream().limit(3).map(Map.Entry::getKey).collect(Collectors.toList());

            BigDecimal topSum = sorted.stream().limit(3).map(Map.Entry::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);
            double topRatio = (total.compareTo(BigDecimal.ZERO) > 0)
                    ? topSum.divide(total, 4, RoundingMode.HALF_UP).doubleValue()
                    : 0.0;

            String summaryStr = sorted.stream().limit(5)
                    .map(e -> e.getKey() + ": ¥" + e.getValue().setScale(0, RoundingMode.HALF_UP))
                    .collect(Collectors.joining(", "));

            return new HistoricalExpenseProfile(avg, topCats, topRatio, summaryStr);
        } catch (Exception e) {
            log.error("分析历史开销流水异常: {}", e.getMessage(), e);
            return new HistoricalExpenseProfile(BigDecimal.ZERO, Collections.emptyList(), 0.0, "分析异常");
        }
    }

    private BigDecimal roundToHundred(BigDecimal val) {
        if (val == null || val.compareTo(BigDecimal.ZERO) <= 0) {
            return new BigDecimal("2000.00");
        }
        long num = val.setScale(0, RoundingMode.HALF_UP).longValue();
        long rounded = Math.round(num / 100.0) * 100;
        if (rounded <= 0) rounded = 1000;
        return new BigDecimal(rounded + ".00");
    }

    private String cleanJsonMarkdown(String raw) {
        String text = raw.trim();
        if (text.startsWith("```json")) {
            text = text.substring(7);
        } else if (text.startsWith("```")) {
            text = text.substring(3);
        }
        if (text.endsWith("```")) {
            text = text.substring(0, text.length() - 3);
        }
        return text.trim();
    }

    private Map<String, String[]> getCategoryKeywords() {
        Map<String, String[]> map = new LinkedHashMap<>();
        map.put("餐饮美食", new String[]{"餐饮", "吃饭", "美食", "外卖", "零食", "饮品", "咖啡", "奶茶", "夜宵"});
        map.put("日用百货", new String[]{"日用", "百货", "超市", "生活用品", "买菜", "柴米油盐"});
        map.put("交通出行", new String[]{"交通", "打车", "公交", "地铁", "加油", "停车", "出行", "车费"});
        map.put("休闲娱乐", new String[]{"娱乐", "电影", "游戏", "旅游", "门票", "桌游", "唱歌", "休闲"});
        map.put("服饰装扮", new String[]{"服饰", "衣服", "鞋子", "包包", "装扮", "化妆品", "护肤", "理发"});
        map.put("数码电器", new String[]{"数码", "手机", "电脑", "电器", "数码产品", "电子"});
        map.put("医疗保健", new String[]{"医疗", "买药", "医院", "保健", "体检"});
        return map;
    }

    private String resolveTargetMonth(String month) {
        if (StringUtils.isNotBlank(month)) {
            return month.trim();
        }
        return DateUtils.now().format(DateUtils.FORMATTER_YYYY_MM);
    }

    private Long resolveBelongTo(Long belongTo) {
        if (belongTo != null) {
            return belongTo;
        }
        if (userUtils != null) {
            TUserVo loginUser = userUtils.getLoginUser();
            if (loginUser != null) {
                return loginUser.getId();
            }
        }
        return null;
    }

    @lombok.Value
    public static class HistoricalExpenseProfile {
        BigDecimal monthlyAverage;
        List<String> topCategories;
        double topRatio;
        String topCategorySummary;
    }
}
