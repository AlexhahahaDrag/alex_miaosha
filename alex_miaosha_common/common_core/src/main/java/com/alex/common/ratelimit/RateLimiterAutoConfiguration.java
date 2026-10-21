package com.alex.common.ratelimit;

import com.alex.common.aspect.RateLimiterAspect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 分布式限流与防刷自动装配配置类
 */
@Configuration
@Slf4j
public class RateLimiterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(RateLimitKeyResolver.class)
    public RateLimitKeyResolver rateLimitKeyResolver(ApplicationContext applicationContext) {
        return new RateLimitKeyResolver(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean(CaffeineFallbackRateLimiter.class)
    public CaffeineFallbackRateLimiter caffeineFallbackRateLimiter() {
        return new CaffeineFallbackRateLimiter();
    }

    @Bean
    @ConditionalOnMissingBean(RateLimiterManager.class)
    public RateLimiterManager rateLimiterManager(
            ObjectProvider<StringRedisTemplate> redisTemplateProvider,
            CaffeineFallbackRateLimiter fallbackRateLimiter) {

        StringRedisTemplate stringRedisTemplate = redisTemplateProvider.getIfAvailable();
        RedisSlidingWindowRateLimiter redisRateLimiter = null;
        if (stringRedisTemplate != null) {
            redisRateLimiter = new RedisSlidingWindowRateLimiter(stringRedisTemplate);
            log.info(">>>>>> [RateLimiter] 分布式滑动窗口限流器装配成功 (Redis + Lua)");
        } else {
            log.warn(">>>>>> [RateLimiter] 未检测到 StringRedisTemplate，将使用本地 Caffeine 限流降级运行");
        }

        return new RateLimiterManager(redisRateLimiter, fallbackRateLimiter);
    }

    @Bean
    @ConditionalOnMissingBean(RateLimiterAspect.class)
    public RateLimiterAspect rateLimiterAspect(RateLimiterManager rateLimiterManager, RateLimitKeyResolver keyResolver) {
        return new RateLimiterAspect(rateLimiterManager, keyResolver);
    }
}
