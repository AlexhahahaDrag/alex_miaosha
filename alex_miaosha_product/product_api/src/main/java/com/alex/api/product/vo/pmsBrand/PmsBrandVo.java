package com.alex.api.product.vo.pmsBrand;

import com.alex.common.common.BaseVo;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @description:  品牌Vo
 * @author:       alex
 * @createDate:   2023-03-02 15:31:52
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "PmsBrandVo", description = "品牌Vo")
public class PmsBrandVo extends BaseVo<PmsBrandVo>{

    @ApiModelProperty(value = "品牌id")
    private Long brandId;

    @ApiModelProperty(value = "品牌名")
    private String name;

    @ApiModelProperty(value = "品牌logo地址")
    private String logo;

    @ApiModelProperty(value = "介绍")
    private String descript;

    @ApiModelProperty(value = "显示状态[0-不显示；1-显示]")
    private Integer showStatus;

    @ApiModelProperty(value = "检索首字母")
    private String firstLetter;

    @ApiModelProperty(value = "排序")
    private Integer sort;

}
