package com.alex.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * description:
 * author: alex
 * createDate: 2023/3/27 7:11
 * version: 1.0.0
 */
@ConfigurationProperties(prefix = "gateway.audience")
@Component
@Data
public class GatewayAudience {

    private String tokenHeader;

    private List<String> whiteList;

    private List<String> docWhiteList;

    /**
     * IP 黑名单列表（支持单 IP 如 192.168.1.50 或前缀如 10.0.0.*）
     */
    private List<String> ipBlackList;

    /**
     * 灰度标记请求头（默认 X-Gray-Tag）
     */
    private String grayHeaderName = "X-Gray-Tag";
}
