package com.alex.finance.cpnRedemptionRecordInfo.entity;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @description:  消费券核销记录表 (按数量核销)类
 * @author:       alex
 * @createDate: 2025-12-17 17:54:00
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("cpn_redemption_record_info_t")
@ApiModel(value = "CpnRedemptionRecordInfo对象", description = "消费券核销记录表 (按数量核销)")
public class CpnRedemptionRecordInfo extends BaseEntity<CpnRedemptionRecordInfo>{

    @ApiModelProperty(value = "被核销的券实例ID (外键关联 cpn_user_coupon_info_t.id)")
    @TableField("user_coupon_id")
    private Long userCouponId;

    @ApiModelProperty(value = "核销用户ID")
    @TableField("user_id")
    // 统一 userId 为 Long，避免前后端/DB bigint 对齐问题
    private Long userId;

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

    @ApiModelProperty(value = "备注")
    @TableField("remarks")
    private String remarks;

}
