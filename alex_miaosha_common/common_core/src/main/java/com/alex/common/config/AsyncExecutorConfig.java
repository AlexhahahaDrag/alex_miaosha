package com.alex.common.config;

import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * description:  通用异步执行线程池配置类
 * author:       majf
 * createDate:   2022/7/12 10:39
 * version:      2.0.0
 */
@Configuration
@Data
@EnableAsync
public class AsyncExecutorConfig {

    /**
     * 核心线程数
     */
    private int corePoolSize = 5;

    /**
     * 最大线程数
     */
    private int maxPoolSize = 10;

    /**
     * 队列容量（治理前仅为 2，突发并发易频繁抛出 RejectedExecutionException，治理调整为 200）
     */
    private int queueCapacity = 200;

    /**
     * 线程空闲保持时间（秒）
     */
    private int keepAliveSeconds = 600;

    /**
     * 线程名称前缀（便于日志与 APM 链路排查）
     */
    private String preFix = "common-async-";

    @Bean(name = {"myExecutor", "asyncExecutor"})
    public Executor myExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix(preFix);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
