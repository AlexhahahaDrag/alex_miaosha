package com.alex.api.product.vo.pmsShopProduct;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品/秒杀营销文案与卖点 AI 生成请求
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "ProductAiCopyReq", description = "商品/秒杀营销文案与卖点 AI 生成请求")
public class ProductAiCopyReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商品名称", required = true)
    private String productName;

    @ApiModelProperty(value = "商品品类/分类")
    private String categoryName;

    @ApiModelProperty(value = "商品原价(元)")
    private BigDecimal originalPrice;

    @ApiModelProperty(value = "秒杀促销价(元)")
    private BigDecimal seckillPrice;

    @ApiModelProperty(value = "目标人群/受众")
    private String targetAudience;

    @ApiModelProperty(value = "原始商品亮点或规格参数说明")
    private String features;
}
