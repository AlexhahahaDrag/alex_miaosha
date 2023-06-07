package com.alex.api.user.vo.user;

import com.alex.common.common.BaseVo;
import com.alex.common.config.Long2StringSerializer;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

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
    private String password;

    @ApiModelProperty(value = "性别(1:男2:女)")
    private Integer gender;

    @ApiModelProperty(value = "个人头像id")
    @JsonSerialize(using = Long2StringSerializer.class)
    private Long avatar;

    @ApiModelProperty(value = "个人头像url")
    private String avatarUrl;

    @ApiModelProperty(value = "邮箱")
//    @Email(message = "邮箱不合法！", groups = {Insert.class, Update.class})
    private String email;

    @ApiModelProperty(value = "出生年月日")
    private LocalDateTime birthday;

    // TODO: 2023/2/15 自定义添加，为空时不校验
    @ApiModelProperty(value = "手机")
    @Pattern(regexp = "^\s{0}$|^1[0-9]{10}$", message = "手机号不合法!", groups = {Insert.class, Update.class})
    private String mobile;

    @ApiModelProperty(value = "邮箱验证码")
    private String validCode;

    @ApiModelProperty(value = "自我简介最多150字")
    private String summary;

    @ApiModelProperty(value = "状态")
    private String status;

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
