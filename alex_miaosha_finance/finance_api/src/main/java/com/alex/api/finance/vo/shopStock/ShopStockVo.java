package com.alex.api.finance.vo.shopStock;

import com.alex.common.common.BaseVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * description:  商店库存表Vo
 * author:       alex
 * createDate:   2024-03-12 16:49:20
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "ShopStockVo", description = "商店库存表Vo")
public class ShopStockVo extends BaseVo<ShopStockVo>{

    @ApiModelProperty(value = "商品名称")
    private String shopName;

    @ApiModelProperty(value = "商品编码")
    private String shopCode;

    @ApiModelProperty(value = "成本价")
    private BigDecimal costAmount;

    @ApiModelProperty(value = "零售价")
    private BigDecimal saleAmount;

    @ApiModelProperty(value = "状态,字典(is_valid) 1：有效,0:失效)")
    private String isValid;

    @ApiModelProperty(value = "进货日期")
    private LocalDateTime saleDate;

    @ApiModelProperty(value = "类别,字典(shop_category)")
    private String category;

    @ApiModelProperty(value = "进货地点,字典(stock_place) ")
    private String purchasePlace;

    @ApiModelProperty(value = "数量")
    private BigDecimal saleNum;

    @ApiModelProperty(value = "原商品编码")
    private String oldShopCode;

    @ApiModelProperty(value = "描述")
    private String description;
}
