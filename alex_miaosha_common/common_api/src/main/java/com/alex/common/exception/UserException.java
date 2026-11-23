package com.alex.common.exception;

import com.alex.base.enums.ResultEnum;
import lombok.EqualsAndHashCode;

/**
 * description:  用户与鉴权业务异常
 * author:       majf
 * createDate:   2022/7/12 16:21
 * version:      2.0.0
 */
@EqualsAndHashCode(callSuper = true)
public class UserException extends SystemException {

    public UserException(ResultEnum resultEnum) {
        super(resultEnum);
    }

    public UserException(String code, String message) {
        super(code, message);
    }

    public static UserException forbidden(String message) {
        return new UserException(ResultEnum.UNAUTHORIZED.getCode(), message);
    }

    public static UserException notLogin() {
        return new UserException(ResultEnum.USER_NO_LOGIN);
    }
}
