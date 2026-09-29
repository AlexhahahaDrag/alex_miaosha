package com.alex.finance.cpnCouponInfo;

import com.alex.api.finance.cpnCouponInfo.vo.CpnCouponAiPlanReq;
import com.alex.api.finance.cpnCouponInfo.vo.CpnCouponAiPlanVo;
import com.alex.finance.cpnCouponInfo.service.CouponAiService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class CouponAiServiceTest {

    private final CouponAiService couponAiService = new CouponAiService(null);

    @Test
    @DisplayName("AI优惠券策划：拉新目标下降级规则兜底测试")
    void testNewUserGoalFallback() {
        CpnCouponAiPlanReq req = new CpnCouponAiPlanReq()
                .setBudget(new BigDecimal("3000.00"))
                .setTargetGoal("拉新获客")
                .setIndustryCategory("餐饮美食")
                .setExpectedUsers(200);

        CpnCouponAiPlanVo plan = couponAiService.generatePlan(req);

        Assertions.assertNotNull(plan);
        Assertions.assertTrue(plan.getCouponName().contains("新客"));
        Assertions.assertEquals(new BigDecimal("15.00"), plan.getUnitValue());
        Assertions.assertEquals(new BigDecimal("50.00"), plan.getMinSpend());
        Assertions.assertEquals(200, plan.getTotalQuantity()); // 3000 / 15 = 200
        Assertions.assertEquals(7, plan.getValidDays());
        Assertions.assertNotNull(plan.getMarketingCopy());
        Assertions.assertNotNull(plan.getStrategyReasoning());
    }

    @Test
    @DisplayName("AI优惠券策划：老客促活目标下策略测试")
    void testRetentionGoalFallback() {
        CpnCouponAiPlanReq req = new CpnCouponAiPlanReq()
                .setBudget(new BigDecimal("5000.00"))
                .setTargetGoal("沉睡老客促活")
                .setIndustryCategory("数码百货")
                .setExpectedUsers(200);

        CpnCouponAiPlanVo plan = couponAiService.generatePlan(req);

        Assertions.assertNotNull(plan);
        Assertions.assertTrue(plan.getCouponName().contains("老友回归"));
        Assertions.assertEquals(new BigDecimal("25.00"), plan.getUnitValue());
        Assertions.assertEquals(new BigDecimal("120.00"), plan.getMinSpend());
        Assertions.assertEquals(200, plan.getTotalQuantity()); // 5000 / 25 = 200
        Assertions.assertEquals(5, plan.getValidDays());
    }
}
