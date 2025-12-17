package com.alex.finance.couponInfoT.entity;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import com.alex.common.config.Long2StringSerializer;

/**
 * @description:  消费券信息表类
 * @author:       alex
 * @createDate: 2025-12-16 15:53:10
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("coupon_info_t")
@ApiModel(value = "CouponInfo对象", description = "消费券信息表")
public class CouponInfo extends BaseEntity<CouponInfo>{

    @ApiModelProperty(value = "消费券名称")
    @TableField("coupon_name")
    private String couponName;

    @ApiModelProperty(value = "消费券总发行数量")
    @TableField("total_quantity")
    private Integer totalQuantity;

    @ApiModelProperty(value = "有效期开始时间")
    @TableField("start_date")
    private LocalDateTime startDate;

    @ApiModelProperty(value = "有效期结束时间")
    @TableField("end_date")
    private LocalDateTime endDate;

    @ApiModelProperty(value = "消费券单张面值")
    @TableField("unit_value")
    private BigDecimal unitValue;

    @ApiModelProperty(value = "最低消费门槛")
    @TableField("min_spend")
    private BigDecimal minSpend;

}
