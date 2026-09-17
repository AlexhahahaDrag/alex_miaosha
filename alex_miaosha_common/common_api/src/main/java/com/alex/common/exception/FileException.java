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
        super(resultEnum != null ? resultEnum.getValue() : null);
        log.error("编码：{}，信息：{}", resultEnum != null ? resultEnum.getCode() : null, resultEnum != null ? resultEnum.getValue() : null);
        this.code = resultEnum != null ? resultEnum.getCode() : null;
        this.msg = resultEnum != null ? resultEnum.getValue() : null;
    }

    public FileException(ResultEnum resultEnum, String customMsg) {
        super(customMsg != null ? customMsg : (resultEnum != null ? resultEnum.getValue() : null));
        log.error("编码：{}，信息：{} - {}", resultEnum != null ? resultEnum.getCode() : null, resultEnum != null ? resultEnum.getValue() : null, customMsg);
        this.code = resultEnum != null ? resultEnum.getCode() : null;
        this.msg = customMsg != null ? customMsg : (resultEnum != null ? resultEnum.getValue() : null);
    }

    @Override
    public String getMessage() {
        return this.msg != null ? this.msg : super.getMessage();
    }
}
