package com.alex.finance.entity.shopFinance;

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
 * description:  商店财务表类
 * author:       majf
 * createDate: 2024-02-23 21:19:49
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_shop_finance")
@ApiModel(value = "ShopFinance对象", description = "商店财务表")
public class ShopFinance extends BaseEntity<ShopFinance>{

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

    @ApiModelProperty(value = "收支类型")
    @TableField("income_and_expenses")
    private String incomeAndExpenses;

    @ApiModelProperty(value = "支付方式")
    @TableField("pay_way")
    private String payWay;
}
