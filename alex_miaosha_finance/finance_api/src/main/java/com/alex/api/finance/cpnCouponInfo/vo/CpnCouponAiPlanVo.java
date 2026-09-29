package com.alex.api.finance.cpnCouponInfo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 优惠券 AI 智能策划方案结果
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "CpnCouponAiPlanVo", description = "优惠券 AI 智能策划方案结果")
public class CpnCouponAiPlanVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "建议优惠券名称")
    private String couponName;

    @ApiModelProperty(value = "建议单张面值(元)")
    private BigDecimal unitValue;

    @ApiModelProperty(value = "建议最低使用门槛(元)")
    private BigDecimal minSpend;

    @ApiModelProperty(value = "建议发行总量(张)")
    private Integer totalQuantity;

    @ApiModelProperty(value = "建议有效期天数")
    private Integer validDays;

    @ApiModelProperty(value = "折扣力度说明(如: 满100减20相当于8折)")
    private String discountRate;

    @ApiModelProperty(value = "营销转化文案")
    private String marketingCopy;

    @ApiModelProperty(value = "AI策略考量与预算核算说明")
    private String strategyReasoning;
}
