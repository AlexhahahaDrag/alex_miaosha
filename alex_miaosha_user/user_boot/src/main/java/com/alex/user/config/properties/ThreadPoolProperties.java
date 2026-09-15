package com.alex.user.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 线程池配置属性类
 *
 * @author alex
 * @version 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "thread-pool")
public class ThreadPoolProperties {

    /**
     * 通用异步任务线程池（核心业务链路，如登录异步查询、构建权限上下文）
     */
    private PoolProperties asyncTask = new PoolProperties(
            8,
            16,
            50,
            60,
            "async-task-",
            30
    );

    /**
     * 在线用户管理线程池（旁路任务，如解析 IP 地址并写入在线用户列表）
     */
    private PoolProperties onlineUser = new PoolProperties(
            2,
            4,
            100,
            30,
            "online-user-",
            10
    );

    /**
     * Token 刷新线程池（异步刷新 Redis Token 与过期时间）
     */
    private PoolProperties tokenRefresh = new PoolProperties(
            2,
            4,
            100,
            60,
            "token-refresh-",
            30
    );

    @Data
    public static class PoolProperties {
        /**
         * 核心线程数
         */
        private int corePoolSize;

        /**
         * 最大线程数
         */
        private int maxPoolSize;

        /**
         * 队列容量
         */
        private int queueCapacity;

        /**
         * 线程空闲时间（秒）
         */
        private int keepAliveSeconds;

        /**
         * 线程名前缀
         */
        private String threadNamePrefix;

        /**
         * 优雅关闭等待时间（秒）
         */
        private int awaitTerminationSeconds;

        public PoolProperties() {
        }

        public PoolProperties(int corePoolSize, int maxPoolSize, int queueCapacity, int keepAliveSeconds, String threadNamePrefix, int awaitTerminationSeconds) {
            this.corePoolSize = corePoolSize;
            this.maxPoolSize = maxPoolSize;
            this.queueCapacity = queueCapacity;
            this.keepAliveSeconds = keepAliveSeconds;
            this.threadNamePrefix = threadNamePrefix;
            this.awaitTerminationSeconds = awaitTerminationSeconds;
        }
    }
}
