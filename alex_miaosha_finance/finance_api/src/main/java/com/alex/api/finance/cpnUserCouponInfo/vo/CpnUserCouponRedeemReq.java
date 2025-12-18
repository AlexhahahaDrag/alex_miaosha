package com.alex.api.finance.cpnUserCouponInfo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @description:  用户消费券核销请求（按数量核销）
 * @author:       alex
 * @createDate:   2025-12-18
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "CpnUserCouponRedeemReq", description = "用户消费券核销请求（按数量核销）")
public class CpnUserCouponRedeemReq {

    @ApiModelProperty(value = "核销用户ID", required = true)
    @NotNull(message = "userId 不能为空") // 入参校验
    private Long userId;

    @ApiModelProperty(value = "消费券ID（外键关联 cpn_coupon_info_t.id）", required = true)
    @NotNull(message = "couponId 不能为空") // 入参校验
    private Long couponId;

    @ApiModelProperty(value = "消费券ID（外键关联 cpn_user_coupon_info_t.id）", required = true)
    private Long userCouponId;

    @ApiModelProperty(value = "本次核销数量", required = true, example = "1")
    @NotNull(message = "redemptionQuantity 不能为空") // 入参校验
    private Integer redemptionQuantity;

    @ApiModelProperty(value = "关联的订单ID")
    private Long orderId;

    @ApiModelProperty(value = "核销商家ID")
    private Integer merchantId;

    @ApiModelProperty(value = "核销券的面值（参考，可不传）")
    private BigDecimal redemptionValue;

    @ApiModelProperty(value = "核销时间（不传默认取当前时间）")
    private LocalDateTime redemptionTime;

    @ApiModelProperty(value = "备注")
    private String remarks;
}


