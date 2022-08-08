package com.alex.common.exception;

import com.alex.base.enums.ResultEnum;

/**
 * @description:  注册异常类
 * @author:       majf
 * @createDate:   2022/8/8 17:13
 * @version:      1.0.0
 */
public class RegisterException extends RuntimeException{

    private String code;

    private String msg;

    public RegisterException(ResultEnum resultEnum) {
        super();
        this.code = resultEnum.getCode();
        this.msg = resultEnum.getValue();
    }
}
