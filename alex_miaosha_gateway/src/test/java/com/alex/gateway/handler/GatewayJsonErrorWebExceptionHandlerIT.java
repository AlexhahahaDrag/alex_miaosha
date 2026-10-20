package com.alex.gateway.handler;

import com.alex.base.common.Result;
import com.alex.gateway.utils.EncryptionUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.gateway.support.TimeoutException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 集成测试：网关全局统一 JSON 异常处理器契约验证
 */
class GatewayJsonErrorWebExceptionHandlerIT {

    private GatewayJsonErrorWebExceptionHandler exceptionHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        exceptionHandler = new GatewayJsonErrorWebExceptionHandler();
    }

    private String extractResponseBody(MockServerWebExchange exchange) {
        Flux<DataBuffer> bodyFlux = exchange.getResponse().getBody();
        DataBuffer combinedBuffer = bodyFlux.reduce((b1, b2) -> {
            int len = b1.readableByteCount() + b2.readableByteCount();
            byte[] bytes = new byte[len];
            int b1Count = b1.readableByteCount();
            b1.read(bytes, 0, b1Count);
            b2.read(bytes, b1Count, b2.readableByteCount());
            return exchange.getResponse().bufferFactory().wrap(bytes);
        }).block();

        if (combinedBuffer == null) {
            return "";
        }
        byte[] bytes = new byte[combinedBuffer.readableByteCount()];
        combinedBuffer.read(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("404 路由或资源不存在：统一包装为 Result.error(404) 标准 JSON")
    void testHandle_NotFoundException() throws Exception {
        MockServerHttpRequest request = MockServerHttpRequest.get("/am-user/non-existent-api").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        NotFoundException ex = new NotFoundException("Route not found");

        exceptionHandler.handle(exchange, ex).block();

        assertEquals(HttpStatus.NOT_FOUND, exchange.getResponse().getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, exchange.getResponse().getHeaders().getContentType());

        String json = extractResponseBody(exchange);
        Result<Void> res = objectMapper.readValue(json, new TypeReference<>() {});
        assertNotNull(res);
        assertEquals("404", res.getCode());
        assertTrue(res.getMessage().contains("未找到对应服务路由或资源不存在"));
    }

    @Test
    @DisplayName("503 目标服务离线或连接拒绝：统一包装为 Result.error(503) 标准 JSON")
    void testHandle_ConnectException() throws Exception {
        MockServerHttpRequest request = MockServerHttpRequest.get("/am-finance/api/v1/ledger").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        ConnectException connectEx = new ConnectException("Connection refused: alex-finance-dev:30008");

        exceptionHandler.handle(exchange, connectEx).block();

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exchange.getResponse().getStatusCode());
        String json = extractResponseBody(exchange);
        Result<Void> res = objectMapper.readValue(json, new TypeReference<>() {});
        assertNotNull(res);
        assertEquals("503", res.getCode());
        assertTrue(res.getMessage().contains("目标微服务不可用或网络不可达"));
    }

    @Test
    @DisplayName("504 下游调用超时：统一包装为 Result.error(504) 标准 JSON")
    void testHandle_TimeoutException() throws Exception {
        MockServerHttpRequest request = MockServerHttpRequest.get("/am-ai/api/v1/chat/stream").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        TimeoutException timeoutEx = new TimeoutException("Response timeout after 180s");

        exceptionHandler.handle(exchange, timeoutEx).block();

        assertEquals(HttpStatus.GATEWAY_TIMEOUT, exchange.getResponse().getStatusCode());
        String json = extractResponseBody(exchange);
        Result<Void> res = objectMapper.readValue(json, new TypeReference<>() {});
        assertNotNull(res);
        assertEquals("504", res.getCode());
        assertTrue(res.getMessage().contains("下游微服务响应超时"));
    }

    @Test
    @DisplayName("未知内部异常：统一包装为 Result.error(500) 标准兜底 JSON")
    void testHandle_UnknownRuntimeException() throws Exception {
        MockServerHttpRequest request = MockServerHttpRequest.get("/am-user/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        RuntimeException runtimeEx = new RuntimeException("Unexpected gateway memory glitch");

        exceptionHandler.handle(exchange, runtimeEx).block();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exchange.getResponse().getStatusCode());
        String json = extractResponseBody(exchange);
        Result<Void> res = objectMapper.readValue(json, new TypeReference<>() {});
        assertNotNull(res);
        assertEquals("500", res.getCode());
        assertTrue(res.getMessage().contains("网关内部处理异常"));
    }

    @Test
    @DisplayName("协议协商：客户端传 X-Crypto-Version: 2.0 时错误响应头自动回显")
    void testHandle_CryptoVersionHeaderEcho() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/am-user/not-found")
                .header(EncryptionUtils.HEADER_CRYPTO_VERSION, EncryptionUtils.VERSION_2_0)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Path not found");

        exceptionHandler.handle(exchange, ex).block();

        assertEquals(EncryptionUtils.VERSION_2_0,
                exchange.getResponse().getHeaders().getFirst(EncryptionUtils.HEADER_CRYPTO_VERSION));
    }
}
