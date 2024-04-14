package com.alex.api.finance.vo.shopFinance;

import com.alex.common.common.BaseVo;
import com.alex.common.config.Long2StringSerializer;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * description:  商店财务表Vo
 * author:       majf
 * createDate:   2024-02-23 21:19:49
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "ShopFinanceVo", description = "商店财务表Vo")
public class ShopFinanceVo extends BaseVo<ShopFinanceVo>{

    @ApiModelProperty(value = "商品名称")
    private String shopName;

    @ApiModelProperty(value = "商品编码")
    private String shopCode;

    @ApiModelProperty(value = "收支类型")
    private String incomeAndExpenses;

    @ApiModelProperty(value = "支付方式")
    private String payWay;

    @ApiModelProperty(value = "售价")
    private BigDecimal saleAmount;

    @ApiModelProperty(value = "个数")
    private BigDecimal saleNum;

    @ApiModelProperty(value = "售价从")
    private BigDecimal saleAmountFrom;

    @ApiModelProperty(value = "售价到")
    private BigDecimal saleAmountEnd;

    @ApiModelProperty(value = "是否有效")
    private String isValid;

    @ApiModelProperty(value = "销售日期")
    private LocalDateTime saleDate;

    @ApiModelProperty(value = "销售日期从")
    private LocalDate saleDateFrom;

    @ApiModelProperty(value = "销售日期到")
    private LocalDate saleDateEnd;

    @ApiModelProperty(value = "销售订单id")
    @JsonSerialize(using = Long2StringSerializer.class)
    private Long shopOrderId;

    @ApiModelProperty(value = "商品库存id")
    @JsonSerialize(using = Long2StringSerializer.class)
    private Long shopStockId;
}
