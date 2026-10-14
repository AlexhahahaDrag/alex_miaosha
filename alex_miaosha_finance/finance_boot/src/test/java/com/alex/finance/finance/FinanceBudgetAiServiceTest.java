package com.alex.finance.finance;

import com.alex.api.finance.vo.finance.FinanceBudgetAiParseReq;
import com.alex.api.finance.vo.finance.FinanceBudgetAiParseVo;
import com.alex.api.finance.vo.finance.FinanceBudgetAiRecommendReq;
import com.alex.api.finance.vo.finance.FinanceBudgetAiRecommendVo;
import com.alex.finance.finance.service.FinanceBudgetAiService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;

class FinanceBudgetAiServiceTest {

    private final FinanceBudgetAiService financeBudgetAiService = new FinanceBudgetAiService();

    @Test
    @DisplayName("AI预算测算：冷启动无流水时规则兜底三档推荐测试")
    void testRecommendColdStart() {
        FinanceBudgetAiRecommendReq req = FinanceBudgetAiRecommendReq.builder()
                .budgetMonth("2026-10")
                .targetGoal("适度平衡")
                .build();

        FinanceBudgetAiRecommendVo vo = financeBudgetAiService.recommendBudget(req);

        Assertions.assertNotNull(vo);
        Assertions.assertEquals("2026-10", vo.getBudgetMonth());
        Assertions.assertEquals(new BigDecimal("3000.00"), vo.getModerateAmount());
        Assertions.assertEquals(new BigDecimal("2000.00"), vo.getConservativeAmount());
        Assertions.assertEquals(new BigDecimal("5000.00"), vo.getFlexibleAmount());
        Assertions.assertEquals(new BigDecimal("3000.00"), vo.getRecommendedAmount());
        Assertions.assertFalse(vo.getRecommendedCategoryCodes().isEmpty());
        Assertions.assertNotNull(vo.getReasoning());
        Assertions.assertNotNull(vo.getHealthAdvice());
        Assertions.assertTrue(vo.getIsFallback());
    }

    @Test
    @DisplayName("AI预算测算：有历史流水时节约偏好策略推荐测试")
    void testRecommendWithHistoricalExpenses() {
        FinanceBudgetAiService.HistoricalExpenseProfile profile = new FinanceBudgetAiService.HistoricalExpenseProfile(
                new BigDecimal("2850.00"),
                Arrays.asList("餐饮美食", "日用百货"),
                0.72,
                "餐饮美食: ¥2050, 日用百货: ¥800"
        );

        FinanceBudgetAiRecommendVo vo = financeBudgetAiService.buildFallbackRecommend("2026-10", profile, "节约存钱");

        Assertions.assertNotNull(vo);
        Assertions.assertEquals("2026-10", vo.getBudgetMonth());
        Assertions.assertEquals(new BigDecimal("2900.00"), vo.getModerateAmount()); // 2850 四舍五入到百位
        Assertions.assertEquals(new BigDecimal("2300.00"), vo.getConservativeAmount()); // 2850 * 0.8 = 2280 -> 2300
        Assertions.assertEquals(new BigDecimal("2300.00"), vo.getRecommendedAmount()); // 节约目标采纳 conservative
        Assertions.assertEquals("custom", vo.getRecommendedScopeMode()); // topRatio >= 0.6 推荐 custom
        Assertions.assertTrue(vo.getRecommendedCategoryCodes().contains("餐饮美食"));
        Assertions.assertTrue(vo.getReasoning().contains("餐饮美食"));
    }

    @Test
    @DisplayName("AI自然语言解析：口语化自选分类指令解析测试")
    void testParseNaturalLanguageCommandWithCategories() {
        FinanceBudgetAiParseReq req = FinanceBudgetAiParseReq.builder()
                .text("帮我设3000预算，只管餐饮和交通")
                .budgetMonth("2026-10")
                .build();

        FinanceBudgetAiParseVo vo = financeBudgetAiService.parseBudgetCommand(req);

        Assertions.assertNotNull(vo);
        Assertions.assertEquals(new BigDecimal("3000"), vo.getBudgetAmount());
        Assertions.assertEquals("custom", vo.getScopeMode());
        Assertions.assertEquals("expense", vo.getIncomeAndExpenses());
        Assertions.assertTrue(vo.getCategoryCodes().contains("餐饮美食"));
        Assertions.assertTrue(vo.getCategoryCodes().contains("交通出行"));
        Assertions.assertTrue(vo.getParseSummary().contains("3000"));
        Assertions.assertTrue(vo.getIsFallback());
    }

    @Test
    @DisplayName("AI自然语言解析：不设上限模式解析测试")
    void testParseNaturalLanguageCommandNoLimit() {
        FinanceBudgetAiParseReq req = FinanceBudgetAiParseReq.builder()
                .text("下个月不设上限，统计全部日常消费")
                .budgetMonth("2026-10")
                .build();

        FinanceBudgetAiParseVo vo = financeBudgetAiService.parseBudgetCommand(req);

        Assertions.assertNotNull(vo);
        Assertions.assertEquals(BigDecimal.ZERO, vo.getBudgetAmount());
        Assertions.assertEquals("all", vo.getScopeMode());
        Assertions.assertEquals("expense", vo.getIncomeAndExpenses());
        Assertions.assertTrue(vo.getParseSummary().contains("不设上限"));
    }

    @Test
    @DisplayName("AI预算测算与指令解析：前端旧命名兼容性测试")
    void testFrontendCompatibility() {
        // 测试推荐返回 VO 兼容字段
        FinanceBudgetAiRecommendVo recommendVo = FinanceBudgetAiRecommendVo.builder()
                .budgetMonth("2026-10")
                .recommendedAmount(new BigDecimal("3000.00"))
                .moderateAmount(new BigDecimal("3000.00"))
                .conservativeAmount(new BigDecimal("2000.00"))
                .flexibleAmount(new BigDecimal("5000.00"))
                .historyMonthlyAverage(new BigDecimal("3200.00"))
                .recommendedCategoryCodes(Arrays.asList("餐饮美食", "交通出行"))
                .dailyLimitSuggest(new BigDecimal("100.0"))
                .build();

        Assertions.assertEquals(new BigDecimal("2000.00"), recommendVo.getSavingAmount());
        Assertions.assertEquals(new BigDecimal("5000.00"), recommendVo.getRelaxedAmount());
        Assertions.assertEquals(new BigDecimal("3000.00"), recommendVo.getRecommendAmount());
        Assertions.assertEquals(new BigDecimal("3200.00"), recommendVo.getAvgMonthlyExpense());
        Assertions.assertEquals(Arrays.asList("餐饮美食", "交通出行"), recommendVo.getRecommendedCategories());
        Assertions.assertEquals(new BigDecimal("100.0"), recommendVo.getDailyLimitSuggest());

        // 测试前端传入 command 字段自动映射至 text
        FinanceBudgetAiParseReq parseReq = new FinanceBudgetAiParseReq();
        parseReq.setCommand("每月2000零花钱，全部分类");
        Assertions.assertEquals("每月2000零花钱，全部分类", parseReq.getText());

        FinanceBudgetAiParseVo parseVo = financeBudgetAiService.parseBudgetCommand(parseReq);
        Assertions.assertNotNull(parseVo);
        Assertions.assertTrue(parseVo.getHasBudget());
        Assertions.assertEquals(new BigDecimal("2000"), parseVo.getAmount());
        Assertions.assertNotNull(parseVo.getReasoning());
    }
}
