package com.alex.api.finance.shopCart.vo;

import com.alex.common.common.BaseVo;
import com.baomidou.mybatisplus.annotation.TableField;
import java.math.BigDecimal;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * description:  购物车表Vo
 * author:       alex
 * createDate:   2024-04-03 11:36:19
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "ShopCartVo", description = "购物车表Vo")
public class ShopCartVo extends BaseVo<ShopCartVo>{

    @ApiModelProperty(value = "商品id")
    private Long shopId;

    @ApiModelProperty(value = "人员id")
    private Long userId;

    @ApiModelProperty(value = "客户id")
    private Long customerId;

    @ApiModelProperty(value = "是否有效")
    private String isValid;

    @ApiModelProperty(value = "数量")
    private BigDecimal saleNum;

}
