package com.alex.user.roleOrgInfo.entity;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * description:  角色机构信息表类
 * author:       majf
 * createDate:   2026-08-19
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_role_org_info")
@ApiModel(value = "RoleOrgInfo对象", description = "角色机构信息表")
public class RoleOrgInfo extends BaseEntity<RoleOrgInfo> {

    @ApiModelProperty(value = "角色id")
    @TableField("role_id")
    private String roleId;

    @ApiModelProperty(value = "机构id")
    @TableField("org_id")
    private String orgId;

    @ApiModelProperty(value = "描述")
    @TableField("summary")
    private String summary;

    @ApiModelProperty(value = "状态字典(is_valid) 1：有效 0:失效")
    @TableField("`status`")
    private String status;

}
