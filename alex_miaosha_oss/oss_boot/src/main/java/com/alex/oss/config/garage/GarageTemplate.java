package com.alex.oss.config.garage;

import com.alex.oss.config.s3.BaseS3Template;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * description:  Garage 模板类，继承自统一的 BaseS3Template
 * 具备 Garage S3 存储全部操作能力并复用 BaseS3Template 的安全流式传输与 Bucket 内存缓存
 *
 * @author alex
 * @version 2.0.0
 */
@Configuration
@Slf4j
@EnableConfigurationProperties({GarageProperties.class})
@Getter
public class GarageTemplate extends BaseS3Template implements InitializingBean {

    private final GarageProperties garageProperties;

    public GarageTemplate(GarageProperties garageProperties) {
        this.garageProperties = garageProperties;
    }

    @Override
    public void afterPropertiesSet() {
        if (garageProperties == null) {
            log.warn("[GarageTemplate] 未注入 GarageProperties，跳过 Garage 初始化");
            return;
        }
        String url = garageProperties.getUrl();
        Integer port = garageProperties.getPort();
        String accessKey = garageProperties.getAccessKey();
        String secretKey = garageProperties.getSecretKey();
        String region = garageProperties.getRegion();
        Boolean secure = garageProperties.getSecure();
        String publicUrl = garageProperties.getPublicUrl();

        // 优雅容错检查：如果环境未完整配置 garage 相关属性，平稳跳过，不抛异常崩溃应用
        if (url == null || port == null || accessKey == null || secretKey == null) {
            log.info("[GarageTemplate] 检测到当前环境未完整配置 garage (url={}, port={})，已安全跳过初始化（若需启用请配置 garage.*）",
                    url, port);
            return;
        }

        initClient(url, port, accessKey, secretKey, region, secure, publicUrl);
    }
}
