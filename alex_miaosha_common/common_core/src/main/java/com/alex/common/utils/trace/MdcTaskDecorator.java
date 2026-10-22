package com.alex.common.utils.trace;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一微服务异步线程池 MDC 与 TraceId 链路透传装饰器
 * 1. 任务提交时：在父线程捕获当前 TraceId 与 MDC 诊断上下文快照；
 * 2. 任务执行前：在子工作线程回填 TraceId 与 MDC 上下文，保证日志与下游调用链路不断链；
 * 3. 任务执行后：在子线程退出时彻底清理 TraceIdContext 与 MDC，防止线程池复用污染。
 *
 * @author alex
 * @version 1.0.0
 */
public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        String parentTraceId = TraceIdContext.getTraceId();
        Map<String, String> parentMdcContext = MDC.getCopyOfContextMap();

        return () -> {
            try {
                if (parentTraceId != null && !parentTraceId.trim().isEmpty()) {
                    TraceIdContext.setTraceId(parentTraceId);
                } else if (parentMdcContext != null && parentMdcContext.containsKey(TraceIdContext.MDC_TRACE_ID_KEY)) {
                    TraceIdContext.setTraceId(parentMdcContext.get(TraceIdContext.MDC_TRACE_ID_KEY));
                }

                if (parentMdcContext != null) {
                    Map<String, String> mdcToSet = new HashMap<>(parentMdcContext);
                    String currentTraceId = TraceIdContext.getTraceId();
                    if (currentTraceId != null) {
                        mdcToSet.put(TraceIdContext.MDC_TRACE_ID_KEY, currentTraceId);
                        mdcToSet.put(TraceIdContext.MDC_X_TRACE_ID_KEY, currentTraceId);
                    }
                    MDC.setContextMap(mdcToSet);
                } else {
                    String currentTraceId = TraceIdContext.getTraceId();
                    if (currentTraceId != null) {
                        MDC.put(TraceIdContext.MDC_TRACE_ID_KEY, currentTraceId);
                        MDC.put(TraceIdContext.MDC_X_TRACE_ID_KEY, currentTraceId);
                    }
                }
                runnable.run();
            } finally {
                TraceIdContext.clear();
                try {
                    MDC.clear();
                } catch (Throwable ignored) {
                }
            }
        };
    }
}
