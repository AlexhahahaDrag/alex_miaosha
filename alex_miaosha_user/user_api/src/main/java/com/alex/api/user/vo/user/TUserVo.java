package com.alex.api.user.vo.user;

import com.alex.common.common.BaseVo;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;

/**
 * @description:  管理员表Vo
 * @author:       alex
 * @createDate:   2022-12-26 17:20:38
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "TUserVo", description = "管理员表Vo")
@AllArgsConstructor
@NoArgsConstructor
public class TUserVo extends BaseVo<TUserVo>{

    @ApiModelProperty(value = "用户名")
    @NotBlank(groups = {Insert.class, Update.class}, message = "用户名不能为空！")
    private String username;

    @ApiModelProperty(value = "密码")
    @NotBlank(groups = {Insert.class, Update.class}, message = "密码不能为空！")
    private String password;

    @ApiModelProperty(value = "性别(1:男2:女)")
    private Integer gender;

    @ApiModelProperty(value = "个人头像")
    private String avatar;

    @ApiModelProperty(value = "邮箱")
    @Email
    private String email;

    @ApiModelProperty(value = "出生年月日")
    private LocalDateTime birthday;

    @ApiModelProperty(value = "手机")
    @Pattern(regexp = "^1[0-9]{10}$", message = "手机号不合法")
    private String mobile;

    @ApiModelProperty(value = "邮箱验证码")
    private String validCode;

    @ApiModelProperty(value = "自我简介最多150字")
    private String summary;

    @ApiModelProperty(value = "登录次数")
    private Integer loginCount;

    @ApiModelProperty(value = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @ApiModelProperty(value = "最后登录IP")
    private String lastLoginIp;

    @ApiModelProperty(value = "状态")
    private Integer status;

    @ApiModelProperty(value = "昵称")
    private String nickName;

    @ApiModelProperty(value = "QQ号")
    private String qqNumber;

    @ApiModelProperty(value = "微信号")
    private String weChat;

    @ApiModelProperty(value = "职业")
    private String occupation;

    @ApiModelProperty(value = "github地址")
    private String github;

    @ApiModelProperty(value = "gitee地址")
    private String gitee;

    @ApiModelProperty(value = "拥有的角色uid")
    private String roleId;

    @ApiModelProperty(value = "履历")
    private String personResume;
}
