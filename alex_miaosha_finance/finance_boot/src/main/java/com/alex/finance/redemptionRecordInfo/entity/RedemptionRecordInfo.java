package com.alex.finance.redemptionRecordInfo.entity;

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
 * @description:  消费券核销记录表 (按数量核销)类
 * @author:       alex
 * @createDate: 2025-12-17 14:08:55
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("redemption_record_info_t")
@ApiModel(value = "RedemptionRecordInfo对象", description = "消费券核销记录表 (按数量核销)")
public class RedemptionRecordInfo extends BaseEntity<RedemptionRecordInfo>{

    @ApiModelProperty(value = "被核销的券实例ID (外键关联 user_coupon_info_t.id)")
    @TableField("user_coupon_id")
    private Long userCouponId;

    @ApiModelProperty(value = "核销用户ID")
    @TableField("user_id")
    private Integer userId;

    @ApiModelProperty(value = "关联的订单ID")
    @TableField("order_id")
    private Long orderId;

    @ApiModelProperty(value = "本次核销数量 (固定为1)")
    @TableField("redemption_quantity")
    private Integer redemptionQuantity;

    @ApiModelProperty(value = "核销券的面值（参考）")
    @TableField("redemption_value")
    private BigDecimal redemptionValue;

    @ApiModelProperty(value = "核销时间")
    @TableField("redemption_time")
    private LocalDateTime redemptionTime;

    @ApiModelProperty(value = "核销商家ID")
    @TableField("merchant_id")
    private Integer merchantId;

}
