package com.alex.web.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * description:  角色表类
 * author:       majf
 * createDate: 2022-08-09 14:12:42
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("role")
@ApiModel(value = "Role对象", description = "角色表")
public class Role extends Model<Role> {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "角色")
    @TableField("`role`")
    private String role;

    @ApiModelProperty(value = "角色名称")
    @TableField("role_name")
    private String roleName;

}
