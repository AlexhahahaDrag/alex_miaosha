package com.alex.api.finance.gift.person.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "GiftPersonBusinessVo", description = "gift person aggregate row")
public class GiftPersonBusinessVo extends GiftPersonInfoTVo {

    @ApiModelProperty(value = "total give amount")
    private BigDecimal totalGiveAmount = BigDecimal.ZERO;

    @ApiModelProperty(value = "total receive amount")
    private BigDecimal totalReceiveAmount = BigDecimal.ZERO;

    @ApiModelProperty(value = "net amount")
    private BigDecimal netAmount = BigDecimal.ZERO;

    @ApiModelProperty(value = "latest record time")
    private LocalDateTime latestRecordTime;

    @ApiModelProperty(value = "latest event name")
    private String latestEventName;

    @ApiModelProperty(value = "latest direction")
    private String latestDirection;

    @ApiModelProperty(value = "pending return amount")
    private BigDecimal pendingReturnAmount = BigDecimal.ZERO;
}
