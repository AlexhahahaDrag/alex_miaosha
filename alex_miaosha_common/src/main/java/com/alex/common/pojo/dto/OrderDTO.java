package com.alex.common.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@Accessors(chain = true)
@ApiModel(value = "OrderDTO", description = "订单信息")
public class OrderDTO {

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "商品id")
    private Long goodsId;

    @ApiModelProperty("创建时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdTime;

    @ApiModelProperty("更新时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedTime;
}
