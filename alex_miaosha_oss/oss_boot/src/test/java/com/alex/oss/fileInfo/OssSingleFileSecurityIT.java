package com.alex.oss.fileInfo;

import com.alex.api.oss.fileInfo.vo.FileInfoVo;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.FileException;
import com.alex.oss.fileInfo.entity.FileInfo;
import com.alex.oss.fileInfo.mapper.FileInfoMapper;
import com.alex.oss.fileInfo.service.impl.FileInfoServiceImp;
import com.alex.oss.storage.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OSS 单文件上传与修改安全白名单校验、空文件防御与状态集成测试
 */
class OssSingleFileSecurityIT {

    private FileInfoServiceImp fileInfoService;
    private FileInfoMapper fileInfoMapper;
    private FileStorageService storageService;

    @BeforeEach
    void setUp() {
        fileInfoMapper = mock(FileInfoMapper.class);
        storageService = mock(FileStorageService.class);
        Map<String, FileStorageService> map = Map.of("minioFileService", storageService);

        fileInfoService = spy(new FileInfoServiceImp(fileInfoMapper, map));
        ReflectionTestUtils.setField(fileInfoService, "activeFileSystem", "minio");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "webshell.jsp", "trojan.exe", "backdoor.php", "script.sh",
            "batch.bat", "worm.py", "macro.vbs", "exploit.dll"
    })
    @DisplayName("单文件新增拦截：addFileInfo 必须前置阻断非法脚本与可执行文件")
    void addFileInfo_dangerousExtension_mustBeRejected(String dangerousName) {
        MockMultipartFile file = new MockMultipartFile("file", dangerousName, "application/octet-stream", "bad-bytes".getBytes());

        FileException ex = assertThrows(FileException.class, () ->
                fileInfoService.addFileInfo("user", file, true, true)
        );

        assertEquals(ResultEnum.PARAM_ERROR.getCode(), ex.getCode());
        assertTrue(ex.getMsg().contains("不支持的文件格式"), "必须明确提示不支持的格式: " + ex.getMsg());
        verify(storageService, never()).uploadFile(any(), any(), anyBoolean(), anyBoolean());
        verify(fileInfoMapper, never()).insert(any(FileInfo.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "webshell.jsp", "evil.exe", "hack.php", "rootkit.sh"
    })
    @DisplayName("单文件更新拦截：updateFileInfo 传入非法脚本文件时必须强制拦截且不触发底层上传")
    void updateFileInfo_dangerousExtension_mustBeRejected(String dangerousName) {
        FileInfo mockEntity = new FileInfo();
        mockEntity.setId(100L);
        mockEntity.setUrl("user/2026-10-20/original.png");
        doReturn(mockEntity).when(fileInfoService).getById(100L);

        MockMultipartFile attackFile = new MockMultipartFile("file", dangerousName, "application/octet-stream", "bad-bytes".getBytes());

        FileException ex = assertThrows(FileException.class, () ->
                fileInfoService.updateFileInfo(100L, "user", attackFile, true, true)
        );

        assertEquals(ResultEnum.PARAM_ERROR.getCode(), ex.getCode());
        assertTrue(ex.getMsg().contains("不支持的文件格式"), "必须明确提示不支持的格式: " + ex.getMsg());
        verify(storageService, never()).uploadFile(any(), any(), anyBoolean(), anyBoolean());
        verify(fileInfoMapper, never()).updateById(any(FileInfo.class));
    }

    @Test
    @DisplayName("单文件新增放行：合规白名单文件成功上传并入库")
    void addFileInfo_validExtension_shouldSucceed() throws Exception {
        MockMultipartFile validFile = new MockMultipartFile("file", "avatar.png", "image/png", "valid-png-data".getBytes());

        FileInfoVo mockVo = new FileInfoVo();
        mockVo.setUrl("user/2026-10-20/avatar_123.png");
        mockVo.setThumbnailUrl("user/2026-10-20/thumb_avatar_123.png");
        mockVo.setBucketName("user-bucket");
        mockVo.setFileSystem("minio");

        when(storageService.uploadFile(eq(validFile), eq("user"), eq(true), eq(true))).thenReturn(mockVo);
        when(fileInfoMapper.queryFileInfo(any())).thenReturn(mockVo);

        FileInfoVo result = fileInfoService.addFileInfo("user", validFile, true, true);

        assertNotNull(result);
        assertEquals("user/2026-10-20/avatar_123.png", result.getUrl());
        verify(fileInfoMapper).insert(any(FileInfo.class));
    }

    @Test
    @DisplayName("单文件更新放行：合规白名单文件成功覆盖更新")
    void updateFileInfo_validExtension_shouldSucceed() throws Exception {
        FileInfo existingEntity = new FileInfo();
        existingEntity.setId(200L);
        existingEntity.setUrl("user/2026-10-20/old.jpg");
        doReturn(existingEntity).when(fileInfoService).getById(200L);

        MockMultipartFile newFile = new MockMultipartFile("file", "new_avatar.jpg", "image/jpeg", "new-bytes".getBytes());

        FileInfoVo mockUploaded = new FileInfoVo();
        mockUploaded.setUrl("user/2026-10-20/new_avatar_456.jpg");
        mockUploaded.setBucketName("user-bucket");
        mockUploaded.setFileSystem("minio");

        when(storageService.uploadFile(eq(newFile), eq("user"), eq(true), eq(true))).thenReturn(mockUploaded);

        FileInfoVo result = fileInfoService.updateFileInfo(200L, "user", newFile, true, true);

        assertNotNull(result);
        assertEquals("user/2026-10-20/new_avatar_456.jpg", result.getUrl());
        verify(fileInfoMapper).updateById(existingEntity);
    }

    @Test
    @DisplayName("单文件更新边界：传 file == null 时仅更新元数据，不校验扩展名与上传")
    void updateFileInfo_nullFile_shouldSkipUpload() {
        FileInfo existingEntity = new FileInfo();
        existingEntity.setId(300L);
        existingEntity.setUrl("user/2026-10-20/keep.png");
        doReturn(existingEntity).when(fileInfoService).getById(300L);

        FileInfoVo result = fileInfoService.updateFileInfo(300L, "user", null, true, true);

        assertNotNull(result);
        assertEquals(300L, result.getId());
        verify(storageService, never()).uploadFile(any(), any(), anyBoolean(), anyBoolean());
        verify(fileInfoMapper).updateById(existingEntity);
    }

    @Test
    @DisplayName("单文件更新边界：记录不存在时抛出 SYSTEM_NO_AVAILABLE 业务异常")
    void updateFileInfo_recordNotFound_mustThrow() {
        doReturn(null).when(fileInfoService).getById(999L);
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "bytes".getBytes());

        FileException ex = assertThrows(FileException.class, () ->
                fileInfoService.updateFileInfo(999L, "user", file, true, true)
        );

        assertEquals(ResultEnum.SYSTEM_NO_AVAILABLE.getCode(), ex.getCode());
        assertTrue(ex.getMsg().contains("文件记录不存在"));
    }

    @Test
    @DisplayName("安全防御：空字节截断与空文件名拦截")
    void nullByteAndEmptyName_mustBeRejected() {
        // 1. 空字节截断攻击
        MockMultipartFile nullByteFile = new MockMultipartFile("file", "test.png\0.jsp", "image/png", "evil".getBytes());
        FileException ex1 = assertThrows(FileException.class, () ->
                fileInfoService.addFileInfo("user", nullByteFile, true, true)
        );
        assertEquals(ResultEnum.PARAM_ERROR.getCode(), ex1.getCode());
        assertTrue(ex1.getMsg().contains("非法截断字符"));

        // 2. 空文件名
        MockMultipartFile blankNameFile = new MockMultipartFile("file", "   ", "image/png", "data".getBytes());
        FileException ex2 = assertThrows(FileException.class, () ->
                fileInfoService.addFileInfo("user", blankNameFile, true, true)
        );
        assertEquals(ResultEnum.PARAM_ERROR.getCode(), ex2.getCode());
        assertTrue(ex2.getMsg().contains("文件名不能为空"));
    }
}
