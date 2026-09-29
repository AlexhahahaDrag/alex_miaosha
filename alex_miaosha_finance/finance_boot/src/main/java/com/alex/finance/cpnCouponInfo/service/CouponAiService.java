package com.alex.finance.cpnCouponInfo.service;

import com.alex.api.ai.api.AiAnalyzeApi;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.alex.api.ai.vo.AiAnalyzeResp;
import com.alex.api.finance.cpnCouponInfo.vo.CpnCouponAiPlanReq;
import com.alex.api.finance.cpnCouponInfo.vo.CpnCouponAiPlanVo;
import com.alex.base.common.Result;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 优惠券 AI 智能策划服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponAiService {

    private final AiAnalyzeApi aiAnalyzeApi;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CpnCouponAiPlanVo generatePlan(CpnCouponAiPlanReq req) {
        CpnCouponAiPlanVo plan = buildFallbackPlan(req);

        if (aiAnalyzeApi == null) {
            return plan;
        }

        try {
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("budget", req != null ? req.getBudget() : null);
            ctx.put("targetGoal", req != null ? req.getTargetGoal() : null);
            ctx.put("industryCategory", req != null ? req.getIndustryCategory() : null);
            ctx.put("expectedUsers", req != null ? req.getExpectedUsers() : null);

            String prompt = String.format(
                    "作为电商与零售营销增长专家，请根据以下预算与目标设计最优优惠券方案：\n" +
                    "- 营销总预算：%s 元\n" +
                    "- 营销目标：%s\n" +
                    "- 适用行业：%s\n" +
                    "- 期望触达人数：%s\n" +
                    "请以标准 JSON 格式输出，包含以下字段：couponName(券名称), unitValue(单张减免面额，数字), minSpend(使用门槛金额，数字), totalQuantity(发券张数，整数), validDays(有效天数，整数), discountRate(折扣说明), marketingCopy(引流宣传文案), strategyReasoning(策划理由与ROI预估)。只需返回JSON，不要markdown。",
                    (req != null && req.getBudget() != null) ? req.getBudget() : "5000",
                    (req != null && StringUtils.hasText(req.getTargetGoal())) ? req.getTargetGoal() : "拉新获客",
                    (req != null && StringUtils.hasText(req.getIndustryCategory())) ? req.getIndustryCategory() : "全品类通用",
                    (req != null && req.getExpectedUsers() != null) ? req.getExpectedUsers() : "500"
            );

            AiAnalyzeReq aiReq = new AiAnalyzeReq();
            aiReq.setBizType("coupon-ai-plan");
            aiReq.setContent(prompt);
            aiReq.setContext(ctx);

            Result<AiAnalyzeResp> result = aiAnalyzeApi.chat(aiReq);
            if (result != null && result.getData() != null && StringUtils.hasText(result.getData().getSummary())) {
                String text = result.getData().getSummary().trim();
                if (text.startsWith("```json")) {
                    text = text.substring(7);
                } else if (text.startsWith("```")) {
                    text = text.substring(3);
                }
                if (text.endsWith("```")) {
                    text = text.substring(0, text.length() - 3);
                }
                text = text.trim();
                JsonNode root = objectMapper.readTree(text);
                if (root != null) {
                    if (root.hasNonNull("couponName")) plan.setCouponName(root.get("couponName").asText());
                    if (root.hasNonNull("unitValue")) plan.setUnitValue(new BigDecimal(root.get("unitValue").asText()));
                    if (root.hasNonNull("minSpend")) plan.setMinSpend(new BigDecimal(root.get("minSpend").asText()));
                    if (root.hasNonNull("totalQuantity")) plan.setTotalQuantity(root.get("totalQuantity").asInt());
                    if (root.hasNonNull("validDays")) plan.setValidDays(root.get("validDays").asInt());
                    if (root.hasNonNull("discountRate")) plan.setDiscountRate(root.get("discountRate").asText());
                    if (root.hasNonNull("marketingCopy")) plan.setMarketingCopy(root.get("marketingCopy").asText());
                    if (root.hasNonNull("strategyReasoning")) plan.setStrategyReasoning(root.get("strategyReasoning").asText());
                }
            }
        } catch (Exception ex) {
            log.warn("CouponAiService AI plan call degraded to fallback: {}", ex.getMessage());
        }

        return plan;
    }

    public CpnCouponAiPlanVo buildFallbackPlan(CpnCouponAiPlanReq req) {
        BigDecimal budget = (req != null && req.getBudget() != null && req.getBudget().compareTo(BigDecimal.ZERO) > 0)
                ? req.getBudget()
                : new BigDecimal("5000.00");
        String goal = (req != null && StringUtils.hasText(req.getTargetGoal())) ? req.getTargetGoal().trim() : "拉新";
        String category = (req != null && StringUtils.hasText(req.getIndustryCategory())) ? req.getIndustryCategory().trim() : "全品类";

        BigDecimal unitValue;
        BigDecimal minSpend;
        int validDays;
        String couponName;
        String discountRate;
        String marketingCopy;
        String reasoning;

        if (goal.contains("拉新") || goal.contains("新客") || goal.contains("获客")) {
            unitValue = new BigDecimal("15.00");
            minSpend = new BigDecimal("50.00");
            validDays = 7;
            couponName = category + "新客专享满50减15元券";
            discountRate = "满50减15 (相当于7折)";
            marketingCopy = "新人首单专享好礼！全场精选满50立减15元，即刻开启省钱之旅！";
            reasoning = "针对新客破冰，低门槛高感知立减能有效打消首次消费顾虑，大幅提升首次支付转化率。";
        } else if (goal.contains("促活") || goal.contains("唤醒") || goal.contains("老客") || goal.contains("复购")) {
            unitValue = new BigDecimal("25.00");
            minSpend = new BigDecimal("120.00");
            validDays = 5;
            couponName = category + "老友回归满120减25元券";
            discountRate = "满120减25 (约7.9折)";
            marketingCopy = "老朋友好久不见！专属心意礼遇已就绪，满120立减25元，欢迎回家！";
            reasoning = "通过适度提高客单门槛激发老客复购冲动，短期5天时效制造紧迫感，有效唤醒沉睡用户。";
        } else if (goal.contains("清仓") || goal.contains("库存") || goal.contains("冲量")) {
            unitValue = new BigDecimal("50.00");
            minSpend = new BigDecimal("200.00");
            validDays = 3;
            couponName = category + "爆款狂欢满200减50元券";
            discountRate = "满200减50 (7.5折大额券)";
            marketingCopy = "限时仓储大促！全场爆款拼单凑单立享折上折，满200立减50元手慢无！";
            reasoning = "大额立减倒逼凑单买多件，3天超短窗口期促进决策，集中消化季节性与爆款库存。";
        } else {
            unitValue = new BigDecimal("20.00");
            minSpend = new BigDecimal("100.00");
            validDays = 7;
            couponName = category + "专享特惠满100减20元券";
            discountRate = "满100减20 (8折普惠)";
            marketingCopy = "诚意满满，精选特惠！满100即享20元直减，品质优选触手可得！";
            reasoning = "经典8折适度折扣方案，兼顾商家毛利率与消费者实惠感，适合常规节日促活及日常引流。";
        }

        int quantity = budget.divide(unitValue, 0, RoundingMode.FLOOR).intValue();
        if (quantity < 10) {
            quantity = 10;
        }

        return new CpnCouponAiPlanVo()
                .setCouponName(couponName)
                .setUnitValue(unitValue)
                .setMinSpend(minSpend)
                .setTotalQuantity(quantity)
                .setValidDays(validDays)
                .setDiscountRate(discountRate)
                .setMarketingCopy(marketingCopy)
                .setStrategyReasoning(reasoning);
    }
}
