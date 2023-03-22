package com.alex.mission.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
@ApiModel(value = "OrderDetailVo", description = "订单明细信息")
public class OrderDetailVo extends BaseVo {

    @ApiModelProperty(value = "订单id")
    private Long orderId;

    @ApiModelProperty(value = "商品id")
    private Long goodsId;

    @ApiModelProperty(value = "商品名称")
    private String goodsName;

    @ApiModelProperty(value = "创建时间")
    private Date createdTime;

    @ApiModelProperty(value = "创建时间")
    private Date updatedTime;
}
