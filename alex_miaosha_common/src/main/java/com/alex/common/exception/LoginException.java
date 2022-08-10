package com.alex.common.exception;

import com.alex.base.common.Result;
import com.alex.base.enums.ResultEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 *description:  登录异常
 *author:       majf
 *createDate:   2022/7/12 16:21
 *version:      1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class LoginException extends RuntimeException{

    private String code;

    private String msg;

    public LoginException(ResultEnum resultEnum) {
        this.code = resultEnum.getCode();
        this.msg = resultEnum.getValue();
    }

    public LoginException(Result userResult, String mobile) {
        if (ResultEnum.PASSWORD_ERROR.equals(userResult.getCode())) {
            log.error("{} 号码登录失败，密码错误", mobile);
        } else if (ResultEnum.MOBILE_NOT_EXIST.equals(userResult.getCode())) {
            log.error("{} 号码登录失败，无此手机号", mobile);
        }
        this.code = userResult.getCode();
        this.msg = userResult.getMessage();
    }
}
