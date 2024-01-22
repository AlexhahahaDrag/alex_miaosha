package com.alex.api.user.vo.rolePermissionInfo;

import com.alex.common.common.BaseVo;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * description:  角色权限信息表Vo
 * author:       majf
 * createDate:   2024-01-19 14:52:21
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "RolePermissionInfoVo", description = "角色权限信息表Vo")
public class RolePermissionInfoVo extends BaseVo<RolePermissionInfoVo>{

    @ApiModelProperty(value = "角色id")
    private String roleId;

    @ApiModelProperty(value = "权限id")
    private String permissionId;

    @ApiModelProperty(value = "描述")
    private String summary;

    @ApiModelProperty(value = "状态,字典(is_valid) 1：有效,0:失效)")
    private String status;

}
