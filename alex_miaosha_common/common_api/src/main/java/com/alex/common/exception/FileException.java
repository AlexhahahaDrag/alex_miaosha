package com.alex.common.exception;

import com.alex.base.enums.ResultEnum;
import lombok.EqualsAndHashCode;

/**
 * description:  文件异常
 * author:       majf
 * createDate:   2022/7/12 16:21
 * version:      2.0.0
 */
@EqualsAndHashCode(callSuper = true)
public class FileException extends SystemException {

    public FileException(ResultEnum resultEnum) {
        super(resultEnum);
    }

    public FileException(ResultEnum resultEnum, String customMsg) {
        super(
                resultEnum != null ? resultEnum.getCode() : null,
                customMsg != null ? customMsg : (resultEnum != null ? resultEnum.getValue() : null)
        );
    }
}
