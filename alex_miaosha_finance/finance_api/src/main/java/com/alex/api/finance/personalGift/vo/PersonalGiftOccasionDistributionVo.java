package com.alex.api.finance.personalGift.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * description:  个人随礼场合分布Vo
 * author:       alex
 * createDate:   2025-11-05
 * version:      1.0.0
 * 用于前端展示随礼场合分布饼图数据
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "PersonalGiftOccasionDistributionVo", description = "个人随礼场合分布Vo")
public class PersonalGiftOccasionDistributionVo implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "场合列表")
	private List<String> occasions;

	@ApiModelProperty(value = "各场合对应的随礼金额列表")
	private List<BigDecimal> amounts;

	@ApiModelProperty(value = "各场合对应的随礼次数列表")
	private List<Integer> counts;

	@ApiModelProperty(value = "各场合对应的百分比列表（%）")
	private List<BigDecimal> percentages;

	@ApiModelProperty(value = "随礼总额")
	private BigDecimal totalAmount;

	@ApiModelProperty(value = "随礼总次数")
	private Integer totalCount;

}
