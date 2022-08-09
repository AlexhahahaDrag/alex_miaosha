package com.alex.web.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "Role对象DTO", description = "角色DTO")
public class RoleDTO {

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "角色")
    private String role;

    @ApiModelProperty(value = "角色名称")
    private String roleName;

    @ApiModelProperty(value = "权限")
    private List<PermissionDTO> permissions;
}
