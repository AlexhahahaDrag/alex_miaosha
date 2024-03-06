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
@ApiModel(value = "ShopFinanceChainYearVo", description = "商店财务同环比Vo")
public class ShopFinanceChainYearVo {

    @ApiModelProperty(value = "业务日期")
    private String infoDate;

    @ApiModelProperty(value = "销售总数")
    private BigDecimal saleAmount;

    @ApiModelProperty(value = "销售数量")
    private BigDecimal saleNum;

    @ApiModelProperty(value = "上月销售总数")
    private BigDecimal monthSaleAmount;

    @ApiModelProperty(value = "上月销售数量")
    private BigDecimal monthSaleNum;

    @ApiModelProperty(value = "去年同期销售总数")
    private BigDecimal yearSaleAmount;

    @ApiModelProperty(value = "去年同期销售数量")
    private BigDecimal yearSaleNum;

    @ApiModelProperty(value = "销售额环比")
    private BigDecimal saleAmountChain;

    @ApiModelProperty(value = "销售数量环比")
    private BigDecimal saleNumChain;

    @ApiModelProperty(value = "销售额同比")
    private BigDecimal saleAmountYear;

    @ApiModelProperty(value = "销售数量同比")
    private BigDecimal saleNumYear;
}

