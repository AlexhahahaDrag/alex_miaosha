package com.alex.common.config.job;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * XXL-Job 执行器与调度中心通用配置属性
 */
@Data
@ConfigurationProperties(prefix = "xxl.job")
public class XxlJobExecutorProperties {

    /**
     * 是否启用 XXL-Job 执行器，默认为 true
     */
    private boolean enabled = true;

    /**
     * 调度中心配置
     */
    private Admin admin = new Admin();

    /**
     * 平铺兼容调度中心地址 (xxl.job.admin-addresses / xxl.job.adminAddresses)
     */
    private String adminAddresses;

    public String resolveAdminAddresses() {
        if (admin != null && StringUtils.isNotBlank(admin.getAddresses())) {
            return admin.getAddresses();
        }
        return adminAddresses;
    }

    /**
     * 执行器配置
     */
    private Executor executor = new Executor();

    @Data
    public static class Admin {
        /**
         * 调度中心集群地址，多个地址逗号分隔，如 "http://127.0.0.1:8080/xxl-job-admin"
         */
        private String addresses;
    }

    @Data
    public static class Executor {
        /**
         * 执行器 AppName（对应调度中心创建的执行器AppName）
         */
        private String appName;

        /**
         * 执行器注册 IP（为空时自动探测本机首个内网有效非回环 IP）
         */
        private String ip;

        /**
         * 执行器通讯端口，默认 9999（大于 0）
         */
        private int port = 9999;

        /**
         * 执行器与调度中心通信的访问令牌 AccessToken
         */
        private String accessToken;

        /**
         * 执行器运行日志存储根路径
         */
        private String logPath = "logs/xxl-job";

        /**
         * 执行器日志保留天数，默认 30 天
         */
        private int logRetentionDays = 30;
    }
}
