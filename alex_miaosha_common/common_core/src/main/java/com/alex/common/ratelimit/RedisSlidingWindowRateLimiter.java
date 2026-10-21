package com.alex.common.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.UUID;

/**
 * 基于 Redis ZSet + Lua 脚本的精准滑动时间窗口分布式限流器
 * <p>彻底根治固定时间窗口在 59s/61s 临界点放行双倍突发流量的漏洞。
 */
@Slf4j
public class RedisSlidingWindowRateLimiter {

    private static final String SLIDING_WINDOW_LUA =
            "local key = KEYS[1]\n" +
            "local now = tonumber(ARGV[1])\n" +
            "local window = tonumber(ARGV[2])\n" +
            "local limit = tonumber(ARGV[3])\n" +
            "local member = ARGV[4]\n" +
            "\n" +
            "local clearBefore = now - (window * 1000)\n" +
            "redis.call('ZREMRANGEBYSCORE', key, 0, clearBefore)\n" +
            "local currentRequests = redis.call('ZCARD', key)\n" +
            "\n" +
            "if currentRequests < limit then\n" +
            "    redis.call('ZADD', key, now, member)\n" +
            "    redis.call('EXPIRE', key, window + 2)\n" +
            "    return 1\n" +
            "else\n" +
            "    return 0\n" +
            "end";

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Long> redisScript;

    public RedisSlidingWindowRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(SLIDING_WINDOW_LUA);
        script.setResultType(Long.class);
        this.redisScript = script;
    }

    /**
     * 尝试获取访问令牌
     *
     * @param key           限流键
     * @param windowSeconds 窗口时长（秒）
     * @param limitCount    窗口期内最大允许次数
     * @return true-允许访问；false-触发限流
     */
    public boolean tryAcquire(String key, int windowSeconds, int limitCount) {
        if (redisTemplate == null) {
            throw new IllegalStateException("StringRedisTemplate 为空，无法执行分布式限流");
        }

        long now = System.currentTimeMillis();
        String member = now + ":" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        Long result = redisTemplate.execute(
                redisScript,
                Collections.singletonList(key),
                String.valueOf(now),
                String.valueOf(windowSeconds),
                String.valueOf(limitCount),
                member
        );

        return result != null && result == 1L;
    }
}
