package com.alex.user.entity.user;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @description:  管理员表类
 * @author:       alex
 * @createDate: 2022-12-26 17:20:38
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_user")
@ApiModel(value = "TUser对象", description = "管理员表")
public class TUser extends BaseEntity<TUser>{

    @ApiModelProperty(value = "用户名")
    @TableField("username")
    private String username;

    @ApiModelProperty(value = "密码")
    @TableField("`password`")
    private String password;

    @ApiModelProperty(value = "性别(1:男2:女)")
    @TableField("gender")
    private Integer gender;

    @ApiModelProperty(value = "个人头像")
    @TableField("avatar")
    private String avatar;

    @ApiModelProperty(value = "邮箱")
    @TableField("email")
    private String email;

    @ApiModelProperty(value = "出生年月日")
    @TableField("birthday")
    private LocalDateTime birthday;

    @ApiModelProperty(value = "手机")
    @TableField("mobile")
    private String mobile;

    @ApiModelProperty(value = "邮箱验证码")
    @TableField("valid_code")
    private String validCode;

    @ApiModelProperty(value = "自我简介最多150字")
    @TableField("summary")
    private String summary;

    @ApiModelProperty(value = "状态")
    @TableField("`status`")
    private String status;

    @ApiModelProperty(value = "昵称")
    @TableField("nick_name")
    private String nickName;

    @ApiModelProperty(value = "QQ号")
    @TableField("qq_number")
    private String qqNumber;

    @ApiModelProperty(value = "微信号")
    @TableField("we_chat")
    private String weChat;

    @ApiModelProperty(value = "职业")
    @TableField("occupation")
    private String occupation;

    @ApiModelProperty(value = "github地址")
    @TableField("github")
    private String github;

    @ApiModelProperty(value = "gitee地址")
    @TableField("gitee")
    private String gitee;

    @ApiModelProperty(value = "拥有的角色uid")
    @TableField("role_id")
    private String roleId;

    @ApiModelProperty(value = "履历")
    @TableField("person_resume")
    private String personResume;

}
