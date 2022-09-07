package com.alex.user.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:  登录参数
 * @author:       majf
 * @createDate:   2022/8/8 15:49
 * @version:      1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "LoginParam", description = "登录参数")
public class LoginParam {

    @ApiModelProperty(value = "电话号", name = "mobile")
    private String mobile;

    @ApiModelProperty(value = "密码", name = "password")
    private String password;
}
