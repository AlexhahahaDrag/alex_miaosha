package com.alex.api.user.userInfo.vo;

import com.alex.api.user.menuInfo.vo.MenuInfoVo;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
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

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.List;

/**
 * description:  管理员表Vo
 * author:       alex
 * createDate:   2022-12-26 17:20:38
 * version:      1.0.0
 */
@Getter
@Setter
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
    private String gender;

    @ApiModelProperty(value = "个人头像id")
    @JsonSerialize(using = Long2StringSerializer.class)
    private Long avatar;

    @ApiModelProperty(value = "个人头像url")
    private String avatarUrl;

    @ApiModelProperty(value = "个人头像缩略图url")
    private String avatarThumbnailUrl;

    @ApiModelProperty(value = "邮箱")
//    @Email(message = "邮箱不合法！", groups = {Insert.class, Update.class})
    private String email;

    @ApiModelProperty(value = "出生年月日")
    private LocalDateTime birthday;

    @ApiModelProperty(value = "手机")
    @Pattern(regexp = "^$|^1[0-9]{10}$", message = "手机号不合法!", groups = {Insert.class, Update.class})
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

    @ApiModelProperty(value = "机构信息")
    private OrgInfoVo orgInfoVo;

    @ApiModelProperty(value = "角色信息")
    private RoleInfoVo roleInfoVo;

    @ApiModelProperty(value = "菜单信息")
    private List<MenuInfoVo> menuInfoVoList;

    @ApiModelProperty(value = "权限上下文")
    private UserPermissionContextVo permissionContext;

    @ApiModelProperty(value = "角色信息列表")
    private List<RoleInfoVo> roleInfoVoList;

    @ApiModelProperty(value = "权限编码列表")
    private List<String> permissionCodes;

    @ApiModelProperty(value = "按钮权限编码列表")
    private List<String> buttonPermissionCodes;

    @ApiModelProperty(value = "token信息")
    private String token;

    @ApiModelProperty(value = "所属机构名称")
    private String orgName;

    @ApiModelProperty(value = "所属机构编码")
    private String orgCode;

    @ApiModelProperty(value = "角色名称")
    private String roleName;

    @ApiModelProperty(value = "角色编码")
    private String roleCode;
}
