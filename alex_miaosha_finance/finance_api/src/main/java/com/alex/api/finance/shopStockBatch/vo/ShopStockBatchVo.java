package com.alex.api.finance.shopStockBatch.vo;

import com.alex.common.common.BaseVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * description:  商店库存批次表Vo
 * author:       alex
 * createDate:   2024-05-10 17:30:40
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "ShopStockBatchVo", description = "商店库存批次表Vo")
public class ShopStockBatchVo extends BaseVo<ShopStockBatchVo>{

    @ApiModelProperty(value = "订单编码")
    private String batchCode;

    @ApiModelProperty(value = "订单名称")
    private String batchName;

    @ApiModelProperty(value = "状态,字典(is_valid) 1：有效,0:失效)")
    private String isValid;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "标题")
    private String title;
}
