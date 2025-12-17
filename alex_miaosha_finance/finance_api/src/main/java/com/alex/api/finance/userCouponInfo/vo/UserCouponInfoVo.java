package com.alex.api.finance.userCouponInfo.vo;

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
 * @createDate:   2025-12-17 14:08:13
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "UserCouponInfoVo", description = "用户消费券库存表 (按数量核销)Vo")
public class UserCouponInfoVo extends BaseVo<UserCouponInfoVo>{

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "领取用户ID")
    private Long userId;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "对应的消费券ID (外键关联 coupon_info_t.id)")
    private Long couponId;

    @ApiModelProperty(value = "状态（UNUSED, USED, EXPIRED）")
    private String status;

    @ApiModelProperty(value = "领取时间")
    private LocalDateTime receiveTime;

    @ApiModelProperty(value = "有效期截止时间")
    private LocalDateTime expireTime;

}
