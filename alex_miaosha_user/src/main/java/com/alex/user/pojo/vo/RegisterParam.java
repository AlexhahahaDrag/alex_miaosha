package com.alex.user.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @description:  注册字段
 * @author:       majf
 * @createDate:   2022/8/8 15:49
 * @version:      1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "Register参数", description = "注册参数")
public class RegisterParam {

    @NotBlank(message = "注册手机号不能为空")
    @ApiModelProperty(value = "注册手机号", name = "registerMobile")
    private String registerMobile;

    @NotBlank(message = "注册用户名不能为空")
    @ApiModelProperty(value = "注册用户名", name = "registerUsername")
    private String registerUsername;

    @NotBlank(message = "注册身份不能为空")
    @ApiModelProperty(value = "注册身份", name = "registerIdentity")
    private String registerIdentity;

    @NotBlank(message = "注册密码不能为空")
    @ApiModelProperty(value = "注册密码", name = "registerPassword")
    private String registerPassword;
}
