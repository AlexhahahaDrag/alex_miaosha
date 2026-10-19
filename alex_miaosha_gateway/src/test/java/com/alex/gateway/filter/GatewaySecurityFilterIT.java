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
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 网关核心过滤器安全鉴权、白名单与加密旁路集成测试
 */
class GatewaySecurityFilterIT {

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
        mockApplicationContext = Mockito.mock(ApplicationContext.class);
        Mockito.when(mockApplicationContext.getBean(UserApi.class)).thenReturn(mockUserApi);

        setStaticApplicationContext(mockApplicationContext);
    }

    @AfterEach
    void tearDown() throws Exception {
        setStaticApplicationContext(null);
        com.alex.common.utils.secret.AESUtils.setStaticProperties(null);
    }

    private void setStaticApplicationContext(ApplicationContext context) throws Exception {
        Field field = AutowiredBean.class.getDeclaredField("applicationContext");
        field.setAccessible(true);
        field.set(null, context);
    }

    @Test
    @DisplayName("文档白名单：doc.html 与 swagger 路径无需鉴权直接通过 FilterChain 放行")
    void docWhiteList_shouldPassDirectlyWithoutAuthentication() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/doc.html").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicBoolean chainExecuted = new AtomicBoolean(false);
        gatewayFilter.filter(exchange, ex -> {
            chainExecuted.set(true);
            return Mono.empty();
        }).block();

        assertTrue(chainExecuted.get(), "文档白名单路径应直接调用下游 filter 放行");
        Mockito.verifyNoInteractions(mockUserApi);
    }

    @Test
    @DisplayName("未认证拦截：缺少有效 Token 时直接阻断返回 403 密文报文")
    void missingToken_shouldReturn403EncryptedJson() {
        Mockito.when(mockUserApi.authToken(Mockito.any())).thenReturn(Result.error("403", "认证失败"));

        MockServerHttpRequest request = MockServerHttpRequest.get("/am-user/api/v1/user/profile").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicBoolean chainExecuted = new AtomicBoolean(false);
        gatewayFilter.filter(exchange, ex -> {
            chainExecuted.set(true);
            return Mono.empty();
        }).block();

        assertFalse(chainExecuted.get(), "未认证请求绝不能调用下游 FilterChain");

        Flux<DataBuffer> bodyFlux = exchange.getResponse().getBody();
        DataBuffer buffer = bodyFlux.blockFirst();
        assertNotNull(buffer, "响应 Buffer 不可为空");

        byte[] bytes = new byte[buffer.readableByteCount()];
        buffer.read(bytes);

        String decrypted = encryptionUtils.decrypt(bytes);
        assertNotNull(decrypted);
        assertTrue(decrypted.contains("403"), "解密后需包含 403 状态码: " + decrypted);
        assertTrue(decrypted.contains("请先登录！"), "解密后需包含提示语: " + decrypted);
    }

    @Test
    @DisplayName("协议协商：客户端携带 X-Crypto-Version: 2.0 时响应头回显 2.0")
    void cryptoVersionNegotiation_v2_shouldEchoV2Header() {
        Mockito.when(mockUserApi.authToken(Mockito.any())).thenReturn(Result.error("403", "认证失败"));

        MockServerHttpRequest request = MockServerHttpRequest.get("/am-finance/sensitive")
                .header(EncryptionUtils.HEADER_CRYPTO_VERSION, "2.0")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        gatewayFilter.filter(exchange, ex -> Mono.empty()).block();

        assertEquals("2.0", exchange.getResponse().getHeaders().getFirst(EncryptionUtils.HEADER_CRYPTO_VERSION));
    }

    @Test
    @DisplayName("加密旁路策略：SSE 流式响应与大文件下载路径必须豁免加密")
    void sseStream_and_binaryFile_shouldSkipResponseEncryption() throws Exception {
        Method method = GatewayFilter.class.getDeclaredMethod("shouldSkipResponseEncryption", ServerHttpResponse.class, String.class);
        method.setAccessible(true);

        // 1. SSE Content-Type
        MockServerWebExchange exchangeSse = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
        exchangeSse.getResponse().getHeaders().setContentType(MediaType.TEXT_EVENT_STREAM);
        boolean skipSseContentType = (boolean) method.invoke(gatewayFilter, exchangeSse.getResponse(), "/test");
        assertTrue(skipSseContentType, "text/event-stream 必须跳过加密");

        // 2. SSE URL 路径模式
        MockServerWebExchange exchangeAi = MockServerWebExchange.from(MockServerHttpRequest.get("/am-ai/api/v1/ai/chat/stream").build());
        boolean skipAiPath = (boolean) method.invoke(gatewayFilter, exchangeAi.getResponse(), "/am-ai/api/v1/ai/chat/stream");
        assertTrue(skipAiPath, "/chat/stream 路径必须跳过加密");

        // 3. Excel 导出二进制 Content-Type
        MockServerWebExchange exchangeExcel = MockServerWebExchange.from(MockServerHttpRequest.get("/export").build());
        exchangeExcel.getResponse().getHeaders().set("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        boolean skipExcel = (boolean) method.invoke(gatewayFilter, exchangeExcel.getResponse(), "/export");
        assertTrue(skipExcel, "Excel 导出必须跳过加密");

        // 4. 普通 JSON 响应不可豁免加密
        MockServerWebExchange exchangeJson = MockServerWebExchange.from(MockServerHttpRequest.get("/am-user/info").build());
        exchangeJson.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        boolean skipJson = (boolean) method.invoke(gatewayFilter, exchangeJson.getResponse(), "/am-user/info");
        assertFalse(skipJson, "普通 JSON 响应不得跳过加密");
    }
}
