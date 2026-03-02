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
}
