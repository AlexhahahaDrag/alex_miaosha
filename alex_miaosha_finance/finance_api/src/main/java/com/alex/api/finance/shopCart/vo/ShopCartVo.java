package com.alex.api.finance.shopCart.vo;

import com.alex.common.common.BaseVo;
import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * description:  购物车表Vo
 * author:       alex
 * createDate:   2024-04-03 11:36:19
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "ShopCartVo", description = "购物车表Vo")
public class ShopCartVo extends BaseVo<ShopCartVo>{

    @ApiModelProperty(value = "商品id")
    @JsonSerialize(using = Long2StringSerializer.class)
    private Long shopId;

    @ApiModelProperty(value = "人员id")
    @JsonSerialize(using = Long2StringSerializer.class)
    private Long userId;

    @ApiModelProperty(value = "客户id")
    @JsonSerialize(using = Long2StringSerializer.class)
    private Long customerId;

    @ApiModelProperty(value = "是否有效")
    private String isValid;

    @ApiModelProperty(value = "数量")
    private BigDecimal saleNum;

    @ApiModelProperty(value = "库存数量")
    private BigDecimal stockNum;

    @ApiModelProperty(value = "商品名称")
    private String shopName;

    @ApiModelProperty(value = "商品编码")
    private String shopCode;

    @ApiModelProperty(value = "零售价")
    private BigDecimal saleAmount;

    @ApiModelProperty(value = "类别,字典(shop_category)")
    private String category;

    @ApiModelProperty(value = "进货地点,字典(stock_place) ")
    private String purchasePlace;

    @ApiModelProperty(value = "原商品编码")
    private String oldShopCode;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "checked")
    private Boolean checked;
}
