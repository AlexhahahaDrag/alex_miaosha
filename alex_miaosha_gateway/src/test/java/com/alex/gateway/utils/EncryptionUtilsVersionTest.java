package com.alex.gateway.utils;

import com.alex.common.config.EncryptionProperties;
import com.alex.common.utils.secret.AESGcmUtils;
import com.alex.common.utils.secret.AESUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionUtilsVersionTest {

    private EncryptionUtils encryptionUtils;

    @BeforeEach
    void setUp() {
        EncryptionProperties properties = new EncryptionProperties();
        properties.setKey("20230610HelloDog");
        properties.setIv("1234567890123456");
        properties.setPadding("PKCS5Padding");
        properties.setEnabled(true);

        AESUtils.setStaticProperties(properties);
        AESGcmUtils.setStaticProperties(properties);
        encryptionUtils = new EncryptionUtils(properties);
    }

    @Test
    @DisplayName("版本协商：未指定版本默认降级为 v1.0 (AES-CBC)")
    void testDefaultVersionLegacyCbc() {
        String plain = "{\"msg\":\"legacy_test\"}";
        byte[] encryptedBytes = encryptionUtils.encrypt(plain);
        String encryptedStr = new String(encryptedBytes, StandardCharsets.UTF_8);

        // 使用 v1.0 解密应能还原
        String decrypted = encryptionUtils.decryptByVersion(encryptedBytes, "1.0");
        assertEquals(plain, decrypted);
    }

    @Test
    @DisplayName("版本协商：显式指定 2.0 走 AES-GCM (AEAD) 并能正确解密")
    void testVersion20Gcm() {
        String plain = "{\"msg\":\"gcm_v2_test\"}";
        byte[] encryptedBytes = encryptionUtils.encryptByVersion(plain, EncryptionUtils.VERSION_2_0);

        // 使用 2.0 解密应还原
        String decrypted = encryptionUtils.decryptByVersion(encryptedBytes, EncryptionUtils.VERSION_2_0);
        assertEquals(plain, decrypted);
    }

    @Test
    @DisplayName("未启用加密时原样透传")
    void testDisabledEncryption() {
        EncryptionProperties properties = new EncryptionProperties();
        properties.setEnabled(false);
        EncryptionUtils disabledUtils = new EncryptionUtils(properties);

        String plain = "raw_data";
        byte[] encryptedBytes = disabledUtils.encryptByVersion(plain, "2.0");
        assertEquals(plain, new String(encryptedBytes, StandardCharsets.UTF_8));
    }
}
