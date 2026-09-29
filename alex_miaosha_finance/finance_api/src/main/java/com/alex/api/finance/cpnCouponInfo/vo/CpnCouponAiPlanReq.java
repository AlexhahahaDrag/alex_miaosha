package com.alex.api.finance.cpnCouponInfo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 优惠券 AI 智能策划请求
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "CpnCouponAiPlanReq", description = "优惠券 AI 智能策划请求")
public class CpnCouponAiPlanReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "营销总预算(元)")
    private BigDecimal budget;

    @ApiModelProperty(value = "营销核心目标(如: 拉新获客/提升客单价/沉睡促活/清库存)")
    private String targetGoal;

    @ApiModelProperty(value = "适用行业/品类(如: 美食餐饮/数码美妆/日常百货)")
    private String industryCategory;

    @ApiModelProperty(value = "期望触达/发放用户数")
    private Integer expectedUsers;
}
