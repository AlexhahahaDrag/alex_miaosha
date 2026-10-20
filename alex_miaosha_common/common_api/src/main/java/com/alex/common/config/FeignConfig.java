package com.alex.common.config;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * description:  微服务间 Feign RPC 统一配置与拦截器
 * 负责链路追踪凭据（TraceId/RequestId）、灰度染色标签（GrayTag/RouteTag）、安全认证头（Authorization）的跨服务透传，
 * 以及默认超时、重试防呆与统一错误解码配置。
 * author:       majf / alex
 * createDate:   2022/12/28 11:41
 * version:      2.0.0
 */
@Configuration
@Slf4j
public class FeignConfig implements RequestInterceptor {

    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_TRACE_ID = "X-Trace-Id";
    public static final String HEADER_REQUEST_ID = "X-Request-Id";
    public static final String HEADER_GRAY_TAG = "X-Gray-Tag";
    public static final String HEADER_ROUTE_TAG = "X-Route-Tag";

    public static final String MDC_KEY_TRACE_ID = "traceId";
    public static final String MDC_KEY_X_TRACE_ID = "X-Trace-Id";

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String traceId = null;
        String requestId = null;
        String grayTag = null;
        String routeTag = null;
        String authorization = null;

        // 1. 尝试从当前 Servlet Web 上下文提取
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                authorization = request.getHeader(HEADER_AUTHORIZATION);
                traceId = request.getHeader(HEADER_TRACE_ID);
                requestId = request.getHeader(HEADER_REQUEST_ID);
                grayTag = request.getHeader(HEADER_GRAY_TAG);
                routeTag = request.getHeader(HEADER_ROUTE_TAG);
            }
        } catch (Throwable ignored) {
            // WebFlux / Non-Servlet 环境静默跳过 Servlet 上下文获取
        }

        // 2. 若 traceId 为空（如处于异步线程池、定时任务、无 HTTP 上下文），尝试从 SLF4J MDC 提取
        if (traceId == null || traceId.trim().isEmpty()) {
            try {
                traceId = MDC.get(MDC_KEY_TRACE_ID);
                if (traceId == null || traceId.trim().isEmpty()) {
                    traceId = MDC.get(MDC_KEY_X_TRACE_ID);
                }
            } catch (Throwable ignored) {
            }
        }

        // 3. 若依然无 TraceId，自动生成 32 位 Hex TraceId，实现链路保底闭环
        if (traceId == null || traceId.trim().isEmpty()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }

        // 4. 将追踪凭据与标签注入下游 Feign 请求头
        requestTemplate.header(HEADER_TRACE_ID, traceId);

        if (authorization != null && !authorization.trim().isEmpty()) {
            requestTemplate.header(HEADER_AUTHORIZATION, authorization);
        }
        if (requestId != null && !requestId.trim().isEmpty()) {
            requestTemplate.header(HEADER_REQUEST_ID, requestId);
        }
        if (grayTag != null && !grayTag.trim().isEmpty()) {
            requestTemplate.header(HEADER_GRAY_TAG, grayTag);
        }
        if (routeTag != null && !routeTag.trim().isEmpty()) {
            requestTemplate.header(HEADER_ROUTE_TAG, routeTag);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public Logger.Level level() {
        // 记录请求方法、URL、响应状态代码以及执行时间
        return Logger.Level.BASIC;
    }

    @Bean
    @ConditionalOnMissingBean
    public Request.Options options() {
        // 连接超时 5 秒，读取超时 10 秒
        return new Request.Options(5000, TimeUnit.MILLISECONDS, 10000, TimeUnit.MILLISECONDS, true);
    }

    @Bean
    @ConditionalOnMissingBean
    public Retryer retryer() {
        // 默认禁用自动重试，防止网络抖动时对非幂等记账/扣减接口造成重复调用灾难
        return Retryer.NEVER_RETRY;
    }

    @Bean
    @ConditionalOnMissingBean
    public ErrorDecoder errorDecoder() {
        // 微服务统一结构化异常解码
        return new FeignErrorDecoder();
    }

    @Bean
    @ConditionalOnClass(name = "javax.servlet.ServletRequestListener")
    @ConditionalOnMissingBean
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }
}
