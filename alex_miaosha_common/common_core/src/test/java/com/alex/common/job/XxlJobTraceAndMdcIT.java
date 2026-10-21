package com.alex.common.job;

import com.alex.common.job.alarm.LoggingJobAlarmNotifier;
import com.alex.common.job.trace.XxlJobTraceAspect;
import com.alex.common.utils.trace.TraceIdContext;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("XXL-Job 全链路 TraceId 自动染色与 MDC 贯通集成测试")
class XxlJobTraceAndMdcIT {

    static class SampleJobService {
        private final AtomicReference<String> capturedTraceIdInJob = new AtomicReference<>();
        private final AtomicReference<String> capturedMdcInJob = new AtomicReference<>();

        @XxlJob("sampleTaskHandler")
        public void executeSampleTask() {
            capturedTraceIdInJob.set(TraceIdContext.getTraceId());
            capturedMdcInJob.set(MDC.get(TraceIdContext.MDC_TRACE_ID_KEY));
        }

        public String getCapturedTraceIdInJob() {
            return capturedTraceIdInJob.get();
        }

        public String getCapturedMdcInJob() {
            return capturedMdcInJob.get();
        }
    }

    private SampleJobService proxiedJobService;

    @BeforeEach
    void setUp() {
        TraceIdContext.clear();
        SampleJobService target = new SampleJobService();
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        XxlJobTraceAspect aspect = new XxlJobTraceAspect(new LoggingJobAlarmNotifier());
        factory.addAspect(aspect);
        proxiedJobService = factory.getProxy();
    }

    @AfterEach
    void tearDown() {
        TraceIdContext.clear();
    }

    @Test
    @DisplayName("1. @XxlJob 任务执行期间自动染色 job-* TraceId 并写入 MDC")
    void testTraceIdInjectedDuringJobExecution() {
        // 执行前检查：无 TraceId 上下文
        assertNull(TraceIdContext.getTraceId(), "任务执行前 TraceId 上下文应为空");
        assertNull(MDC.get(TraceIdContext.MDC_TRACE_ID_KEY), "任务执行前 MDC 应为空");

        // 执行任务
        proxiedJobService.executeSampleTask();

        // 验证任务内部捕获的 TraceId
        String capturedTraceId = proxiedJobService.getCapturedTraceIdInJob();
        assertNotNull(capturedTraceId, "任务内部必须成功获取到 TraceId");
        assertTrue(capturedTraceId.startsWith("job-sampleTaskHandler-"),
                "TraceId 格式应以 job-{handler}- 开头，实际为: " + capturedTraceId);

        // 验证 MDC 染色
        String capturedMdc = proxiedJobService.getCapturedMdcInJob();
        assertEquals(capturedTraceId, capturedMdc, "MDC 中记录的 TraceId 应与上下文完全一致");
    }

    @Test
    @DisplayName("2. 任务执行完成后 finally 强制清理 TraceId 与 MDC 防线程池污染")
    void testTraceIdContextClearedAfterJobCompletion() {
        proxiedJobService.executeSampleTask();

        // 执行后检查：必须被清理，杜绝线程池污染
        assertNull(TraceIdContext.getTraceId(), "任务完成后 TraceIdContext 必须已被清空");
        assertNull(MDC.get(TraceIdContext.MDC_TRACE_ID_KEY), "任务完成后 MDC_TRACE_ID_KEY 必须已被清空");
        assertNull(MDC.get(TraceIdContext.MDC_X_TRACE_ID_KEY), "任务完成后 MDC_X_TRACE_ID_KEY 必须已被清空");
    }
}
