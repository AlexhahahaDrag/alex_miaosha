package com.alex.web.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @description:  权限菜单表类
 * @author:       majf
 * @createDate: 2022-08-09 14:12:42
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("permission_menu")
@ApiModel(value = "PermissionMenu对象", description = "权限菜单表")
public class PermissionMenu extends Model<PermissionMenu> {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "一级还是二级菜单(目录/菜单)")
    @TableField("`level`")
    private Integer level;

    @ApiModelProperty(value = "排序")
    @TableField("sort")
    private Integer sort;

    @ApiModelProperty(value = "权限Id")
    @TableField("permission_id")
    private Long permissionId;

    @ApiModelProperty(value = "父级ID")
    @TableField("parent_id")
    private String parentId;

    @ApiModelProperty(value = "组建页面")
    @TableField("`key`")
    private String key;

    @ApiModelProperty(value = "名称")
    @TableField("`name`")
    private String name;

    @ApiModelProperty(value = "图标 设置该路由的图标")
    @TableField("icon")
    private String icon;

}
