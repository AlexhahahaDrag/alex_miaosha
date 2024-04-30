package com.alex.api.finance.vo.shopFinanceAnalysis;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * description:  商店财务表Vo
 * author:       majf
 * createDate:   2024-02-23 21:19:49
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "ShopStockAnalysisVo", description = "商店库存分析Vo")
public class ShopStockAnalysisVo {

    @ApiModelProperty(value = "机构id")
    private Long orgId;

    @ApiModelProperty(value = "业务日期")
    private String infoDate;

    @ApiModelProperty(value = "成本总数")
    private BigDecimal costAmount;

    @ApiModelProperty(value = "销售总数")
    private BigDecimal saleAmount;

    @ApiModelProperty(value = "总数量")
    private BigDecimal saleNum;
}

