package com.alex.finance.vo.financeAnalysis;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * @description:
 * @author: alex
 * @createDate: 2022/10/22 15:03
 * @version: 1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "AnalysisVo", description = "分析vo")
public class AnalysisVo {

    @ApiModelProperty(name = "typeCode", value = "类别编码")
    private String typeCode;

    @ApiModelProperty(name = "typeName", value = "类别")
    private String typeName;

    @ApiModelProperty(name = "incomeAndExpenses", value = "支出收入类型")
    private String incomeAndExpenses;

    @ApiModelProperty(name = "amount", value = "钱数")
    private BigDecimal amount;
}
