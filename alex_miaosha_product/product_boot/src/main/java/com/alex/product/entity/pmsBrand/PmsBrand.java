package com.alex.product.entity.pmsBrand;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @description:  品牌类
 * @author:       alex
 * @createDate: 2023-03-02 15:31:52
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("pms_brand")
@ApiModel(value = "PmsBrand对象", description = "品牌")
public class PmsBrand extends BaseEntity<PmsBrand>{

    @ApiModelProperty(value = "品牌id")
    @TableId(value = "brand_id", type = IdType.AUTO)
    private Long brandId;

    @ApiModelProperty(value = "品牌名")
    @TableField("`name`")
    private String name;

    @ApiModelProperty(value = "品牌logo地址")
    @TableField("logo")
    private String logo;

    @ApiModelProperty(value = "介绍")
    @TableField("descript")
    private String descript;

    @ApiModelProperty(value = "显示状态[0-不显示；1-显示]")
    @TableField("show_status")
    private Integer showStatus;

    @ApiModelProperty(value = "检索首字母")
    @TableField("first_letter")
    private String firstLetter;

    @ApiModelProperty(value = "排序")
    @TableField("sort")
    private Integer sort;

}
