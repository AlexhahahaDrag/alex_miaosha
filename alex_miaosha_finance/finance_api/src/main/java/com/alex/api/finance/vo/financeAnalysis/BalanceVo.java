package com.alex.api.finance.vo.financeAnalysis;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

/**
 * description: 余额分析VO
 * author: alex
 * createDate: 2026/03/29
 * version: 1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "BalanceVo", description = "余额分析VO")
public class BalanceVo {

    @ApiModelProperty(name = "list", value = "余额明细列表")
    private List<AnalysisVo> list;

    @ApiModelProperty(name = "momTrend", value = "总金额环比")
    private String momTrend;

    @ApiModelProperty(name = "yoyTrend", value = "总金额同比")
    private String yoyTrend;

    @ApiModelProperty(name = "monthIncomeSum", value = "月总收入")
    private BigDecimal monthIncomeSum;

    @ApiModelProperty(name = "monthExpenseSum", value = "月总支出")
    private BigDecimal monthExpenseSum;

    @ApiModelProperty(name = "incomeMomTrend", value = "收入环比")
    private String incomeMomTrend;

    @ApiModelProperty(name = "incomeYoyTrend", value = "收入同比")
    private String incomeYoyTrend;

    @ApiModelProperty(name = "expenseMomTrend", value = "支出环比")
    private String expenseMomTrend;

    @ApiModelProperty(name = "expenseYoyTrend", value = "支出同比")
    private String expenseYoyTrend;
}
