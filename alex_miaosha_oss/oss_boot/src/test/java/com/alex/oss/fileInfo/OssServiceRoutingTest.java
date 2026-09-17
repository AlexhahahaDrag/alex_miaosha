package com.alex.oss.fileInfo;

import com.alex.api.oss.fileInfo.vo.FileInfoVo;
import com.alex.oss.config.garage.GarageProperties;
import com.alex.oss.config.garage.GarageTemplate;
import com.alex.oss.config.minio.MinioProperties;
import com.alex.oss.config.minio.MinioTemplate;
import com.alex.oss.fileInfo.mapper.FileInfoMapper;
import com.alex.oss.fileInfo.service.impl.FileInfoServiceImp;
import com.alex.oss.minio.service.MinioFileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 验证 OSS 存储多后端策略路由与降级兜底逻辑，以及 MinioTemplate 启动容错能力
 */
class OssServiceRoutingTest {

    private FileInfoMapper fileInfoMapper;
    private MinioFileService minioFileService;
    private MinioFileService garageFileService;
    private FileInfoServiceImp fileInfoService;

    @BeforeEach
    void setUp() {
        fileInfoMapper = mock(FileInfoMapper.class);
        minioFileService = mock(MinioFileService.class);
        garageFileService = mock(MinioFileService.class);

        Map<String, MinioFileService> map = new HashMap<>();
        map.put("minioFileService", minioFileService);
        map.put("garageFileService", garageFileService);

        fileInfoService = new FileInfoServiceImp(fileInfoMapper, map);
        ReflectionTestUtils.setField(fileInfoService, "activeFileSystem", "garage");
    }

    @Test
    @DisplayName("验证根据 file_system=minio 路由到 minioFileService")
    void testRouteToMinio() {
        FileInfoVo vo = new FileInfoVo();
        vo.setId(101L);
        vo.setFileSystem("minio");
        vo.setBucketName("user-bucket");
        vo.setUrl("user/2026-09-17/test.png");

        when(fileInfoMapper.queryFileInfo(101L)).thenReturn(vo);
        when(minioFileService.fileDownload(vo)).thenReturn(new ByteArrayInputStream("minio-bytes".getBytes(StandardCharsets.UTF_8)));

        InputStream is = fileInfoService.fileDownload(101L);
        assertNotNull(is);
        verify(minioFileService, times(1)).fileDownload(vo);
        verify(garageFileService, never()).fileDownload(any());
    }

    @Test
    @DisplayName("验证根据 file_system=garage 路由到 garageFileService")
    void testRouteToGarage() {
        FileInfoVo vo = new FileInfoVo();
        vo.setId(102L);
        vo.setFileSystem("garage");
        vo.setBucketName("common-bucket");
        vo.setUrl("common/2026-09-17/file.pdf");

        when(fileInfoMapper.queryFileInfo(102L)).thenReturn(vo);
        when(garageFileService.fileDownload(vo)).thenReturn(new ByteArrayInputStream("garage-bytes".getBytes(StandardCharsets.UTF_8)));

        InputStream is = fileInfoService.fileDownload(102L);
        assertNotNull(is);
        verify(garageFileService, times(1)).fileDownload(vo);
        verify(minioFileService, never()).fileDownload(any());
    }

    @Test
    @DisplayName("验证当未指定 file_system 时自动兜底到 activeFileSystem (garage)")
    void testRouteToActiveFallback() {
        FileInfoVo vo = new FileInfoVo();
        vo.setId(103L);
        vo.setFileSystem(null); // 未知或历史空字段
        vo.setBucketName("alex-miaosha");
        vo.setUrl("goods/2026-09-17/item.png");

        when(fileInfoMapper.queryFileInfo(103L)).thenReturn(vo);
        when(garageFileService.fileDownload(vo)).thenReturn(new ByteArrayInputStream("active-bytes".getBytes(StandardCharsets.UTF_8)));

        InputStream is = fileInfoService.fileDownload(103L);
        assertNotNull(is);
        verify(garageFileService, times(1)).fileDownload(vo);
    }

    @Test
    @DisplayName("验证 MinioTemplate 与 GarageTemplate 在未提供配置时优雅跳过，不抛异常崩溃")
    void testTemplateGracefulInitWithoutConfig() {
        MinioProperties minioProperties = new MinioProperties();
        MinioTemplate minioTemplate = new MinioTemplate(minioProperties);
        // 执行生命周期回调，验证不抛出 IllegalArgumentException
        assertDoesNotThrow(minioTemplate::afterPropertiesSet);
        assertFalse(minioTemplate.isInitialized());

        GarageProperties garageProperties = new GarageProperties();
        GarageTemplate garageTemplate = new GarageTemplate(garageProperties);
        assertDoesNotThrow(garageTemplate::afterPropertiesSet);
        assertFalse(garageTemplate.isInitialized());
    }
}
