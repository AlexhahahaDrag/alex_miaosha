package com.alex.finance.shopCart.entity;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import com.alex.common.config.Long2StringSerializer;

/**
 * description:  购物车表类
 * author:       alex
 * createDate: 2024-04-03 11:36:19
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_shop_cart")
@ApiModel(value = "ShopCart对象", description = "购物车表")
public class ShopCart extends BaseEntity<ShopCart>{

    @ApiModelProperty(value = "商品id")
    @TableField("shop_id")
    private Long shopId;

    @ApiModelProperty(value = "人员id")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "客户id")
    @TableField("customer_id")
    private Long customerId;

    @ApiModelProperty(value = "是否有效")
    @TableField("is_valid")
    private String isValid;

    @ApiModelProperty(value = "数量")
    @TableField("sale_num")
    private BigDecimal saleNum;

}
