package com.alex.api.product.vo.pmsShopWantProduct;

import com.alex.common.common.BaseVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * @description:  商品想买网上商品信息Vo
 * @author:       alex
 * @createDate:   2023-05-25 16:18:10
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "PmsShopWantProductVo", description = "商品想买网上商品信息Vo")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PmsShopWantProductVo extends BaseVo<PmsShopWantProductVo>{

    @ApiModelProperty(value = "商品名称")
    private String name;

    @ApiModelProperty(value = "商铺")
    private String shop;

    @ApiModelProperty(value = "标签")
    private String icons;

    @ApiModelProperty(value = "来源")
    private String source;

    @ApiModelProperty(value = "状态")
    private Integer status;
}
