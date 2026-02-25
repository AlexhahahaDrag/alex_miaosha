package com.alex.api.finance.cpnCouponInfo.vo;

import com.alex.common.common.BaseVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @description:  消费券信息表视图
 * @author:       alex
 * @createDate:   2025-12-17 17:54:42
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "CpnCouponInfoVo", description = "消费券信息表Vo")
public class CpnCouponInfoVo extends BaseVo<CpnCouponInfoVo>{

    @ApiModelProperty(value = "已核销数量（cpn_user_coupon_info_t.status = USED 汇总）")
    private Integer consumedQuantity;

    @ApiModelProperty(value = "未核销数量（remainingQuantity = totalQuantity - consumedQuantity）")
    private Integer remainingQuantity;

    @ApiModelProperty(value = "消费券名称")
    private String couponName;

    @ApiModelProperty(value = "消费券总发行数量")
    private Integer totalQuantity;

    @ApiModelProperty(value = "有效期开始时间")
    private LocalDateTime startDate;

    @ApiModelProperty(value = "有效期结束时间")
    private LocalDateTime endDate;

    @ApiModelProperty(value = "消费券单张面值")
    private BigDecimal unitValue;

    @ApiModelProperty(value = "最低消费门槛")
    private BigDecimal minSpend;

    @ApiModelProperty(value = "过期状态（展示用：离过期还有三天/离过期还有一天/离过期还有X小时/离过期还有X分钟/过期）")
    private String expireStatus;

    @ApiModelProperty(value = "过期区间状态（数字：0已过期，1<1天，2=1-3天，3>3天）")
    private Integer expireRangeStatus;

    /**
     * AI Agent：支付状态
     * - 1：已支付
     * - 0：未支付
     */
    @ApiModelProperty(value = "支付状态（1：已支付，0：未支付）")
    private Integer paymentStatus;

    /**
     * AI Agent：仅查询有效的、未核销完成的数据
     * - true：只查询 remainingQuantity > 0（还有剩余数量）且未过期的数据
     * - false/null：不限制
     */
    @ApiModelProperty(value = "仅查询有效的、未核销完成的数据（true：还有剩余数量且未过期）")
    private Boolean onlyValidAndNotFullyRedeemed;

}
