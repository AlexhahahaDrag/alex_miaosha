package com.alex.product.entity.pmsAttr;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import com.alex.common.config.Long2StringSerializer;

/**
 * @description:  商品属性类
 * @author:       alex
 * @createDate: 2023-12-27 16:02:07
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("pms_attr")
@ApiModel(value = "PmsAttr对象", description = "商品属性")
public class PmsAttr extends BaseEntity<PmsAttr>{

    @ApiModelProperty(value = "属性名")
    @TableField("attr_name")
    private String attrName;

    @ApiModelProperty(value = "是否需要检索[0-不需要，1-需要]")
    @TableField("search_type")
    private Integer searchType;

    @ApiModelProperty(value = "属性图标")
    @TableField("icon")
    private String icon;

    @ApiModelProperty(value = "可选值列表[用逗号分隔]")
    @TableField("value_select")
    private String valueSelect;

    @ApiModelProperty(value = "属性类型[0-销售属性，1-基本属性，2-既是销售属性又是基本属性]")
    @TableField("attr_type")
    private Integer attrType;

    @ApiModelProperty(value = "启用状态[0 - 禁用，1 - 启用]")
    @TableField("`enable`")
    private Long enable;

    @ApiModelProperty(value = "所属分类")
    @TableField("catelog_id")
    private Long catelogId;

    @ApiModelProperty(value = "快速展示【是否展示在介绍上；0-否 1-是】，在sku中仍然可以调整")
    @TableField("show_desc")
    private Integer showDesc;

}
