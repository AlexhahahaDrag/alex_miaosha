package com.alex.user.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置类
 * 
 * @author alex
 * @createDate 2024/12/19
 * @version 1.0.0
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncTaskConfig {

    /**
     * 在线用户管理专用线程池
     * 
     * @return Executor
     */
    @Bean("onlineUserExecutor")
    public Executor onlineUserExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数：1个线程专门处理在线用户管理
        executor.setCorePoolSize(1);
        
        // 最大线程数：最多2个线程处理在线用户管理
        executor.setMaxPoolSize(2);
        
        // 队列容量：最多50个在线用户管理任务等待处理
        executor.setQueueCapacity(50);
        
        // 线程名前缀
        executor.setThreadNamePrefix("online-user-");
        
        // 线程空闲时间：30秒后回收空闲线程
        executor.setKeepAliveSeconds(30);
        
        // 拒绝策略：当队列满了且线程池达到最大线程数时，由调用线程处理
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间：10秒
        executor.setAwaitTerminationSeconds(10);
        
        // 初始化线程池
        executor.initialize();
        
        log.info("在线用户管理线程池初始化完成，核心线程数：{}，最大线程数：{}，队列容量：{}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }

    /**
     * 通用异步任务线程池
     * 
     * @return Executor
     */
    @Bean("asyncTaskExecutor")
    public Executor asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数：3个线程处理通用异步任务
        executor.setCorePoolSize(3);
        
        // 最大线程数：最多6个线程处理通用异步任务
        executor.setMaxPoolSize(6);
        
        // 队列容量：最多200个通用异步任务等待处理
        executor.setQueueCapacity(200);
        
        // 线程名前缀
        executor.setThreadNamePrefix("async-task-");
        
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
        
        log.info("通用异步任务线程池初始化完成，核心线程数：{}，最大线程数：{}，队列容量：{}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }
} 