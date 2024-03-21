package com.alex.finance.entity.shopStock;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * description:  商店库存表类
 * author:       alex
 * createDate: 2024-03-12 16:49:20
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_shop_stock")
@ApiModel(value = "ShopStock对象", description = "商店库存表")
public class ShopStock extends BaseEntity<ShopStock>{

    @ApiModelProperty(value = "商品名称")
    @TableField("shop_name")
    private String shopName;

    @ApiModelProperty(value = "商品编码")
    @TableField("shop_code")
    private String shopCode;

    @ApiModelProperty(value = "成本价")
    @TableField("cost_amount")
    private BigDecimal costAmount;

    @ApiModelProperty(value = "零售价")
    @TableField("sale_amount")
    private BigDecimal saleAmount;

    @ApiModelProperty(value = "状态,字典(is_valid) 1：有效,0:失效)")
    @TableField("is_valid")
    private String isValid;

    @ApiModelProperty(value = "进货日期")
    @TableField("sale_date")
    private LocalDateTime saleDate;

    @ApiModelProperty(value = "类别,字典(shop_category)")
    @TableField("category")
    private String category;

    @ApiModelProperty(value = "进货地点,字典(stock_place) ")
    @TableField("purchase_place")
    private String purchasePlace;

    @ApiModelProperty(value = "数量")
    @TableField("sale_num")
    private BigDecimal saleNum;

    @ApiModelProperty(value = "原商品编码")
    @TableField("old_shop_code")
    private String oldShopCode;

    @ApiModelProperty(value = "描述")
    @TableField("description")
    private String description;
}
