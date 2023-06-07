package com.alex.api.user.vo.tUserLogin;

import com.alex.common.common.BaseVo;
import com.alex.common.config.Long2StringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @description:  用户登录表Vo
 * @author:       alex
 * @createDate:   2023-02-16 14:11:55
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@ApiModel(value = "TUserLoginVo", description = "用户登录表Vo")
public class TUserLoginVo extends BaseVo<TUserLoginVo>{

    @ApiModelProperty(value = "用户id")
    @JsonSerialize(using = Long2StringSerializer.class)
    private Long userId;

    @ApiModelProperty(value = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @ApiModelProperty(value = "token在redis中对应的tokenId")
    private String tokenId;

    @ApiModelProperty(value = "系统")
    private String os;

    @ApiModelProperty(value = "登录浏览器")
    private String broswer;

    @ApiModelProperty(value = "登录IP")
    private String loginIp;

    @ApiModelProperty(value = "登录地址")
    private String loginLocation;
}
