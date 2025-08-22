package com.alex.api.finance.prepaidConsumeRecordT.vo;

import com.alex.common.common.BaseVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "ConsumptionTrendPointVo", description = "消费/充值趋势点")
public class ConsumptionTrendPointVo extends BaseVo<ConsumptionTrendPointVo> {

    @ApiModelProperty(value = "横坐标日期（天/周起始/月份）")
    private LocalDate bucketDate;

    @ApiModelProperty(value = "消费额（正值）")
    private BigDecimal expenseAmount;

    @ApiModelProperty(value = "充值额（正值）")
    private BigDecimal rechargeAmount;
}

