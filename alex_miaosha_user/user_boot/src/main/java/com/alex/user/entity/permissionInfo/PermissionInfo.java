package com.alex.user.entity.permissionInfo;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * description:  权限信息表类
 * author:       majf
 * createDate: 2024-01-16 15:43:56
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_permission_info")
@ApiModel(value = "PermissionInfo对象", description = "权限信息表")
public class PermissionInfo extends BaseEntity<PermissionInfo>{

    @ApiModelProperty(value = "权限编码")
    @TableField("permission_code")
    private String permissionCode;

    @ApiModelProperty(value = "权限名称")
    @TableField("permission_name")
    private String permissionName;

    @ApiModelProperty(value = "描述")
    @TableField("summary")
    private String summary;

    @ApiModelProperty(value = "状态,字典(is_valid) 1：有效,0:失效)")
    @TableField("`status`")
    private String status;

    @ApiModelProperty(value = "url")
    @TableField("`options`")
    private String options;

    @ApiModelProperty(value = "parent_id")
    @TableField("parent_id")
    private Long parentId;
}
