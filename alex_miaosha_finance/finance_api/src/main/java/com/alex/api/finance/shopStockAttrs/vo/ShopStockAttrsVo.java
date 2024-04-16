package com.alex.api.finance.shopStockAttrs.vo;

import com.alex.common.common.BaseVo;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * description:  商店库存属性表Vo
 * author:       alex
 * createDate:   2024-04-16 19:50:29
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "ShopStockAttrsVo", description = "商店库存属性表Vo")
public class ShopStockAttrsVo extends BaseVo<ShopStockAttrsVo>{

    @ApiModelProperty(value = "库存id")
    private Long stockId;

    @ApiModelProperty(value = "商品属性编码")
    private String attrCode;

    @ApiModelProperty(value = "商品属性名称")
    private String attrName;

    @ApiModelProperty(value = "商品属性值")
    private String attrValue;

    @ApiModelProperty(value = "状态,字典(is_valid) 1：有效,0:失效)")
    private String isValid;

    @ApiModelProperty(value = "描述")
    private String description;

}
