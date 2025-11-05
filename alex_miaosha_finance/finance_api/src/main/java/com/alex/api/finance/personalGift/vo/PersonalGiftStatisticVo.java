package com.alex.api.finance.personalGift.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * description:  个人随礼统计概览Vo
 * author:       AI Agent
 * createDate:   2025-11-05
 * version:      1.0.0
 * 用于前端展示个人随礼统计概览页面的数据
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "PersonalGiftStatisticVo", description = "个人随礼统计概览Vo")
public class PersonalGiftStatisticVo implements Serializable {

	private static final long serialVersionUID = 1L;

	// ===== 本月数据 =====

	@ApiModelProperty(value = "本月随礼总额")
	private BigDecimal monthGiftOutAmount;

	@ApiModelProperty(value = "本月随礼环比增长率（%），例如：12.5 表示增长12.5%")
	private BigDecimal monthGiftOutMoM;

	@ApiModelProperty(value = "本月收礼总额")
	private BigDecimal monthGiftInAmount;

	@ApiModelProperty(value = "本月收礼环比增长率（%），例如：8.0 表示增长8%")
	private BigDecimal monthGiftInMoM;

	@ApiModelProperty(value = "本月净差额（收礼总额 - 随礼总额）")
	private BigDecimal monthNetAmount;

	@ApiModelProperty(value = "本月随礼次数")
	private Integer monthGiftOutCount;

	@ApiModelProperty(value = "本月收礼次数")
	private Integer monthGiftInCount;

	@ApiModelProperty(value = "上月随礼总额")
	private BigDecimal lastMonthGiftOutAmount;

	@ApiModelProperty(value = "上月收礼总额")
	private BigDecimal lastMonthGiftInAmount;

	// ===== 年度数据 =====

	@ApiModelProperty(value = "年度随礼总额")
	private BigDecimal yearGiftOutAmount;

	@ApiModelProperty(value = "年度随礼同比增长率（%），例如：15.0 表示增长15%")
	private BigDecimal yearGiftOutYoY;

	@ApiModelProperty(value = "年度收礼总额")
	private BigDecimal yearGiftInAmount;

	@ApiModelProperty(value = "年度收礼同比增长率（%），例如：10.0 表示增长10%")
	private BigDecimal yearGiftInYoY;

	@ApiModelProperty(value = "年度净差额（收礼总额 - 随礼总额）")
	private BigDecimal yearNetAmount;

	@ApiModelProperty(value = "年度随礼次数")
	private Integer yearGiftOutCount;

	@ApiModelProperty(value = "年度收礼次数")
	private Integer yearGiftInCount;

	@ApiModelProperty(value = "去年随礼总额")
	private BigDecimal lastYearGiftOutAmount;

	@ApiModelProperty(value = "去年收礼总额")
	private BigDecimal lastYearGiftInAmount;

	// ===== 联系人统计 =====

	@ApiModelProperty(value = "联系人总数")
	private Integer totalContacts;

	@ApiModelProperty(value = "本月新增联系人数")
	private Integer monthNewContacts;

	@ApiModelProperty(value = "年度新增联系人数")
	private Integer yearNewContacts;

}
