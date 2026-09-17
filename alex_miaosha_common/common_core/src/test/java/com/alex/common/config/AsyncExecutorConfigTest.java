package com.alex.common.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 验证通用异步线程池配置治理：
 * 1. 队列容量从 2 扩容至 200
 * 2. 拒绝策略为 CallerRunsPolicy（防止突发任务被抛弃）
 * 3. 线程前缀为 "common-async-"
 */
class AsyncExecutorConfigTest {

    @Test
    @DisplayName("验证 AsyncExecutorConfig 线程池参数治理效果")
    void testExecutorConfiguration() {
        AsyncExecutorConfig config = new AsyncExecutorConfig();
        assertEquals(200, config.getQueueCapacity(), "队列容量必须调整为 200");
        assertEquals("common-async-", config.getPreFix(), "线程名前缀应为 common-async-");
        assertEquals(5, config.getCorePoolSize());
        assertEquals(10, config.getMaxPoolSize());

        Executor executor = config.myExecutor();
        assertTrue(executor instanceof ThreadPoolTaskExecutor, "返回实例必须是 ThreadPoolTaskExecutor");

        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;
        assertEquals(5, taskExecutor.getCorePoolSize());
        assertEquals(10, taskExecutor.getMaxPoolSize());
        assertEquals(200, taskExecutor.getQueueCapacity());
        assertEquals("common-async-", taskExecutor.getThreadNamePrefix());

        ThreadPoolExecutor threadPoolExecutor = taskExecutor.getThreadPoolExecutor();
        assertTrue(threadPoolExecutor.getRejectedExecutionHandler() instanceof ThreadPoolExecutor.CallerRunsPolicy,
                "拒绝策略必须是 CallerRunsPolicy，以保任务不丢失");

        taskExecutor.shutdown();
    }
}
