package com.alex.common.config;

import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("微服务间 Feign 链路追踪 TraceId 与请求头透传集成测试")
class FeignTraceIdAndHeaderPropagationIT {

    private FeignConfig feignConfig;

    @BeforeEach
    void setUp() {
        feignConfig = new FeignConfig();
        RequestContextHolder.resetRequestAttributes();
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        MDC.clear();
    }

    @Test
    @DisplayName("1. Servlet Web 线程存在时，完整保真透传全部关键请求头")
    void apply_whenHttpServletRequestContainsHeaders_faithfullyPropagatesAll() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(FeignConfig.HEADER_AUTHORIZATION, "Bearer test-jwt-token-123456");
        request.addHeader(FeignConfig.HEADER_TRACE_ID, "trace-abc-1234567890abcdef");
        request.addHeader(FeignConfig.HEADER_REQUEST_ID, "req-xyz-987654321");
        request.addHeader(FeignConfig.HEADER_GRAY_TAG, "gray");
        request.addHeader(FeignConfig.HEADER_ROUTE_TAG, "v2.5.0");

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        RequestTemplate template = new RequestTemplate();
        template.method(Request.HttpMethod.GET);
        template.target("http://alex-user-dev");

        feignConfig.apply(template);

        Map<String, Collection<String>> headers = template.headers();
        assertTrue(headers.containsKey(FeignConfig.HEADER_AUTHORIZATION));
        assertEquals("Bearer test-jwt-token-123456", headers.get(FeignConfig.HEADER_AUTHORIZATION).iterator().next());

        assertTrue(headers.containsKey(FeignConfig.HEADER_TRACE_ID));
        assertEquals("trace-abc-1234567890abcdef", headers.get(FeignConfig.HEADER_TRACE_ID).iterator().next());

        assertTrue(headers.containsKey(FeignConfig.HEADER_REQUEST_ID));
        assertEquals("req-xyz-987654321", headers.get(FeignConfig.HEADER_REQUEST_ID).iterator().next());

        assertTrue(headers.containsKey(FeignConfig.HEADER_GRAY_TAG));
        assertEquals("gray", headers.get(FeignConfig.HEADER_GRAY_TAG).iterator().next());

        assertTrue(headers.containsKey(FeignConfig.HEADER_ROUTE_TAG));
        assertEquals("v2.5.0", headers.get(FeignConfig.HEADER_ROUTE_TAG).iterator().next());
    }

    @Test
    @DisplayName("2. 异步线程池/定时任务（无 Servlet 上下文）下，优先从 SLF4J MDC 提取 TraceId")
    void apply_whenInAsyncThreadWithoutServletRequest_extractsFromMdc() {
        assertNull(RequestContextHolder.getRequestAttributes());

        MDC.put(FeignConfig.MDC_KEY_TRACE_ID, "mdc-async-trace-00112233");

        RequestTemplate template = new RequestTemplate();
        template.method(Request.HttpMethod.POST);
        template.target("http://alex-finance-dev");

        feignConfig.apply(template);

        Map<String, Collection<String>> headers = template.headers();
        assertTrue(headers.containsKey(FeignConfig.HEADER_TRACE_ID));
        assertEquals("mdc-async-trace-00112233", headers.get(FeignConfig.HEADER_TRACE_ID).iterator().next());
        assertFalse(headers.containsKey(FeignConfig.HEADER_AUTHORIZATION));
    }

    @Test
    @DisplayName("3. MDC 与 Servlet 上下文皆为空时，自动生成 32 位 Hex TraceId 保底闭环")
    void apply_whenBothServletAndMdcEmpty_generates32HexTraceId() {
        assertNull(RequestContextHolder.getRequestAttributes());
        assertTrue(MDC.getCopyOfContextMap() == null || MDC.getCopyOfContextMap().isEmpty());

        RequestTemplate template = new RequestTemplate();
        template.method(Request.HttpMethod.GET);
        template.target("http://alex-ai-dev");

        feignConfig.apply(template);

        Map<String, Collection<String>> headers = template.headers();
        assertTrue(headers.containsKey(FeignConfig.HEADER_TRACE_ID));

        String generatedTraceId = headers.get(FeignConfig.HEADER_TRACE_ID).iterator().next();
        assertNotNull(generatedTraceId);
        assertEquals(32, generatedTraceId.length());
        assertTrue(generatedTraceId.matches("^[0-9a-fA-F]{32}$"));
    }

    @Test
    @DisplayName("4. 仅携带部分头时，不注入未定义的空头，保障报文整洁")
    void apply_whenOnlyAuthorizationPresent_onlyInjectsAuthAndGeneratedTraceId() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(FeignConfig.HEADER_AUTHORIZATION, "Bearer only-token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        RequestTemplate template = new RequestTemplate();
        template.method(Request.HttpMethod.GET);
        template.target("http://alex-oss-dev");

        feignConfig.apply(template);

        Map<String, Collection<String>> headers = template.headers();
        assertTrue(headers.containsKey(FeignConfig.HEADER_AUTHORIZATION));
        assertTrue(headers.containsKey(FeignConfig.HEADER_TRACE_ID));
        assertFalse(headers.containsKey(FeignConfig.HEADER_REQUEST_ID));
        assertFalse(headers.containsKey(FeignConfig.HEADER_GRAY_TAG));
        assertFalse(headers.containsKey(FeignConfig.HEADER_ROUTE_TAG));
    }
}
