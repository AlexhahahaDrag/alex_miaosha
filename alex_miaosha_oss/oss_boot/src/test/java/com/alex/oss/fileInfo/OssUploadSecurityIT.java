package com.alex.oss.fileInfo;

import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.FileException;
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
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * OSS 文件上传安全过滤、白名单校验与防御机制集成测试
 */
class OssUploadSecurityIT {

    private FileInfoServiceImp fileInfoService;
    private Method validateSingleFileExtensionMethod;
    private Method validateBatchFilesMethod;

    @BeforeEach
    void setUp() throws Exception {
        FileInfoMapper mapper = mock(FileInfoMapper.class);
        FileStorageService storageService = mock(FileStorageService.class);
        Map<String, FileStorageService> map = Map.of("minioFileService", storageService);

        fileInfoService = new FileInfoServiceImp(mapper, map);
        ReflectionTestUtils.setField(fileInfoService, "activeFileSystem", "minio");

        validateSingleFileExtensionMethod = FileInfoServiceImp.class.getDeclaredMethod("validateSingleFileExtension", MultipartFile.class);
        validateSingleFileExtensionMethod.setAccessible(true);

        validateBatchFilesMethod = FileInfoServiceImp.class.getDeclaredMethod("validateBatchFiles", List.class);
        validateBatchFilesMethod.setAccessible(true);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "webshell.jsp", "evil.php", "attack.sh", "malware.bat",
            "trojan.exe", "backdoor.py", "script.vbs", "exploit.dll"
    })
    @DisplayName("安全拦截：危险可执行/脚本文件格式必须被白名单机制强制拒绝")
    void dangerousExecutableFileExtensions_mustBeRejected(String dangerousFilename) {
        MockMultipartFile file = new MockMultipartFile("file", dangerousFilename, "application/octet-stream", "evil-bytes".getBytes());

        FileException ex = assertThrows(FileException.class, () -> {
            try {
                validateSingleFileExtensionMethod.invoke(fileInfoService, file);
            } catch (Exception e) {
                if (e.getCause() instanceof FileException fe) {
                    throw fe;
                }
                throw e;
            }
        });

        assertEquals(ResultEnum.PARAM_ERROR.getCode(), ex.getCode());
        assertTrue(ex.getMsg().contains("不支持的文件格式"), "必须明确提示不支持的文件格式: " + ex.getMsg());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "avatar.png", "photo.jpg", "banner.jpeg", "anim.gif", "icon.svg", "sample.webp", "drawing.bmp",
            "report.pdf", "data.xlsx", "sheet.xls", "doc.docx", "legacy.doc", "readme.txt",
            "archive.zip", "compressed.rar", "table.csv"
    })
    @DisplayName("白名单放行：标准图片、文档与压缩格式合规放行")
    void allowedFileExtensions_shouldPassValidation(String validFilename) {
        MockMultipartFile file = new MockMultipartFile("file", validFilename, "application/octet-stream", "valid-content".getBytes());

        assertDoesNotThrow(() -> {
            validateSingleFileExtensionMethod.invoke(fileInfoService, file);
        }, "合规白名单文件不得抛出异常: " + validFilename);
    }

    @Test
    @DisplayName("配额防御：多附件批量上传超过 9 个上限时立即阻断")
    void batchUpload_exceedingNineFiles_mustBeRejected() {
        List<MultipartFile> files = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            files.add(new MockMultipartFile("files", "img_" + i + ".png", "image/png", ("content-" + i).getBytes()));
        }

        FileException ex = assertThrows(FileException.class, () -> {
            try {
                validateBatchFilesMethod.invoke(fileInfoService, files);
            } catch (Exception e) {
                if (e.getCause() instanceof FileException fe) {
                    throw fe;
                }
                throw e;
            }
        });

        assertEquals(ResultEnum.PARAM_ERROR.getCode(), ex.getCode());
        assertTrue(ex.getMsg().contains("最多支持上传 9 个附件"), "超额提示信息必须明确: " + ex.getMsg());
    }

    @Test
    @DisplayName("空文件防御：0 字节空文件与空列表必须抛出 IMAGE_NO_FOUNT 异常")
    void emptyAndZeroByteFiles_mustBeRejected() {
        // 1. 0 字节文件
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);
        FileException ex1 = assertThrows(FileException.class, () -> {
            try {
                validateSingleFileExtensionMethod.invoke(fileInfoService, emptyFile);
            } catch (Exception e) {
                if (e.getCause() instanceof FileException fe) {
                    throw fe;
                }
                throw e;
            }
        });
        assertEquals(ResultEnum.IMAGE_NO_FOUNT.getCode(), ex1.getCode());

        // 2. 空列表
        FileException ex2 = assertThrows(FileException.class, () -> {
            try {
                validateBatchFilesMethod.invoke(fileInfoService, Collections.emptyList());
            } catch (Exception e) {
                if (e.getCause() instanceof FileException fe) {
                    throw fe;
                }
                throw e;
            }
        });
        assertEquals(ResultEnum.IMAGE_NO_FOUNT.getCode(), ex2.getCode());

        // 3. 单文件上传为 null
        assertThrows(FileException.class, () -> {
            fileInfoService.addFileInfo("user", null, true, true);
        });
    }

    @Test
    @DisplayName("路径穿越防御：文件名含 ../ 路径穿越攻击时扩展名探测拒绝非法格式")
    void pathTraversal_fileName_mustBeRejected() {
        MockMultipartFile attackFile = new MockMultipartFile("file", "../../etc/passwd", "text/plain", "root:x:0:0".getBytes());

        FileException ex = assertThrows(FileException.class, () -> {
            try {
                validateSingleFileExtensionMethod.invoke(fileInfoService, attackFile);
            } catch (Exception e) {
                if (e.getCause() instanceof FileException fe) {
                    throw fe;
                }
                throw e;
            }
        });
        assertEquals(ResultEnum.PARAM_ERROR.getCode(), ex.getCode());
    }
}
