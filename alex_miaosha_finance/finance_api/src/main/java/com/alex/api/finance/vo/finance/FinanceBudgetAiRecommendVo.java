package com.alex.api.finance.vo.finance;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * description: AI 智能预算测算推荐结果 VO
 * author: alex
 * createDate: 2026-10-12
 * version: 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel(value = "FinanceBudgetAiRecommendVo", description = "AI 智能预算测算推荐结果 VO")
public class FinanceBudgetAiRecommendVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "推荐目标月份 (YYYY-MM)")
    private String budgetMonth;

    @ApiModelProperty(value = "主推建议金额 (默认通常采用适度型)")
    private BigDecimal recommendedAmount;

    @ApiModelProperty(value = "适度型推荐金额 (基于历史基线加合理缓冲)")
    private BigDecimal moderateAmount;

    @ApiModelProperty(value = "节约/稳健型推荐金额 (收紧弹性开销，适合存钱目标)")
    private BigDecimal conservativeAmount;

    @ApiModelProperty(value = "宽松型推荐金额 (预留较高品质生活空间)")
    private BigDecimal flexibleAmount;

    @ApiModelProperty(value = "推荐统计范畴 (all:全量日常, custom:自选分类)")
    private String recommendedScopeMode;

    @ApiModelProperty(value = "推荐重点管控分类编码/名称列表")
    private List<String> recommendedCategoryCodes;

    @ApiModelProperty(value = "AI 测算推导与消费画像理由说明")
    private String reasoning;

    @ApiModelProperty(value = "消费健康诊断与省钱小贴士(含建议日均开销)")
    private String healthAdvice;

    @ApiModelProperty(value = "近3个月历史月均总支出(参考基准)")
    private BigDecimal historyMonthlyAverage;

    @ApiModelProperty(value = "每日均摊建议额度")
    private BigDecimal dailyLimitSuggest;

    @ApiModelProperty(value = "是否由兜底规则引擎生成(true:规则降级, false:大模型生成)")
    private Boolean isFallback;

    // --- 前端旧命名兼容层 ---
    public BigDecimal getSavingAmount() {
        return conservativeAmount;
    }

    public BigDecimal getRelaxedAmount() {
        return flexibleAmount;
    }

    public BigDecimal getRecommendAmount() {
        return recommendedAmount;
    }

    public BigDecimal getAvgMonthlyExpense() {
        return historyMonthlyAverage;
    }

    public List<String> getRecommendedCategories() {
        return recommendedCategoryCodes;
    }

    public BigDecimal getDailyLimitSuggest() {
        if (dailyLimitSuggest != null) {
            return dailyLimitSuggest;
        }
        BigDecimal base = recommendedAmount != null ? recommendedAmount : moderateAmount;
        if (base != null && base.compareTo(BigDecimal.ZERO) > 0) {
            return base.divide(BigDecimal.valueOf(30), 1, java.math.RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
}
