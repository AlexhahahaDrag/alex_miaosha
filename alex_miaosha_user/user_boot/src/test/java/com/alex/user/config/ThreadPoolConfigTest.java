package com.alex.user.config;

import com.alex.user.config.properties.ThreadPoolProperties;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 线程池配置及 MDC 传递单元测试
 *
 * @author alex
 * @version 1.0.0
 */
class ThreadPoolConfigTest {

    @Test
    void testAsyncTaskExecutorInitialization() {
        ThreadPoolProperties properties = new ThreadPoolProperties();
        AsyncTaskConfig config = new AsyncTaskConfig(properties);

        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) config.asyncTaskExecutor();
        assertNotNull(executor);
        assertEquals(properties.getAsyncTask().getCorePoolSize(), executor.getCorePoolSize());
        assertEquals(properties.getAsyncTask().getMaxPoolSize(), executor.getMaxPoolSize());
        assertEquals(properties.getAsyncTask().getQueueCapacity(), executor.getQueueCapacity());
        assertEquals(properties.getAsyncTask().getThreadNamePrefix(), executor.getThreadNamePrefix());

        executor.shutdown();
    }

    @Test
    void testOnlineUserExecutorInitialization() {
        ThreadPoolProperties properties = new ThreadPoolProperties();
        AsyncTaskConfig config = new AsyncTaskConfig(properties);

        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) config.onlineUserExecutor();
        assertNotNull(executor);
        assertEquals(properties.getOnlineUser().getCorePoolSize(), executor.getCorePoolSize());
        assertEquals(properties.getOnlineUser().getMaxPoolSize(), executor.getMaxPoolSize());
        assertEquals(properties.getOnlineUser().getQueueCapacity(), executor.getQueueCapacity());
        assertEquals(properties.getOnlineUser().getThreadNamePrefix(), executor.getThreadNamePrefix());

        executor.shutdown();
    }

    @Test
    void testTokenRefreshExecutorInitialization() {
        ThreadPoolProperties properties = new ThreadPoolProperties();
        AsyncTaskConfig config = new AsyncTaskConfig(properties);

        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) config.tokenRefreshExecutor();
        assertNotNull(executor);
        assertEquals(properties.getTokenRefresh().getCorePoolSize(), executor.getCorePoolSize());
        assertEquals(properties.getTokenRefresh().getMaxPoolSize(), executor.getMaxPoolSize());
        assertEquals(properties.getTokenRefresh().getQueueCapacity(), executor.getQueueCapacity());
        assertEquals(properties.getTokenRefresh().getThreadNamePrefix(), executor.getThreadNamePrefix());

        executor.shutdown();
    }

    @Test
    void testMdcTaskDecoratorPropagatesTraceContext() throws InterruptedException {
        MdcTaskDecorator decorator = new MdcTaskDecorator();
        String traceKey = "traceId";
        String traceVal = "test-trace-123456";
        MDC.put(traceKey, traceVal);

        AtomicReference<String> asyncTraceVal = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Runnable decoratedTask = decorator.decorate(() -> {
            asyncTraceVal.set(MDC.get(traceKey));
            latch.countDown();
        });

        Thread worker = new Thread(decoratedTask);
        worker.start();

        boolean completed = latch.await(2, TimeUnit.SECONDS);
        MDC.clear();

        assertEquals(true, completed);
        assertEquals(traceVal, asyncTraceVal.get());
    }
}
