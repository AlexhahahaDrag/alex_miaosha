package com.alex.common.exception;

import com.alex.base.enums.ResultEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 *description:  财务分析异常
 *author:       majf
 *createDate:   2022/7/12 16:21
 *version:      1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class FinanceException extends RuntimeException{

    private String code;

    private String msg;

    public FinanceException(ResultEnum resultEnum) {
        this.code = resultEnum.getCode();
        this.msg = resultEnum.getValue();
    }

    public FinanceException(String code, String message) {
        log.error(code + ":" + message);
        this.code = code;
        this.msg = message;
    }
}
