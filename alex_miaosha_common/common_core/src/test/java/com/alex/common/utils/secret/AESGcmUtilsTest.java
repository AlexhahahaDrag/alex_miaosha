package com.alex.common.utils.secret;

import com.alex.common.config.EncryptionProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.AEADBadTagException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class AESGcmUtilsTest {

    private static final String TEST_KEY = "20230610HelloDog";

    @BeforeEach
    void setUp() {
        EncryptionProperties properties = new EncryptionProperties();
        properties.setKey(TEST_KEY);
        AESGcmUtils.setStaticProperties(properties);
    }

    @Test
    @DisplayName("GCM 加密与解密成功还原原文")
    void testEncryptAndDecryptSuccess() throws Exception {
        String plain = "{\"code\":200,\"message\":\"success\",\"data\":{\"userId\":123456789}}";
        String encrypted = AESGcmUtils.encrypt(plain, TEST_KEY);

        assertNotNull(encrypted);
        assertNotEquals(plain, encrypted);

        String decrypted = AESGcmUtils.decrypt(encrypted, TEST_KEY);
        assertEquals(plain, decrypted);
    }

    @Test
    @DisplayName("相同明文连续加密生成的密文因随机 IV 而不同（防重放与防字典猜测）")
    void testEncryptGeneratesDifferentCiphertextForSamePlain() throws Exception {
        String plain = "HelloWorld";
        String cipher1 = AESGcmUtils.encrypt(plain, TEST_KEY);
        String cipher2 = AESGcmUtils.encrypt(plain, TEST_KEY);

        assertNotEquals(cipher1, cipher2);
        assertEquals(plain, AESGcmUtils.decrypt(cipher1, TEST_KEY));
        assertEquals(plain, AESGcmUtils.decrypt(cipher2, TEST_KEY));
    }

    @Test
    @DisplayName("密文遭恶意篡改时应拒绝解密并抛出 AEADBadTagException")
    void testTamperedCiphertextThrowsException() throws Exception {
        String plain = "SecretFinancialRecord";
        String encrypted = AESGcmUtils.encrypt(plain, TEST_KEY);

        byte[] raw = Base64.getDecoder().decode(encrypted);
        // 篡改密文主体中的一个字节
        raw[raw.length - 5] ^= 0x55;
        String tampered = Base64.getEncoder().encodeToString(raw);

        assertThrows(AEADBadTagException.class, () -> AESGcmUtils.decrypt(tampered, TEST_KEY));
    }

    @Test
    @DisplayName("密文长度小于最小长度 (12 字节 IV + 16 字节 Tag) 抛出 IllegalArgumentException")
    void testInvalidCiphertextLengthThrowsException() {
        byte[] shortBytes = new byte[15];
        String shortBase64 = Base64.getEncoder().encodeToString(shortBytes);

        assertThrows(IllegalArgumentException.class, () -> AESGcmUtils.decrypt(shortBase64, TEST_KEY));
    }

    @Test
    @DisplayName("null 入参安全处理")
    void testNullHandling() throws Exception {
        assertNull(AESGcmUtils.encrypt(null, TEST_KEY));
        assertNull(AESGcmUtils.decrypt(null, TEST_KEY));
    }

    @Test
    @DisplayName("基于 Spring 配置的 encryptWithConfig 和 decryptWithConfig 正常运行")
    void testConfigEncryption() throws Exception {
        String plain = "ConfigBasedData";
        String encrypted = AESGcmUtils.encryptWithConfig(plain);
        String decrypted = AESGcmUtils.decryptWithConfig(encrypted);

        assertEquals(plain, decrypted);
    }
}
