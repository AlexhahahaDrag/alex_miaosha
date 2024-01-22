package com.alex.api.user.vo.permissionInfo;

import com.alex.common.common.BaseVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * description:  权限信息表Vo
 * author:       majf
 * createDate:   2024-01-16 15:43:56
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "PermissionInfoVo", description = "权限信息表Vo")
public class PermissionInfoVo extends BaseVo<PermissionInfoVo>{

    @ApiModelProperty(value = "权限编码")
    private String permissionCode;

    @ApiModelProperty(value = "权限名称")
    private String permissionName;

    @ApiModelProperty(value = "描述")
    private String summary;

    @ApiModelProperty(value = "状态,字典(is_valid) 1：有效,0:失效)")
    private String status;

    @ApiModelProperty(value = "url")
    private String options;

    @ApiModelProperty(value = "parent_id")
    private Long parentId;

    @ApiModelProperty(value = "子权限")
    private List<PermissionInfoVo> children;
}
