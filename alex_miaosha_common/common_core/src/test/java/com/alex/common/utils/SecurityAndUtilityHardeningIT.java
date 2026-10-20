package com.alex.common.utils;

import com.alex.common.utils.date.DateUtils;
import com.alex.common.utils.ip.IpUtils;
import com.alex.common.utils.secret.AESGcmUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.crypto.AEADBadTagException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 集成测试：核心安全加密（AES-GCM）、时区安全日期格式化（DateUtils）与 IP 反向代理工具健壮性加固
 */
class SecurityAndUtilityHardeningIT {

    private static final String AES_KEY_16 = "1234567890123456"; // 16 bytes for AES-128

    @Test
    @DisplayName("AESGcmUtils: 加解密全生命周期与 IV 随机性（同一明文两次加密密文不同）")
    void testAesGcm_EncryptAndDecrypt() throws Exception {
        String plainText = "Secret_Confidential_Text_999";

        String cipherText1 = AESGcmUtils.encrypt(plainText, AES_KEY_16);
        String cipherText2 = AESGcmUtils.encrypt(plainText, AES_KEY_16);

        assertNotNull(cipherText1);
        assertNotNull(cipherText2);
        assertNotEquals(cipherText1, cipherText2, "由于使用了安全随机 IV，每次加密同一明文输出的密文必须不同");

        String decrypted1 = AESGcmUtils.decrypt(cipherText1, AES_KEY_16);
        String decrypted2 = AESGcmUtils.decrypt(cipherText2, AES_KEY_16);

        assertEquals(plainText, decrypted1);
        assertEquals(plainText, decrypted2);
    }

    @Test
    @DisplayName("AESGcmUtils: 密文单字节篡改时通过 AEAD 标签校验阻断攻击并抛出 AEADBadTagException")
    void testAesGcm_TamperedCiphertextThrowsAEADBadTagException() throws Exception {
        String plainText = "HighSecurityTransactionPayload";
        String cipherText = AESGcmUtils.encrypt(plainText, AES_KEY_16);

        byte[] rawBytes = Base64.getDecoder().decode(cipherText);
        // 篡改密文最后一位（认证标签 Tag 部分）
        rawBytes[rawBytes.length - 1] ^= 0x01;
        String tamperedCipher = Base64.getEncoder().encodeToString(rawBytes);

        Exception ex = assertThrows(Exception.class, () -> AESGcmUtils.decrypt(tamperedCipher, AES_KEY_16));
        assertTrue(ex instanceof AEADBadTagException || ex.getCause() instanceof AEADBadTagException,
                "密文被篡改后必须触发 AEADBadTagException 拒绝解密");
    }

    @Test
    @DisplayName("AESGcmUtils: 密文长度非法或小于最小字节限制时防御性拦截")
    void testAesGcm_IllegalLengthGuard() {
        // 小于 12(IV) + 16(Tag) = 28 字节
        byte[] tooShort = new byte[20];
        String base64Short = Base64.getEncoder().encodeToString(tooShort);

        assertThrows(IllegalArgumentException.class, () -> AESGcmUtils.decrypt(base64Short, AES_KEY_16));
    }

    @Test
    @DisplayName("DateUtils: 多线程高并发下基于不可变静态 Formatter 格式化线程安全")
    void testDateUtils_ConcurrentSafety() throws Exception {
        int threadCount = 10;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCounter = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        LocalDateTime now = DateUtils.now();
                        String formatted = DateUtils.getNowTimeStr();
                        assertNotNull(formatted);
                        assertEquals(19, formatted.length(), "yyyy-MM-dd HH:mm:ss 长度必须为 19");

                        String customFormatted = DateUtils.getTimeStr(now, DateUtils.YYYY_MM_DD);
                        assertEquals(10, customFormatted.length(), "yyyy-MM-dd 长度必须为 10");
                        successCounter.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean finished = latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();
        assertTrue(finished, "多线程并发测试超时");
        assertEquals(threadCount * operationsPerThread, successCounter.get(), "所有并发格式化必须 100% 成功");
    }

    @Test
    @DisplayName("DateUtils: 当天起止时间转换边界严格无偏移")
    void testDateUtils_StartAndEndTime() {
        String startTimeStr = DateUtils.getNowTimeStrStartTime();
        assertNotNull(startTimeStr);
        assertTrue(startTimeStr.endsWith("00:00:00"), "当天起始时间必须以 00:00:00 结尾");

        Long nowMilli = DateUtils.getNowTimeLong();
        assertNotNull(nowMilli);
        assertTrue(nowMilli > 1600000000000L, "毫秒时间戳应符合正常现代纪元范围");
    }

    @Test
    @DisplayName("IpUtils: 多级反向代理 X-Forwarded-For 场景下准确提取首个真实客户端 IP")
    void testIpUtils_XForwardedForMultiProxy() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        // 模拟 CDN/Nginx 多级转发：真实IP, CDN代理1, 反向代理2
        request.addHeader("x-forwarded-for", "203.0.113.195, 198.51.100.1, 10.0.0.1");

        String clientIp = IpUtils.getIpAddr(request);
        assertEquals("203.0.113.195", clientIp, "必须截取第一跳 IP 作为客户端真实 IP");
    }

    @Test
    @DisplayName("IpUtils: User-Agent 解析客户端操作系统与浏览器类型")
    void testIpUtils_OsAndBrowserExtraction() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36");

        Map<String, String> info = IpUtils.getOsAndBrowserInfo(request);
        assertNotNull(info);
        assertEquals("Windows", info.get("OS"));
        assertTrue(info.get("BROWSER").contains("Chrome"));
    }

    @Test
    @DisplayName("IpUtils: null 请求防御性处理返回空字符串而不崩溃")
    void testIpUtils_NullRequestHandling() throws Exception {
        String ip = IpUtils.getIpAddr(null);
        assertEquals("", ip, "入参 request 为 null 时应安全返回空字符串");
    }
}
