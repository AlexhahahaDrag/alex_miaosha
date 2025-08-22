package com.alex.user.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Token刷新线程池配置
 * 
 * @author alex
 * @createDate 2024/12/19
 * @version 1.0.0
 */
@Configuration
@EnableAsync
@Slf4j
public class TokenRefreshConfig {

    /**
     * Token刷新专用线程池
     * 
     * @return Executor
     */
    @Bean("tokenRefreshExecutor")
    public Executor tokenRefreshExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数：2个线程专门处理token刷新
        executor.setCorePoolSize(2);
        
        // 最大线程数：最多4个线程处理token刷新
        executor.setMaxPoolSize(4);
        
        // 队列容量：最多100个token刷新任务等待处理
        executor.setQueueCapacity(100);
        
        // 线程名前缀
        executor.setThreadNamePrefix("token-refresh-");
        
        // 线程空闲时间：60秒后回收空闲线程
        executor.setKeepAliveSeconds(60);
        
        // 拒绝策略：当队列满了且线程池达到最大线程数时，由调用线程处理
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间：30秒
        executor.setAwaitTerminationSeconds(30);
        
        // 初始化线程池
        executor.initialize();
        
        log.info("Token刷新线程池初始化完成，核心线程数：{}，最大线程数：{}，队列容量：{}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }
} 