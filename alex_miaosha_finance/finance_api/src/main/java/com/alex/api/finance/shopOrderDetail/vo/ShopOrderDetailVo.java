package com.alex.api.finance.shopOrderDetail.vo;

import com.alex.common.common.BaseVo;
import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * description:  商店订单明细表Vo
 * author:       alex
 * createDate:   2024-04-09 15:35:21
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "ShopOrderDetailVo", description = "商店订单明细表Vo")
public class ShopOrderDetailVo extends BaseVo<ShopOrderDetailVo>{

    @ApiModelProperty(value = "订单id")
    @JsonSerialize(using = Long2StringSerializer.class)
    private Long orderId;

    @ApiModelProperty(value = "商品名称")
    private String shopName;

    @ApiModelProperty(value = "商品编码")
    private String shopCode;

    @ApiModelProperty(value = "原商品编码")
    private String oldShopCode;


    @ApiModelProperty(value = "售价")
    private BigDecimal saleAmount;

    @ApiModelProperty(value = "是否有效")
    private String isValid;

    @ApiModelProperty(value = "销售日期")
    private LocalDateTime saleDate;

    @ApiModelProperty(value = "支付方式")
    private String payWay;

    @ApiModelProperty(value = "个数")
    private BigDecimal saleNum;

    @ApiModelProperty(value = "商品库存id")
    @JsonSerialize(using = Long2StringSerializer.class)
    private Long shopStockId;

    @ApiModelProperty(value = "商品购物车id")
    @JsonSerialize(using = Long2StringSerializer.class)
    private Long shopCartId;
}
