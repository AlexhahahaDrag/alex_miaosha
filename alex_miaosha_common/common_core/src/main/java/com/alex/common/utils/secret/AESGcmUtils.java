package com.alex.common.utils.secret;

import com.alex.common.config.EncryptionProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * description: 基于 AES-GCM (AEAD) 模式的高安全性加解密工具类
 * 规范：NIST SP 800-38D
 * - 每次加密使用 SecureRandom 生成全新的 12 字节 IV (Nonce)
 * - 认证标签 (Auth Tag) 长度为 128 位 (16 字节)
 * - 报文封装：Base64(12 字节 IV + GCM 密文含 Tag)
 *
 * author: alex
 * createDate: 2026/09/18
 * version: 2.0.0
 */
@Component
public class AESGcmUtils {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static EncryptionProperties encryptionProperties;

    AESGcmUtils() {
    }

    @Autowired
    public void setEncryptionProperties(EncryptionProperties properties) {
        AESGcmUtils.encryptionProperties = properties;
    }

    public static void setStaticProperties(EncryptionProperties properties) {
        AESGcmUtils.encryptionProperties = properties;
    }

    /**
     * AES-GCM 加密
     *
     * @param text 原文明文
     * @param key 密钥字符串
     * @return Base64(12 字节 IV + 密文含 Tag)
     * @throws Exception 加密异常
     */
    public static String encrypt(String text, String key) throws Exception {
        if (text == null) {
            return null;
        }
        byte[] iv = new byte[IV_LENGTH_BYTE];
        SECURE_RANDOM.nextBytes(iv);

        SecretKey secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BIT, iv));

        byte[] cipherTextWithTag = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));

        ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherTextWithTag.length);
        buffer.put(iv);
        buffer.put(cipherTextWithTag);

        return Base64.getEncoder().encodeToString(buffer.array());
    }

    /**
     * AES-GCM 解密
     *
     * @param base64Encrypted Base64 编码的 (12 字节 IV + 密文含 Tag)
     * @param key 密钥字符串
     * @return 解密后的原文字符串
     * @throws Exception 解密异常或密文被篡改时抛出 AEADBadTagException
     */
    public static String decrypt(String base64Encrypted, String key) throws Exception {
        if (base64Encrypted == null) {
            return null;
        }
        byte[] decoded = Base64.getDecoder().decode(base64Encrypted);
        if (decoded.length < IV_LENGTH_BYTE + 16) {
            throw new IllegalArgumentException("GCM 密文长度非法，小于最小长度 (12 字节 IV + 16 字节 Tag)");
        }

        byte[] iv = new byte[IV_LENGTH_BYTE];
        System.arraycopy(decoded, 0, iv, 0, IV_LENGTH_BYTE);

        int cipherLength = decoded.length - IV_LENGTH_BYTE;
        byte[] cipherTextWithTag = new byte[cipherLength];
        System.arraycopy(decoded, IV_LENGTH_BYTE, cipherTextWithTag, 0, cipherLength);

        SecretKey secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BIT, iv));

        byte[] decrypted = cipher.doFinal(cipherTextWithTag);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * 使用默认配置密钥进行 GCM 加密
     *
     * @param text 原文明文
     * @return 加密后 Base64 字符串
     * @throws Exception 加密异常
     */
    public static String encryptWithConfig(String text) throws Exception {
        if (encryptionProperties == null) {
            throw new IllegalStateException("EncryptionProperties not initialized");
        }
        return encrypt(text, encryptionProperties.getKey());
    }

    /**
     * 使用默认配置密钥进行 GCM 解密
     *
     * @param base64Encrypted Base64 密文
     * @return 解密后原文字符串
     * @throws Exception 解密异常
     */
    public static String decryptWithConfig(String base64Encrypted) throws Exception {
        if (encryptionProperties == null) {
            throw new IllegalStateException("EncryptionProperties not initialized");
        }
        return decrypt(base64Encrypted, encryptionProperties.getKey());
    }
}
