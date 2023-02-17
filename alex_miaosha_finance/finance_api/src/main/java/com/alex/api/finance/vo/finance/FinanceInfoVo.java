package com.alex.api.finance.vo.finance;

import com.alex.common.common.BaseVo;
import com.alex.common.config.Long2StringSerializer;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;
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

    @NotBlank(groups = {Insert.class, Update.class}, message = "名称不能为空！")
    @ApiModelProperty(name = "name", value = "名称")
    private String name;

    @NotBlank(groups = {Insert.class, Update.class}, message = "类别不能为空！")
    @ApiModelProperty(name = "typeCode", value = "类别编码")
    private String typeCode;

    @ApiModelProperty(value = "类别")
    private String typeName;

    @NotBlank(groups = {Insert.class, Update.class}, message = "金额不能为空！")
    @ApiModelProperty(name = "amount", value = "金额")
    private BigDecimal amount;

    @NotBlank(groups = {Insert.class, Update.class}, message = "来源不能为空！")
    @ApiModelProperty(name = "fromSource", value = "来源编码")
    private String fromSource;

    @NotBlank(groups = {Insert.class, Update.class}, message = "收支类型不能为空！")
    @ApiModelProperty(name = "incomeAndExpenses", value = "收支类型")
    private String incomeAndExpenses;

    @NotBlank(groups = {Insert.class, Update.class}, message = "属于不能为空！")
    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(name = "belongTo", value = "属于")
    private Long belongTo;

    @ApiModelProperty(name = "infoDate", value = "业务日期")
    private LocalDateTime infoDate;

    @ApiModelProperty(name = "isValid", value = "是否有效")
    private String isValid;

    @ApiModelProperty(name = "belongToName", value = "属于")
    private String belongToName;

    @ApiModelProperty(name = "infoDateStart", value = "开始业务日期")
    private LocalDate infoDateStart;

    @ApiModelProperty(name = "infoDateEnd", value = "结束业务日期")
    private LocalDate infoDateEnd;
}
