package com.alex.api.finance.gift.summary.vo;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "GiftDashboardSummaryVo", description = "gift dashboard summary")
public class GiftDashboardSummaryVo {

    private BigDecimal totalGiveAmount = BigDecimal.ZERO;

    private BigDecimal totalReceiveAmount = BigDecimal.ZERO;

    private Long personCount = 0L;

    private BigDecimal pendingReturnAmount = BigDecimal.ZERO;
}
