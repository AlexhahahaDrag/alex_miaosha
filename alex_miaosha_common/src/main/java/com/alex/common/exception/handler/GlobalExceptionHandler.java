package com.alex.common.exception.handler;

import com.alex.base.common.Result;
import com.alex.common.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
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
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(LoginException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<String> handler(LoginException exception) {
        log.error("登录异常:{}", exception.getMessage(), exception);
        return Result.error(exception.getCode(), exception.getMsg());
    }

    @ExceptionHandler(CustomizeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handle(CustomizeException exception) {
        log.error("自定义异常:{}", exception.getMessage(), exception);
        return Result.error(exception.getCode(), exception.getMsg());
    }

    @ExceptionHandler(RegisterException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handle(RegisterException exception) {
        log.error("注册异常:{}", exception.getMessage(), exception);
        return Result.error(exception.getCode(), exception.getMsg());
    }

    @ExceptionHandler(SeckillException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handle(SeckillException exception) {
        log.error("秒杀异常:{}", exception.getMessage(), exception);
        return Result.error(exception.getCode(), exception.getMsg());
    }

    @ExceptionHandler(UserException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handle(UserException userException) {
        log.error("用户异常:{}", userException.getMsg(), userException);
        return Result.error(userException.getCode(), userException.getMsg());
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handle(BindException bindException) {
        String errorMsg = null;
        if (bindException.hasErrors()) {
            errorMsg = bindException.getFieldErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining(","));
        }
        log.error("参数校验异常:{}", errorMsg, bindException);
        return Result.error("400", errorMsg);
    }

    @ExceptionHandler(ProductException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handle(ProductException e) {
        log.error("商品异常:{}", e.getMessage(), e);
        return Result.error(e.getCode(), e.getMsg());
    }

    @ExceptionHandler(FinanceException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handle(FinanceException e) {
        log.error("财务异常:{}", e.getMessage(), e);
        return Result.error(e.getCode(), e.getMsg());
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handle(Exception ex) {
        log.error("系统异常:{},", ex.getMessage(), ex);
        return Result.error("500", ex.getMessage());
    }
}
