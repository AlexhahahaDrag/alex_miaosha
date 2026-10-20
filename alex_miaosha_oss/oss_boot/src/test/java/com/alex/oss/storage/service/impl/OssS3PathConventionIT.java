package com.alex.oss.storage.service.impl;

import com.alex.api.oss.fileInfo.vo.FileInfoVo;
import com.alex.oss.config.s3.BaseS3Template;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * S3/MinIO 对象存储路径规范与环境一致性契约测试
 */
class OssS3PathConventionIT {

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

    @Test
    @DisplayName("S3 路径分隔符规范：Object Key 必须强制使用正斜杠 '/'，绝不出现 Windows 反斜杠 '\\'")
    void s3Delimiter_mustAlwaysBeForwardSlash() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "test-bytes".getBytes());

        when(mockTemplate.upload(anyString(), anyString(), any(InputStream.class), anyLong(), anyString()))
                .thenReturn(Map.of("url", "user/2026-10-20/avatar_123.png"));
        when(mockTemplate.thumbnail(anyString(), anyString(), any(InputStream.class), anyString()))
                .thenReturn(Map.of("url", "user/2026-10-20/thumb_avatar_123.png"));

        FileInfoVo vo = s3FileService.uploadFile(file, "user", true, true);

        assertNotNull(vo);
        ArgumentCaptor<String> filenameCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockTemplate).upload(eq("user-bucket"), filenameCaptor.capture(), any(InputStream.class), anyLong(), anyString());

        String objectKey = filenameCaptor.getValue();
        assertNotNull(objectKey, "生成的 S3 Object Key 不可为空");

        // 强断言不含 Windows 反斜杠
        assertFalse(objectKey.contains("\\"), "S3 Object Key 绝不可包含操作系统反斜杠 '\\': " + objectKey);
        assertTrue(objectKey.startsWith("user/"), "Object Key 必须以业务类型和正斜杠开头: " + objectKey);

        // 验证日期目录结构：user/yyyy-MM-dd/...
        String[] parts = objectKey.split("/");
        assertEquals(3, parts.length, "Object Key 必须按 {type}/{date}/{filename} 三段式组织: " + objectKey);
        assertTrue(parts[1].matches("^\\d{4}-\\d{2}-\\d{2}$"), "二级目录必须遵循 yyyy-MM-dd 日期格式: " + parts[1]);
        assertTrue(parts[2].startsWith("avatar_") && parts[2].endsWith(".png"), "文件名需包含原文件名和时间戳: " + parts[2]);
    }

    @Test
    @DisplayName("预览直链契约：支持 isPublic 参数透传给底层 S3 模板")
    void previewUrl_delegation_publicVsSigned() throws Exception {
        when(mockTemplate.preview(eq("finance-bucket"), eq("invoice.pdf"), eq(true)))
                .thenReturn("http://s3.local/finance-bucket/invoice.pdf");
        when(mockTemplate.preview(eq("finance-bucket"), eq("invoice.pdf"), eq(false)))
                .thenReturn("http://s3.local/finance-bucket/invoice.pdf?X-Amz-Signature=xyz");

        // 1. 公开直链 (isPublic = true)
        String publicUrl = s3FileService.preview("finance-bucket", "invoice.pdf", true);
        assertEquals("http://s3.local/finance-bucket/invoice.pdf", publicUrl);

        // 2. 签名直链 (isPublic = false)
        String signedUrl = s3FileService.preview("finance-bucket", "invoice.pdf", false);
        assertEquals("http://s3.local/finance-bucket/invoice.pdf?X-Amz-Signature=xyz", signedUrl);

        verify(mockTemplate, times(1)).preview("finance-bucket", "invoice.pdf", true);
        verify(mockTemplate, times(1)).preview("finance-bucket", "invoice.pdf", false);
    }
}
