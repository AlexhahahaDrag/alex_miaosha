package com.alex.common.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@Builder
@EqualsAndHashCode
@ApiModel(value = "GoodsDTO", description = "商品信息")
public class WelcomeVo implements Serializable {

    @ApiModelProperty(value = "商品数")
    private Long goodsCount;

    @ApiModelProperty(value = "秒杀数")
    private Long seckillCount;

    @ApiModelProperty(value = "订单数")
    private Long orderCount;
}
