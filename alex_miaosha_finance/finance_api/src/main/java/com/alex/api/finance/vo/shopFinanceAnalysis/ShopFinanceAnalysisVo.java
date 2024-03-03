package com.alex.api.finance.vo.shopFinanceAnalysis;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * description:  商店财务表Vo
 * author:       majf
 * createDate:   2024-02-23 21:19:49
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "shopFinanceAnalysisVo", description = "商店财务分析Vo")
public class ShopFinanceAnalysisVo {

    @ApiModelProperty(value = "业务日期")
    private String infoDate;

    @ApiModelProperty(value = "销售总数")
    private String saleAmount;

    @ApiModelProperty(value = "销售数量")
    private String saleNum;
}
