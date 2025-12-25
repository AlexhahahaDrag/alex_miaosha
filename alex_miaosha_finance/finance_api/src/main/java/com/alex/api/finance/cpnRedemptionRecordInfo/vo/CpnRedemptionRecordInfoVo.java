package com.alex.api.finance.cpnRedemptionRecordInfo.vo;

import com.alex.common.common.BaseVo;
import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @description:  消费券核销记录表 (按数量核销)视图
 * @author:       alex
 * @createDate:   2025-12-17 17:54:00
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "CpnRedemptionRecordInfoVo", description = "消费券核销记录表 (按数量核销)Vo")
public class CpnRedemptionRecordInfoVo extends BaseVo<CpnRedemptionRecordInfoVo>{

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "被核销的券实例ID (外键关联 cpn_user_coupon_info_t.id)")
    private Long userCouponId;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "核销用户ID")
    // 统一 userId 为 Long，避免前后端/DB bigint 对齐问题
    private Long userId;

    /**
     * AI Agent：关联用户表返回（t_user.username / t_user.nick_name）
     * 用于列表展示与按用户名模糊检索（如需）。
     */
    @ApiModelProperty(value = "核销用户名称（关联用户表）")
    private String userName;

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "关联的订单ID")
    private Long orderId;

    /**
     * AI Agent：关联消费券表返回（cpn_coupon_info_t.coupon_name）
     * 说明：当前业务里 orderId 实际承载的是 couponId（见核销写入逻辑），这里新增 couponName 仅用于展示。
     */
    @ApiModelProperty(value = "消费券名称（关联消费券表）")
    private String couponName;

    @ApiModelProperty(value = "本次核销数量")
    private Integer redemptionQuantity;

    @ApiModelProperty(value = "核销券的面值（参考）")
    private BigDecimal redemptionValue;

    @ApiModelProperty(value = "核销时间")
    private LocalDateTime redemptionTime;

    @ApiModelProperty(value = "核销商家ID")
    private Integer merchantId;

    @ApiModelProperty(value = "备注")
    private String remarks;

}
