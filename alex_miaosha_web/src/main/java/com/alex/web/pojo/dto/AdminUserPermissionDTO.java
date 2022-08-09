package com.alex.web.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(value = "管理员权限DTO", description = "管理员权限DTO")
public class AdminUserPermissionDTO {

    @NotBlank(message = "id不能为空")
    @ApiModelProperty(value = "id")
    private Long id;

    @NotBlank(message = "权限不能为空")
    @ApiModelProperty(value = "权限列表")
    private Long[] permissionIds;
}
