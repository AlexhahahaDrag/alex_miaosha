package com.alex.common.cache.multilevel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("L1 (Caffeine) 与 L2 (Redis) 多级缓存层级流转集成测试")
class MultiLevelCacheHierarchyIT {

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
        hotKeyDetector = new HotKeyDetector(properties);
        cacheService = new MultiLevelCacheService(properties, redisTemplate, hotKeyDetector);
    }

    @Test
    @DisplayName("1. 验证 L1 命中时直接返回，零网络 IO（不访问 L2 Redis）")
    void get_whenL1Hit_returnsDirectlyWithoutQueryingL2() {
        String key = "user:info:1001";
        String sampleJson = "{\"id\":\"1001\",\"username\":\"alex_tester\"}";
        SampleUser expectedUser = new SampleUser("1001", "alex_tester");

        // 预热 L1
        cacheService.getL1Cache().put(key, expectedUser);

        SampleUser actual = cacheService.get(key, SampleUser.class);
        assertNotNull(actual);
        assertEquals("1001", actual.getId());
        assertEquals("alex_tester", actual.getUsername());

        // 验证从未调用 L2 Redis
        verify(valueOperations, never()).get(anyString());
    }

    @Test
    @DisplayName("2. 验证 L1 未命中但 L2 命中时，自动回填 L1 本地缓存，后续请求命中 L1")
    void get_whenL1MissAndL2Hit_returnsValueAndBackfillsL1() {
        String key = "user:info:1002";
        String sampleJson = "{\"id\":\"1002\",\"username\":\"bob\"}";
        when(valueOperations.get(key)).thenReturn(sampleJson);

        // 首次查询：L1 未命中，回源 L2
        SampleUser user1 = cacheService.get(key, SampleUser.class);
        assertNotNull(user1);
        assertEquals("1002", user1.getId());
        assertEquals("bob", user1.getUsername());
        verify(valueOperations, times(1)).get(key);

        // 验证已自动回填 L1
        assertNotNull(cacheService.getL1Cache().getIfPresent(key));

        // 第二次查询：命中 L1，不再调用 L2
        SampleUser user2 = cacheService.get(key, SampleUser.class);
        assertNotNull(user2);
        assertEquals("1002", user2.getId());
        verify(valueOperations, times(1)).get(key); // 仍为 1 次，证明命中 L1
    }

    @Test
    @DisplayName("3. 验证 put 操作同时写入 L1 与 L2")
    void put_updatesBothL1AndL2() {
        String key = "dict:status:1";
        SampleUser user = new SampleUser("1", "active");

        cacheService.put(key, user, 300L);

        // 验证 L1 写入
        assertNotNull(cacheService.getL1Cache().getIfPresent(key));

        // 验证 L2 set 调用
        verify(valueOperations, times(1)).set(eq(key), anyString(), longThat(t -> t >= 300L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("4. 验证 evict 双层原子淘汰，L1 与 L2 同步失效")
    void evict_invalidatesBothL1AndL2() {
        String key = "role:permission:admin";
        cacheService.getL1Cache().put(key, "all_perms");

        cacheService.evict(key);

        // L1 已清空
        assertNull(cacheService.getL1Cache().getIfPresent(key));

        // L2 delete 被调用
        verify(redisTemplate, times(1)).delete(key);
    }

    @Test
    @DisplayName("5. 验证 clearLocal 只清空本地 L1，不影响远程分布式 L2")
    void clearLocal_onlyClearsL1WhilePreservingL2() {
        cacheService.getL1Cache().put("key1", "val1");
        cacheService.getL1Cache().put("key2", "val2");

        cacheService.clearLocal();

        assertNull(cacheService.getL1Cache().getIfPresent("key1"));
        assertNull(cacheService.getL1Cache().getIfPresent("key2"));
        verify(redisTemplate, never()).delete(anyString());
    }

    public static class SampleUser {
        private String id;
        private String username;

        public SampleUser() {}

        public SampleUser(String id, String username) {
            this.id = id;
            this.username = username;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }
}
