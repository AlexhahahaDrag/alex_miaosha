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
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 集成测试：网关过滤器认证短路优化、有效性校验与下游超时安全降级契约
 */
class GatewayFilterAuthOptimizationIT {

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
    @DisplayName("空 Token 请求：前置短路返回 403，零远程 RPC 调用")
    void missingToken_shouldShortCircuitWithoutRpc() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/am-finance/api/v1/ledger").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicBoolean chainExecuted = new AtomicBoolean(false);
        gatewayFilter.filter(exchange, ex -> {
            chainExecuted.set(true);
            return Mono.empty();
        }).block();

        assertFalse(chainExecuted.get(), "缺少 Token 的请求绝不能放行");
        // 关键断言：绝对不触发远程 UserApi.authToken RPC
        Mockito.verifyNoInteractions(mockUserApi);
    }

    @Test
    @DisplayName("纯空白字符串 Token：前置短路返回 403，零远程 RPC 调用")
    void blankToken_shouldShortCircuitWithoutRpc() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/am-finance/api/v1/ledger")
                .header("Authorization", "   ")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicBoolean chainExecuted = new AtomicBoolean(false);
        gatewayFilter.filter(exchange, ex -> {
            chainExecuted.set(true);
            return Mono.empty();
        }).block();

        assertFalse(chainExecuted.get(), "空白 Token 请求绝不能放行");
        Mockito.verifyNoInteractions(mockUserApi);
    }

    @Test
    @DisplayName("有效 Token 认证成功：正常放行并执行下游 FilterChain")
    void validToken_authenticationSuccess_shouldPass() {
        String validToken = "valid-jwt-token-alex-123456";
        Mockito.when(mockUserApi.authToken(validToken)).thenReturn(Result.success(true));

        MockServerHttpRequest request = MockServerHttpRequest.get("/am-user/api/v1/user/info")
                .header("Authorization", validToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicBoolean chainExecuted = new AtomicBoolean(false);
        gatewayFilter.filter(exchange, ex -> {
            chainExecuted.set(true);
            return Mono.empty();
        }).block();

        assertTrue(chainExecuted.get(), "认证通过的请求必须成功放行");
        Mockito.verify(mockUserApi, Mockito.times(1)).authToken(validToken);
    }

    @Test
    @DisplayName("有效 Token 认证失败：安全拦截返回 403，不执行下游链路")
    void validToken_authenticationFailed_shouldBlock() {
        String invalidToken = "invalid-jwt-token-expired";
        Mockito.when(mockUserApi.authToken(invalidToken)).thenReturn(Result.error("403", "Token已失效"));

        MockServerHttpRequest request = MockServerHttpRequest.get("/am-user/api/v1/user/info")
                .header("Authorization", invalidToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicBoolean chainExecuted = new AtomicBoolean(false);
        gatewayFilter.filter(exchange, ex -> {
            chainExecuted.set(true);
            return Mono.empty();
        }).block();

        assertFalse(chainExecuted.get(), "认证失败的请求绝不能放行");
        Mockito.verify(mockUserApi, Mockito.times(1)).authToken(invalidToken);
    }

    @Test
    @DisplayName("下游认证服务异常：安全降级返回 403，网关不抛出未经处理的 RuntimeException")
    void userApiThrowsException_shouldDegradeGracefully() {
        String token = "any-token-triggers-exception";
        Mockito.when(mockUserApi.authToken(token)).thenThrow(new RuntimeException("User service feign timeout"));

        MockServerHttpRequest request = MockServerHttpRequest.get("/am-finance/api/v1/ledger")
                .header("Authorization", token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicBoolean chainExecuted = new AtomicBoolean(false);
        assertDoesNotThrow(() -> gatewayFilter.filter(exchange, ex -> {
            chainExecuted.set(true);
            return Mono.empty();
        }).block());

        assertFalse(chainExecuted.get(), "服务异常时必须安全降级阻断，不放行");
    }
}
