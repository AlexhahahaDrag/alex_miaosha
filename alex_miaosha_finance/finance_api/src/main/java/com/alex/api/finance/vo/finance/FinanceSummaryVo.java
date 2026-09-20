package com.alex.api.finance.vo.finance;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * description: 财务信息多维统计聚合Vo
 * author: alex
 * createDate: 2026-09-21
 * version: 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel(value = "FinanceSummaryVo", description = "财务信息多维统计聚合Vo")
public class FinanceSummaryVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "筛选集总支出金额")
    private BigDecimal totalExpense;

    @ApiModelProperty(value = "筛选集总收入金额")
    private BigDecimal totalIncome;

    @ApiModelProperty(value = "筛选集净结余(总收入 - 总支出)")
    private BigDecimal totalBalance;

    @ApiModelProperty(value = "匹配账单总笔数")
    private Long totalCount;
}
