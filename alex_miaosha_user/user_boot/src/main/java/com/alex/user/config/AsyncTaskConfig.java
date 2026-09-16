package com.alex.user.config;

import com.alex.user.config.properties.ThreadPoolProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务与业务专属线程池统一配置类
 *
 * @author alex
 * @version 1.0.0
 */
@Configuration
@EnableAsync
@RequiredArgsConstructor
@Slf4j
public class AsyncTaskConfig {

    private final ThreadPoolProperties threadPoolProperties;

    /**
     * 在线用户管理专用线程池（旁路任务，如解析 IP 地址并写入在线用户列表）
     *
     * @return Executor
     */
    @Bean("onlineUserExecutor")
    public Executor onlineUserExecutor() {
        ThreadPoolProperties.PoolProperties pool = threadPoolProperties.getOnlineUser();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(pool.getCorePoolSize());
        executor.setMaxPoolSize(pool.getMaxPoolSize());
        executor.setQueueCapacity(pool.getQueueCapacity());
        executor.setThreadNamePrefix(pool.getThreadNamePrefix());
        executor.setKeepAliveSeconds(pool.getKeepAliveSeconds());
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(pool.getAwaitTerminationSeconds());
        executor.initialize();
        
        log.info("在线用户管理线程池初始化完成，核心线程数：{}，最大线程数：{}，队列容量：{}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }

    /**
     * 通用异步任务线程池（核心主链路，如登录并行查询 Redis/权限/头像等）
     *
     * @return Executor
     */
    @Bean("asyncTaskExecutor")
    public Executor asyncTaskExecutor() {
        ThreadPoolProperties.PoolProperties pool = threadPoolProperties.getAsyncTask();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(pool.getCorePoolSize());
        executor.setMaxPoolSize(pool.getMaxPoolSize());
        executor.setQueueCapacity(pool.getQueueCapacity());
        executor.setThreadNamePrefix(pool.getThreadNamePrefix());
        executor.setKeepAliveSeconds(pool.getKeepAliveSeconds());
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(pool.getAwaitTerminationSeconds());
        executor.initialize();
        
        log.info("通用异步任务线程池初始化完成，核心线程数：{}，最大线程数：{}，队列容量：{}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }

    /**
     * Token刷新专用线程池（异步刷新 Redis Token）
     *
     * @return Executor
     */
    @Bean("tokenRefreshExecutor")
    public Executor tokenRefreshExecutor() {
        ThreadPoolProperties.PoolProperties pool = threadPoolProperties.getTokenRefresh();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(pool.getCorePoolSize());
        executor.setMaxPoolSize(pool.getMaxPoolSize());
        executor.setQueueCapacity(pool.getQueueCapacity());
        executor.setThreadNamePrefix(pool.getThreadNamePrefix());
        executor.setKeepAliveSeconds(pool.getKeepAliveSeconds());
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(pool.getAwaitTerminationSeconds());
        executor.initialize();
        
        log.info("Token刷新线程池初始化完成，核心线程数：{}，最大线程数：{}，队列容量：{}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }
}