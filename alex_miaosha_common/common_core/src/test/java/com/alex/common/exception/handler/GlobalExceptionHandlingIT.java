package com.alex.common.exception.handler;

import com.alex.base.common.Result;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 阶段三十一：公共底座 GlobalExceptionHandler 全局异常拦截与保真响应集成测试
 */
public class GlobalExceptionHandlingIT {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("SystemException 修复验证：参数错误时准确返回 400 状态码与明细，绝不降级为 500")
    void testHandleSystemExceptionParamError() {
        SystemException ex = new SystemException(ResultEnum.PARAM_ERROR, "用户ID非法:");

        Result<String> result = handler.handle(ex);

        assertNotNull(result);
        assertEquals(ResultEnum.PARAM_ERROR.getCode(), result.getCode());
        assertEquals("400", result.getCode());
        assertTrue(result.getMessage().contains("用户ID非法"));
    }

    @Test
    @DisplayName("SystemException 跨微服务降级验证：服务不可用异常准确返回 500100 业务码")
    void testHandleSystemExceptionServiceUnavailable() {
        SystemException ex = new SystemException(ResultEnum.SYSTEM_NO_AVAILABLE, "ai");

        Result<String> result = handler.handle(ex);

        assertNotNull(result);
        assertEquals(ResultEnum.SYSTEM_NO_AVAILABLE.getCode(), result.getCode());
        assertEquals("500102", result.getCode());
        assertTrue(result.getMessage().contains("ai"));
    }

    @Test
    @DisplayName("BindException 参数校验异常：提取所有 FieldError 的默认提示并组合为 400 结果")
    void testHandleBindExceptionAggregatesFieldErrors() {
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "testDto");
        bindingResult.addError(new FieldError("testDto", "username", "用户名不能为空"));
        bindingResult.addError(new FieldError("testDto", "age", "年龄必须大于0"));

        BindException bindException = new BindException(bindingResult);

        Result<String> result = handler.handle(bindException);

        assertNotNull(result);
        assertEquals("400", result.getCode());
        assertTrue(result.getMessage().contains("用户名不能为空"));
        assertTrue(result.getMessage().contains("年龄必须大于0"));
    }

    @Test
    @DisplayName("HttpMessageNotReadableException 格式错误：捕获并友好返回 400")
    void testHandleHttpMessageNotReadableException() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "JSON parse error: Unexpected character",
                new MockHttpInputMessage("bad json".getBytes())
        );

        Result<String> result = handler.handle(ex);

        assertNotNull(result);
        assertEquals("400", result.getCode());
        assertEquals("请求体 JSON 格式错误", result.getMessage());
    }

    @Test
    @DisplayName("领域专用异常保真：FinanceException、AiException、UserException 保留专属错误码")
    void testHandleDomainExceptions() {
        FinanceException financeEx = new FinanceException("500301", "记账金额超出单笔上限");
        Result<String> financeResult = handler.handle(financeEx);
        assertEquals("500301", financeResult.getCode());
        assertEquals("记账金额超出单笔上限", financeResult.getMessage());

        AiException aiEx = new AiException("500701", "AI 引擎不可用");
        Result<String> aiResult = handler.handle(aiEx);
        assertEquals("500701", aiResult.getCode());
        assertEquals("AI 引擎不可用", aiResult.getMessage());

        UserException userEx = new UserException("500201", "用户角色权限已被禁用");
        Result<String> userResult = handler.handle(userEx);
        assertEquals("500201", userResult.getCode());
        assertEquals("用户角色权限已被禁用", userResult.getMessage());
    }

    @Test
    @DisplayName("未知通用 Exception 兜底：返回 500 状态码与异常明细")
    void testHandleGenericException() {
        NullPointerException npe = new NullPointerException("空指针调试异常");

        Result<String> result = handler.handle(npe);

        assertNotNull(result);
        assertEquals("500", result.getCode());
        assertEquals("空指针调试异常", result.getMessage());
    }
}
