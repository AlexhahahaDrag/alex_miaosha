package com.alex.oss.config.minio;

import com.alex.oss.config.s3.BaseS3Template;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * description:  MinIO 模板类，继承自统一的 BaseS3Template
 * 继承通用 S3 能力：流式下载不提前关流、Bucket 内存缓存、明确传入流长度、预签名域名映射
 *
 * @author majf, alex
 * @version 2.0.0
 */
@Configuration
@Slf4j
@EnableConfigurationProperties({MinioProperties.class})
@Getter
public class MinioTemplate extends BaseS3Template implements InitializingBean {

    private final MinioProperties minioProperties;

    public MinioTemplate(MinioProperties minioProperties) {
        this.minioProperties = minioProperties;
    }

    @Override
    public void afterPropertiesSet() {
        if (minioProperties == null) {
            log.warn("[MinioTemplate] 未注入 MinioProperties，跳过 MinIO 初始化");
            return;
        }
        String url = minioProperties.getUrl();
        Integer port = minioProperties.getPort();
        String accessKey = minioProperties.getAccessKey();
        String secretKey = minioProperties.getSecretKey();
        String region = minioProperties.getRegion();
        Boolean secure = minioProperties.getSecure();
        String publicUrl = minioProperties.getPublicUrl();

        // 优雅容错检查：如果环境未配置 minio 相关属性（如生产环境主用 garage 时），平稳跳过，不抛异常崩溃应用
        if (url == null || port == null || accessKey == null || secretKey == null) {
            log.info("[MinioTemplate] 检测到当前环境未完整配置 minio (url={}, port={})，已安全跳过初始化（若需启用请配置 minio.*）",
                    url, port);
            return;
        }

        initClient(url, port, accessKey, secretKey, region, secure, publicUrl);
    }
}
