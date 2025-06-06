package com.alex.api.finance.prepaidConsumeRecordT.vo;

import com.alex.common.common.BaseVo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * description:  消费卡交易记录表Vo
 * author:       alex
 * createDate:   2025-04-30 08:21:48
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "PrepaidConsumeRecordTVo", description = "消费卡交易记录表Vo")
public class PrepaidConsumeRecordTVo extends BaseVo<PrepaidConsumeRecordTVo>{

    @JsonSerialize(using = Long2StringSerializer.class)
    @ApiModelProperty(value = "卡号（关联prepaid_card_info_t.card_id）")
    private Long cardId;

    @ApiModelProperty(value = "卡名称")
    private String cardName;

    @ApiModelProperty(value = "消费类型")
    private String type;

    @ApiModelProperty(value = "交易流水号（业务唯一）")
    private String orderNo;

    @ApiModelProperty(value = "交易金额（正消费，负充值）")
    private BigDecimal amount;

    @ApiModelProperty(value = "交易后余额")
    private BigDecimal balanceAfter;

    @ApiModelProperty(value = "商户名称")
    private String merchantName;

    @ApiModelProperty(value = "交易时间")
    private LocalDateTime consumeTime;

    @ApiModelProperty(value = "交易描述")
    private String description;

}
