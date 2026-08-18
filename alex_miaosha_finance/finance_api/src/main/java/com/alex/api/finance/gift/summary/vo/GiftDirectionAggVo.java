package com.alex.api.finance.gift.summary.vo;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 按礼金方向的 SQL 聚合结果行（overview / summary 下沉 SQL 用，替代内存全量扫描）。
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "GiftDirectionAggVo", description = "gift per-direction aggregation")
public class GiftDirectionAggVo {

    /** 礼金方向：RECEIVE / GIVE / RETURN */
    private String direction;

    /** 该方向记录笔数 */
    private Long recordCount = 0L;

    /** 该方向金额合计 */
    private BigDecimal totalAmount = BigDecimal.ZERO;
}
