package com.alex.oss.config.garage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * description:  garage配置属性
 * author:       alex
 * createDate:   2026/03/02
 * version:      1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "garage")
public class GarageProperties {

    private String url;

    private Integer port;

    private String accessKey;

    private String secretKey;

    private String bucketName;

    private String region;

    /**
     * 对外公开访问/预签名 URL 域名或反代地址
     */
    private String publicUrl;

    /**
     * 是否启用 HTTPS 传输（默认 false）
     */
    private Boolean secure = false;
}
