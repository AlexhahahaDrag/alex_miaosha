package com.alex.common.exception;

import com.alex.base.enums.ResultEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 *description:  AI 引擎异常
 *author:       majf
 *createDate:   2022/7/12 16:21
 *version:      1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class AiException extends RuntimeException{

    private String code;

    private String msg;

    public AiException(ResultEnum resultEnum) {
        super(resultEnum.getValue());
        this.code = resultEnum.getCode();
        this.msg = resultEnum.getValue();
    }

    public AiException(String code, String message) {
        super(message);
        log.error("编码：{}，信息：{}", code, message);
        this.code = code;
        this.msg = message;
    }
}
