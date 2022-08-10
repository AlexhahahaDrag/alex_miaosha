package com.alex.web.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @description:  权限菜单表类
 * @author:       majf
 * @createDate: 2022-08-09 14:12:42
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "PermissionMenu对象DTO", description = "权限菜单表dto")
public class PermissionMenuDTO {

    private Long id;

    @ApiModelProperty(value = "一级还是二级菜单(目录/菜单)")
    private Integer level;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "权限Id")
    private Integer permissionId;

    @ApiModelProperty(value = "父级ID")
    private String parentId;

    @ApiModelProperty(value = "组建页面")
    private String key;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "图标 设置该路由的图标")
    private String icon;

    @ApiModelProperty(value = "权限dto")
    private PermissionDTO permission;

    @ApiModelProperty(value = "权限子集")
    private List<PermissionMenuDTO> children;
}
