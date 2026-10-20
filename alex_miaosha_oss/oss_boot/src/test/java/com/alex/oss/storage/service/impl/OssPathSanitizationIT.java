package com.alex.oss.storage.service.impl;

import com.alex.api.oss.fileInfo.vo.FileInfoVo;
import com.alex.oss.config.s3.BaseS3Template;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OSS S3 存储对象键路径穿越清洗、特殊字符净化与目录安全性集成测试
 */
class OssPathSanitizationIT {

    private BaseS3Template mockTemplate;
    private TestS3FileService s3FileService;

    static class TestS3FileService extends AbstractS3FileService {
        private final BaseS3Template template;

        TestS3FileService(BaseS3Template template) {
            this.template = template;
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
            return "default-bucket";
        }
    }

    @BeforeEach
    void setUp() {
        mockTemplate = mock(BaseS3Template.class);
        s3FileService = new TestS3FileService(mockTemplate);
    }

    @ParameterizedTest
    @CsvSource({
            "'../../evil', 'evil'",
            "'..\\\\..\\\\user', 'user'",
            "'goods/../../hack', 'hack'",
            "'###', 'common'",
            "'', 'common'",
            "'   ', 'common'",
            "'finance_report', 'finance_report'",
            "'user-profile', 'user-profile'"
    })
    @DisplayName("业务类型净化：sanitizeType 必须剔除任何路径穿越字符与非法符号")
    void sanitizeType_shouldStripTraversalAndSpecialChars(String rawType, String expectedClean) {
        String cleanType = AbstractS3FileService.sanitizeType(rawType);
        assertEquals(expectedClean, cleanType);
    }

    @ParameterizedTest
    @CsvSource({
            "'../../etc/passwd.png', 'passwd'",
            "'..\\\\..\\\\windows\\\\system32\\\\cmd.png', 'cmd'",
            "'nested/dir/my_document.pdf', 'my_document'",
            "'avatar.png', 'avatar'",
            "'simple', 'simple'",
            "'', 'file'",
            "'   ', 'file'",
            "'...', 'file'"
    })
    @DisplayName("文件名净化：sanitizeBaseFileName 必须剥离路径前缀并提取纯净基础名称")
    void sanitizeBaseFileName_shouldExtractPureBaseName(String rawFileName, String expectedBaseName) {
        String baseName = AbstractS3FileService.sanitizeBaseFileName(rawFileName);
        assertEquals(expectedBaseName, baseName);
    }

    @Test
    @DisplayName("空字节防御：文件名包含 \\0 时必须被安全净化剥离")
    void sanitizeBaseFileName_nullByte_shouldBeStripped() {
        String rawWithNull = "avatar\0evil.png";
        String baseName = AbstractS3FileService.sanitizeBaseFileName(rawWithNull);
        assertEquals("avatarevil", baseName);
        assertFalse(baseName.contains("\0"), "文件名严禁残留空字节 \\0");
    }

    @Test
    @DisplayName("端到端路径穿越防御：uploadFile 接收穿越文件名和恶意 type 时，Object Key 强制约束在安全虚拟目录内")
    void uploadFile_withPathTraversalInputs_mustProduceSafeObjectKey() throws Exception {
        MockMultipartFile traversalFile = new MockMultipartFile(
                "file",
                "../../secret/config/database.png",
                "image/png",
                "test-png-payload".getBytes()
        );

        when(mockTemplate.upload(anyString(), anyString(), any(InputStream.class), anyLong(), anyString()))
                .thenReturn(Map.of("url", "dummy-url"));
        when(mockTemplate.thumbnail(anyString(), anyString(), any(InputStream.class), anyString()))
                .thenReturn(Map.of("url", "dummy-thumb"));

        FileInfoVo vo = s3FileService.uploadFile(traversalFile, "../../finance", false, true);
        assertNotNull(vo);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        // 验证恶意 type 导致存储桶安全降级到 default-bucket
        verify(mockTemplate).upload(eq("default-bucket"), keyCaptor.capture(), any(InputStream.class), anyLong(), anyString());

        String generatedKey = keyCaptor.getValue();
        assertNotNull(generatedKey);

        // 强断言：必须以合规 type 开头，绝不能包含 .. 穿越符，绝不能包含反斜杠
        assertTrue(generatedKey.startsWith("finance/"), "Object Key 必须以规范化的 finance/ 开头: " + generatedKey);
        assertFalse(generatedKey.contains(".."), "Object Key 严禁包含 .. 路径穿越符: " + generatedKey);
        assertFalse(generatedKey.contains("\\"), "Object Key 严禁包含 Windows 反斜杠: " + generatedKey);

        // 校验目录层级结构必须严格为三段: finance/{date}/database_{timestamp}.png
        String[] parts = generatedKey.split("/");
        assertEquals(3, parts.length, "Object Key 必须严格保持 3 段目录结构: " + generatedKey);
        assertEquals("finance", parts[0]);
        assertTrue(parts[1].matches("^\\d{4}-\\d{2}-\\d{2}$"), "二级目录为日期格式: " + parts[1]);
        assertTrue(parts[2].startsWith("database_") && parts[2].endsWith(".png"), "文件名提取净化为 database_{timestamp}.png: " + parts[2]);
    }
}
