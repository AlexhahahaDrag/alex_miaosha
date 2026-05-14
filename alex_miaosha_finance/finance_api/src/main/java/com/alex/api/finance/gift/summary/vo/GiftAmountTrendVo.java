package com.alex.api.finance.gift.summary.vo;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "GiftAmountTrendVo", description = "gift amount trend")
public class GiftAmountTrendVo {

    private String label;

    private BigDecimal giveAmount = BigDecimal.ZERO;

    private BigDecimal receiveAmount = BigDecimal.ZERO;
}
