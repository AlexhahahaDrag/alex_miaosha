package com.alex.common.exception;

import com.alex.base.enums.ResultEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * description:  注册异常类
 * author:       majf
 * createDate:   2022/8/8 17:13
 * version:      1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class RegisterException extends RuntimeException{

    private String code;

    private String msg;

    public RegisterException(ResultEnum resultEnum) {
        super();
        this.code = resultEnum.getCode();
        this.msg = resultEnum.getValue();
        log.error("编码：{}，信息：{}", resultEnum.getCode(), resultEnum.getValue());
    }
}
