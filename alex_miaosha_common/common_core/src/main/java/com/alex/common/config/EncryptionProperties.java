package com.alex.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 通用加密配置类
 * 
 * @author alex
 * @createDate 2024/12/19
 * @version 1.0.0
 */
@ConfigurationProperties(prefix = "alex.encryption")
@Component
@Data
public class EncryptionProperties {

    /**
     * AES密钥
     */
    private String key = "20230610HelloDog";

    /**
     * AES向量
     */
    private String iv = "1234567890123456";

    /**
     * AES填充模式
     */
    private String padding = "PKCS5Padding";

    /**
     * 是否启用加密
     */
    private boolean enabled = true;

    /**
     * 字符编码
     */
    private String charset = "UTF-8";
} 