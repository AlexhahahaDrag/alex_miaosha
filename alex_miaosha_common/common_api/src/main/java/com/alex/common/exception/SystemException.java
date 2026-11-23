package com.alex.common.exception;

import com.alex.base.enums.ResultEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * description:  统一系统与通用参数异常基类
 * author:       majf
 * createDate:   2022/7/12 16:21
 * version:      2.0.0
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SystemException extends RuntimeException {

    private String code;

    private String msg;

    public SystemException(ResultEnum resultEnum) {
        super(resultEnum != null ? resultEnum.getValue() : null);
        this.code = resultEnum != null ? resultEnum.getCode() : null;
        this.msg = resultEnum != null ? resultEnum.getValue() : null;
    }

    public SystemException(ResultEnum resultEnum, String system) {
        super(system + (resultEnum != null ? resultEnum.getValue() : ""));
        this.code = resultEnum != null ? resultEnum.getCode() : null;
        this.msg = system + (resultEnum != null ? resultEnum.getValue() : "");
    }

    public SystemException(String code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    @Override
    public String getMessage() {
        return this.msg != null ? this.msg : super.getMessage();
    }

    public static SystemException param(String message) {
        return new SystemException(ResultEnum.PARAM_ERROR.getCode(), message);
    }

    public static SystemException system(String message) {
        return new SystemException(ResultEnum.SYSTEM_ERROR.getCode(), message);
    }
}
