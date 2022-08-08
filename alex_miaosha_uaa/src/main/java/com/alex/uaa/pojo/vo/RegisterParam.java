package com.alex.uaa.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @ApiModelProperty(value = "注册手机号", name = "registerMobile")
    private String registerMobile;

    @ApiModelProperty(value = "注册用户名", name = "registerUsername")
    private String registerUsername;

    @ApiModelProperty(value = "注册身份", name = "registerIdentity")
    private String registerIdentity;

    @ApiModelProperty(value = "注册密码", name = "registerPassword")
    private String registerPassword;
}
