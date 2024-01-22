package com.alex.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wechat")
public class WechatAccountConfig {

    private String url;

    private String appId;

    private String secret;

    private String accountTimeOutTemplateId;

    private String userId;
}
