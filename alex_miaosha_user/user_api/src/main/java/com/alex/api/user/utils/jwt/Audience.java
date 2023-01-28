package com.alex.api.user.utils.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *description:  jwt相关配置类
 *author:       alex
 *createDate:   2021/7/24 22:00
 *version:      1.0.0
 */
@ConfigurationProperties(prefix = "audience")
@Component
@Data
public class Audience {

    private String clientId;

    private String base64Secret;

    private String name;

    private Long expiresSecond;

    private Integer refreshSecond;

    private String tokenHead;

    private String tokenHeader;

    private List<String> whiteList;
}
