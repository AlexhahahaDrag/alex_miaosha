package com.alex.api.user.userInfo.vo;

import com.alex.api.user.menuInfo.vo.MenuInfoVo;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "UserPermissionContextVo", description = "用户权限上下文Vo")
public class UserPermissionContextVo {

    @ApiModelProperty(value = "机构信息")
    private OrgInfoVo orgInfo;

    @ApiModelProperty(value = "角色列表")
    private List<RoleInfoVo> roleList;

    @ApiModelProperty(value = "权限编码列表")
    private List<String> permissionCodes;

    @ApiModelProperty(value = "按钮权限编码列表")
    private List<String> buttonPermissionCodes;

    @ApiModelProperty(value = "菜单列表")
    private List<MenuInfoVo> menuList;

    @ApiModelProperty(value = "是否超级管理员")
    private Boolean superAdmin;
}
