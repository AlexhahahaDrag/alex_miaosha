package com.alex.common.config;

import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *description:  同步执行配置类
 *author:       majf
 *createDate:   2022/7/12 10:39
 *version:      1.0.0
 */
@Configuration
@Data
@EnableAsync
public class AsyncExecutorConfig {

    /**
     * 核心线程
     */
    private int corePoolSize = 5;

    /**
     * 最大线程
     */
    private int maxPoolSize = 10;

    /**
     * 队列容量
     */
    private int queueCapacity = 2;

    /**
     * 保持时间
     */
    private int keepAliveSeconds = 600;

    /**
     * 名称前缀
     */
    private String preFix = "name_";

    @Bean
    public Executor myExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix(preFix);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }
}
