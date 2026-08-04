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

    /** 收礼笔数（供移动端方向统计等场景直接消费，避免前端全量拉记录自行聚合） */
    private Long receiveCount = 0L;

    /** 送礼笔数 */
    private Long giveCount = 0L;

    /** 回礼笔数 */
    private Long returnCount = 0L;
}
