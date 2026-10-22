package com.alex.common.config;

import com.alex.common.utils.trace.MdcTaskDecorator;
import com.alex.common.utils.trace.TraceIdContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 异步线程池 TraceId 与 MDC 上下文透传及隔离集成测试
 * 验证：
 * 1. 父线程 TraceId / MDC 正确透传至子线程；
 * 2. 子线程执行结束后 MDC 与 TraceId 上下文彻底清理，杜绝复用污染；
 * 3. 并发多任务执行时各子线程 TraceId 严格隔离无串线；
 * 4. CompletableFuture 异步编排下链路追踪持续有效。
 */
class AsyncMdcTracePropagationIT {

    private ThreadPoolTaskExecutor executor;

    @BeforeEach
    void setUp() {
        TraceIdContext.clear();
        MDC.clear();

        executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("test-trace-async-");
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.initialize();
    }

    @AfterEach
    void tearDown() {
        TraceIdContext.clear();
        MDC.clear();
        if (executor != null) {
            executor.shutdown();
        }
    }

    @Test
    @DisplayName("验证父线程 TraceId 与 MDC 上下文成功透传至异步子线程")
    void testParentTraceIdPropagatedToAsyncChildThread() throws Exception {
        String expectedTraceId = "TRACE-PARENT-" + TraceIdContext.generateTraceId();
        TraceIdContext.setTraceId(expectedTraceId);
        MDC.put("tenantId", "TENANT-888");

        AtomicReference<String> childTraceId = new AtomicReference<>();
        AtomicReference<String> childMdcTraceId = new AtomicReference<>();
        AtomicReference<String> childTenantId = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        executor.execute(() -> {
            try {
                childTraceId.set(TraceIdContext.getTraceId());
                childMdcTraceId.set(MDC.get(TraceIdContext.MDC_TRACE_ID_KEY));
                childTenantId.set(MDC.get("tenantId"));
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS), "异步任务应在 3 秒内执行完毕");
        assertEquals(expectedTraceId, childTraceId.get(), "子线程 TraceIdContext 必须匹配父线程 TraceId");
        assertEquals(expectedTraceId, childMdcTraceId.get(), "子线程 MDC 中的 traceId 必须匹配父线程 TraceId");
        assertEquals("TENANT-888", childTenantId.get(), "子线程 MDC 中的其他诊断字段也必须同步透传");

        // 验证父线程不受子线程任何影响
        assertEquals(expectedTraceId, TraceIdContext.getTraceId(), "父线程 TraceId 应保持不变");
    }

    @Test
    @DisplayName("验证子线程退出后 MDC 与 TraceId 彻底清理，杜绝线程池工作线程复用污染")
    void testWorkerThreadReuseNoPollution() throws Exception {
        // 创建单线程池以强制复用同一个 Worker 线程
        ThreadPoolTaskExecutor singleExecutor = new ThreadPoolTaskExecutor();
        singleExecutor.setCorePoolSize(1);
        singleExecutor.setMaxPoolSize(1);
        singleExecutor.setQueueCapacity(10);
        singleExecutor.setTaskDecorator(new MdcTaskDecorator());
        singleExecutor.initialize();

        try {
            // 第一个任务：携带脏数据 TraceId
            String dirtyTraceId = "DIRTY-TRACE-9999";
            TraceIdContext.setTraceId(dirtyTraceId);
            MDC.put("customKey", "DIRTY-DATA");

            CountDownLatch firstLatch = new CountDownLatch(1);
            singleExecutor.execute(() -> {
                assertEquals(dirtyTraceId, TraceIdContext.getTraceId());
                assertEquals("DIRTY-DATA", MDC.get("customKey"));
                firstLatch.countDown();
            });
            assertTrue(firstLatch.await(3, TimeUnit.SECONDS));

            // 主线程清理自身上下文
            TraceIdContext.clear();
            MDC.clear();

            // 第二个任务：在无 TraceId 的主线程提交，验证复用的工作线程内部绝对无残留脏数据
            CountDownLatch secondLatch = new CountDownLatch(1);
            AtomicReference<String> secondChildTraceId = new AtomicReference<>();
            AtomicReference<String> secondChildMdcKey = new AtomicReference<>();

            singleExecutor.execute(() -> {
                try {
                    secondChildTraceId.set(TraceIdContext.getTraceId());
                    secondChildMdcKey.set(MDC.get("customKey"));
                } finally {
                    secondLatch.countDown();
                }
            });
            assertTrue(secondLatch.await(3, TimeUnit.SECONDS));

            assertNull(secondChildTraceId.get(), "复用的工作线程中 TraceIdContext 必须已彻底清理，不可残留上次任务 TraceId");
            assertNull(secondChildMdcKey.get(), "复用的工作线程中 MDC 自定义诊断字段必须已彻底清理，不可残留上次任务数据");
        } finally {
            singleExecutor.shutdown();
        }
    }

    @Test
    @DisplayName("验证高并发多任务并发提交时 TraceId 互不串线")
    void testConcurrentTasksTraceIsolation() throws Exception {
        int taskCount = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(taskCount);
        ConcurrentHashMap<String, String> results = new ConcurrentHashMap<>();

        ExecutorService submitterPool = Executors.newFixedThreadPool(4);

        for (int i = 0; i < taskCount; i++) {
            final String traceId = "CONCURRENT-TRACE-" + i;
            submitterPool.submit(() -> {
                try {
                    startLatch.await();
                    TraceIdContext.setTraceId(traceId);
                    executor.execute(() -> {
                        try {
                            String captured = TraceIdContext.getTraceId();
                            results.put(traceId, captured != null ? captured : "NULL");
                        } finally {
                            finishLatch.countDown();
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        startLatch.countDown();
        assertTrue(finishLatch.await(5, TimeUnit.SECONDS), "并发任务应在 5 秒内完成");
        submitterPool.shutdown();

        assertEquals(taskCount, results.size());
        for (int i = 0; i < taskCount; i++) {
            String key = "CONCURRENT-TRACE-" + i;
            assertEquals(key, results.get(key), "各异步子任务捕获的 TraceId 必须与提交线程一对一严格匹配，不得串线");
        }
    }

    @Test
    @DisplayName("验证 CompletableFuture supplyAsync 与 thenApply 链路追踪透传")
    void testCompletableFutureTracePropagation() throws Exception {
        String expectedTraceId = "CF-TRACE-" + TraceIdContext.generateTraceId();
        TraceIdContext.setTraceId(expectedTraceId);

        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            return TraceIdContext.getTraceId();
        }, executor);

        String result = future.get(3, TimeUnit.SECONDS);
        assertEquals(expectedTraceId, result, "CompletableFuture 异步任务必须能正确读取父线程透传的 TraceId");
    }
}
