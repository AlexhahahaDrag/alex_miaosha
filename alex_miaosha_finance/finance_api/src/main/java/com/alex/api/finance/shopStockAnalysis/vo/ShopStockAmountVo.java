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
@ApiModel(value = "ShopStockAmountVo", description = "商店库存金额Vo")
public class ShopStockAmountVo {

    @ApiModelProperty(value = "机构id")
    private Long orgId;

    @ApiModelProperty(value = "编码")
    private String typeCode;

    @ApiModelProperty(value = "名称")
    private String typeName;

    @ApiModelProperty(value = "金额")
    private BigDecimal amount;
}

