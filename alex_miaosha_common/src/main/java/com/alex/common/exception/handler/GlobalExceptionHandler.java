package com.alex.common.exception.handler;

import com.alex.common.common.Result;
import com.alex.common.exception.CustomizeException;
import com.alex.common.exception.LoginException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *description:  全局异常处理
 *author:       majf
 *createDate:   2022/7/12 16:19
 *version:      1.0.0
 */
public class GlobalExceptionHandler {

    @ExceptionHandler(LoginException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<String> handler(LoginException exception) {
        return Result.error("100", exception.getMsg());
    }

    @ExceptionHandler(CustomizeException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Object> handle(CustomizeException exception) {
        return Result.error("100", exception.getMsg());
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handle(Exception ex) {
        return Result.error("500", ex.getMessage());
    }
}
