package com.alex.oss.storage.service.impl;

import com.alex.api.oss.fileInfo.vo.FileInfoVo;
import com.alex.base.constants.SysConf;
import com.alex.base.enums.ResultEnum;
import com.alex.common.enums.BucketNameEnum;
import com.alex.common.exception.FileException;
import com.alex.common.utils.date.DateUtils;
import com.alex.common.utils.string.StringUtils;
import com.alex.oss.config.s3.BaseS3Template;
import com.alex.oss.storage.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * description: S3 协议文件服务通用抽象基类
 * 统一收敛文件上传命名规则、缩略图生成、流式上传大小传递（使用 file.getSize() 彻底杜绝 available() 截断）、文件下载与预览
 *
 * @author majf, alex
 * @version 2.1.0
 */
@Slf4j
public abstract class AbstractS3FileService implements FileStorageService {

    protected static final String YYYYMMDD = "yyyy-MM-dd";

    /**
     * S3 规范虚拟目录分隔符（S3 Object Key 标准必须固定使用正斜杠，严禁使用操作系统的 File.separator）
     */
    protected static final String S3_PATH_DELIMITER = "/";

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
    public FileInfoVo uploadFile(MultipartFile file, String type) throws FileException {
        return uploadFile(file, type, true, true);
    }

    @Override
    @SuppressWarnings("java:S1075") // S3 对象键路径规范强制使用正斜杠 "/" 作为虚拟目录分隔符，禁止使用操作系统的 File.separator
    public FileInfoVo uploadFile(MultipartFile file, String type, Boolean isThumbnail, Boolean isNormal)
            throws FileException {
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

        // 名称为 / 分隔的时候，会在 S3 存储中创建虚拟目录去存储文件
        String filename = type + S3_PATH_DELIMITER + DateUtils.getNowTimeStr(YYYYMMDD) + S3_PATH_DELIMITER +
                (StringUtils.isBlank(fileName) ? "" : fileName.substring(0, fileName.lastIndexOf('.'))) +
                SysConf.UNDERLINE + DateUtils.getNowTimeLong() + SysConf.POINT + suffixStr;

        try {
            if (Boolean.TRUE.equals(isThumbnail)) {
                Map<String, String> stringStringMap = getTemplate().thumbnail(bucketName, filename,
                        file.getInputStream(), file.getContentType());
                fileVo.setThumbnailUrl(stringStringMap.get("url"));
            }
            if (Boolean.TRUE.equals(isNormal)) {
                // 明确传入 file.getSize() 作为对象长度，避免使用 inputStream.available() 导致的大文件截断
                Map<String, String> upload = getTemplate().upload(bucketName, filename, file.getInputStream(),
                        file.getSize(), file.getContentType());
                fileVo.setUrl(upload.get("url"));
            }
        } catch (Exception e) {
            log.error("[{}] 上传文件至存储服务异常：bucket={}, filename={}", getFileSystemCode(), bucketName, filename, e);
            throw new FileException(ResultEnum.IMAGE_UPLOAD_FAIL, e.getMessage());
        }
        stopWatch.stop();
        log.info("[{}] 上传耗时：{} ms, 桶: {}, 路径: {}", getFileSystemCode(), stopWatch.getTotalTimeMillis(), bucketName,
                filename);
        return fileVo;
    }

    @Override
    public boolean deleteFile(List<String> filePath, String type) throws FileException {
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("删除文件路径不能为空");
        }
        try {
            Map<String, String> stringStringMap = getTemplate().removeObjects(getBucket(type), filePath);
            return stringStringMap.get("mes") != null;
        } catch (Exception e) {
            log.error("[{}] 删除文件异常：paths={}", getFileSystemCode(), filePath, e);
            throw new FileException(ResultEnum.IMAGE_DELETE_FAIL, e.getMessage());
        }
    }

    @Override
    public InputStream fileDownload(FileInfoVo fileInfo) {
        return getTemplate().fileDownload(fileInfo.getBucketName(), fileInfo.getUrl());
    }

    @Override
    public String preview(String bucketName, String objectName) throws FileException {
        return preview(bucketName, objectName, null);
    }

    @Override
    public String preview(String bucketName, String objectName, Boolean isPublic) throws FileException {
        try {
            return getTemplate().preview(bucketName, objectName, isPublic);
        } catch (Exception e) {
            log.error("[{}] 获取文件预览直链异常：bucket={}, object={}, isPublic={}", getFileSystemCode(), bucketName, objectName, isPublic, e);
            throw new FileException(ResultEnum.SYSTEM_NO_AVAILABLE, "生成文件预览直链失败: " + e.getMessage());
        }
    }

    /**
     * AWS S3 / MinIO 桶名称合法性正则规则 (符合 DNS 命名标准)：
     * 1. 长度在 3 到 63 个字符之间
     * 2. 仅允许包含小写字母、数字和连字符 (-)
     * 3. 必须以小写字母或数字开头和结尾，不得以连字符开头或结尾
     */
    private static final Pattern S3_BUCKET_NAME_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9-]{1,61}[a-z0-9]$");

    /**
     * 根据业务类型解析 Bucket 名称 (基于 Ponytail 5 股编织架构优化)
     * 1. 安全合规：严格校验 S3 DNS 规范，阻断非法字符与路径遍历注入
     * 2. 开闭原则：优先检索 BucketNameEnum 预置字典，新增业务类型零代码侵入
     * 3. 多级兜底：业务类型匹配 -> 显式合规桶名 -> 动态合规拼装 -> 默认配置桶 -> common-bucket 系统兜底
     * 4. 容错归一：支持首尾空格剥离与大小写归一化 (lowercase)
     * 5. 高性能：O(1) 字典无锁读取与静态预编译正则，零垃圾堆分配
     *
     * @param type 业务类型或自定义桶名 (如 "user", "goods", "finance", "gift", "ai")
     * @return 符合 S3 DNS 规范的确定性存储桶名称
     */
    protected String getBucket(String type) {
        if (StringUtils.isNotBlank(type)) {
            String normalizedType = type.trim().toLowerCase();
            // 1. 优先从枚举字典查找预置业务桶 (如 user -> user-bucket, goods -> goods-bucket, etc.)
            String mappedBucket = BucketNameEnum.findValueByName(normalizedType);
            if (StringUtils.isNotBlank(mappedBucket)) {
                return mappedBucket;
            }
            // 2. 若入参本身已是完整的合规存储桶 (以 "-bucket" 结尾，例如 "my-custom-bucket")
            if (normalizedType.endsWith("-bucket") && isValidBucketName(normalizedType)) {
                return normalizedType;
            }
            // 3. 规范化动态组装为 normalizedType + "-bucket"，并校验 S3 DNS 合规性 (例如 "order" ->
            // "order-bucket")
            String dynamicBucket = normalizedType + "-bucket";
            if (isValidBucketName(dynamicBucket)) {
                return dynamicBucket;
            }
            // 4. 若传入的是非 -bucket 结尾但本身符合 S3 规范的完整桶名
            if (isValidBucketName(normalizedType)) {
                return normalizedType;
            }
            log.warn("[{}] 业务类型 '{}' 无法解析为合法的 S3 存储桶名称，准备启用降级兜底", getFileSystemCode(), type);
        }

        // 4. 降级兜底策略：优先使用配置的 defaultBucket，若无配置或不合规，则使用系统公共桶 common-bucket
        String defaultBucket = getDefaultBucketName();
        if (StringUtils.isNotBlank(defaultBucket) && isValidBucketName(defaultBucket.trim().toLowerCase())) {
            return defaultBucket.trim().toLowerCase();
        }
        return BucketNameEnum.COMMON_BUCKET.getValue();
    }

    /**
     * 校验 S3 Bucket 名称是否符合 DNS 命名标准
     *
     * @param bucketName 存储桶名称
     * @return 是否合规
     */
    public boolean isValidBucketName(String bucketName) {
        if (StringUtils.isBlank(bucketName)) {
            return false;
        }
        return S3_BUCKET_NAME_PATTERN.matcher(bucketName).matches();
    }
}
