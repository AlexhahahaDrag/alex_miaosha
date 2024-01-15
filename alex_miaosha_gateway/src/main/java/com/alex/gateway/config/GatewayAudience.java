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
}
