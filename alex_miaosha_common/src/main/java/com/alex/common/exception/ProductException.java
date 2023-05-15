package com.alex.common.exception;

import com.alex.base.enums.ResultEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * @description:  商品异常
 * @author:       majf
 * @createDate:   2023/5/15 8:41
 * @version:      1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class ProductException extends RuntimeException {

    private String code;

    private String msg;

    public ProductException(ResultEnum resultEnum) {
        super();
        this.code = resultEnum.getCode();
        this.msg = resultEnum.getValue();
        log.info("商品异常");
    }
}
