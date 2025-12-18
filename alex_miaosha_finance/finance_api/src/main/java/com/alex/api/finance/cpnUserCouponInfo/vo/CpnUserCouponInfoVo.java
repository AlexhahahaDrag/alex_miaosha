package com.alex.api.finance.cpnUserCouponInfo.vo;

import com.alex.common.common.BaseVo;
import com.baomidou.mybatisplus.annotation.TableField;
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

    @ApiModelProperty(value = "领取用户ID")
    // 统一 userId 为 Long，避免前后端/DB bigint 对齐问题
    private Long userId;

    @ApiModelProperty(value = "对应的消费券ID (外键关联 cpn_coupon_info_t.id)")
    private Long couponId;

    @ApiModelProperty(value = "状态（UNUSED, USED, EXPIRED）")
    private String status;

    @ApiModelProperty(value = "领取时间")
    private LocalDateTime receiveTime;

    @ApiModelProperty(value = "有效期截止时间")
    private LocalDateTime expireTime;

    @ApiModelProperty(value = "核销数量（仅用于核销接口入参/中转，不落库）")
    @TableField(exist = false)
    private Integer redemptionQuantity;

}
