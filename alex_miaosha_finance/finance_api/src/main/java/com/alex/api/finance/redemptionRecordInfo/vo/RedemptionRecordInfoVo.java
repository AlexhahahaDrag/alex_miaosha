package com.alex.api.finance.redemptionRecordInfo.vo;

import com.alex.common.common.BaseVo;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @description:  消费券核销记录表 (按数量核销)视图
 * @author:       alex
 * @createDate:   2025-12-17 14:08:55
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "RedemptionRecordInfoVo", description = "消费券核销记录表 (按数量核销)Vo")
public class RedemptionRecordInfoVo extends BaseVo<RedemptionRecordInfoVo>{

    @ApiModelProperty(value = "消费券ID（外键关联 coupon_info_t.id）")
    private Long couponId;

    @ApiModelProperty(value = "消费券名称")
    private String couponName;

    @ApiModelProperty(value = "被核销的券实例ID (外键关联 user_coupon_info_t.id)")
    private Long userCouponId;

    @ApiModelProperty(value = "核销用户ID")
    private Long userId;

    @ApiModelProperty(value = "关联的订单ID")
    private Long orderId;

    @ApiModelProperty(value = "本次核销数量 (固定为1)")
    private Integer redemptionQuantity;

    @ApiModelProperty(value = "核销券的面值（参考）")
    private BigDecimal redemptionValue;

    @ApiModelProperty(value = "核销时间")
    private LocalDateTime redemptionTime;

    @ApiModelProperty(value = "核销商家ID")
    private Integer merchantId;

}
