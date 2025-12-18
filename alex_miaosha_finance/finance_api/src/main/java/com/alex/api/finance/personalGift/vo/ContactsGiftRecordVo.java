package com.alex.api.finance.personalGift.vo;

import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.io.Serial;

/**
 * description:  联系人随礼记录Vo
 * author:       alex
 * createDate:   2025-11-05
 * version:      1.0.0
 * 用于前端展示联系人记录页面，显示每个联系人的随礼、收礼、净差额等统计信息
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "ContactsGiftRecordVo", description = "联系人随礼记录信息Vo")
public class ContactsGiftRecordVo implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonSerialize(using = Long2StringSerializer.class)
	@ApiModelProperty(value = "联系人ID")
	private Long contactsUserId;

	@ApiModelProperty(value = "联系人姓名")
	private String contactsUserName;

	@ApiModelProperty(value = "人物关系")
	private String relationship;

	@ApiModelProperty(value = "上次往来时间")
	private LocalDateTime lastContactTime;

	// ===== 统计数据字段 =====

	@ApiModelProperty(value = "随礼总额（给出的金额，红色显示）")
	private BigDecimal giftOutAmount;

	@ApiModelProperty(value = "收礼总额（收到的金额，绿色显示）")
	private BigDecimal giftInAmount;

	@ApiModelProperty(value = "净差额（收礼总额 - 随礼总额）")
	private BigDecimal netAmount;

	@ApiModelProperty(value = "随礼次数")
	private Integer giftOutCount;

	@ApiModelProperty(value = "收礼次数")
	private Integer giftInCount;

}
