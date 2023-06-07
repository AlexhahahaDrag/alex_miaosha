package com.alex.api.product.vo.pmsBrand;

import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @description:  品牌Vo
 * @author:       alex
 * @createDate:   2023-03-05 21:39:54
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "PmsBrandVo", description = "品牌Vo")
public class PmsBrandVo {

    @ApiModelProperty(value = "品牌id")
    @JsonSerialize(using = Long2StringSerializer.class)
    private Long brandId;

    @ApiModelProperty(value = "品牌名")
    private String name;

    @ApiModelProperty(value = "品牌logo地址")
    @JsonSerialize(using = Long2StringSerializer.class)
    private Long logo;

    @ApiModelProperty(value = "品牌logo地址url")
    private String logoUrl;

    @ApiModelProperty(value = "介绍")
    private String descript;

    @ApiModelProperty(value = "显示状态[0-不显示；1-显示]")
    private Integer showStatus;

    @ApiModelProperty(value = "检索首字母")
    private String firstLetter;

    @ApiModelProperty(value = "排序")
    private Integer sort;

}
