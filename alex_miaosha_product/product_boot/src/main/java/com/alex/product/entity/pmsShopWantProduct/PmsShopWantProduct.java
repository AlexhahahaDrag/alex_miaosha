package com.alex.product.entity.pmsShopWantProduct;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @description:  商品想买网上商品信息类
 * @author:       alex
 * @createDate: 2023-05-25 16:18:10
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("pms_shop_want_product")
@ApiModel(value = "PmsShopWantProduct对象", description = "商品想买网上商品信息")
public class PmsShopWantProduct extends BaseEntity<PmsShopWantProduct>{

    @ApiModelProperty(value = "商品名称")
    @TableField("`name`")
    private String name;

    @ApiModelProperty(value = "商铺")
    @TableField("shop")
    private String shop;

    @ApiModelProperty(value = "标签")
    @TableField("icons")
    private String icons;

    @ApiModelProperty(value = "来源")
    @TableField("`source`")
    private String source;

    @ApiModelProperty(value = "状态")
    @TableField("status")
    private Integer status;
}
