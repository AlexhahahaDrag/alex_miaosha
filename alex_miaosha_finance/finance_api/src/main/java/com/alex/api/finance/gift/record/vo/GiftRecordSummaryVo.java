package com.alex.api.finance.gift.record.vo;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "GiftRecordSummaryVo", description = "gift record filtered summary")
public class GiftRecordSummaryVo {

    private BigDecimal receiveAmount = BigDecimal.ZERO;

    private BigDecimal giveAmount = BigDecimal.ZERO;

    private BigDecimal returnAmount = BigDecimal.ZERO;

    private BigDecimal netAmount = BigDecimal.ZERO;

    private Long recordCount = 0L;
}
