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

    public static final String MODE_AUTO = "AUTO";
    public static final String MODE_FORCE_V1 = "FORCE_V1";
    public static final String MODE_FORCE_V2 = "FORCE_V2";

    /**
     * 加密模式：AUTO (客户端协商，默认), FORCE_V1 / 1.0 (强制旧版 AES-CBC), FORCE_V2 / 2.0 (强制新版 AES-GCM)
     */
    private String mode = MODE_AUTO;

    /**
     * 解析实际生效的协议版本
     *
     * @param clientVersion 客户端请求头传入的版本
     * @return 实际生效版本（"1.0" 或 "2.0"）
     */
    public String resolveEffectiveVersion(String clientVersion) {
        if (MODE_FORCE_V1.equalsIgnoreCase(mode) || "1.0".equalsIgnoreCase(mode)) {
            return "1.0";
        }
        if (MODE_FORCE_V2.equalsIgnoreCase(mode) || "2.0".equalsIgnoreCase(mode)) {
            return "2.0";
        }
        return "2.0".equalsIgnoreCase(clientVersion) ? "2.0" : "1.0";
    }
} 