package com.alex.api.finance.gift.event.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

@Data
@Accessors(chain = true)
@ApiModel(value = "GiftRecordRecommendAmountVo", description = "金额智能推荐")
public class GiftRecordRecommendAmountVo {

    @ApiModelProperty(value = "历史平均金额")
    private BigDecimal averageAmount;

    @ApiModelProperty(value = "最近一次金额")
    private BigDecimal latestAmount;

    @ApiModelProperty(value = "事由默认推荐金额")
    private BigDecimal defaultAmount;

    @ApiModelProperty(value = "梯度推荐金额列表")
    private List<BigDecimal> recommendations;
}
