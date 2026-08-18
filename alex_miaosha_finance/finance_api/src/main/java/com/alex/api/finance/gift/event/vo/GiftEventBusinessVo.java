package com.alex.api.finance.gift.event.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "GiftEventBusinessVo", description = "gift event aggregate row")
public class GiftEventBusinessVo extends GiftEventInfoVo {

    @ApiModelProperty(value = "participant count")
    private Long participantCount = 0L;

    @ApiModelProperty(value = "total amount")
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @ApiModelProperty(value = "receive amount")
    private BigDecimal receiveAmount = BigDecimal.ZERO;

    @ApiModelProperty(value = "give amount")
    private BigDecimal giveAmount = BigDecimal.ZERO;

    @ApiModelProperty(value = "event status")
    private String eventStatus;

    @ApiModelProperty(value = "location text")
    private String locationText;
}
