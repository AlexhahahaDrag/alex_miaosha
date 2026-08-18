package com.alex.api.finance.gift.summary.vo;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "GiftRankingItemVo", description = "gift ranking item")
public class GiftRankingItemVo {

    private String name;

    private BigDecimal amount = BigDecimal.ZERO;

    private Long count = 0L;
}
