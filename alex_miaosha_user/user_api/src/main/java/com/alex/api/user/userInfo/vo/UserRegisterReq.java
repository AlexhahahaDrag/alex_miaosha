package com.alex.api.user.userInfo.vo;

import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * description: 手机号注册请求参数
 * author: alex
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "UserRegisterReq", description = "手机号注册请求参数")
public class UserRegisterReq implements Serializable {

    @ApiModelProperty(value = "手机号（11位）", required = true)
    private String mobile;

    @ApiModelProperty(value = "登录密码（至少6位）", required = true)
    private String password;

    @ApiModelProperty(value = "昵称（可选，默认取手机号）")
    private String nickName;

    @ApiModelProperty(value = "图形验证码标识ID", required = true)
    private String captchaId;

    @ApiModelProperty(value = "图形验证码内容", required = true)
    private String captchaCode;

    @ApiModelProperty(value = "机构模式：CREATE(自建机构) | JOIN(申请加入已有机构)", required = true)
    private String orgMode;

    @ApiModelProperty(value = "新建机构名称（orgMode=CREATE 时必填）")
    private String orgName;

    @ApiModelProperty(value = "申请加入的目标机构ID（orgMode=JOIN 时必填）")
    @JsonSerialize(using = Long2StringSerializer.class)
    private Long targetOrgId;
}
