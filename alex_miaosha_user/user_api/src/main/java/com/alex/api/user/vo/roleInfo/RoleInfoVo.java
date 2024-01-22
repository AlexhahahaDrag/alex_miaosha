package com.alex.api.user.vo.roleInfo;

import com.alex.api.user.vo.permissionInfo.PermissionInfoVo;
import com.alex.common.common.BaseVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * description:  角色信息表Vo
 * author:       majf
 * createDate:   2024-01-14 21:56:18
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
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

    @ApiModelProperty(value = "权限列表")
    List<PermissionInfoVo> permissionList;
}
