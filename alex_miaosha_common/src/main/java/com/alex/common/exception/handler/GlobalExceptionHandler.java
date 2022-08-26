package com.alex.common.exception.handler;

import com.alex.base.common.Result;
import com.alex.common.exception.CustomizeException;
import com.alex.common.exception.LoginException;
import com.alex.common.exception.RegisterException;
import com.alex.common.exception.SeckillException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 *description:  全局异常处理
 *author:       majf
 *createDate:   2022/7/12 16:19
 *version:      1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LoginException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<String> handler(LoginException exception) {
        exception.printStackTrace();
        return Result.error(exception.getCode(), exception.getMsg());
    }

    @ExceptionHandler(CustomizeException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Object> handle(CustomizeException exception) {
        exception.printStackTrace();
        return Result.error(exception.getCode(), exception.getMsg());
    }

    @ExceptionHandler(RegisterException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Object> handle(RegisterException exception) {
        exception.printStackTrace();
        return Result.error(exception.getCode(), exception.getMsg());
    }

    @ExceptionHandler(SeckillException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Object> handle(SeckillException exception) {
        exception.printStackTrace();
        return Result.error(exception.getCode(), exception.getMsg());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handle(Exception ex) {
        ex.printStackTrace();
        return Result.error("500", ex.getMessage());
    }
}
