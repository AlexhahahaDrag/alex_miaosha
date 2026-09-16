package com.alex.api.user.roleInfo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 角色分配机构请求体
 */
@Data
@ApiModel(value = "RoleOrgAssignRequest", description = "角色分配机构请求")
public class RoleOrgAssignRequest {

    @ApiModelProperty(value = "角色id")
    private Long roleId;

    @ApiModelProperty(value = "机构id列表")
    private List<Long> orgIds;
}
