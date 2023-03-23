package com.alex.api.product.vo.pmsSkuInfo;

import com.alex.common.common.BaseVo;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.math.BigDecimal;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @description:  sku信息Vo
 * @author:       alex
 * @createDate:   2023-03-17 10:30:27
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "PmsSkuInfoVo", description = "sku信息Vo")
public class PmsSkuInfoVo extends BaseVo<PmsSkuInfoVo>{

    @ApiModelProperty(value = "skuId")
    private Long skuId;

    @ApiModelProperty(value = "spuId")
    private Long spuId;

    @ApiModelProperty(value = "sku名称")
    private String skuName;

    @ApiModelProperty(value = "sku介绍描述")
    private String skuDesc;

    @ApiModelProperty(value = "所属分类id")
    private Long catalogId;

    @ApiModelProperty(value = "品牌id")
    private Long brandId;

    @ApiModelProperty(value = "默认图片")
    private String skuDefaultImg;

    @ApiModelProperty(value = "标题")
    private String skuTitle;

    @ApiModelProperty(value = "副标题")
    private String skuSubtitle;

    @ApiModelProperty(value = "价格")
    private BigDecimal price;

    @ApiModelProperty(value = "销量")
    private Long saleCount;

}
