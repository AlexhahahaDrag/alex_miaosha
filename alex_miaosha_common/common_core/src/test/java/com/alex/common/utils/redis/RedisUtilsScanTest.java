package com.alex.common.utils.redis;

import com.alex.common.redis.key.CommonKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisUtilsScanTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @InjectMocks
    private RedisUtils redisUtils;

    @Test
    @DisplayName("验证 RedisUtils.scan 基于游标安全迭代并关闭 Cursor，杜绝 KEYS * 阻塞单线程")
    void testScan_SuccessWithCursor() {
        Cursor<String> mockCursor = mock(Cursor.class);
        Iterator<String> keyIterator = Arrays.asList("dict:gender", "dict:status", "dict:type").iterator();

        when(mockCursor.hasNext()).thenAnswer(inv -> keyIterator.hasNext());
        when(mockCursor.next()).thenAnswer(inv -> keyIterator.next());
        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(mockCursor);

        Set<String> result = redisUtils.scan("dict:*");

        assertEquals(3, result.size());
        assertTrue(result.contains("dict:gender"));
        assertTrue(result.contains("dict:status"));
        assertTrue(result.contains("dict:type"));

        // 验证游标被 try-with-resources 安全关闭
        verify(mockCursor, times(1)).close();
    }

    @Test
    @DisplayName("验证 keys(KeyPrefix) 平滑委托给游标 scan 方法")
    void testKeysDelegatesToScan() {
        Cursor<String> mockCursor = mock(Cursor.class);
        Iterator<String> keyIterator = Arrays.asList("CommonKey:common:key1").iterator();

        when(mockCursor.hasNext()).thenAnswer(inv -> keyIterator.hasNext());
        when(mockCursor.next()).thenAnswer(inv -> keyIterator.next());
        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(mockCursor);

        Set<String> result = redisUtils.keys(CommonKey.commonKey);

        assertEquals(1, result.size());
        assertTrue(result.contains("CommonKey:common:key1"));
        verify(mockCursor, times(1)).close();
    }

    @Test
    @DisplayName("验证 scan 传入空前缀或空 Pattern 时安全返回空集合")
    void testScan_EmptyPatternOrNullPrefix() {
        assertTrue(redisUtils.scan((String) null).isEmpty());
        assertTrue(redisUtils.scan("").isEmpty());
        assertTrue(redisUtils.scan((com.alex.common.redis.key.KeyPrefix) null).isEmpty());
    }
}
