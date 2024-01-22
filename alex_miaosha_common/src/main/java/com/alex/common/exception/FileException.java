package com.alex.common.exception;

import com.alex.base.enums.ResultEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 *description:  文件异常
 *author:       majf
 *createDate:   2022/7/12 16:21
 *version:      1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class FileException extends RuntimeException{

    private String code;

    private String msg;

    public FileException(ResultEnum resultEnum) {
        log.error(resultEnum.getCode() + ":" + resultEnum.getValue());
        this.code = resultEnum.getCode();
        this.msg = resultEnum.getValue();
    }
}