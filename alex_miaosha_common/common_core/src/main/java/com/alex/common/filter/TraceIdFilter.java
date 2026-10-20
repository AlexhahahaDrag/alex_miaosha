package com.alex.common.filter;

import com.alex.common.utils.trace.TraceIdContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 微服务全局链路追踪 Servlet 过滤器
 * 优先级设置为 HIGHEST_PRECEDENCE + 5，紧随底层容器过滤器之后。
 * 负责从 HTTP 请求头提取 X-Trace-Id / X-Request-Id，注入 TraceIdContext 与 MDC，
 * 并在 HttpServletResponse 响应头中回显 X-Trace-Id，请求完成后可靠执行清理。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
@Slf4j
public class TraceIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String traceId = request.getHeader(TraceIdContext.TRACE_ID_HEADER);
        if (traceId == null || traceId.trim().isEmpty()) {
            traceId = request.getHeader(TraceIdContext.REQUEST_ID_HEADER);
        }
        if (traceId == null || traceId.trim().isEmpty()) {
            traceId = TraceIdContext.generateTraceId();
        }

        TraceIdContext.setTraceId(traceId);
        response.setHeader(TraceIdContext.TRACE_ID_HEADER, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            TraceIdContext.clear();
        }
    }
}
