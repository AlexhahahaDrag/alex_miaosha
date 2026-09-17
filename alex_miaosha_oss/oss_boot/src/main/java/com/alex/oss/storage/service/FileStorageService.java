package com.alex.oss.storage.service;

import com.alex.api.oss.fileInfo.vo.FileInfoVo;
import com.alex.common.exception.FileException;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * description:  通用文件存储服务接口（支持 MinIO、Garage 等 S3 兼容后端）
 * author:       majf, alex
 * createDate:   2023/1/12 14:40
 * version:      2.1.0
 */
public interface FileStorageService {

    FileInfoVo uploadFile(MultipartFile file, String type) throws FileException;

    FileInfoVo uploadFile(MultipartFile file, String type, Boolean isThumbnail, Boolean isNormal) throws FileException;

    boolean deleteFile(List<String> filePath, String type) throws FileException;

    InputStream fileDownload(FileInfoVo fileInfo);

    String preview(String bucketName, String objectName) throws FileException;
}
