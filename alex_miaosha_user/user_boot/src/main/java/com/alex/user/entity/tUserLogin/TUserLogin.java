package com.alex.user.entity.tUserLogin;

import com.alex.common.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * @description:  用户登录表类
 * @author:       alex
 * @createDate: 2023-02-16 14:11:55
 * @version:      1.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_user_login")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(value = "TUserLogin对象", description = "用户登录表")
public class TUserLogin extends BaseEntity<TUserLogin>{

    @ApiModelProperty(value = "用户id")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "用户名")
    @TableField(exist = false)
    private String username;

    @ApiModelProperty(value = "昵称")
    @TableField(exist = false)
    private String nickName;

    @ApiModelProperty(value = "最后登录时间")
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    @ApiModelProperty(value = "token在redis中对应的tokenId")
    @TableField("token_id")
    private String tokenId;

    @ApiModelProperty(value = "token信息")
    @TableField("token")
    private String token;

    @ApiModelProperty(value = "系统")
    @TableField("os")
    private String os;

    @ApiModelProperty(value = "登录浏览器")
    @TableField("broswer")
    private String broswer;

    @ApiModelProperty(value = "最后登录IP")
    @TableField("last_login_ip")
    private String lastLoginIp;
}
