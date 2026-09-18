package com.alex.gateway.utils;

import com.alex.common.config.EncryptionProperties;
import com.alex.common.utils.secret.AESGcmUtils;
import com.alex.common.utils.secret.AESUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 加密工具类
 * 
 * author alex
 * createDate 2024/12/19
 * @version 2.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EncryptionUtils {

    public static final String HEADER_CRYPTO_VERSION = "X-Crypto-Version";
    public static final String VERSION_1_0 = "1.0";
    public static final String VERSION_2_0 = "2.0";

    private final EncryptionProperties encryptionProperties;

    /**
     * 加密字符串 (默认 v1.0 AES-CBC 模式)
     * 
     * @param content 要加密的内容
     * @return 加密后的字节数组
     */
    public byte[] encrypt(String content) {
        return encryptByVersion(content, VERSION_1_0);
    }

    /**
     * 根据指定协议版本加密字符串
     *
     * @param content 要加密的内容
     * @param version 客户端协议版本（如 "2.0" 或 "1.0"）
     * @return 加密后的字节数组
     */
    public byte[] encryptByVersion(String content, String version) {
        try {
            if (!encryptionProperties.isEnabled()) {
                return content.getBytes(StandardCharsets.UTF_8);
            }
            if (VERSION_2_0.equalsIgnoreCase(version)) {
                String encrypted = AESGcmUtils.encrypt(content, encryptionProperties.getKey());
                return encrypted.getBytes(StandardCharsets.UTF_8);
            }
            // 兼容降级模式：v1.0 AES-CBC
            String encrypted = AESUtils.encryptWithConfig(content);
            return encrypted.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("按版本 [{}] 加密失败: {}", version, e.getMessage(), e);
            throw new RuntimeException("加密失败", e);
        }
    }

    /**
     * 解密字符串 (默认 v1.0 AES-CBC 模式)
     * 
     * @param encryptedContent 加密的内容
     * @return 解密后的字符串
     */
    public String decrypt(byte[] encryptedContent) {
        return decryptByVersion(encryptedContent, VERSION_1_0);
    }

    /**
     * 根据指定协议版本解密字符串
     *
     * @param encryptedContent 加密的内容
     * @param version 客户端协议版本
     * @return 解密后的字符串
     */
    public String decryptByVersion(byte[] encryptedContent, String version) {
        try {
            if (!encryptionProperties.isEnabled()) {
                return new String(encryptedContent, StandardCharsets.UTF_8);
            }
            String cipherText = new String(encryptedContent, StandardCharsets.UTF_8);
            if (VERSION_2_0.equalsIgnoreCase(version)) {
                return AESGcmUtils.decrypt(cipherText, encryptionProperties.getKey());
            }
            // 兼容降级模式：v1.0 AES-CBC
            return AESUtils.decryptWithConfig(cipherText);
        } catch (Exception e) {
            log.error("按版本 [{}] 解密失败: {}", version, e.getMessage(), e);
            throw new RuntimeException("解密失败", e);
        }
    }

    /**
     * 检查是否启用加密
     * 
     * @return 是否启用加密
     */
    public boolean isEncryptionEnabled() {
        return encryptionProperties.isEnabled();
    }
} 