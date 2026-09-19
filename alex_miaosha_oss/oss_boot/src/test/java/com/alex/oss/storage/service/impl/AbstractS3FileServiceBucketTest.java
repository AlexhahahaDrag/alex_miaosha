package com.alex.oss.storage.service.impl;

import com.alex.common.enums.BucketNameEnum;
import com.alex.oss.config.s3.BaseS3Template;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * 验证 AbstractS3FileService#getBucket 的 Ponytail 5 股编织架构优化：
 * 1. 预置枚举精准匹配 (user, goods, common, finance, gift, ai)
 * 2. 大小写与前后空白符归一化容错
 * 3. 完整合规桶名直通
 * 4. 动态扩展拼装业务桶
 * 5. 恶意字符与路径穿越阻断防注入
 * 6. 空值在有配置桶时的安全降级
 * 7. 空值在无配置桶时的系统基线 common-bucket 兜底 (根治 "-bucket" 缺陷)
 * 8. 不合规配置桶的自我纠偏防御
 * 9. S3 DNS 桶名格式校验规则 (isValidBucketName)
 */
class AbstractS3FileServiceBucketTest {

    static class TestS3Service extends AbstractS3FileService {
        private final BaseS3Template template = mock(BaseS3Template.class);
        private String defaultBucketName;

        public void setDefaultBucketName(String defaultBucketName) {
            this.defaultBucketName = defaultBucketName;
        }

        @Override
        public BaseS3Template getTemplate() {
            return template;
        }

        @Override
        protected String getFileSystemCode() {
            return "test-s3";
        }

        @Override
        protected String getDefaultBucketName() {
            return defaultBucketName;
        }

        // 暴露出受保护方法以便单元测试
        public String resolveBucket(String type) {
            return getBucket(type);
        }
    }

    @Test
    @DisplayName("1. 验证预置枚举字典快速映射 (user, goods, common, finance, gift, ai)")
    void testPresetEnumBuckets() {
        TestS3Service service = new TestS3Service();
        assertEquals("user-bucket", service.resolveBucket("user"));
        assertEquals("goods-bucket", service.resolveBucket("goods"));
        assertEquals("common-bucket", service.resolveBucket("common"));
        assertEquals("finance-bucket", service.resolveBucket("finance"));
        assertEquals("gift-bucket", service.resolveBucket("gift"));
        assertEquals("ai-bucket", service.resolveBucket("ai"));
    }

    @Test
    @DisplayName("2. 验证大小写与前后空格归一化容错 (USER,  goods , FiNaNcE)")
    void testCaseAndWhitespaceNormalization() {
        TestS3Service service = new TestS3Service();
        assertEquals("user-bucket", service.resolveBucket(" USER "));
        assertEquals("goods-bucket", service.resolveBucket("  goods  "));
        assertEquals("finance-bucket", service.resolveBucket("FiNaNcE"));
        assertEquals("ai-bucket", service.resolveBucket(" AI "));
    }

    @Test
    @DisplayName("3. 验证显式传入合规完整存储桶名称时直接直通")
    void testDirectValidBucketName() {
        TestS3Service service = new TestS3Service();
        assertEquals("my-custom-bucket", service.resolveBucket("my-custom-bucket"));
        assertEquals("company-backup-bucket", service.resolveBucket("company-backup-bucket"));
    }

    @Test
    @DisplayName("4. 验证未在枚举中的合法业务标识动态拼装为 -bucket")
    void testDynamicAssembleBucket() {
        TestS3Service service = new TestS3Service();
        assertEquals("order-bucket", service.resolveBucket("order"));
        assertEquals("report-bucket", service.resolveBucket("report"));
        assertEquals("log-archive-bucket", service.resolveBucket("log-archive"));
    }

    @Test
    @DisplayName("5. 验证恶意字符与路径穿越注入时被阻断并安全降级到配置桶")
    void testIllegalCharactersFallbackToDefault() {
        TestS3Service service = new TestS3Service();
        service.setDefaultBucketName("alex-default");

        // 包含下划线、特殊符号、路径遍历等非法字符
        assertEquals("alex-default", service.resolveBucket("bad_name!@#"));
        assertEquals("alex-default", service.resolveBucket("../../etc/passwd"));
        assertEquals("alex-default", service.resolveBucket("..\\windows\\system32"));
        assertEquals("alex-default", service.resolveBucket("UPPER_CASE_AND_BAD_CHARS!"));
    }

    @Test
    @DisplayName("6. 验证入参为空且配置了 defaultBucket 时平滑返回配置桶")
    void testNullOrBlankTypeWithDefaultBucket() {
        TestS3Service service = new TestS3Service();
        service.setDefaultBucketName("alex-default");

        assertEquals("alex-default", service.resolveBucket(null));
        assertEquals("alex-default", service.resolveBucket(""));
        assertEquals("alex-default", service.resolveBucket("   "));
    }

    @Test
    @DisplayName("7. 验证入参为空且无默认配置时兜底为 common-bucket (彻底根治历史 -bucket 缺陷)")
    void testNullOrBlankTypeWithoutDefaultBucket() {
        TestS3Service service = new TestS3Service();
        service.setDefaultBucketName(null);

        assertEquals(BucketNameEnum.COMMON_BUCKET.getValue(), service.resolveBucket(null));
        assertEquals(BucketNameEnum.COMMON_BUCKET.getValue(), service.resolveBucket(""));
        assertEquals(BucketNameEnum.COMMON_BUCKET.getValue(), service.resolveBucket("   "));
        assertNotEquals("-bucket", service.resolveBucket(null));
        assertNotEquals("-bucket", service.resolveBucket(""));
    }

    @Test
    @DisplayName("8. 验证配置桶本身不符合 S3 规范时的自我纠偏防御")
    void testInvalidDefaultBucketFallbackToCommon() {
        TestS3Service service = new TestS3Service();
        // 传入不合规的配置桶名 (以连字符开头结尾、过短或含特殊字符)
        service.setDefaultBucketName("-bad-configured-bucket-");

        assertEquals(BucketNameEnum.COMMON_BUCKET.getValue(), service.resolveBucket(null));
        assertEquals(BucketNameEnum.COMMON_BUCKET.getValue(), service.resolveBucket("illegal#type"));
    }

    @Test
    @DisplayName("9. 验证 S3 DNS 桶名格式校验规则 (isValidBucketName)")
    void testIsValidBucketName() {
        TestS3Service service = new TestS3Service();

        // 合法命名 (3-63位小写字母、数字、中划线，首尾必须是字母数字)
        assertTrue(service.isValidBucketName("user-bucket"));
        assertTrue(service.isValidBucketName("alex123"));
        assertTrue(service.isValidBucketName("a-b-c-9"));
        assertTrue(service.isValidBucketName("123-bucket"));

        // 非法命名
        assertFalse(service.isValidBucketName(null));
        assertFalse(service.isValidBucketName(""));
        assertFalse(service.isValidBucketName("   "));
        assertFalse(service.isValidBucketName("ab")); // 小于3字符
        assertFalse(service.isValidBucketName("a".repeat(64))); // 大于63字符
        assertFalse(service.isValidBucketName("-start-with-hyphen")); // 连字符开头
        assertFalse(service.isValidBucketName("end-with-hyphen-")); // 连字符结尾
        assertFalse(service.isValidBucketName("has_underscore")); // 含下划线
        assertFalse(service.isValidBucketName("UPPERCASE")); // 大写字母
        assertFalse(service.isValidBucketName("path/traversal")); // 斜杠
        assertFalse(service.isValidBucketName("has..dots")); // 连续点
    }

    @Test
    @DisplayName("10. 验证 BucketNameEnum 公私分级判定 (isPublicBucket)")
    void testBucketPublicClassification() {
        // 公开桶
        assertTrue(BucketNameEnum.isPublicBucket("user"));
        assertTrue(BucketNameEnum.isPublicBucket("user-bucket"));
        assertTrue(BucketNameEnum.isPublicBucket(" USER "));
        assertTrue(BucketNameEnum.isPublicBucket(" goods "));
        assertTrue(BucketNameEnum.isPublicBucket("goods-bucket"));

        // 私有桶
        assertFalse(BucketNameEnum.isPublicBucket("common"));
        assertFalse(BucketNameEnum.isPublicBucket("common-bucket"));
        assertFalse(BucketNameEnum.isPublicBucket("finance"));
        assertFalse(BucketNameEnum.isPublicBucket("finance-bucket"));
        assertFalse(BucketNameEnum.isPublicBucket("gift"));
        assertFalse(BucketNameEnum.isPublicBucket("gift-bucket"));
        assertFalse(BucketNameEnum.isPublicBucket("ai-bucket"));

        // 未知或空值
        assertFalse(BucketNameEnum.isPublicBucket(null));
        assertFalse(BucketNameEnum.isPublicBucket(""));
        assertFalse(BucketNameEnum.isPublicBucket("unknown-bucket"));
    }
}
