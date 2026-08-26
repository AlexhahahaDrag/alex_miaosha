package com.alex.api.user.roleInfo.vo;

import com.alex.api.user.permissionInfo.vo.PermissionInfoVo;
import com.alex.api.user.rolePermissionInfo.vo.RolePermissionInfoVo;
import com.alex.api.user.roleUserInfo.vo.RoleUserInfoVo;
import com.alex.common.common.BaseVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * description:  角色信息表Vo
 * author:       majf
 * createDate:   2024-01-14 21:56:18
 * version:      1.0.0
 */
@Getter
@Setter
@ApiModel(value = "RoleInfoVo", description = "角色信息表Vo")
public class RoleInfoVo extends BaseVo<RoleInfoVo>{

    @ApiModelProperty(value = "角色编码")
    private String roleCode;

    @ApiModelProperty(value = "角色名称")
    private String roleName;

    @ApiModelProperty(value = "描述")
    private String summary;

    @ApiModelProperty(value = "状态,字典(is_valid) 1：有效,0:失效)")
    private String status;

    @ApiModelProperty(value = "绑定用户数")
    private Long boundUserCount;

    @ApiModelProperty(value = "权限数量")
    private Long permissionCount;

    @ApiModelProperty(value = "权限列表")
    List<PermissionInfoVo> permissionList;

    @ApiModelProperty(value = "角色权限列表")
    List<RolePermissionInfoVo> rolePermissionInfoVoList;

    @ApiModelProperty(value = "角色用户列表")
    List<RoleUserInfoVo> roleUserInfoVoList;

    @ApiModelProperty(value = "绑定机构id列表（创建时可选；缺省则绑定登录用户有效机构）")
    private List<String> orgIds;
}
