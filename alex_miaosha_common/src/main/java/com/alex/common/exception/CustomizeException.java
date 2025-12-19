package com.alex.common.exception;

import com.alex.base.enums.ResultEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class CustomizeException extends RuntimeException {

    private String code;

    private String msg;

    public CustomizeException(ResultEnum resultEnum) {
        log.error("编码：{}，信息：{}", resultEnum.getCode(), resultEnum.getValue());
        this.code = resultEnum.getCode();
        this.msg = resultEnum.getValue();
    }
}
