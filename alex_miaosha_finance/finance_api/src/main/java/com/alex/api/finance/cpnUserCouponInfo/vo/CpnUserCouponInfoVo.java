package com.alex.api.finance.cpnUserCouponInfo.vo;

import com.alex.common.common.BaseVo;
import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @description:  用户消费券库存表 (按数量核销)视图
 * @author:       alex
 * @createDate:   2025-12-17 17:55:32
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "CpnUserCouponInfoVo", description = "用户消费券库存表 (按数量核销)Vo")
public class CpnUserCouponInfoVo extends BaseVo<CpnUserCouponInfoVo>{

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "领取用户ID")
    // 统一 userId 为 Long，避免前后端/DB bigint 对齐问题
    private Long userId;

    @ApiModelProperty(value = "用户名称（关联用户表）")
    private String userName;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "对应的消费券ID (外键关联 cpn_coupon_info_t.id)")
    private Long couponId;

    @ApiModelProperty(value = "消费券名称（关联消费券表）")
    private String couponName;

    @ApiModelProperty(value = "状态（UNUSED, USED, EXPIRED）")
    private String status;

    @ApiModelProperty(value = "领取时间")
    private LocalDateTime receiveTime;

    @ApiModelProperty(value = "有效期截止时间")
    private LocalDateTime expireTime;

    /**
     * 核销数量（按数量核销）
     * - 该字段会落库到 cpn_user_coupon_info_t.redemption_quantity
     * - 也作为核销接口入参字段使用
     */
    @ApiModelProperty(value = "核销数量（按数量核销）")
    private Integer redemptionQuantity;

}
