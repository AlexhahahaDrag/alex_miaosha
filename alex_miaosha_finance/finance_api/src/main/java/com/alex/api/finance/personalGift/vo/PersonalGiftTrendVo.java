package com.alex.api.finance.personalGift.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * description:  个人随礼趋势Vo
 * author:       alex
 * createDate:   2025-11-05
 * version:      1.0.0
 * 用于前端展示近12个月的随礼收礼趋势数据
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "PersonalGiftTrendVo", description = "个人随礼趋势数据Vo")
public class PersonalGiftTrendVo implements Serializable {

	@Serial
    private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "月份（格式：YYYY-MM）")
	private String month;

	@ApiModelProperty(value = "随礼金额")
	private BigDecimal giftOutAmount;

	@ApiModelProperty(value = "收礼金额")
	private BigDecimal giftInAmount;

	@ApiModelProperty(value = "随礼次数")
	private Integer giftOutCount;

	@ApiModelProperty(value = "收礼次数")
	private Integer giftInCount;

}
