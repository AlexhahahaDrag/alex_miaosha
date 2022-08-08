package com.alex.uaa.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "用户表", description = "用户表")
@TableName(value = "user")
@Builder
public class User implements Serializable {

    private static final long serialVersionUid = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("用户名")
    @TableField(value = "user_name")
    private String userName;

    @ApiModelProperty("登录手机号")
    @TableField(value = "phone")
    private String phone;

    @ApiModelProperty("密码")
    @TableField(value = "password")
    private String password;

    @ApiModelProperty("身份证号")
    @TableField(value = "identity_card_id")
    private String identityCardId;

    @ApiModelProperty("创建时间")
    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    @ApiModelProperty("更新时间")
    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;

    @ApiModelProperty("最后一次登录时间")
    @TableField(value = "last_login_time")
    private LocalDateTime lastLoginTime;
}
