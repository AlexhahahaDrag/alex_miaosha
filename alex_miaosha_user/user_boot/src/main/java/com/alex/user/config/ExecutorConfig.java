package com.alex.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfig {

    @Bean
    public Executor taskExecutor() {
        return Executors.newFixedThreadPool(10); // 或者使用 ThreadPoolTaskExecutor 自定义线程池
    }
}
