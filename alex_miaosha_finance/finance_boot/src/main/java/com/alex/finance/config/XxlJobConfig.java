package com.alex.finance.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class XxlJobConfig {

    private final XxlProperties xxlProperties;

    @Value("${xxl.job.admin.addresses}")
    private String adminAddresses;

    @Bean(initMethod = "start", destroyMethod = "destroy")
    public XxlJobSpringExecutor xxlJobExecutor() {
        log.info(">>>>>>>>>>> xxl-job config init.");
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(adminAddresses);
        xxlJobSpringExecutor.setAppname(xxlProperties.getAppname());
        xxlJobSpringExecutor.setIp(xxlProperties.getIp());
        xxlJobSpringExecutor.setPort(xxlProperties.getPort());
        xxlJobSpringExecutor.setAccessToken(xxlProperties.getAccessToken());
        xxlJobSpringExecutor.setLogPath(xxlProperties.getLogPath());
        xxlJobSpringExecutor.setLogRetentionDays(xxlProperties.getLogRetentionDays());
        return xxlJobSpringExecutor;
    }
}
