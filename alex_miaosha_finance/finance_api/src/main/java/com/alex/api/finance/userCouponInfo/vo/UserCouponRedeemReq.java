package com.alex.api.finance.userCouponInfo.vo;

import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI Agent
 * @description: 用户消费券核销请求（按数量核销）
 * @author: alex
 * @createDate: 2025-12-17
 */
@Data
@ApiModel(value = "UserCouponRedeemReq", description = "用户消费券核销请求（按数量核销）")
public class UserCouponRedeemReq {

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "核销用户ID", required = true)
    private Long userId;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "消费券ID（外键关联 coupon_info_t.id）", required = true)
    private Long couponId;

    @ApiModelProperty(value = "核销数量（默认 1）", example = "1")
    private Integer redemptionQuantity;

    @ApiModelProperty(value = "关联的订单ID")
    private Long orderId;

    @ApiModelProperty(value = "核销商家ID")
    private Integer merchantId;

    @ApiModelProperty(value = "核销券的面值（参考）")
    private BigDecimal redemptionValue;

    @ApiModelProperty(value = "核销时间（默认当前时间）")
    private LocalDateTime redemptionTime;

    @ApiModelProperty(value = "用户券实例状态（默认 USED）")
    private String status;

    @ApiModelProperty(value = "领取时间（默认当前时间）")
    private LocalDateTime receiveTime;

    @ApiModelProperty(value = "有效期截止时间")
    private LocalDateTime expireTime;
}


