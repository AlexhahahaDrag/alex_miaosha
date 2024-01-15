package com.alex.user.entity.roleInfo;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import com.alex.common.config.Long2StringSerializer;

/**
 * description:  角色信息表类
 * author:       majf
 * createDate: 2024-01-14 21:56:18
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_role_info")
@ApiModel(value = "RoleInfo对象", description = "角色信息表")
public class RoleInfo extends BaseEntity<RoleInfo>{

    @ApiModelProperty(value = "角色编码")
    @TableField("role_code")
    private String roleCode;

    @ApiModelProperty(value = "角色名称")
    @TableField("role_name")
    private String roleName;

    @ApiModelProperty(value = "描述")
    @TableField("summary")
    private String summary;

    @ApiModelProperty(value = "状态,字典(is_valid) 1：有效,0:失效)")
    @TableField("`status`")
    private String status;

}
