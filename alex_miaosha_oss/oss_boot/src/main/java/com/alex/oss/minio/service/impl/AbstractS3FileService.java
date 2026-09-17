package com.alex.oss.minio.service.impl;

import com.alex.api.oss.fileInfo.vo.FileInfoVo;
import com.alex.base.constants.SysConf;
import com.alex.common.enums.BucketNameEnum;
import com.alex.common.utils.date.DateUtils;
import com.alex.common.utils.string.StringUtils;
import com.alex.oss.config.s3.BaseS3Template;
import com.alex.oss.minio.service.MinioFileService;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
 * description: S3 协议文件服务通用抽象类
 * 统一收敛文件上传命名规则、缩略图生成、流式上传大小传递（使用 file.getSize() 彻底杜绝 available() 截断）、文件下载与预览
 *
 * @author majf, alex
 * @version 2.0.0
 */
@Slf4j
public abstract class AbstractS3FileService implements MinioFileService {

    protected static final String YYYYMMDD = "YYYY-MM-dd";

    /**
     * 获取当前实现的 S3 模板实例
     */
    public abstract BaseS3Template getTemplate();

    /**
     * 获取文件系统编码（如 minio, garage）
     */
    protected abstract String getFileSystemCode();

    /**
     * 获取配置的默认存储桶名称
     */
    protected abstract String getDefaultBucketName();

    @Override
    public FileInfoVo uploadFile(MultipartFile file, String type) throws Exception {
        return uploadFile(file, type, true, true);
    }

    @Override
    public FileInfoVo uploadFile(MultipartFile file, String type, Boolean isThumbnail, Boolean isNormal) throws Exception {
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
        fileVo.setFileSystem(getFileSystemCode());

        // 名称为/分隔的时候，会在 S3 存储中创建虚拟目录去存储文件
        String filename = type + "/" + DateUtils.getNowTimeStr(YYYYMMDD) + "/" +
                (StringUtils.isBlank(fileName) ? "" : fileName.substring(0, fileName.lastIndexOf('.'))) +
                SysConf.UNDERLINE + DateUtils.getNowTimeLong() + SysConf.POINT + suffixStr;

        if (Boolean.TRUE.equals(isThumbnail)) {
            Map<String, String> stringStringMap = getTemplate().thumbnail(bucketName, filename, file.getInputStream(), file.getContentType());
            fileVo.setThumbnailUrl(stringStringMap.get("url"));
        }
        if (Boolean.TRUE.equals(isNormal)) {
            // 修复：明确传入 file.getSize() 作为对象长度，避免使用 inputStream.available() 导致的大文件截断
            Map<String, String> upload = getTemplate().upload(bucketName, filename, file.getInputStream(), file.getSize(), file.getContentType());
            fileVo.setUrl(upload.get("url"));
        }
        stopWatch.stop();
        log.info("[{}] 上传耗时：{} ms, 桶: {}, 路径: {}", getFileSystemCode(), stopWatch.getTotalTimeMillis(), bucketName, filename);
        return fileVo;
    }

    @Override
    public boolean deleteFile(List<String> filePath, String type) throws Exception {
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("删除文件路径不能为空");
        }
        Map<String, String> stringStringMap = getTemplate().removeObjects(getBucket(type), filePath);
        return stringStringMap.get("mes") != null;
    }

    @Override
    public InputStream fileDownload(FileInfoVo fileInfo) {
        return getTemplate().fileDownload(fileInfo.getBucketName(), fileInfo.getUrl());
    }

    @Override
    public String preview(String bucketName, String objectName) throws IOException, InvalidResponseException, InvalidKeyException, NoSuchAlgorithmException, ServerException, ErrorResponseException, XmlParserException, InsufficientDataException, InternalException {
        return getTemplate().preview(bucketName, objectName);
    }

    /**
     * 根据业务类型解析 Bucket 名称
     */
    protected String getBucket(String type) {
        String defaultBucket = getDefaultBucketName();
        return switch (StringUtils.isEmpty(type) ? "" : type) {
            case "user" -> BucketNameEnum.USER_BUCKET.getValue();
            case "goods" -> BucketNameEnum.GOODS_BUCKET.getValue();
            case "common" -> BucketNameEnum.COMMON_BUCKET.getValue();
            default -> StringUtils.isNotBlank(defaultBucket) ? defaultBucket : type + "-bucket";
        };
    }
}
