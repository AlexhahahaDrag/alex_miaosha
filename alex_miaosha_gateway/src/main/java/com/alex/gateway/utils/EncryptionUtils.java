package com.alex.gateway.utils;

import com.alex.common.config.EncryptionProperties;
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
 * @version 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EncryptionUtils {

    private final EncryptionProperties encryptionProperties;

    /**
     * 加密字符串
     * 
     * @param content 要加密的内容
     * @return 加密后的字节数组
     */
    public byte[] encrypt(String content) {
        try {
            if (!encryptionProperties.isEnabled()) {
                return content.getBytes(StandardCharsets.UTF_8);
            }
            
            // 使用配置化的 AES 加密
            String encrypted = AESUtils.encryptWithConfig(content);
            return encrypted.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("加密失败: {}", e.getMessage(), e);
            throw new RuntimeException("加密失败", e);
        }
    }

    /**
     * 解密字符串
     * 
     * @param encryptedContent 加密的内容
     * @return 解密后的字符串
     */
    public String decrypt(byte[] encryptedContent) {
        try {
            if (!encryptionProperties.isEnabled()) {
                return new String(encryptedContent, StandardCharsets.UTF_8);
            }
            // 使用配置化的AES解密
            return AESUtils.decryptWithConfig(new String(encryptedContent, StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("解密失败: {}", e.getMessage(), e);
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