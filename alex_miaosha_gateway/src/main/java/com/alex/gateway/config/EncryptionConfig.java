package com.alex.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AES加密配置类
 * 
 * @author alex
 * @createDate 2024/12/19
 * @version 1.0.0
 */
@ConfigurationProperties(prefix = "alex.encryption")
@Component
@Data
public class EncryptionConfig {

    /**
     * AES密钥
     */
    private String key;

    /**
     * AES向量
     */
    private String iv;

    /**
     * AES填充模式
     */
    private String padding;

    /**
     * 是否启用加密
     */
    private boolean enabled;

    /**
     * 字符编码
     */
    private String charset;
} 