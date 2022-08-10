package com.alex.web.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @description:  权限dto
 * @author:       majf
 * @createDate:   2022/8/9 14:29
 * @version:      1.0.0
 */
@Data
@ApiModel(value = "Permission对象DTO", description = "权限表DTO")
public class PermissionDTO {

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "权限")
    private String permission;

    @ApiModelProperty(value = "权限名称")
    private String permissionName;
}
