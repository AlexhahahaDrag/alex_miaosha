package com.alex.finance.shopOrder.entity;

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
 * description:  商店订单表类
 * author:       alex
 * createDate: 2024-04-09 15:20:01
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_shop_order")
@ApiModel(value = "ShopOrder对象", description = "商店订单表")
public class ShopOrder extends BaseEntity<ShopOrder>{

    @ApiModelProperty(value = "订单编码")
    @TableField("sale_order_code")
    private String saleOrderCode;

    @ApiModelProperty(value = "订单名称")
    @TableField("sale_order_name")
    private String saleOrderName;

    @ApiModelProperty(value = "总销售金额")
    @TableField("sale_amount")
    private BigDecimal saleAmount;

    @ApiModelProperty(value = "状态,字典(is_valid) 1：有效,0:失效)")
    @TableField("is_valid")
    private String isValid;

    @ApiModelProperty(value = "销售日期")
    @TableField("sale_date")
    private LocalDateTime saleDate;

    @ApiModelProperty(value = "描述")
    @TableField("`description`")
    private String description;

    @ApiModelProperty(value = "支付方式")
    @TableField("pay_way")
    private String payWay;

    @ApiModelProperty(value = "销售数量")
    @TableField("sale_count")
    private BigDecimal saleCount;

}
