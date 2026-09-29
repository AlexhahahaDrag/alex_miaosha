package com.alex.api.product.vo.pmsShopProduct;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 商品/秒杀营销文案与卖点 AI 生成响应
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "ProductAiCopyVo", description = "商品/秒杀营销文案与卖点 AI 生成响应")
public class ProductAiCopyVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "AI 爆款商品标题")
    private String title;

    @ApiModelProperty(value = "一句话秒杀营销口号/副标题")
    private String slogan;

    @ApiModelProperty(value = "精选核心卖点列表")
    private List<String> sellingPoints;

    @ApiModelProperty(value = "详版营销种草/转化文案")
    private String marketingDescription;

    @ApiModelProperty(value = "折扣力度说明(如: 直降100元，立享5折爆款)")
    private String discountText;
}
