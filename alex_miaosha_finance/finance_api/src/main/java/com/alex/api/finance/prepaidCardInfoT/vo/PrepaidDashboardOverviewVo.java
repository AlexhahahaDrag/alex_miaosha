package com.alex.api.finance.prepaidCardInfoT.vo;

import com.alex.common.common.BaseVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "PrepaidDashboardOverviewVo", description = "消费卡仪表盘总览数据")
public class PrepaidDashboardOverviewVo extends BaseVo<PrepaidDashboardOverviewVo> {

    @ApiModelProperty(value = "总消费卡数量")
    private Integer totalCards;

    @ApiModelProperty(value = "较上月卡数变化（新增-上月新增）")
    private Integer totalCardsMoM;

    @ApiModelProperty(value = "总余额")
    private BigDecimal totalBalance;

    @ApiModelProperty(value = "较上月总余额变化（本月净变动-上月净变动）")
    private BigDecimal totalBalanceMoM;

    @ApiModelProperty(value = "本月消费（正值）")
    private BigDecimal monthExpense;

    @ApiModelProperty(value = "本月消费较上月变化")
    private BigDecimal monthExpenseMoM;

    @ApiModelProperty(value = "本月充值（正值）")
    private BigDecimal monthRecharge;

    @ApiModelProperty(value = "本月充值较上月变化")
    private BigDecimal monthRechargeMoM;
}

