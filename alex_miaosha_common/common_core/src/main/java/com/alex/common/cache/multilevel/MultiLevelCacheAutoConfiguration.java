package com.alex.common.cache.multilevel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * description:  分布式多级缓存自动装配配置类
 * author:       alex
 * createDate:   2026/10/21
 * version:      1.0.0
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.cache.multilevel", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(MultiLevelCacheProperties.class)
public class MultiLevelCacheAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public HotKeyDetector hotKeyDetector(MultiLevelCacheProperties properties) {
        return new HotKeyDetector(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public MultiLevelCacheService multiLevelCacheService(MultiLevelCacheProperties properties,
                                                         @Autowired(required = false) StringRedisTemplate stringRedisTemplate,
                                                         HotKeyDetector hotKeyDetector) {
        return new MultiLevelCacheService(properties, stringRedisTemplate, hotKeyDetector);
    }
}
