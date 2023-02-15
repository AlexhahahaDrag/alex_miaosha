package com.alex.common.exception.handler;

import com.alex.base.common.Result;
import com.alex.common.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * description:  全局异常处理
 * author:       majf
 * createDate:   2022/7/12 16:19
 * version:      1.0.0
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

    @ExceptionHandler(UserException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handle(UserException userException) {
        userException.printStackTrace();
        return Result.error(userException.getCode(), userException.getMsg());
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handle(BindException bindException) {
        String errorMsg = null;
        if (bindException.hasErrors()) {
            errorMsg = bindException.getFieldErrors().stream().map(item -> item.getDefaultMessage()).collect(Collectors.joining(","));
        }
        bindException.printStackTrace();
        return Result.error("400", errorMsg);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handle(Exception ex) {
        ex.printStackTrace();
        return Result.error("500", ex.getMessage());
    }
}
