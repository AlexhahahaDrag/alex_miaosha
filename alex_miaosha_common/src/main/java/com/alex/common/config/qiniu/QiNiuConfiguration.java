package com.alex.common.config.qiniu;

import com.alex.common.utils.qiniu.QiNiuUploadKit;
import com.qiniu.processing.OperationManager;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties(QiNiuProperties.class)
@org.springframework.context.annotation.Configuration
@RequiredArgsConstructor
public class QiNiuConfiguration {

    private final QiNiuProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public UploadManager uploadManager(Configuration configuration) {
        return new UploadManager(configuration);
    }

    @Bean
    public Configuration configuration() {
        return new Configuration(Region.huabei());
    }

    @Bean
    @ConditionalOnMissingBean
    public Auth auth() {
        return Auth.create(properties.getAccessKey(), properties.getSecretKey());
    }

    @Bean
    @ConditionalOnMissingBean
    public BucketManager bucketManager(Auth auth, Configuration configuration) {
        return new BucketManager(auth, configuration);
    }

    @Bean
    @ConditionalOnMissingBean
    public OperationManager operationManager(Auth auth, Configuration configuration) {
        return new OperationManager(auth, configuration);
    }

    @Bean
    @ConditionalOnMissingBean
    public QiNiuUploadKit qiNiuUploadKit(Auth auth, UploadManager uploadManager, BucketManager bucketManager) {
        QiNiuUploadKit qiNiuUploadKit = new QiNiuUploadKit();
        qiNiuUploadKit.setAuth(auth);
        qiNiuUploadKit.setUploadManager(uploadManager);
        qiNiuUploadKit.setBucketManager(bucketManager);
        qiNiuUploadKit.setBucket(properties.getBucket());
        return qiNiuUploadKit;
    }
}
