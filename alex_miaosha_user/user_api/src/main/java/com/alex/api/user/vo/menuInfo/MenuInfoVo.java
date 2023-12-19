package com.alex.api.user.vo.menuInfo;

import com.alex.common.common.BaseVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @description:  菜单管理表Vo
 * @author:       alex
 * @createDate:   2023-12-19 17:34:23
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "MenuInfoVo", description = "菜单管理表Vo")
public class MenuInfoVo extends BaseVo<MenuInfoVo>{

    @ApiModelProperty(value = "菜单名称")
    private String name;

    @ApiModelProperty(value = "菜单路径")
    private String path;

    @ApiModelProperty(value = "菜单标题")
    private String title;

    @ApiModelProperty(value = "组件")
    private String component;

    @ApiModelProperty(value = "跳转")
    private String redirect;

    @ApiModelProperty(value = "菜单图标")
    private String icon;

    @ApiModelProperty(value = "是否隐藏菜单,字典(true_or_false) 1：有效,0:失效)")
    private String hideInMenu;

    @ApiModelProperty(value = "父级机构id")
    private Long parentId;

    @ApiModelProperty(value = "备注")
    private String summary;

    @ApiModelProperty(value = "状态,字典(is_valid) 1：有效,0:失效)")
    private String status;

    @ApiModelProperty(value = "排序")
    private Integer orderBy;

    private List<MenuInfoVo> children;
}
