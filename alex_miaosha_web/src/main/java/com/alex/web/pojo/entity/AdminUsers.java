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

import java.time.LocalDateTime;

/**
 * @description:  用户表类
 * @author:       majf
 * @createDate: 2022-08-09 14:12:41
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("admin_users")
@ApiModel(value = "AdminUsers对象", description = "用户表")
public class AdminUsers extends Model<AdminUsers> {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "账号")
    @TableField("username")
    private String username;

    @ApiModelProperty(value = "昵称")
    @TableField("`name`")
    private String name;

    @ApiModelProperty(value = "头像")
    @TableField("avatar")
    private String avatar;

    @ApiModelProperty(value = "手机号")
    @TableField("phone")
    private String phone;

    @ApiModelProperty(value = "角色多个角色可以使用夺标关联或者本字段用逗号分隔")
    @TableField("`role`")
    private String role;

    @ApiModelProperty(value = "用户密码")
    @TableField("`password`")
    private String password;

    @ApiModelProperty(value = "是否禁用")
    @TableField("is_ban")
    private Boolean isBan;

    @ApiModelProperty(value = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
