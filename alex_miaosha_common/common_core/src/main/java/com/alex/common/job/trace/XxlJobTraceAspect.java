package com.alex.common.job.trace;

import com.alex.common.job.alarm.JobAlarmMessage;
import com.alex.common.job.alarm.JobAlarmNotifier;
import com.alex.common.utils.date.DateUtils;
import com.alex.common.utils.trace.TraceIdContext;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * XXL-Job 任务执行环绕切面
 * <p>核心职责：
 * 1. 自动生成标准 Job TraceId 并注入 {@link TraceIdContext} 与 MDC，打通定时任务全链路日志；
 * 2. 精确度量任务执行耗时；
 * 3. 任务失败自动调用 {@link XxlJobHelper#handleFail(String)} 并触发告警通知器 {@link JobAlarmNotifier}；
 * 4. {@code finally} 块中强制清空 ThreadLocal 与 MDC，彻底杜绝执行器线程池复用污染。
 */
@Aspect
@Order(-100) // 确保高优先级先建立 TraceId 上下文
@Slf4j
@RequiredArgsConstructor
public class XxlJobTraceAspect {

    private final JobAlarmNotifier jobAlarmNotifier;

    @Around("@annotation(xxlJob)")
    public Object aroundXxlJob(ProceedingJoinPoint joinPoint, XxlJob xxlJob) throws Throwable {
        String handlerName = xxlJob != null ? xxlJob.value() : "unknownJob";
        String traceId = "job-" + handlerName + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        TraceIdContext.setTraceId(traceId);

        String jobParam = null;
        try {
            jobParam = XxlJobHelper.getJobParam();
        } catch (Throwable ignored) {
            // 非调度环境可能无法获取参数，安全忽略
        }

        long startTimeNano = System.nanoTime();
        log.info(">>>>>> [XXL-JOB-START] 开始执行定时任务 handler=[{}], param=[{}]", handlerName, jobParam);

        try {
            Object result = joinPoint.proceed();
            long costMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTimeNano);
            log.info("<<<<<< [XXL-JOB-SUCCESS] 定时任务执行成功 handler=[{}], 耗时=[{}ms]", handlerName, costMs);
            try {
                XxlJobHelper.handleSuccess("执行成功，耗时: " + costMs + "ms");
            } catch (Throwable ignored) {
                // 非调度执行环境安全忽略
            }
            return result;
        } catch (Throwable ex) {
            long costMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTimeNano);
            log.error("xxxxxx [XXL-JOB-FAILED] 定时任务执行失败 handler=[{}], 耗时=[{}ms], 错误: {}",
                    handlerName, costMs, ex.getMessage(), ex);

            String errorStack = extractStackTrace(ex, 15);
            try {
                XxlJobHelper.handleFail("任务执行失败: " + ex.getMessage());
            } catch (Throwable ignored) {
                // 非调度执行环境安全忽略
            }

            // 触发失败告警通知
            triggerAlarm(handlerName, jobParam, traceId, costMs, ex.getMessage(), errorStack);

            // 向上抛出以保证上层感知
            throw ex;
        } finally {
            TraceIdContext.clear();
        }
    }

    private void triggerAlarm(String handlerName, String jobParam, String traceId, long costMs, String errorMsg, String errorStack) {
        if (jobAlarmNotifier == null) {
            return;
        }
        try {
            JobAlarmMessage message = JobAlarmMessage.builder()
                    .jobHandler(handlerName)
                    .jobParam(jobParam)
                    .traceId(traceId)
                    .costMillis(costMs)
                    .errorMessage(errorMsg)
                    .errorStack(errorStack)
                    .occurTime(DateUtils.now())
                    .build();
            jobAlarmNotifier.notifyAlarm(message);
        } catch (Throwable t) {
            log.warn("触发 XXL-Job 告警通知异常: {}", t.getMessage());
        }
    }

    private String extractStackTrace(Throwable t, int maxLines) {
        if (t == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        String[] lines = sw.toString().split("\\r?\\n");
        StringBuilder sb = new StringBuilder();
        int limit = Math.min(lines.length, maxLines);
        for (int i = 0; i < limit; i++) {
            sb.append(lines[i]).append(System.lineSeparator());
        }
        return sb.toString();
    }
}
