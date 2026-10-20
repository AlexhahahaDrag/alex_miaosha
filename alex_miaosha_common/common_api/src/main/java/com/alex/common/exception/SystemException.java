package com.alex.common.exception;

import com.alex.base.enums.ResultEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 *description:  登录异常
 *author:       majf
 *createDate:   2022/7/12 16:21
 *version:      1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class SystemException extends RuntimeException {

    private String code;

    private String msg;

    public SystemException(ResultEnum resultEnum, String system) {
        super(system + (resultEnum != null ? resultEnum.getValue() : ""));
        this.code = resultEnum != null ? resultEnum.getCode() : null;
        this.msg = system + (resultEnum != null ? resultEnum.getValue() : "");
        log.error("编码：{}，信息：{}", resultEnum != null ? resultEnum.getCode() : null, resultEnum != null ? resultEnum.getValue() : null);
    }

    public SystemException(String code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
        log.error("编码：{}，信息：{}", code, msg);
    }
}
