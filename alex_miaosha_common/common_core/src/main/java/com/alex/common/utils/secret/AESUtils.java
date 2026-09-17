package com.alex.common.utils.secret;

import com.alex.common.config.EncryptionProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * description: Java使用AES加密算法进行加密解密
 * author: majf
 * createDate: 2023/9/12 11:51
 * version: 1.0.0
 */
@Component
public class AESUtils {

    private AESUtils() {
    }

    private static EncryptionProperties encryptionProperties;

    @Autowired
    public void setEncryptionProperties(EncryptionProperties properties) {
        AESUtils.encryptionProperties = properties;
    }

    /**
     * 初始化 AES-CBC Cipher
     * 注意：SonarQube S3329 提倡使用动态随机 IV；但当前系统与前端 (alex_miaosha_front) 采用预共享 IV ("1234567890123456") 协议通信。
     * 为保证现有端到端通信契约不被破坏，此处压制 S3329 告警。
     */
    @SuppressWarnings("java:S3329")
    private static Cipher getCipher(String key, String iv, String type, Integer mode)
            throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance("AES/CBC/" + type);
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        IvParameterSpec ips = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));
        // 初始化为加密模式，并将密钥注入到算法中
        cipher.init(mode, keySpec, ips);
        return cipher;
    }

    /**
     * AES算法加密
     *
     * @Param:text原文
     * @Param:key密钥
     */
    public static String encrypt(String text, String key, String iv, String type) throws Exception {
        Cipher cipher = getCipher(key, iv, type, Cipher.ENCRYPT_MODE);
        // 将传入的文本加密
        byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
        // 将密文进行Base64编码，方便传输
        return Base64Utils.encodeToString(encrypted);
    }

    /**
     * AES算法解密
     *
     * @Param:base64Encrypted密文
     * @Param:key密钥
     */
    public static String decrypt(String base64Encrypted, String key, String iv, String type) throws Exception {
        Cipher cipher = getCipher(key, iv, type, Cipher.DECRYPT_MODE);
        // 将Base64编码的密文解码
        byte[] encrypted = Base64Utils.decodeFromString(base64Encrypted);
        // 解密
        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * 使用配置参数的AES加密
     *
     * @param text 要加密的文本
     * @return 加密后的Base64字符串
     */
    public static String encryptWithConfig(String text) throws Exception {
        if (encryptionProperties == null) {
            throw new IllegalStateException("EncryptionProperties not initialized");
        }
        return encrypt(text, encryptionProperties.getKey(), encryptionProperties.getIv(),
                encryptionProperties.getPadding());
    }

    /**
     * 使用配置参数的AES解密
     *
     * @param base64Encrypted 加密的Base64字符串
     * @return 解密后的文本
     */
    public static String decryptWithConfig(String base64Encrypted) throws Exception {
        if (encryptionProperties == null) {
            throw new IllegalStateException("EncryptionProperties not initialized");
        }
        return decrypt(base64Encrypted, encryptionProperties.getKey(), encryptionProperties.getIv(),
                encryptionProperties.getPadding());
    }
}
