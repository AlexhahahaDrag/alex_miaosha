package com.alex.oss.minio.service.impl;

import com.alex.api.oss.fileInfo.vo.FileInfoVo;
import com.alex.base.constants.SysConf;
import com.alex.common.enums.BucketNameEnum;
import com.alex.common.enums.FileSystemTypeEnum;
import com.alex.common.utils.date.DateUtils;
import com.alex.common.utils.string.StringUtils;
import com.alex.oss.config.garage.GarageTemplate;
import com.alex.oss.minio.service.MinioFileService;
import io.minio.errors.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
 * description:  garage文件服务实现类
 * author:       alex
 * createDate:   2026/03/02
 * version:      1.0.0
 */
@Service("garageFileService")
@AllArgsConstructor
@Slf4j
public class GarageFileServiceImpl implements MinioFileService {

    private final GarageTemplate garageTemplate;

    private static final String YYYYMMDD = "YYYY-MM-dd";

    @Override
    public FileInfoVo uploadFile(MultipartFile file, String type) throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        FileInfoVo fileVo = new FileInfoVo();
        String fileName = file.getOriginalFilename();
        fileVo.setFileName(fileName);
        fileVo.setFileSize(file.getSize());
        String suffixStr = StringUtils.isBlank(fileName) ? null : fileName.substring(fileName.lastIndexOf('.') + 1);
        fileVo.setFileType(suffixStr);
        String bucketName = getBucket(type);
        fileVo.setBucketName(bucketName);
        fileVo.setFileSystem(FileSystemTypeEnum.GARAGE.getCode());
        String filename = type + "/" + DateUtils.getNowTimeStr(YYYYMMDD) + "/" +
                (StringUtils.isBlank(fileName) ? "" : fileName.substring(0, fileName.lastIndexOf('.'))) +
                SysConf.UNDERLINE + DateUtils.getNowTimeLong() + SysConf.POINT + suffixStr;
        InputStream inputStream = file.getInputStream();
        Map<String, String> stringStringMap = garageTemplate.thumbnail(bucketName, filename, inputStream, file.getContentType());
        fileVo.setThumbnailUrl(stringStringMap.get("url"));
        Map<String, String> upload = garageTemplate.upload(bucketName, filename, inputStream, file.getContentType());
        fileVo.setUrl(upload.get("url"));
        stopWatch.stop();
        log.info("Garage上传耗时：{}", stopWatch.getTotalTimeMillis());
        return fileVo;
    }

    @Override
    public boolean deleteFile(List<String> filePath, String type) throws Exception {
        if (filePath == null || filePath.isEmpty()) {
            throw new Exception("文件路径不能为空");
        }
        Map<String, String> stringStringMap = garageTemplate.removeObjects(getBucket(type), filePath);
        return stringStringMap.get("mes") != null;
    }

    @Override
    public InputStream fileDownload(FileInfoVo fileInfo) {
        return garageTemplate.fileDownload(fileInfo.getBucketName(), fileInfo.getUrl());
    }

    private String getBucket(String type) {
        return switch (StringUtils.isEmpty(type) ? "" : type) {
            case "user" -> BucketNameEnum.USER_BUCKET.getValue();
            case "goods" -> BucketNameEnum.GOODS_BUCKET.getValue();
            case "common" -> BucketNameEnum.COMMON_BUCKET.getValue();
            default -> type + "-bucket";
        };
    }

    @Override
    public String preview(String bucketName, String objectName) throws IOException, InvalidResponseException, InvalidKeyException, NoSuchAlgorithmException, ServerException, ErrorResponseException, XmlParserException, InsufficientDataException, InternalException {
        return garageTemplate.preview(bucketName, objectName);
    }

    @Override
    public FileInfoVo thumbnail(MultipartFile file, String type) throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        FileInfoVo fileVo = new FileInfoVo();
        String fileName = file.getOriginalFilename();
        fileVo.setFileName(fileName);
        fileVo.setFileSize(file.getSize());
        String suffixStr = StringUtils.isBlank(fileName) ? null : fileName.substring(fileName.lastIndexOf('.') + 1);
        fileVo.setFileType(suffixStr);
        String bucketName = getBucket(type);
        fileVo.setBucketName(bucketName);
        fileVo.setFileSystem(FileSystemTypeEnum.GARAGE.getCode());
        String filename = type + "/" + DateUtils.getNowTimeStr(YYYYMMDD) + "/" +
                (StringUtils.isBlank(fileName) ? "" : fileName.substring(0, fileName.lastIndexOf('.'))) +
                SysConf.UNDERLINE + DateUtils.getNowTimeLong() + SysConf.POINT + suffixStr;
        InputStream inputStream = file.getInputStream();
        Map<String, String> stringStringMap = garageTemplate.thumbnail(bucketName, filename, inputStream, file.getContentType());
        fileVo.setThumbnailUrl(stringStringMap.get("url"));
        stopWatch.stop();
        log.info("Garage缩略图生成耗时：{}", stopWatch.getTotalTimeMillis());
        return fileVo;
    }
}
