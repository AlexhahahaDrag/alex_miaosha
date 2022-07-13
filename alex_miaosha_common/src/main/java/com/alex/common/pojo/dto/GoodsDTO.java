package com.alex.common.pojo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@ApiModel(value = "GoodsDTO", description = "商品信息")
public class GoodsDTO {

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "商品名称")
    @NotBlank(message = "商品名称是必须的")
    private String goodsName;

    @ApiModelProperty(value = "商品标题")
    @NotBlank(message = "商品标题是必须的")
    private String goodsTitle;

    @ApiModelProperty(value = "商品的图片")
    private String goodsImg;

    @ApiModelProperty(value = "商品的详情介绍")
    private String goodsDetail;

    @ApiModelProperty(value = "商品单价")
    @NotNull(message = "商品价格是必须的")
    private BigDecimal goodsPrice;

    @ApiModelProperty(value = "商品库存，-1表示没有限制")
    @NotNull(message = "商品库存是必须的")
    private Integer goodsStock;

    @ApiModelProperty(value = "是否启用")
    @NotNull(message = "是否启用是必须的")
    private Boolean isUsing;

    @ApiModelProperty("秒杀开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private LocalDateTime startTime;

    @ApiModelProperty("秒杀结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private LocalDateTime endTime;
}

