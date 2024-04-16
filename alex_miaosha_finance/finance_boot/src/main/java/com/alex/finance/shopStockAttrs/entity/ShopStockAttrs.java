package com.alex.finance.shopStockAttrs.entity;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * description:  商店库存属性表类
 * author:       alex
 * createDate: 2024-04-16 19:50:29
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_shop_stock_attrs")
@ApiModel(value = "ShopStockAttrs对象", description = "商店库存属性表")
public class ShopStockAttrs extends BaseEntity<ShopStockAttrs>{

    @ApiModelProperty(value = "库存id")
    @TableField("stock_id")
    private Long stockId;

    @ApiModelProperty(value = "商品属性编码")
    @TableField("attr_code")
    private String attrCode;

    @ApiModelProperty(value = "商品属性名称")
    @TableField("attr_name")
    private String attrName;

    @ApiModelProperty(value = "商品属性值")
    @TableField("attr_value")
    private String attrValue;

    @ApiModelProperty(value = "状态,字典(is_valid) 1：有效,0:失效)")
    @TableField("is_valid")
    private String isValid;

    @ApiModelProperty(value = "描述")
    @TableField("`description`")
    private String description;

}
