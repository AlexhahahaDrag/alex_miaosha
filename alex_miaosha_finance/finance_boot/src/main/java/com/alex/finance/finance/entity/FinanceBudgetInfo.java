package com.alex.finance.finance.entity;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * description: 月度零花钱预算表实体
 * author: alex
 * createDate: 2026-10-08
 * version: 1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("finance_budget_info")
@ApiModel(value = "FinanceBudgetInfo 对象", description = "月度零花钱预算表")
public class FinanceBudgetInfo extends BaseEntity<FinanceBudgetInfo> {

    @ApiModelProperty(value = "属于(用户ID)")
    @TableField("belong_to")
    private Long belongTo;

    @ApiModelProperty(value = "预算月份 (YYYY-MM)")
    @TableField("budget_month")
    private String budgetMonth;

    public String getYearMonth() {
        return this.budgetMonth;
    }

    public FinanceBudgetInfo setYearMonth(String yearMonth) {
        this.budgetMonth = yearMonth;
        return this;
    }

    @ApiModelProperty(value = "收支类型 (expense:支出, income:收入)")
    @TableField("income_and_expenses")
    private String incomeAndExpenses;

    @ApiModelProperty(value = "月度零花钱预算金额")
    @TableField("budget_amount")
    private BigDecimal budgetAmount;

    @ApiModelProperty(value = "纳入统计的类别编码列表(逗号分隔)")
    @TableField("category_codes")
    private String categoryCodes;

    @ApiModelProperty(value = "是否有效")
    @TableField(value = "is_valid", fill = FieldFill.INSERT)
    private String isValid;
}
