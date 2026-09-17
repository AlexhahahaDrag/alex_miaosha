package com.alex.common.exception;

import com.alex.base.enums.ResultEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class SeckillException extends RuntimeException {

    private String code;

    private String msg;

    public SeckillException(ResultEnum resultEnum) {
        super();
        this.code = resultEnum.getCode();
        this.msg = resultEnum.getValue();
        log.error("编码：{}，信息：{}", resultEnum.getCode(), resultEnum.getValue());
    }
}
