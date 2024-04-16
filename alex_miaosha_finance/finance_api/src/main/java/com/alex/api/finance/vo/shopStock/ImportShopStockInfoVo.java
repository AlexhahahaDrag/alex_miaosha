package com.alex.api.finance.vo.shopStock;

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
@ApiModel(value = "ImportShopStockInfoVo", description = "导入商品库存信息")
public class ImportShopStockInfoVo implements Serializable, IExcelModel, IExcelDataModel {

    @Excel(name = "商品名称")
    @ApiModelProperty(name = "shopName", value = "商品名称")
    private String shopName;

    @ApiModelProperty(value = "类别")
    private String typeName;

    @Excel(name = "成本价")
    @ApiModelProperty(name = "costAmount", value = "成本价")
    private BigDecimal costAmount;

    @Excel(name = "零售价")
    @ApiModelProperty(name = "saleAmount", value = "零售价")
    private BigDecimal saleAmount;

    @ApiModelProperty(value = "状态,字典(is_valid) 1：有效,0:失效)")
    private String isValid;

    @ApiModelProperty(value = "进货日期")
    @Excel(name = "进货日期")
    private LocalDateTime saleDate;

    @Excel(name = "类别", dict = "shop_category")
    @ApiModelProperty(value = "类别,字典(shop_category)")
    private String category;

    @Excel(name = "进货地点")
    @ApiModelProperty(value = "进货地点,字典(stock_place) ")
    private String purchasePlace;

    @Excel(name = "数量")
    @ApiModelProperty(value = "数量")
    private BigDecimal saleNum;

    @Excel(name = "原商品编码")
    @ApiModelProperty(value = "原商品编码")
    private String oldShopCode;

    @Excel(name = "款式")
    @ApiModelProperty(value = "款式")
    private String style;

    @Excel(name = "颜色")
    @ApiModelProperty(value = "颜色")
    private String color;

    @Excel(name = "尺码")
    @ApiModelProperty(value = "尺码")
    private String size;

    @ApiModelProperty(value = "描述")
    private String description;

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
