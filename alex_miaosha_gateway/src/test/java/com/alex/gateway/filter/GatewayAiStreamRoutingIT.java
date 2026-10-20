package com.alex.gateway.filter;

import com.alex.api.user.userInfo.api.UserApi;
import com.alex.base.common.Result;
import com.alex.common.config.EncryptionProperties;
import com.alex.gateway.config.GatewayAudience;
import com.alex.gateway.utils.AutowiredBean;
import com.alex.gateway.utils.EncryptionUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AI 智能体与网关流式穿透集成测试 (Gateway SSE Bypass & Direct Streaming)
 */
class GatewayAiStreamRoutingIT {

    private GatewayAudience audience;
    private EncryptionUtils encryptionUtils;
    private GatewayFilter gatewayFilter;
    private UserApi mockUserApi;
    private ApplicationContext mockApplicationContext;

    @BeforeEach
    void setUp() throws Exception {
        audience = new GatewayAudience();
        audience.setTokenHeader("Authorization");
        audience.setDocWhiteList(List.of("/swagger-resources/**", "/doc.html", "/**/v3/api-docs/**"));
        audience.setWhiteList(List.of("/am-user/api/v1/user/login"));

        EncryptionProperties encryptionProperties = new EncryptionProperties();
        encryptionProperties.setEnabled(true);
        encryptionProperties.setKey("20230610HelloDog");
        encryptionProperties.setIv("1234567890123456");
        encryptionProperties.setPadding("PKCS5Padding");
        encryptionProperties.setMode("AUTO");

        encryptionUtils = new EncryptionUtils(encryptionProperties);
        com.alex.common.utils.secret.AESUtils.setStaticProperties(encryptionProperties);
        gatewayFilter = new GatewayFilter(audience, encryptionUtils);

        mockUserApi = Mockito.mock(UserApi.class);
        Mockito.when(mockUserApi.authToken(Mockito.any())).thenReturn(Result.error("403", "认证失败"));
        Mockito.when(mockUserApi.authToken(null)).thenReturn(Result.error("403", "认证失败"));

        mockApplicationContext = Mockito.mock(ApplicationContext.class);
        Mockito.when(mockApplicationContext.getBean(UserApi.class)).thenReturn(mockUserApi);

        Field appCtxField = AutowiredBean.class.getDeclaredField("applicationContext");
        appCtxField.setAccessible(true);
        appCtxField.set(null, mockApplicationContext);
    }

    @AfterEach
    void tearDown() throws Exception {
        Field appCtxField = AutowiredBean.class.getDeclaredField("applicationContext");
        appCtxField.setAccessible(true);
        appCtxField.set(null, null);
    }

    @Test
    @DisplayName("已认证用户请求 AI 流式接口：网关必须放行至下游且豁免响应加密，保持原生明文 SSE 帧")
    void authorizedAiStream_shouldForwardToChainWithoutEncryption() {
        String token = "Bearer test-jwt-token-ai-stream";
        Mockito.when(mockUserApi.authToken(token)).thenReturn(Result.success(true));

        MockServerHttpRequest request = MockServerHttpRequest.post("/am-ai/api/v1/ai/chat/stream")
                .header("Authorization", token)
                .header("Accept", "text/event-stream")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicBoolean chainExecuted = new AtomicBoolean(false);
        gatewayFilter.filter(exchange, ex -> {
            chainExecuted.set(true);
            // 模拟下游 AI 微服务返回的 SSE 响应流
            ex.getResponse().getHeaders().setContentType(MediaType.TEXT_EVENT_STREAM);
            byte[] sseFrame = "event: meta\ndata: {\"engine\":\"deepseek\",\"requestId\":\"ai-test-001\"}\n\n".getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = ex.getResponse().bufferFactory().wrap(sseFrame);
            return ex.getResponse().writeWith(Flux.just(buffer));
        }).block();

        assertTrue(chainExecuted.get(), "已认证请求必须放行给下游 AI 链路");

        // 验证响应为 SSE 格式且明文未被 AES 密文整包污染
        Flux<DataBuffer> bodyFlux = exchange.getResponse().getBody();
        DataBuffer outBuffer = bodyFlux.blockFirst();
        assertNotNull(outBuffer, "SSE 响应 Buffer 不可为空");

        byte[] outBytes = new byte[outBuffer.readableByteCount()];
        outBuffer.read(outBytes);
        String sseContent = new String(outBytes, StandardCharsets.UTF_8);

        assertTrue(sseContent.contains("event: meta"), "必须保持明文 event: meta 帧: " + sseContent);
        assertTrue(sseContent.contains("deepseek"), "必须保持明文 JSON 载荷: " + sseContent);
    }

    @Test
    @DisplayName("未认证请求 AI 流式接口：网关必须拦截并返回 403 阻断报文")
    void unauthorizedAiStream_shouldBlockWith403() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/am-ai/api/v1/ai/chat/stream")
                .header("Accept", "text/event-stream")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicBoolean chainExecuted = new AtomicBoolean(false);
        gatewayFilter.filter(exchange, ex -> {
            chainExecuted.set(true);
            return Mono.empty();
        }).block();

        assertFalse(chainExecuted.get(), "未认证请求绝不能调用下游 AI 服务");

        Flux<DataBuffer> bodyFlux = exchange.getResponse().getBody();
        DataBuffer buffer = bodyFlux.blockFirst();
        assertNotNull(buffer);

        byte[] bytes = new byte[buffer.readableByteCount()];
        buffer.read(bytes);

        String decrypted = encryptionUtils.decrypt(bytes);
        assertNotNull(decrypted);
        assertTrue(decrypted.contains("403"));
        assertTrue(decrypted.contains("请先登录！"));
    }

    @Test
    @DisplayName("泛型 SSE 路径识别：通用流式路径 (如 finance-info/ai-parse-stream) 均豁免响应加密")
    void genericSsePaths_shouldSkipResponseEncryption() throws Exception {
        Method method = GatewayFilter.class.getDeclaredMethod("shouldSkipResponseEncryption", ServerHttpResponse.class, String.class);
        method.setAccessible(true);

        MockServerWebExchange exchangeFinance = MockServerWebExchange.from(
                MockServerHttpRequest.post("/am-finance/api/v1/finance-info/ai-parse-stream").build());
        boolean skipFinance = (boolean) method.invoke(gatewayFilter, exchangeFinance.getResponse(), "/am-finance/api/v1/finance-info/ai-parse-stream");
        assertTrue(skipFinance, "财务 AI 流式解析接口必须跳过加密");

        MockServerWebExchange exchangeGeneric = MockServerWebExchange.from(
                MockServerHttpRequest.post("/am-ai/api/v1/custom/stream").build());
        boolean skipGeneric = (boolean) method.invoke(gatewayFilter, exchangeGeneric.getResponse(), "/am-ai/api/v1/custom/stream");
        assertTrue(skipGeneric, "自定义 /stream 接口必须跳过加密");
    }
}
