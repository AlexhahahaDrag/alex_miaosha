package com.alex.api.finance.gift.person.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "GiftPersonSummaryVo", description = "gift person page summary")
public class GiftPersonSummaryVo {

    @ApiModelProperty(value = "person count")
    private Long personCount = 0L;

    @ApiModelProperty(value = "net amount")
    private BigDecimal netAmount = BigDecimal.ZERO;

    @ApiModelProperty(value = "active count")
    private Long activeCount = 0L;

    @ApiModelProperty(value = "pending maintenance count")
    private Long pendingMaintenanceCount = 0L;

    @ApiModelProperty(value = "year total amount")
    private BigDecimal yearTotalAmount = BigDecimal.ZERO;

    @ApiModelProperty(value = "pending return amount")
    private BigDecimal pendingReturnAmount = BigDecimal.ZERO;
}
