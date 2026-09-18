package com.alex.oss.config.minio;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * description:
 * author: majf
 * createDate: 2023/1/12 11:22
 * version: 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    private String url;

    private Integer port;

    private String accessKey;

    private String secretKey;

    private String bucketName;

    private String region;

    /**
     * 对外公开访问/预签名 URL 域名或反代地址（如 https://oss.example.com 或
     * http://115.190.181.243:9000）
     * 若配置，预签名 URL 将使用此域名替代内部物理 endpoint，实现内外网拓扑隔离与 CDN 映射
     */
    private String publicUrl;

    /**
     * 是否启用 HTTPS 传输（默认 false）
     */
    private Boolean secure = false;
}
