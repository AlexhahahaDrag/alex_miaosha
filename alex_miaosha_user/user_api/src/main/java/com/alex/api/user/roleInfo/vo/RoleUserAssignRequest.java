package com.alex.api.user.roleInfo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 角色分配用户请求体
 */
@Data
@ApiModel(value = "RoleUserAssignRequest", description = "角色分配用户请求")
public class RoleUserAssignRequest {

    @ApiModelProperty(value = "角色id")
    private Long roleId;

    @ApiModelProperty(value = "用户id列表")
    private List<Long> userIds;
}
