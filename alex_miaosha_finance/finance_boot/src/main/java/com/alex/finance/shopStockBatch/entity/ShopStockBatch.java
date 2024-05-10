package com.alex.finance.shopStockBatch.entity;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import com.alex.common.config.Long2StringSerializer;

/**
 * description:  商店库存批次表类
 * author:       alex
 * createDate: 2024-05-10 17:30:40
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_shop_stock_batch")
@ApiModel(value = "ShopStockBatch对象", description = "商店库存批次表")
public class ShopStockBatch extends BaseEntity<ShopStockBatch>{

    @ApiModelProperty(value = "订单编码")
    @TableField("batch_code")
    private String batchCode;

    @ApiModelProperty(value = "订单名称")
    @TableField("batch_name")
    private String batchName;

    @ApiModelProperty(value = "状态,字典(is_valid) 1：有效,0:失效)")
    @TableField("is_valid")
    private String isValid;

    @ApiModelProperty(value = "描述")
    @TableField("`description`")
    private String description;

}
