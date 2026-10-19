package com.alex.finance.cpnCouponInfo;

import com.alex.api.ai.api.AiAnalyzeApi;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.alex.api.ai.vo.AiAnalyzeResp;
import com.alex.api.finance.cpnCouponInfo.vo.CpnCouponAiPlanReq;
import com.alex.api.finance.cpnCouponInfo.vo.CpnCouponAiPlanVo;
import com.alex.base.common.Result;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.SystemException;
import com.alex.finance.cpnCouponInfo.service.CouponAiService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 消费券营销策划 AI 服务双路由 (DeepSeek 主路由 + 本地规则降级) 集成测试
 */
@ExtendWith(MockitoExtension.class)
class CouponAiServiceDualRoutingIT {

    @Mock
    private AiAnalyzeApi aiAnalyzeApi;

    @InjectMocks
    private CouponAiService couponAiService;

    @Test
    @DisplayName("主路由：AI大模型调用成功时精准解析优惠券方案全部要素")
    void ai_success_route_should_parse_plan_json_accurately() {
        String mockAiJson = "```json\n" +
                "{\n" +
                "  \"couponName\": \"【初春踏青】特惠立减大礼包\",\n" +
                "  \"unitValue\": 20.00,\n" +
                "  \"minSpend\": 88.00,\n" +
                "  \"totalQuantity\": 500,\n" +
                "  \"validDays\": 14,\n" +
                "  \"discountRate\": \"满88减20（约7.7折）\",\n" +
                "  \"marketingCopy\": \"春暖花开，踏青出游享立减！满88元即可立减20元，速领专享特惠券！\",\n" +
                "  \"strategyReasoning\": \"针对春季出游高峰期，通过门槛88元激励凑单，预计提升客单价35%，ROI达1:4.8。\"\n" +
                "}\n" +
                "```";

        AiAnalyzeResp resp = new AiAnalyzeResp();
        resp.setSummary(mockAiJson);
        when(aiAnalyzeApi.chat(any(AiAnalyzeReq.class))).thenReturn(Result.success(resp));

        CpnCouponAiPlanReq req = new CpnCouponAiPlanReq()
                .setBudget(new BigDecimal("10000.00"))
                .setTargetGoal("拉新与客单提升")
                .setIndustryCategory("户外出行")
                .setExpectedUsers(500);

        CpnCouponAiPlanVo plan = couponAiService.generatePlan(req);

        assertNotNull(plan);
        assertEquals("【初春踏青】特惠立减大礼包", plan.getCouponName());
        assertEquals(0, new BigDecimal("20.00").compareTo(plan.getUnitValue()));
        assertEquals(0, new BigDecimal("88.00").compareTo(plan.getMinSpend()));
        assertEquals(500, plan.getTotalQuantity());
        assertEquals(14, plan.getValidDays());
        assertEquals("满88减20（约7.7折）", plan.getDiscountRate());
        assertTrue(plan.getMarketingCopy().contains("踏青出游"));
        assertTrue(plan.getStrategyReasoning().contains("提升客单价35%"));
    }

    @Test
    @DisplayName("降级路由：Feign微服务网络超时或熔断抛出 SystemException 时，无感降级到本地规则引擎")
    void feign_timeout_or_offline_should_seamlessly_fallback() {
        when(aiAnalyzeApi.chat(any(AiAnalyzeReq.class)))
                .thenThrow(new SystemException(ResultEnum.SYSTEM_NO_AVAILABLE, "ai"));

        CpnCouponAiPlanReq req = new CpnCouponAiPlanReq()
                .setBudget(new BigDecimal("6000.00"))
                .setTargetGoal("拉新获客")
                .setIndustryCategory("美妆护肤")
                .setExpectedUsers(300);

        // 断言：服务层绝不抛出 500 异常，而是平稳返回降级规则方案
        CpnCouponAiPlanVo plan = assertDoesNotThrow(() -> couponAiService.generatePlan(req));

        assertNotNull(plan);
        assertTrue(plan.getCouponName().contains("新客"));
        assertEquals(new BigDecimal("15.00"), plan.getUnitValue());
        assertEquals(new BigDecimal("50.00"), plan.getMinSpend());
        assertEquals(400, plan.getTotalQuantity()); // 6000 / 15 = 400
        assertEquals(7, plan.getValidDays());
        assertNotNull(plan.getMarketingCopy());
        assertNotNull(plan.getStrategyReasoning());
    }

    @Test
    @DisplayName("容错测试：AI返回破损或残缺字符串时，自愈并保持默认规则方案")
    void corrupted_json_should_preserve_default_plan() {
        AiAnalyzeResp resp = new AiAnalyzeResp();
        resp.setSummary("AI 思考超时，无法生成合法 JSON 内容");
        when(aiAnalyzeApi.chat(any(AiAnalyzeReq.class))).thenReturn(Result.success(resp));

        CpnCouponAiPlanReq req = new CpnCouponAiPlanReq()
                .setBudget(new BigDecimal("5000.00"))
                .setTargetGoal("老客促活")
                .setIndustryCategory("母婴用品");

        CpnCouponAiPlanVo plan = assertDoesNotThrow(() -> couponAiService.generatePlan(req));

        assertNotNull(plan);
        assertTrue(plan.getCouponName().contains("老友回归"));
        assertEquals(new BigDecimal("25.00"), plan.getUnitValue());
        assertEquals(new BigDecimal("120.00"), plan.getMinSpend());
    }

    @Test
    @DisplayName("七点法边界：0预算、负预算与 null 请求参数时规则引擎具备健壮性")
    void fallback_plan_seven_point_boundary() {
        CouponAiService offlineService = new CouponAiService(null);

        // 1. null 请求
        CpnCouponAiPlanVo nullVo = offlineService.generatePlan(null);
        assertNotNull(nullVo);
        assertTrue(nullVo.getCouponName().contains("新客"));

        // 2. 0 预算兜底
        CpnCouponAiPlanReq zeroBudgetReq = new CpnCouponAiPlanReq().setBudget(BigDecimal.ZERO);
        CpnCouponAiPlanVo zeroBudgetVo = offlineService.generatePlan(zeroBudgetReq);
        assertNotNull(zeroBudgetVo);
        assertTrue(zeroBudgetVo.getTotalQuantity() > 0);

        // 3. 负预算兜底为默认 5000
        CpnCouponAiPlanReq negativeBudgetReq = new CpnCouponAiPlanReq().setBudget(new BigDecimal("-1000.00"));
        CpnCouponAiPlanVo negativeBudgetVo = offlineService.generatePlan(negativeBudgetReq);
        assertNotNull(negativeBudgetVo);
        assertEquals(new BigDecimal("15.00"), negativeBudgetVo.getUnitValue());
    }
}
