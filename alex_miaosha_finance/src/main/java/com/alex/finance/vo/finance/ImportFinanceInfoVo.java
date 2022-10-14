package com.alex.finance.vo.finance;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.handler.inter.IExcelDataModel;
import cn.afterturn.easypoi.handler.inter.IExcelModel;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @description: 财务信息表Vo
 * @author: majf
 * @createDate: 2022-10-10 18:02:03
 * @version: 1.0.0
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "ImportFinanceInfoVo", description = "导入财务信息表")
public class ImportFinanceInfoVo implements Serializable, IExcelModel, IExcelDataModel {

    @Excel(name = "名称")
    @ApiModelProperty(name = "name", value = "名称")
    private String name;

    @ApiModelProperty(name = "typeCode", value = "类别编码")
    private String typeCode;

    @Excel(name = "类别")
    @ApiModelProperty(value = "类别")
    @TableField("type_name")
    private String typeName;

    @Excel(name = "金额")
    @ApiModelProperty(name = "amount", value = "金额")
    private BigDecimal amount;

    @Excel(name = "来源", dict = "pay_way")
    @ApiModelProperty(name = "fromSource", value = "来源")
    private String fromSource;

    @ApiModelProperty(name = "isValid", value = "是否有效")
    private Integer isValid;

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
