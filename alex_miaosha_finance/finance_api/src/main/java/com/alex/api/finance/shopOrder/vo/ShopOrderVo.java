package com.alex.api.finance.shopOrder.vo;

import com.alex.api.finance.shopOrderDetail.vo.ShopOrderDetailVo;
import com.alex.common.common.BaseVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * description:  商店订单表Vo
 * author:       alex
 * createDate:   2024-04-09 15:20:01
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "ShopOrderVo", description = "商店订单表Vo")
public class ShopOrderVo extends BaseVo<ShopOrderVo>{

    @ApiModelProperty(value = "订单编码")
    private String saleOrderCode;

    @ApiModelProperty(value = "订单名称")
    private String saleOrderName;

    @ApiModelProperty(value = "总销售金额")
    private BigDecimal saleAmount;

    @ApiModelProperty(value = "状态,字典(is_valid) 1：有效,0:失效)")
    private String isValid;

    @ApiModelProperty(value = "销售日期")
    private LocalDateTime saleDate;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "支付方式")
    private String payWay;

    @ApiModelProperty(value = "销售数量")
    private BigDecimal saleCount;

    @ApiModelProperty(value = "商品列表")
    private List<ShopOrderDetailVo> shopOrderDetailVoList;
}
