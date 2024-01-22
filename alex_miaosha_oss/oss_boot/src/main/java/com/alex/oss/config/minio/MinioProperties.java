package com.alex.oss.config.minio;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * description:
 * author:       majf
 * createDate:   2023/1/12 11:22
 * version:      1.0.0
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

    private MinioClient minioClient;
}
