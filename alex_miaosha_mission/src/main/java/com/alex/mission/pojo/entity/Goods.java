package com.alex.mission.pojo.entity;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("goods")
@ApiModel(value = "Goods对象", description = "商品表")
public class Goods extends BaseEntity<Goods> {

    @ApiModelProperty(value = "商品名称")
    @TableField("goods_name")
    private String goodsName;

    @ApiModelProperty(value = "商品标题")
    @TableField("goods_title")
    private String goodsTitle;

    @ApiModelProperty(value = "商品的图片")
    @TableField("goods_img")
    private String goodsImg;

    @ApiModelProperty(value = "商品的详情介绍")
    @TableField("goods_detail")
    private String goodsDetail;

    @ApiModelProperty(value = "商品单价")
    @TableField("goods_price")
    private BigDecimal goodsPrice;

    @ApiModelProperty(value = "商品库存，-1表示没有限制")
    @TableField("goods_stock")
    private Integer goodsStock;

    @ApiModelProperty(value = "是否启用")
    @TableField("is_using")
    private Boolean isUsing;

    @ApiModelProperty(value = "秒杀开始时间")
    @TableField("start_time")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "秒杀结束时间")
    @TableField("end_time")
    private LocalDateTime endTime;
}
