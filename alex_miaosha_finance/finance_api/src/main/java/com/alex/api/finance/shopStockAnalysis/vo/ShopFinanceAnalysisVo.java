package com.alex.api.finance.shopStockAnalysis.vo;

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
@ApiModel(value = "shopFinanceAnalysisVo", description = "商店财务分析Vo")
public class ShopFinanceAnalysisVo {

    @ApiModelProperty(value = "机构id")
    private Long orgId;

    @ApiModelProperty(value = "支付方式")
    private String payWay;

    @ApiModelProperty(value = "支付方式")
    private String payWayName;

    @ApiModelProperty(value = "商品名称")
    private String shopName;

    @ApiModelProperty(value = "业务日期")
    private String infoDate;

    @ApiModelProperty(value = "销售总数")
    private BigDecimal saleAmount;

    @ApiModelProperty(value = "销售数量")
    private BigDecimal saleNum;

    @ApiModelProperty(value = "销售成本")
    private BigDecimal saleCost;
}

