package com.alex.common.exception;

import com.alex.base.enums.ResultEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CustomizeException extends RuntimeException {

    private String code;

    private String msg;

    public CustomizeException(ResultEnum resultEnum) {
        super(resultEnum.getValue());
        this.code = resultEnum.getCode();
        this.msg = resultEnum.getValue();
    }
}
