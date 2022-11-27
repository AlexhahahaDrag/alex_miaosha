package com.alex.finance.vo.finance;

import com.alex.common.common.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @description: 财务信息表Vo
 * @author: majf
 * @createDate: 2022-10-10 18:02:03
 * @version: 1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "FinanceInfoVo", description = "财务信息表Vo")
public class FinanceInfoVo extends BaseVo<FinanceInfoVo> {

    @ApiModelProperty(name = "name", value = "名称")
    private String name;

    @ApiModelProperty(name = "typeCode", value = "类别编码")
    private String typeCode;

    @ApiModelProperty(value = "类别")
    private String typeName;

    @ApiModelProperty(name = "amount", value = "金额")
    private BigDecimal amount;

    @ApiModelProperty(name = "fromSource", value = "来源编码")
    private String fromSource;

    @ApiModelProperty(name = "incomeAndExpenses", value = "收支类型")
    private String incomeAndExpenses;

    @ApiModelProperty(name = "belongTo", value = "属于")
    private Long belongTo;

    @ApiModelProperty(name = "infoDate", value = "业务日期")
    @JsonFormat(pattern="yyyy-MM-dd", timezone = "GMT+8:00")
    private LocalDateTime infoDate;

    @ApiModelProperty(name = "isValid", value = "是否有效")
    private Integer isValid;
}
