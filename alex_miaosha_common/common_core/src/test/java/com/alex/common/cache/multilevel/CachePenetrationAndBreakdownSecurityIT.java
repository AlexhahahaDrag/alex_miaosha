package com.alex.common.cache.multilevel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("缓存防穿透、防击穿互斥锁与防雪崩 TTL 抖动集成测试")
class CachePenetrationAndBreakdownSecurityIT {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private MultiLevelCacheProperties properties;
    private HotKeyDetector hotKeyDetector;
    private MultiLevelCacheService cacheService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        properties = new MultiLevelCacheProperties();
        properties.setNullValueTtlSeconds(30L);
        properties.setMaxJitterSeconds(15);

        hotKeyDetector = new HotKeyDetector(properties);
        cacheService = new MultiLevelCacheService(properties, redisTemplate, hotKeyDetector);
    }

    @Test
    @DisplayName("1. 防穿透：数据库查空时自动缓存空值哨兵，后续请求在缓存层阻断，不重复调用 dbLoader")
    void getWithLoader_whenDbReturnsNull_cachesNullSentinelToPreventPenetration() {
        String nonExistentKey = "user:not_found:99999";
        AtomicInteger dbCallCount = new AtomicInteger(0);

        // 首次请求：数据库返回 null
        String result1 = cacheService.getWithLoader(
                nonExistentKey,
                String.class,
                () -> {
                    dbCallCount.incrementAndGet();
                    return null;
                },
                60L
        );
        assertNull(result1);
        assertEquals(1, dbCallCount.get(), "首次查询应当调用 1 次 dbLoader");

        // 验证写入了空值哨兵
        verify(valueOperations, times(1)).set(eq(nonExistentKey), eq(MultiLevelCacheService.NULL_SENTINEL), eq(30L), eq(TimeUnit.SECONDS));

        // 第二次请求：同一不存在的 Key
        String result2 = cacheService.getWithLoader(
                nonExistentKey,
                String.class,
                () -> {
                    dbCallCount.incrementAndGet();
                    return "unexpected";
                },
                60L
        );
        assertNull(result2, "二次查询必须直接命中空值哨兵返回 null");
        assertEquals(1, dbCallCount.get(), "二次查询严禁再次调用 dbLoader（成功防穿透）");
    }

    @Test
    @DisplayName("2. 防击穿：并发 20 线程同时读取未缓存的冷 Key，互斥锁保障 dbLoader 仅被执行 1 次")
    void getWithLoader_whenConcurrentRequestsOnMiss_onlyCallsDbLoaderOnceDueToMutexLock() throws Exception {
        String hotMissKey = "event:detail:888";
        AtomicInteger dbCallCount = new AtomicInteger(0);

        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                startLatch.await();
                return cacheService.getWithLoader(
                        hotMissKey,
                        String.class,
                        () -> {
                            dbCallCount.incrementAndGet();
                            try {
                                Thread.sleep(50); // 模拟耗时 DB 查询
                            } catch (InterruptedException ignored) {}
                            return "event_data_payload";
                        },
                        120L
                );
            }));
        }

        // 同时放行所有并发线程
        startLatch.countDown();
        for (Future<String> f : futures) {
            String val = f.get(5, TimeUnit.SECONDS);
            assertEquals("event_data_payload", val);
        }
        executor.shutdown();

        assertEquals(1, dbCallCount.get(), "并发 20 线程下，dbLoader 必须且仅能被执行 1 次（成功防击穿）");
    }

    @Test
    @DisplayName("3. 防雪崩：在基准 TTL 上随机叠加抖动（0 ~ maxJitterSeconds），打散到期时刻")
    void calculateJitterTtl_injectsRandomFluctuationToPreventAvalanche() {
        long baseTtl = 3600L;
        boolean hasVariation = false;
        long firstTtl = cacheService.calculateJitterTtl(baseTtl);

        for (int i = 0; i < 50; i++) {
            long jitterTtl = cacheService.calculateJitterTtl(baseTtl);
            assertTrue(jitterTtl >= baseTtl, "最终 TTL 必须大于等于基准 TTL");
            assertTrue(jitterTtl <= baseTtl + 15, "最终 TTL 必须在基准 + maxJitterSeconds 范围内");
            if (jitterTtl != firstTtl) {
                hasVariation = true;
            }
        }

        assertTrue(hasVariation, "多次计算得到的 TTL 必须存在随机扰动打散点（成功防雪崩）");
    }
}
