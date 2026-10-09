package com.alex.api.finance.vo.finance;

import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * description: 月度零花钱预算与消费状态Vo
 * author: alex
 * createDate: 2026-10-08
 * version: 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel(value = "FinanceBudgetStatusVo", description = "月度零花钱预算与消费状态Vo")
public class FinanceBudgetStatusVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "预算记录ID")
    private Long id;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "家庭组/机构ID")
    private Long orgId;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "归属用户ID(可选)")
    private Long belongTo;

    @ApiModelProperty(value = "预算月份 (格式: YYYY-MM)")
    private String budgetMonth;

    public String getYearMonth() {
        return this.budgetMonth;
    }

    public void setYearMonth(String yearMonth) {
        this.budgetMonth = yearMonth;
    }

    @ApiModelProperty(value = "月度零花钱预算金额")
    private BigDecimal budgetAmount;

    @ApiModelProperty(value = "收支类型(expense:支出, income:收入)")
    private String incomeAndExpenses;

    @ApiModelProperty(value = "纳入统计的类别编码列表")
    private List<String> categoryCodes;

    @ApiModelProperty(value = "纳入统计的类别名称列表")
    private List<String> categoryNames;

    @ApiModelProperty(value = "当月在指定分类下的实际支出总额")
    private BigDecimal actualExpense;

    @ApiModelProperty(value = "剩余零花钱 (预算 - 实际支出)")
    private BigDecimal remainingAmount;

    @ApiModelProperty(value = "预算使用百分比 (0~100+)")
    private BigDecimal usagePercent;

    @ApiModelProperty(value = "是否已超支")
    private Boolean isOverBudget;

    @ApiModelProperty(value = "当前配置是否继承自历史月")
    private Boolean isInherited;
}
