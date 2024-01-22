package com.alex.api.product.vo.pmsShopProduct;

import com.alex.common.common.BaseVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * description:  商品网上商品信息Vo
 * author:       alex
 * createDate:   2023-05-15 14:11:10
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "PmsShopProductVo", description = "商品网上商品信息Vo")
public class PmsShopProductVo extends BaseVo<PmsShopProductVo>{

    @ApiModelProperty(value = "图片url")
    private String image;

    @ApiModelProperty(value = "价格")
    private BigDecimal price;

    @ApiModelProperty(value = "name")
    private String name;

    @ApiModelProperty(value = "商铺")
    private String shop;

    @ApiModelProperty(value = "标签")
    private String icons;

    @ApiModelProperty(value = "产品连接")
    private String productUrl;

    @ApiModelProperty(value = "来源")
    private String source;

    @ApiModelProperty(value = "查询关键字")
    private String searchKey;

    @ApiModelProperty(value = "比较价格")
    private BigDecimal comparePrice;

    @ApiModelProperty(value = "是否是比较")
    private Boolean isCompare;

    @ApiModelProperty(value = "商品id")
    private String skuId;
}
