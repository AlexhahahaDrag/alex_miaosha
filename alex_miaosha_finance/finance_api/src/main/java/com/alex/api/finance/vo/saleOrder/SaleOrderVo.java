package com.alex.api.finance.vo.saleOrder;

import com.alex.api.finance.vo.shopStock.ShopStockVo;
import com.alex.common.common.BaseVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

/**
 * description:  商店库存表Vo
 * author:       alex
 * createDate:   2024-03-12 16:49:20
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "ShopStockVo", description = "销售订单Vo")
public class SaleOrderVo extends BaseVo<SaleOrderVo>{

    @ApiModelProperty(value = "销售总金额")
    private BigDecimal sumSaleAmount;

    @ApiModelProperty(value = "收支类型")
    private String incomeAndExpenses;

    @ApiModelProperty(value = "商品列表")
    private List<ShopStockVo> shopStockVoList;
}
