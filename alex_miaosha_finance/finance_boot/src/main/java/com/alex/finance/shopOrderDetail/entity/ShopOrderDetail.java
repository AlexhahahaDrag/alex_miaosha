package com.alex.finance.shopOrderDetail.entity;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import com.alex.common.config.Long2StringSerializer;

/**
 * description:  商店订单明细表类
 * author:       alex
 * createDate: 2024-04-09 15:35:21
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_shop_order_detail")
@ApiModel(value = "ShopOrderDetail对象", description = "商店订单明细表")
public class ShopOrderDetail extends BaseEntity<ShopOrderDetail>{

    @ApiModelProperty(value = "订单id")
    @TableField("order_id")
    private Long orderId;

    @ApiModelProperty(value = "商品名称")
    @TableField("shop_name")
    private String shopName;

    @ApiModelProperty(value = "商品编码")
    @TableField("shop_code")
    private String shopCode;

    @ApiModelProperty(value = "售价")
    @TableField("sale_amount")
    private BigDecimal saleAmount;

    @ApiModelProperty(value = "是否有效")
    @TableField("is_valid")
    private String isValid;

    @ApiModelProperty(value = "销售日期")
    @TableField("sale_date")
    private LocalDateTime saleDate;

    @ApiModelProperty(value = "支付方式")
    @TableField("pay_way")
    private String payWay;

    @ApiModelProperty(value = "个数")
    @TableField("sale_num")
    private BigDecimal saleNum;

    @ApiModelProperty(value = "商品库存id")
    @TableField("shop_stock_id")
    private Long shopStockId;

}
