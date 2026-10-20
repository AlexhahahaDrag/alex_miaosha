package com.alex.common.filter;

import com.alex.base.common.Result;
import com.alex.common.exception.SystemException;
import com.alex.common.exception.handler.GlobalExceptionHandler;
import com.alex.common.utils.trace.TraceIdContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("微服务链路追踪 TraceIdContext 与 TraceIdFilter 单元测试")
class TraceIdPropagationAndContextTest {

    private TraceIdFilter traceIdFilter;
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        traceIdFilter = new TraceIdFilter();
        exceptionHandler = new GlobalExceptionHandler();
        TraceIdContext.clear();
    }

    @AfterEach
    void tearDown() {
        TraceIdContext.clear();
    }

    @Test
    @DisplayName("1. TraceIdContext 设置与清理保障 MDC 联动与无污染")
    void testTraceIdContextLifecycle() {
        assertNull(TraceIdContext.getTraceId());
        assertNull(MDC.get(TraceIdContext.MDC_TRACE_ID_KEY));

        String customTraceId = "test-trace-id-1234567890abcdef";
        TraceIdContext.setTraceId(customTraceId);

        assertEquals(customTraceId, TraceIdContext.getTraceId());
        assertEquals(customTraceId, MDC.get(TraceIdContext.MDC_TRACE_ID_KEY));
        assertEquals(customTraceId, MDC.get(TraceIdContext.MDC_X_TRACE_ID_KEY));

        TraceIdContext.clear();

        assertNull(TraceIdContext.getTraceId());
        assertNull(MDC.get(TraceIdContext.MDC_TRACE_ID_KEY));
        assertNull(MDC.get(TraceIdContext.MDC_X_TRACE_ID_KEY));
    }

    @Test
    @DisplayName("2. 请求携带 X-Trace-Id 时，Filter 保真透传并回写至响应头")
    void testFilterWithIncomingTraceId() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TraceIdContext.TRACE_ID_HEADER, "incoming-trace-id-8888");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        traceIdFilter.doFilter(request, response, chain);

        assertEquals("incoming-trace-id-8888", response.getHeader(TraceIdContext.TRACE_ID_HEADER));
        // 请求结束后上下文应已清理
        assertNull(TraceIdContext.getTraceId());
        assertNull(MDC.get(TraceIdContext.MDC_TRACE_ID_KEY));
    }

    @Test
    @DisplayName("3. 请求缺失 X-Trace-Id 时，Filter 自动生成 32 位 Hex TraceId 并回写")
    void testFilterWithoutIncomingTraceId() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        traceIdFilter.doFilter(request, response, chain);

        String echoedTraceId = response.getHeader(TraceIdContext.TRACE_ID_HEADER);
        assertNotNull(echoedTraceId);
        assertEquals(32, echoedTraceId.length());
        assertFalse(echoedTraceId.contains("-"));
        // 请求结束后上下文已清理
        assertNull(TraceIdContext.getTraceId());
    }

    @Test
    @DisplayName("4. GlobalExceptionHandler 捕获异常时，统一回写当前 traceId")
    void testGlobalExceptionHandlerAttachesTraceId() {
        String activeTraceId = "error-trace-uuid-99999";
        TraceIdContext.setTraceId(activeTraceId);

        SystemException systemException = new SystemException("500", "测试业务错误");
        Result<String> result = exceptionHandler.handle(systemException);

        assertNotNull(result);
        assertEquals("500", result.getCode());
        assertEquals("测试业务错误", result.getMessage());
        assertEquals(activeTraceId, result.getTraceId());

        TraceIdContext.clear();
    }
}
