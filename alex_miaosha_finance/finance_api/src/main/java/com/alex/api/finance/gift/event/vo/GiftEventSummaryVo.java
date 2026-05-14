package com.alex.api.finance.gift.event.vo;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "GiftEventSummaryVo", description = "gift event page summary")
public class GiftEventSummaryVo {

    private Long monthPendingCount = 0L;

    private BigDecimal totalAmount = BigDecimal.ZERO;

    private Long activePersonCount = 0L;
}
