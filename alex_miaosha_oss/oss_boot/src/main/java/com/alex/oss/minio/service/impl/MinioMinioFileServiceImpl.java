package com.alex.oss.minio.service.impl;

import com.alex.common.enums.FileSystemTypeEnum;
import com.alex.oss.config.minio.MinioTemplate;
import com.alex.oss.config.s3.BaseS3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * description:  MinIO 文件服务实现类，继承自统一的 AbstractS3FileService
 *
 * @author majf, alex
 * @version 2.0.0
 */
@Service("minioFileService")
@RequiredArgsConstructor
@Slf4j
public class MinioMinioFileServiceImpl extends AbstractS3FileService {

    private final MinioTemplate minioTemplate;

    @Override
    public BaseS3Template getTemplate() {
        return minioTemplate;
    }

    @Override
    protected String getFileSystemCode() {
        return FileSystemTypeEnum.MINIO.getCode();
    }

    @Override
    protected String getDefaultBucketName() {
        return minioTemplate.getMinioProperties() != null ? minioTemplate.getMinioProperties().getBucketName() : null;
    }
}
