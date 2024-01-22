package com.alex.api.finance.vo.financeAnalysis;

import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * description:
 * author: alex
 * createDate: 2022/10/22 15:03
 * version: 1.0.0
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

    @ApiModelProperty(name = "infoDate", value = "业务日期")
    private String infoDate;

    @ApiModelProperty(name = "userId", value = "用户id")
    @JsonSerialize(using = Long2StringSerializer.class)
    private Long userId;

    @ApiModelProperty(name = "username", value = "用户名")
    private String username;
}
