package com.alex.gateway.config;

import com.alex.gateway.utils.EncryptionUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Flux;
import springfox.documentation.swagger.web.SwaggerResource;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 集成测试：网关 CORS 跨域暴露头协议契约与 Swagger 聚合文档资源完整性
 */
class GatewayCorsAndSwaggerContractIT {

    @Test
    @DisplayName("CORS 配置契约：必须暴露 X-Crypto-Version 与 Content-Disposition 等关键头，并启用凭证与 18000s 预检缓存")
    void corsConfiguration_mustExposeRequiredHeaders() throws Exception {
        GlobalCorsConfig config = new GlobalCorsConfig();
        CorsWebFilter corsWebFilter = config.corsWebFilter();
        assertNotNull(corsWebFilter);

        // 通过反射提取内部 UrlBasedCorsConfigurationSource 验证 CORS 规约
        Field sourceField = CorsWebFilter.class.getDeclaredField("configSource");
        sourceField.setAccessible(true);
        UrlBasedCorsConfigurationSource source = (UrlBasedCorsConfigurationSource) sourceField.get(corsWebFilter);

        MockServerHttpRequest request = MockServerHttpRequest.get("/am-user/test")
                .header(HttpHeaders.ORIGIN, "http://localhost:8080")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        CorsConfiguration corsConfig = source.getCorsConfiguration(exchange);
        assertNotNull(corsConfig);

        // 验证凭据与缓存
        assertTrue(Boolean.TRUE.equals(corsConfig.getAllowCredentials()), "必须允许携带 cookies/凭证跨域");
        assertEquals(18000L, corsConfig.getMaxAge(), "预检请求缓存时间必须为 18000s");

        // 验证暴露头
        List<String> exposedHeaders = corsConfig.getExposedHeaders();
        assertNotNull(exposedHeaders);
        assertTrue(exposedHeaders.contains(EncryptionUtils.HEADER_CRYPTO_VERSION), "必须暴露 X-Crypto-Version 加密协议协商头");
        assertTrue(exposedHeaders.contains(HttpHeaders.CONTENT_DISPOSITION), "必须暴露 Content-Disposition 供前端解析文件下载名");
        assertTrue(exposedHeaders.contains(HttpHeaders.AUTHORIZATION), "必须暴露 Authorization 响应头");
        assertTrue(exposedHeaders.contains("token"), "必须暴露自定义 token 响应头");
    }

    @Test
    @DisplayName("Swagger 资源生成：基于 RouteLocator 与 GatewayProperties 聚合各微服务端点并生成 v3/api-docs 路径")
    void swaggerResourceConfig_generatesAggregatedApiDocs() {
        RouteLocator mockRouteLocator = Mockito.mock(RouteLocator.class);
        GatewayProperties gatewayProperties = new GatewayProperties();

        // 构造两个微服务路由定义
        RouteDefinition userRoute = new RouteDefinition();
        userRoute.setId("alex-user");
        userRoute.setUri(URI.create("lb://alex-user-dev"));
        PredicateDefinition userPathPredicate = new PredicateDefinition("Path=/am-user/**");
        userRoute.setPredicates(List.of(userPathPredicate));

        RouteDefinition financeRoute = new RouteDefinition();
        financeRoute.setId("alex-finance");
        financeRoute.setUri(URI.create("lb://alex-finance-dev"));
        PredicateDefinition financePathPredicate = new PredicateDefinition("Path=/am-finance/**");
        financeRoute.setPredicates(List.of(financePathPredicate));

        gatewayProperties.setRoutes(List.of(userRoute, financeRoute));

        // 构造 RouteLocator 返回对应的 Route 实例
        Route r1 = Route.async().id("alex-user").uri(URI.create("lb://alex-user-dev")).predicate(swe -> true).build();
        Route r2 = Route.async().id("alex-finance").uri(URI.create("lb://alex-finance-dev")).predicate(swe -> true).build();
        Mockito.when(mockRouteLocator.getRoutes()).thenReturn(Flux.just(r1, r2));

        SwaggerResourceConfig swaggerResourceConfig = new SwaggerResourceConfig(mockRouteLocator, gatewayProperties);
        List<SwaggerResource> resources = swaggerResourceConfig.get();

        assertNotNull(resources);
        assertEquals(2, resources.size(), "必须生成 2 个微服务的 Swagger 文档资源");

        SwaggerResource userRes = resources.stream().filter(r -> "alex-user".equals(r.getName())).findFirst().orElse(null);
        assertNotNull(userRes);
        assertEquals("/am-user/v3/api-docs?group=alex-user", userRes.getLocation());
        assertEquals("3.0", userRes.getSwaggerVersion());

        SwaggerResource financeRes = resources.stream().filter(r -> "alex-finance".equals(r.getName())).findFirst().orElse(null);
        assertNotNull(financeRes);
        assertEquals("/am-finance/v3/api-docs?group=alex-finance", financeRes.getLocation());
    }

    @Test
    @DisplayName("Swagger 资源生成：当 RouteLocator 抛异常时平滑降级使用 GatewayProperties 预设路由")
    void swaggerResourceConfig_degradesGracefullyOnRouteLocatorException() {
        RouteLocator mockRouteLocator = Mockito.mock(RouteLocator.class);
        GatewayProperties gatewayProperties = new GatewayProperties();

        RouteDefinition aiRoute = new RouteDefinition();
        aiRoute.setId("alex-ai");
        aiRoute.setUri(URI.create("lb://alex-ai-dev"));
        PredicateDefinition aiPathPredicate = new PredicateDefinition("Path=/am-ai/**");
        aiRoute.setPredicates(List.of(aiPathPredicate));
        gatewayProperties.setRoutes(List.of(aiRoute));

        // 模拟 RouteLocator 响应式流抛出异常
        Mockito.when(mockRouteLocator.getRoutes()).thenReturn(Flux.error(new RuntimeException("Nacos Discovery route refresh failure")));

        SwaggerResourceConfig swaggerResourceConfig = new SwaggerResourceConfig(mockRouteLocator, gatewayProperties);
        List<SwaggerResource> resources = swaggerResourceConfig.get();

        assertNotNull(resources);
        assertEquals(1, resources.size(), "异常降级后仍需正确生成预设的 alex-ai 文档资源");
        assertEquals("alex-ai", resources.get(0).getName());
        assertEquals("/am-ai/v3/api-docs?group=alex-ai", resources.get(0).getLocation());
    }
}
