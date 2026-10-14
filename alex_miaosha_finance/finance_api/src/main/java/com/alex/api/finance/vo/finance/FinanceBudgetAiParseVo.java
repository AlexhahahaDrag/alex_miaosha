package com.alex.api.finance.vo.finance;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * description: AI 自然语言预算指令解析结果 VO
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
@ApiModel(value = "FinanceBudgetAiParseVo", description = "AI 自然语言预算指令解析结果 VO")
public class FinanceBudgetAiParseVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "解析出的预算金额 (0 表示不设上限)")
    private BigDecimal budgetAmount;

    @ApiModelProperty(value = "解析出的统计范畴 (all:全量日常, custom:自选分类)")
    private String scopeMode;

    @ApiModelProperty(value = "解析出的收支方向 (expense:支出, income:收入)")
    private String incomeAndExpenses;

    @ApiModelProperty(value = "解析出的分类编码/名称列表")
    private List<String> categoryCodes;

    @ApiModelProperty(value = "AI 解析结果友好说明与摘要")
    private String parseSummary;

    @ApiModelProperty(value = "是否由兜底规则引擎生成")
    private Boolean isFallback;

    // --- 前端旧命名兼容层 ---
    public Boolean getHasBudget() {
        return budgetAmount != null;
    }

    public BigDecimal getAmount() {
        return budgetAmount;
    }

    public Boolean getHasCategories() {
        return categoryCodes != null && !categoryCodes.isEmpty();
    }

    public List<String> getCategories() {
        return categoryCodes;
    }

    public String getReasoning() {
        return parseSummary;
    }
}
