package com.alex.mission.pojo.vo;


import com.alex.common.pojo.dto.GoodsDTO;
import com.alex.mission.pojo.entity.Goods;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiModel(value = "GoodsDetailVo", description = "商品明细信息")
public class GoodsDetailVo extends BaseVo {

    @ApiModelProperty(value = "剩余时间")
    private Integer remainSeconds = 0;

    @ApiModelProperty(value = "库存数量")
    private Integer stockCount;

    @ApiModelProperty(value = "商品信息")
    private GoodsDTO goods;
}
