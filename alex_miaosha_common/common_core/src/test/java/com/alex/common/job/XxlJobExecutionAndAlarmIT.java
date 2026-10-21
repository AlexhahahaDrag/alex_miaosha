package com.alex.common.job;

import com.alex.common.job.alarm.JobAlarmMessage;
import com.alex.common.job.alarm.JobAlarmNotifier;
import com.alex.common.job.trace.XxlJobTraceAspect;
import com.alex.common.utils.trace.TraceIdContext;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("XXL-Job 统一执行环绕、异常拦截与失败告警集成测试")
class XxlJobExecutionAndAlarmIT {

    static class JobTargetService {

        @XxlJob("successJobHandler")
        public String executeSuccessJob() {
            return "SUCCESS_RESULT";
        }

        @XxlJob("failingJobHandler")
        public void executeFailingJob() {
            throw new IllegalStateException("模拟业务处理致命故障");
        }
    }

    private JobTargetService proxiedService;
    private AtomicReference<JobAlarmMessage> receivedAlarmMessage;

    @BeforeEach
    void setUp() {
        TraceIdContext.clear();
        receivedAlarmMessage = new AtomicReference<>();

        JobAlarmNotifier mockAlarmNotifier = message -> receivedAlarmMessage.set(message);

        JobTargetService target = new JobTargetService();
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        XxlJobTraceAspect aspect = new XxlJobTraceAspect(mockAlarmNotifier);
        factory.addAspect(aspect);
        proxiedService = factory.getProxy();
    }

    @AfterEach
    void tearDown() {
        TraceIdContext.clear();
    }

    @Test
    @DisplayName("1. 正常任务执行顺利返回且清理 Trace 上下文")
    void testNormalExecutionSuccess() {
        String result = proxiedService.executeSuccessJob();
        assertEquals("SUCCESS_RESULT", result);
        assertNull(receivedAlarmMessage.get(), "正常执行时不应触发告警");
        assertNull(TraceIdContext.getTraceId(), "正常执行后必须清空 TraceId 上下文");
    }

    @Test
    @DisplayName("2. 任务抛出异常时自动触发告警通知并携带完整元数据")
    void testFailingJobTriggersAlarm() {
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            proxiedService.executeFailingJob();
        });

        assertEquals("模拟业务处理致命故障", thrown.getMessage());

        JobAlarmMessage alarm = receivedAlarmMessage.get();
        assertNotNull(alarm, "任务失败时必须触发告警通知");
        assertEquals("failingJobHandler", alarm.getJobHandler(), "告警消息应准确携带 jobHandler");
        assertEquals("模拟业务处理致命故障", alarm.getErrorMessage(), "告警消息应携带正确的异常信息");
        assertNotNull(alarm.getTraceId(), "告警消息应携带对应的 TraceId");
        assertTrue(alarm.getTraceId().startsWith("job-failingJobHandler-"));
        assertNotNull(alarm.getOccurTime(), "告警消息应包含发生时间戳");
        assertTrue(alarm.getErrorStack().contains("IllegalStateException"), "堆栈摘要应包含异常类名");

        // 异常退出后依然必须清空 TraceId
        assertNull(TraceIdContext.getTraceId(), "异常退出后 TraceIdContext 必须已被清空");
    }

    @Test
    @DisplayName("3. 告警器内部报错时不应阻断异常向上抛出")
    void testAlarmNotifierFailureRobustness() {
        JobAlarmNotifier brokenNotifier = message -> {
            throw new RuntimeException("钉钉 Webhook 网络异常");
        };

        JobTargetService target = new JobTargetService();
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        XxlJobTraceAspect aspect = new XxlJobTraceAspect(brokenNotifier);
        factory.addAspect(aspect);
        JobTargetService robustProxy = factory.getProxy();

        assertThrows(IllegalStateException.class, robustProxy::executeFailingJob);
        assertNull(TraceIdContext.getTraceId(), "即使告警器异常，上下文也必须清空");
    }
}
