package com.alex.common.config;

import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.SystemException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("微服务间 Feign 异常解码器集成测试")
class FeignErrorDecoderIT {

    private FeignErrorDecoder errorDecoder;

    @BeforeEach
    void setUp() {
        errorDecoder = new FeignErrorDecoder();
    }

    private Response buildResponse(int status, String body) {
        Request request = Request.create(
                Request.HttpMethod.POST,
                "http://alex-user-dev/api/v1/user/list",
                new HashMap<>(),
                Request.Body.empty(),
                null
        );
        return Response.builder()
                .status(status)
                .reason("Test Reason")
                .request(request)
                .headers(Collections.emptyMap())
                .body(body, StandardCharsets.UTF_8)
                .build();
    }

    @Test
    @DisplayName("1. 下游返回结构化 Result 错误报文时，精准提取 code 与 message 包装为 SystemException")
    void decode_whenDownstreamReturnsStructuredResultJson_extractsCodeAndMessage() {
        String jsonBody = "{\"code\":\"60005\",\"message\":\"用户名已存在!\",\"data\":null}";
        Response response = buildResponse(500, jsonBody);

        Exception exception = errorDecoder.decode("UserApi#addUser", response);

        assertTrue(exception instanceof SystemException, "应当解析转换为 SystemException");
        SystemException systemException = (SystemException) exception;
        assertEquals("60005", systemException.getCode());
        assertTrue(systemException.getMsg().contains("用户名已存在!"));
        assertTrue(systemException.getMsg().contains("UserApi#addUser"));
    }

    @Test
    @DisplayName("2. 下游接口 404 Not Found 时，优雅转换为 SYSTEM_NO_AVAILABLE")
    void decode_whenDownstreamReturns404_returnsSystemNoAvailable() {
        Response response = buildResponse(404, "Not Found");

        Exception exception = errorDecoder.decode("MenuInfoApi#query", response);

        assertTrue(exception instanceof SystemException);
        SystemException systemException = (SystemException) exception;
        assertEquals(ResultEnum.SYSTEM_NO_AVAILABLE.getCode(), systemException.getCode());
        assertTrue(systemException.getMsg().contains("404 Not Found"));
    }

    @Test
    @DisplayName("3. 下游微服务 503/502/504 不可用时，优雅转换为 SYSTEM_BUSY")
    void decode_whenDownstreamReturns503_returnsSystemBusy() {
        Response response = buildResponse(503, "Service Unavailable");

        Exception exception = errorDecoder.decode("OssApi#upload", response);

        assertTrue(exception instanceof SystemException);
        SystemException systemException = (SystemException) exception;
        assertEquals(ResultEnum.SYSTEM_BUSY.getCode(), systemException.getCode());
        assertTrue(systemException.getMsg().contains("503 Service Unavailable"));
    }

    @Test
    @DisplayName("4. 下游返回非 JSON 文本错误时，安全降级至默认异常")
    void decode_whenDownstreamReturnsHtmlOrPlainText_fallsBackSafely() {
        Response response = buildResponse(500, "<html><body>Internal Server Error</body></html>");

        Exception exception = errorDecoder.decode("ProductApi#list", response);
        assertNotNull(exception);
    }
}
