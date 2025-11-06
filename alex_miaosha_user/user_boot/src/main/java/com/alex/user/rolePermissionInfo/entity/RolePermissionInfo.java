package com.alex.user.rolePermissionInfo.entity;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * description:  角色权限信息表类
 * author:       majf
 * createDate: 2024-01-19 14:52:21
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_role_permission_info")
@ApiModel(value = "RolePermissionInfo对象", description = "角色权限信息表")
public class RolePermissionInfo extends BaseEntity<RolePermissionInfo>{

    @ApiModelProperty(value = "角色id")
    @TableField("role_id")
    private String roleId;

    @ApiModelProperty(value = "权限id")
    @TableField("permission_id")
    private String permissionId;

    @ApiModelProperty(value = "描述")
    @TableField("summary")
    private String summary;

    @ApiModelProperty(value = "状态字典(is_valid) 1：有效 0:失效")
    @TableField("`status`")
    private String status;

}
