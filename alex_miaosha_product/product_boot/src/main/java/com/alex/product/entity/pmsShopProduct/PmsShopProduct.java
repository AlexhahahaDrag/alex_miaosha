package com.alex.product.entity.pmsShopProduct;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * @description:  商品网上商品信息类
 * @author:       alex
 * @createDate: 2023-05-15 14:11:10
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("pms_shop_product")
@ApiModel(value = "PmsShopProduct对象", description = "商品网上商品信息")
public class PmsShopProduct extends BaseEntity<PmsShopProduct>{

    @ApiModelProperty(value = "图片url")
    @TableField("image")
    private String image;

    @ApiModelProperty(value = "价格")
    @TableField("price")
    private BigDecimal price;

    @ApiModelProperty(value = "name")
    @TableField("`name`")
    private String name;

    @ApiModelProperty(value = "商铺")
    @TableField("shop")
    private String shop;

    @ApiModelProperty(value = "标签")
    @TableField("icons")
    private String icons;

    @ApiModelProperty(value = "产品连接")
    @TableField("product_url")
    private String productUrl;

    @ApiModelProperty(value = "来源")
    @TableField("`source`")
    private String source;

    @ApiModelProperty(value = "查询关键字")
    @TableField("search_key")
    private String searchKey;

    @ApiModelProperty(value = "商品id")
    @TableField("sku_id")
    private String skuId;
}
