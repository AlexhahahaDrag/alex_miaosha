package com.alex.common.exception.handler;

import com.alex.base.common.Result;
import com.fasterxml.jackson.core.JsonParseException;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.HttpMessageNotReadableException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerHttpBodyTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void missingBody_returns400FriendlyMessage() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "Required request body is missing: public void demo()");

        Result<String> result = handler.handle(ex);

        assertEquals("400", result.getCode());
        assertTrue(result.getMessage().contains("请求体缺失"));
    }

    @Test
    void jsonParseError_returns400FriendlyMessage() {
        JsonParseException cause = new JsonParseException(null,
                "Unexpected character ('\\' (code 92)): was expecting double-quote to start field name");
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "JSON parse error: Unexpected character ('\\' (code 92))", cause);

        Result<String> result = handler.handle(ex);

        assertEquals("400", result.getCode());
        assertEquals("请求体 JSON 格式错误", result.getMessage());
    }
}
