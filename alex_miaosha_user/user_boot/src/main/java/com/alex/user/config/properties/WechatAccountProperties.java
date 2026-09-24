package com.alex.user.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信公众号/小程序账号配置属性
 *
 * @author alex
 * @version 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat")
public class WechatAccountProperties {

    private String url;

    private String appId;

    private String secret;

    private String accountTimeOutTemplateId;

    private String userId;
}
