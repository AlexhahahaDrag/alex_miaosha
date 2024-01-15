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
 * description:  权限表类
 * author:       majf
 * createDate: 2022-08-09 14:12:42
 * version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("permission")
@ApiModel(value = "Permission对象", description = "权限表")
public class Permission extends Model<Permission> {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "权限")
    @TableField("permission")
    private String permission;

    @ApiModelProperty(value = "权限名称")
    @TableField("permission_name")
    private String permissionName;

}
