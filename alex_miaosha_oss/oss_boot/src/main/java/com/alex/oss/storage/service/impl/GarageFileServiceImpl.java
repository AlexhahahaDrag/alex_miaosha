package com.alex.oss.storage.service.impl;

import com.alex.common.enums.FileSystemTypeEnum;
import com.alex.oss.config.garage.GarageTemplate;
import com.alex.oss.config.s3.BaseS3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * description:  Garage 文件服务实现类，继承自统一的 AbstractS3FileService
 *
 * @author alex
 * @version 2.1.0
 */
@Service("garageFileService")
@RequiredArgsConstructor
@Slf4j
public class GarageFileServiceImpl extends AbstractS3FileService {

    private final GarageTemplate garageTemplate;

    @Override
    public BaseS3Template getTemplate() {
        return garageTemplate;
    }

    @Override
    protected String getFileSystemCode() {
        return FileSystemTypeEnum.GARAGE.getCode();
    }

    @Override
    protected String getDefaultBucketName() {
        return garageTemplate.getGarageProperties() != null ? garageTemplate.getGarageProperties().getBucketName() : null;
    }
}
