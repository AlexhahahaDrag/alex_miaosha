package com.alex.finance.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * XXL-JOB 执行器配置。
 * <p>仅 {@code prod} 环境加载；并要求 {@code xxl.job.enabled=true}。
 * <p>说明：Nacos 共享配置 {@code xxl.yaml} 优先级高于本地 application-*.yaml，
 * 仅靠本地 {@code enabled=false} 会被远程配置覆盖，因此叠加 {@link Profile} 做环境硬门禁。
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "xxl.job", name = "enabled", havingValue = "true", matchIfMissing = true)
public class XxlJobConfig {

    private final XxlProperties xxlProperties;

    @Value("${xxl.job.admin.addresses}")
    private String adminAddresses;

    @Bean(initMethod = "start", destroyMethod = "destroy")
    @ConditionalOnProperty(prefix = "xxl.job", name = "enabled", havingValue = "true", matchIfMissing = true)
    public XxlJobSpringExecutor xxlJobExecutor() {
        log.info(">>>>>>>>>>> xxl-job config init. (profile=prod, enabled=true)");
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(adminAddresses);
        xxlJobSpringExecutor.setAppname(xxlProperties.getAppName());
        xxlJobSpringExecutor.setIp(xxlProperties.getIp());
        xxlJobSpringExecutor.setPort(xxlProperties.getPort());
        xxlJobSpringExecutor.setAccessToken(xxlProperties.getAccessToken());
        xxlJobSpringExecutor.setLogPath(xxlProperties.getLogPath());
        xxlJobSpringExecutor.setLogRetentionDays(xxlProperties.getLogRetentionDays());
        return xxlJobSpringExecutor;
    }
}
