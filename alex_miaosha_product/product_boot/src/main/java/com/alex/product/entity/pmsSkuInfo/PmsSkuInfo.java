package com.alex.product.entity.pmsSkuInfo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * description:  sku信息类
 * author:       alex
 * createDate: 2023-03-17 10:30:26
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("pms_sku_info")
@ApiModel(value = "PmsSkuInfo对象", description = "sku信息")
public class PmsSkuInfo {

    @ApiModelProperty(value = "skuId")
    @TableId(value = "sku_id", type = IdType.ASSIGN_ID)
    private Long skuId;

    @ApiModelProperty(value = "spuId")
    @TableField("spu_id")
    private Long spuId;

    @ApiModelProperty(value = "sku名称")
    @TableField("sku_name")
    private String skuName;

    @ApiModelProperty(value = "sku介绍描述")
    @TableField("sku_desc")
    private String skuDesc;

    @ApiModelProperty(value = "所属分类id")
    @TableField("catalog_id")
    private Long catalogId;

    @ApiModelProperty(value = "品牌id")
    @TableField("brand_id")
    private Long brandId;

    @ApiModelProperty(value = "默认图片")
    @TableField("sku_default_img")
    private String skuDefaultImg;

    @ApiModelProperty(value = "标题")
    @TableField("sku_title")
    private String skuTitle;

    @ApiModelProperty(value = "副标题")
    @TableField("sku_subtitle")
    private String skuSubtitle;

    @ApiModelProperty(value = "价格")
    @TableField("price")
    private BigDecimal price;

    @ApiModelProperty(value = "销量")
    @TableField("sale_count")
    private Long saleCount;

}
