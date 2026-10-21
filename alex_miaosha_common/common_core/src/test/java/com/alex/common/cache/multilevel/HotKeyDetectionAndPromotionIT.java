package com.alex.common.cache.multilevel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("热 Key 自动探测与统计晋升集成测试")
class HotKeyDetectionAndPromotionIT {

    private MultiLevelCacheProperties properties;
    private HotKeyDetector detector;

    @BeforeEach
    void setUp() {
        properties = new MultiLevelCacheProperties();
        properties.setHotKeyWindowSeconds(2); // 窗口 2 秒
        properties.setHotKeyThreshold(5);     // 阈值 5 次
        detector = new HotKeyDetector(properties);
    }

    @Test
    @DisplayName("1. 验证在时间窗口内访问达到阈值时，准确判定为热 Key")
    void recordAndCheck_whenFrequencyReachesThreshold_detectsHotKey() {
        String key = "hot:config:channel";

        for (int i = 1; i <= 4; i++) {
            assertFalse(detector.recordAndCheck(key), "前 4 次访问不应触发热 Key");
        }

        // 第 5 次访问达到阈值
        boolean isHot = detector.recordAndCheck(key);
        assertTrue(isHot, "第 5 次访问必须触发热 Key 判定");
        assertTrue(detector.isHot(key));
    }

    @Test
    @DisplayName("2. 验证访问频次未达阈值时，不误报为热 Key")
    void recordAndCheck_whenFrequencyBelowThreshold_returnsFalse() {
        String key = "normal:user:123";

        for (int i = 0; i < 3; i++) {
            assertFalse(detector.recordAndCheck(key));
        }

        assertFalse(detector.isHot(key));
    }

    @Test
    @DisplayName("3. 验证窗口过期后自动重置滑动计数器")
    void recordAndCheck_whenWindowExpires_resetsCounter() throws InterruptedException {
        String key = "burst:traffic:item";

        // 窗口内访问 4 次
        for (int i = 0; i < 4; i++) {
            assertFalse(detector.recordAndCheck(key));
        }

        // 等待时间窗口滑出（2.1 秒）
        Thread.sleep(2100);

        // 窗口已过期，首次访问计数重置为 1
        assertFalse(detector.recordAndCheck(key));
        assertFalse(detector.isHot(key));
    }

    @Test
    @DisplayName("4. 验证 MultiLevelCacheService 在 get 读取时自动触发热 Key 检测")
    @SuppressWarnings("unchecked")
    void multiLevelCache_integratesHotKeyDetectionOnAccess() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(ops);
        when(ops.get(anyString())).thenReturn("{\"name\":\"test\"}");

        MultiLevelCacheService cacheService = new MultiLevelCacheService(properties, redisTemplate, detector);

        String key = "user:hot:999";
        for (int i = 1; i <= 5; i++) {
            cacheService.get(key, String.class);
        }

        assertTrue(detector.isHot(key), "经由 MultiLevelCacheService 访问满 5 次后，该 Key 必须被判定为热 Key");
    }
}
