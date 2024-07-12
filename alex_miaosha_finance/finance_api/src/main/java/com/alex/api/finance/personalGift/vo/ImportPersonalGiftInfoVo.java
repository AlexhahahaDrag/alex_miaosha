package com.alex.api.finance.personalGift.vo;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.handler.inter.IExcelDataModel;
import cn.afterturn.easypoi.handler.inter.IExcelModel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * description: 财务信息表Vo
 * author: majf
 * createDate: 2022-10-10 18:02:03
 * version: 1.0.0
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "ImportPersonalGiftInfoVo", description = "导入个人随礼信息")
public class ImportPersonalGiftInfoVo implements Serializable, IExcelModel, IExcelDataModel {

    @Excel(name = "事件名称")
    @ApiModelProperty(name = "eventName", value = "事件名称")
    private String eventName;

    @Excel(name = "金额")
    @ApiModelProperty(name = "amount", value = "金额")
    private BigDecimal amount;

    @Excel(name = "人员")
    @ApiModelProperty(name = "otherPerson", value = "人员")
    private String otherPerson;

    @Excel(name = "随礼时间")
    @ApiModelProperty(name = "eventTime", value = "随礼时间")
    private LocalDateTime eventTime;

    @Excel(name = "备注")
    @ApiModelProperty(name = "remarks", value = "备注")
    private String remarks;

    @Excel(name = "随礼情况（随礼、收礼）", dict = "gift_action")
    @ApiModelProperty(name = "action", value = "来源")
    private String action;

    @Excel(name = "信息")
    private String errorMsg;

    private Integer rowNum;

    @Override
    public String getErrorMsg() {
        return errorMsg;
    }

    @Override
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    @Override
    public Integer getRowNum() {
        return rowNum;
    }

    @Override
    public void setRowNum(Integer rowNum) {
        this.rowNum = rowNum;
    }
}
