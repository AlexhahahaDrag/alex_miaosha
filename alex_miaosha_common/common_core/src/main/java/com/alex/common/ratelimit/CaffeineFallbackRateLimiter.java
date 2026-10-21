package com.alex.common.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

/**
 * 基于本地 Caffeine 缓存的高可用降级滑动窗口限流器
 * <p>当分布式 Redis 服务不可用或网络超时抖动时，自动无感知接管，保障核心系统不中断。
 */
@Slf4j
public class CaffeineFallbackRateLimiter {

    private final Cache<String, ConcurrentLinkedDeque<Long>> localWindowCache;

    public CaffeineFallbackRateLimiter() {
        this.localWindowCache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .build();
    }

    /**
     * 本地滑动窗口限流判断
     *
     * @param key           限流键
     * @param windowSeconds 窗口时长（秒）
     * @param limitCount    窗口期内最大允许次数
     * @return true-允许访问；false-触发限流
     */
    public boolean tryAcquire(String key, int windowSeconds, int limitCount) {
        ConcurrentLinkedDeque<Long> queue = localWindowCache.get(key, k -> new ConcurrentLinkedDeque<>());
        if (queue == null) {
            return true;
        }

        long now = System.currentTimeMillis();
        long clearBefore = now - ((long) windowSeconds * 1000L);

        synchronized (queue) {
            // 清理过期时间戳
            while (!queue.isEmpty() && queue.peekFirst() <= clearBefore) {
                queue.pollFirst();
            }

            if (queue.size() < limitCount) {
                queue.addLast(now);
                return true;
            } else {
                return false;
            }
        }
    }
}
