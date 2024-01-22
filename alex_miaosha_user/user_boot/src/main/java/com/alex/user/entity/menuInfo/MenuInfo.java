package com.alex.user.entity.menuInfo;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * description:  菜单管理表类
 * author:       alex
 * createDate: 2023-12-19 17:34:23
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_menu_info")
@ApiModel(value = "MenuInfo对象", description = "菜单管理表")
public class MenuInfo extends BaseEntity<MenuInfo>{

    @ApiModelProperty(value = "菜单名称")
    @TableField("`name`")
    private String name;

    @ApiModelProperty(value = "菜单路径")
    @TableField("`path`")
    private String path;

    @ApiModelProperty(value = "菜单标题")
    @TableField("title")
    private String title;

    @ApiModelProperty(value = "组件")
    @TableField("`component`")
    private String component;

    @ApiModelProperty(value = "跳转")
    @TableField("redirect")
    private String redirect;

    @ApiModelProperty(value = "菜单图标")
    @TableField("icon")
    private String icon;

    @ApiModelProperty(value = "是否隐藏菜单,字典(true_or_false) 1：有效,0:失效)")
    @TableField("hide_in_menu")
    private String hideInMenu;

    @ApiModelProperty(value = "是否在home中展示,字典(true_or_false) 1：展示,0:不展示)")
    @TableField("show_in_home")
    private String showInHome;

    @ApiModelProperty(value = "父级机构id")
    @TableField("parent_id")
    private Long parentId;

    @ApiModelProperty(value = "备注")
    @TableField("summary")
    private String summary;

    @ApiModelProperty(value = "状态,字典(is_valid) 1：有效,0:失效)")
    @TableField("`status`")
    private String status;

    @ApiModelProperty(value = "排序")
    @TableField("order_by")
    private Integer orderBy;

    @ApiModelProperty(value = "权限编码")
    @TableField("permission_code")
    private String permissionCode;
}
