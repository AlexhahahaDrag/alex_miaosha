package com.alex.common.exception;

import com.alex.base.enums.ResultEnum;
import lombok.EqualsAndHashCode;

/**
 * description:  财务分析异常
 * author:       majf
 * createDate:   2022/7/12 16:21
 * version:      2.0.0
 */
@EqualsAndHashCode(callSuper = true)
public class FinanceException extends SystemException {

    public FinanceException(ResultEnum resultEnum) {
        super(resultEnum);
    }

    public FinanceException(String code, String message) {
        super(code, message);
    }
}
