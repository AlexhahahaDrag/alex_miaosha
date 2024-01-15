package com.alex.product.entity.pmsCategory;

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
 * description:  商品三级分类类
 * author:       alex
 * createDate: 2023-03-17 10:06:58
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("pms_category")
@ApiModel(value = "PmsCategory对象", description = "商品三级分类")
public class PmsCategory {

    @ApiModelProperty(value = "分类id")
    @TableId(value = "cat_id", type = IdType.ASSIGN_ID)
    private Long catId;

    @ApiModelProperty(value = "分类名称")
    @TableField("`name`")
    private String name;

    @ApiModelProperty(value = "父分类id")
    @TableField("parent_cid")
    private Long parentCid;

    @ApiModelProperty(value = "层级")
    @TableField("cat_level")
    private Integer catLevel;

    @ApiModelProperty(value = "是否显示[0-不显示，1显示]")
    @TableField("show_status")
    private Integer showStatus;

    @ApiModelProperty(value = "排序")
    @TableField("sort")
    private Integer sort;

    @ApiModelProperty(value = "图标地址")
    @TableField("icon")
    private String icon;

    @ApiModelProperty(value = "计量单位")
    @TableField("product_unit")
    private String productUnit;

    @ApiModelProperty(value = "商品数量")
    @TableField("product_count")
    private Integer productCount;

}
