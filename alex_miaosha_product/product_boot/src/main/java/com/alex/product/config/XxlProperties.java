package com.alex.product.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "xxl.job.executor")
@Data
public class XxlProperties {

    private String appname;

    private String ip;

    private Integer port;

    private String accessToken;

    private String logPath;

    private Integer logRetentionDays;
}
