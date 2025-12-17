package com.alex.finance.userCouponInfo.entity;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @description:  用户消费券库存表 (按数量核销)类
 * @author:       alex
 * @createDate: 2025-12-17 14:08:13
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("user_coupon_info_t")
@ApiModel(value = "UserCouponInfo对象", description = "用户消费券库存表 (按数量核销)")
public class UserCouponInfo extends BaseEntity<UserCouponInfo>{

    @ApiModelProperty(value = "领取用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "对应的消费券ID (外键关联 coupon_info_t.id)")
    @TableField("coupon_id")
    private Long couponId;

    @ApiModelProperty(value = "状态（UNUSED, USED, EXPIRED）")
    @TableField("`status`")
    private String status;

    @ApiModelProperty(value = "领取时间")
    @TableField("receive_time")
    private LocalDateTime receiveTime;

    @ApiModelProperty(value = "有效期截止时间")
    @TableField("expire_time")
    private LocalDateTime expireTime;

}
