package com.alex.common.exception;

import com.alex.base.enums.ResultEnum;
import lombok.EqualsAndHashCode;

/**
 * description:  AI 引擎异常
 * author:       majf
 * createDate:   2022/7/12 16:21
 * version:      2.0.0
 */
@EqualsAndHashCode(callSuper = true)
public class AiException extends SystemException {

    public AiException(ResultEnum resultEnum) {
        super(resultEnum);
    }

    public AiException(String code, String message) {
        super(code, message);
    }
}
