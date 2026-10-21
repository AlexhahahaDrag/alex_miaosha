package com.alex.common.ratelimit;

import lombok.extern.slf4j.Slf4j;

/**
 * 分布式限流与本地容灾降级门面管理器
 */
@Slf4j
public class RateLimiterManager {

    private final RedisSlidingWindowRateLimiter redisRateLimiter;
    private final CaffeineFallbackRateLimiter fallbackRateLimiter;

    public RateLimiterManager(RedisSlidingWindowRateLimiter redisRateLimiter, CaffeineFallbackRateLimiter fallbackRateLimiter) {
        this.redisRateLimiter = redisRateLimiter;
        this.fallbackRateLimiter = fallbackRateLimiter != null ? fallbackRateLimiter : new CaffeineFallbackRateLimiter();
    }

    /**
     * 高可用尝试获取访问许可
     *
     * @param key           限流键
     * @param windowSeconds 窗口时长（秒）
     * @param limitCount    窗口期内最大允许次数
     * @return true-允许访问；false-触发限流
     */
    public boolean tryAcquire(String key, int windowSeconds, int limitCount) {
        if (redisRateLimiter != null) {
            try {
                return redisRateLimiter.tryAcquire(key, windowSeconds, limitCount);
            } catch (Throwable t) {
                log.warn("Redis 分布式限流器不可用或网络异常，自动优雅降级为本地 Caffeine 限流: {}", t.getMessage());
            }
        }
        // 降级使用本地内存限流
        return fallbackRateLimiter.tryAcquire(key, windowSeconds, limitCount);
    }
}
