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

    // 标签
    private String label;

    // 支出总金额
    private BigDecimal giveAmount = BigDecimal.ZERO;

    // 收入总金额
    private BigDecimal receiveAmount = BigDecimal.ZERO;
}
