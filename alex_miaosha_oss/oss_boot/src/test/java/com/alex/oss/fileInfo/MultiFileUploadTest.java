package com.alex.oss.fileInfo;

import com.alex.api.oss.fileInfo.vo.FileInfoVo;
import com.alex.common.exception.FileException;
import com.alex.oss.fileInfo.entity.FileInfo;
import com.alex.oss.fileInfo.mapper.FileInfoMapper;
import com.alex.oss.fileInfo.service.impl.FileInfoServiceImp;
import com.alex.oss.storage.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 多附件上传单元测试
 * 覆盖：
 * 1. 多附件并行上传成功并装配 preUrl
 * 2. 违规扩展名白名单拦截
 * 3. 超过 9 个附件配额拦截
 * 4. 空文件列表拦截
 * 5. Saga 补偿机制（DB 插入异常时触发 S3 文件自动清理）
 */
class MultiFileUploadTest {

    private FileInfoMapper fileInfoMapper;
    private FileStorageService minioFileService;
    private FileInfoServiceImp fileInfoService;

    @BeforeEach
    void setUp() {
        fileInfoMapper = mock(FileInfoMapper.class);
        minioFileService = mock(FileStorageService.class);

        Map<String, FileStorageService> map = new HashMap<>();
        map.put("minioFileService", minioFileService);

        fileInfoService = new FileInfoServiceImp(fileInfoMapper, map);
        ReflectionTestUtils.setField(fileInfoService, "activeFileSystem", "minio");
    }

    @Test
    @DisplayName("验证多附件并发上传成功并补全预签名预览直链")
    void testUploadMultipleFiles_Success() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile("files", "avatar.png", "image/png", "img1-bytes".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "doc.pdf", "application/pdf", "pdf-bytes".getBytes());

        FileInfoVo vo1 = new FileInfoVo();
        vo1.setFileName("avatar.png");
        vo1.setUrl("user/2026-09-18/avatar.png");
        vo1.setThumbnailUrl("user/2026-09-18/thumb_avatar.png");
        vo1.setFileSystem("minio");
        vo1.setBucketName("user-bucket");

        FileInfoVo vo2 = new FileInfoVo();
        vo2.setFileName("doc.pdf");
        vo2.setUrl("user/2026-09-18/doc.pdf");
        vo2.setFileSystem("minio");
        vo2.setBucketName("user-bucket");

        when(minioFileService.uploadFile(eq(file1), eq("user"), eq(true), eq(true))).thenReturn(vo1);
        when(minioFileService.uploadFile(eq(file2), eq("user"), eq(true), eq(true))).thenReturn(vo2);

        when(minioFileService.preview(eq("user-bucket"), eq("user/2026-09-18/avatar.png")))
                .thenReturn("http://s3.local/preview/avatar.png");
        when(minioFileService.preview(eq("user-bucket"), eq("user/2026-09-18/thumb_avatar.png")))
                .thenReturn("http://s3.local/preview/thumb_avatar.png");
        when(minioFileService.preview(eq("user-bucket"), eq("user/2026-09-18/doc.pdf")))
                .thenReturn("http://s3.local/preview/doc.pdf");

        doAnswer(invocation -> {
            FileInfo entity = invocation.getArgument(0);
            entity.setId(System.currentTimeMillis());
            return 1;
        }).when(fileInfoMapper).insert(any(FileInfo.class));

        List<FileInfoVo> results = fileInfoService.uploadMultipleFiles("user", Arrays.asList(file1, file2), true, true);

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("http://s3.local/preview/avatar.png", results.get(0).getPreUrl());
        assertEquals("http://s3.local/preview/thumb_avatar.png", results.get(0).getPreThumbnailUrl());
        assertEquals("http://s3.local/preview/doc.pdf", results.get(1).getPreUrl());
        verify(fileInfoMapper, times(2)).insert(any(FileInfo.class));
    }

    @Test
    @DisplayName("验证上传违规可执行扩展名时被白名单拦截")
    void testUploadMultipleFiles_DisallowedExtension() {
        MockMultipartFile badFile = new MockMultipartFile("files", "danger.exe", "application/octet-stream", "bad-bytes".getBytes());
        MockMultipartFile normalFile = new MockMultipartFile("files", "good.png", "image/png", "good-bytes".getBytes());

        FileException ex = assertThrows(FileException.class, () ->
                fileInfoService.uploadMultipleFiles("user", Arrays.asList(normalFile, badFile), true, true));

        assertTrue(ex.getMsg() != null && ex.getMsg().contains("不支持的文件格式: .exe"));
        verifyNoInteractions(minioFileService);
        verifyNoInteractions(fileInfoMapper);
    }

    @Test
    @DisplayName("验证单次批量上传超过 9 个附件被拦截")
    void testUploadMultipleFiles_ExceedMaxBatchCount() {
        List<MultipartFile> tenFiles = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tenFiles.add(new MockMultipartFile("files", "file" + i + ".png", "image/png", new byte[]{1}));
        }

        FileException ex = assertThrows(FileException.class, () ->
                fileInfoService.uploadMultipleFiles("user", tenFiles, true, true));

        assertTrue(ex.getMsg() != null && ex.getMsg().contains("单次最多支持上传 9 个附件"));
        verifyNoInteractions(minioFileService);
        verifyNoInteractions(fileInfoMapper);
    }

    @Test
    @DisplayName("验证空文件列表参数异常拦截")
    void testUploadMultipleFiles_EmptyList() {
        assertThrows(FileException.class, () ->
                fileInfoService.uploadMultipleFiles("user", Collections.emptyList(), true, true));

        assertThrows(FileException.class, () ->
                fileInfoService.uploadMultipleFiles("user", null, true, true));
    }

    @Test
    @DisplayName("验证数据库插入失败时触发 Saga 补偿删除孤儿 S3 文件")
    void testUploadMultipleFiles_SagaCompensatingRollback() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile("files", "avatar.png", "image/png", "img1-bytes".getBytes());

        FileInfoVo vo1 = new FileInfoVo();
        vo1.setFileName("avatar.png");
        vo1.setUrl("user/2026-09-18/avatar.png");
        vo1.setThumbnailUrl("user/2026-09-18/thumb_avatar.png");
        vo1.setFileSystem("minio");
        vo1.setBucketName("user-bucket");

        when(minioFileService.uploadFile(eq(file1), eq("user"), eq(true), eq(true))).thenReturn(vo1);
        doThrow(new RuntimeException("DB Connection Pool Timeout")).when(fileInfoMapper).insert(any(FileInfo.class));

        assertThrows(RuntimeException.class, () ->
                fileInfoService.uploadMultipleFiles("user", Collections.singletonList(file1), true, true));

        // 验证 Saga 补偿机制执行：删除已上传的 S3 原图与缩略图路径
        verify(minioFileService, times(1)).deleteFile(argThat(list ->
                list.contains("user/2026-09-18/avatar.png") && list.contains("user/2026-09-18/thumb_avatar.png")
        ), eq("user"));
    }
}
