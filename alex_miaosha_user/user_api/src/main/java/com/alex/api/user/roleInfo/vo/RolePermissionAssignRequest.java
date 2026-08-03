package com.alex.api.user.roleInfo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 角色分配权限请求体
 */
@Data
@ApiModel(value = "RolePermissionAssignRequest", description = "角色分配权限请求")
public class RolePermissionAssignRequest {

    @ApiModelProperty(value = "角色id")
    private Long roleId;

    @ApiModelProperty(value = "权限id列表")
    private List<Long> permissionIds;
}
