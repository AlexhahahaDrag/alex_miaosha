package com.alex.common.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel(value = "SeckillGoodsDTO", description = "秒杀商品信息")
public class SeckillGoodsDTO {

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "商品id")
    private Long goodsId;

    @ApiModelProperty(value = "商品库存，-1表示没有限制")
    private Integer goodsStock;
}
