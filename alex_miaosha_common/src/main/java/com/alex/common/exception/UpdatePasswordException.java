package com.alex.common.exception;

import com.alex.base.enums.ResultEnum;

/**
 * description:  更新密码异常类
 * author:       majf
 * createDate:   2022/8/8 17:13
 * version:      1.0.0
 */
public class UpdatePasswordException extends RuntimeException{

    private String code;

    private String msg;

    public UpdatePasswordException(ResultEnum resultEnum) {
        super();
        this.code = resultEnum.getCode();
        this.msg = resultEnum.getValue();
    }
}
