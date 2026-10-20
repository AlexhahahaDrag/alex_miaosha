package com.alex.common.utils.trace;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * 全局微服务链路追踪上下文 (TraceIdContext)
 * 统一管理 MDC 与本地线程安全 TraceId 凭据，支撑跨服务 RPC 与异步任务的保真透传
 */
public final class TraceIdContext {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String MDC_TRACE_ID_KEY = "traceId";
    public static final String MDC_X_TRACE_ID_KEY = "X-Trace-Id";

    private static final ThreadLocal<String> TRACE_ID_HOLDER = new ThreadLocal<>();

    private TraceIdContext() {
    }

    /**
     * 获取当前线程 TraceId，优先 ThreadLocal，次选 MDC
     */
    public static String getTraceId() {
        String traceId = TRACE_ID_HOLDER.get();
        if (traceId == null || traceId.trim().isEmpty()) {
            traceId = MDC.get(MDC_TRACE_ID_KEY);
        }
        if (traceId == null || traceId.trim().isEmpty()) {
            traceId = MDC.get(MDC_X_TRACE_ID_KEY);
        }
        return traceId;
    }

    /**
     * 设置当前线程 TraceId 并同步至 MDC
     */
    public static void setTraceId(String traceId) {
        if (traceId == null || traceId.trim().isEmpty()) {
            traceId = generateTraceId();
        }
        TRACE_ID_HOLDER.set(traceId);
        try {
            MDC.put(MDC_TRACE_ID_KEY, traceId);
            MDC.put(MDC_X_TRACE_ID_KEY, traceId);
        } catch (Throwable ignored) {
        }
    }

    /**
     * 清理当前线程 TraceId 及 MDC，杜绝线程池复用污染
     */
    public static void clear() {
        TRACE_ID_HOLDER.remove();
        try {
            MDC.remove(MDC_TRACE_ID_KEY);
            MDC.remove(MDC_X_TRACE_ID_KEY);
        } catch (Throwable ignored) {
        }
    }

    /**
     * 生成规范 32 位 Hex UUID TraceId
     */
    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
