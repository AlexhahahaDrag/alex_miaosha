package com.alex.oss.config.s3;

import com.alex.common.enums.BucketNameEnum;
import com.alex.common.utils.string.StringUtils;
import com.alex.oss.storage.vo.ObjectItem;
import com.alibaba.fastjson.JSONObject;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.tasks.UnsupportedFormatException;
import org.apache.commons.lang3.BooleanUtils;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * description: S3 协议通用基础模板抽象类（兼容 MinIO 与 Garage 等 S3 兼容对象存储）
 * 包含：Bucket 内存缓存机制、流式传输安全规约、预签名公网 CDN 域名映射、流及时释放规约、存储桶公私分级直通
 *
 * @author alex
 * @version 1.1.0
 */
@Slf4j
@Data
public abstract class BaseS3Template {

    protected MinioClient minioClient;

    /**
     * 已存在的存储桶内存缓存，避免每次写入都重复发起 existBucket 网络探活请求
     */
    protected final Set<String> knownBuckets = ConcurrentHashMap.newKeySet();

    /**
     * 底座连接地址
     */
    protected String url;

    /**
     * 底座端口
     */
    protected Integer port;

    /**
     * 是否启用 SSL
     */
    protected Boolean secure;

    /**
     * 外部公网/CDN 访问域名（如 https://oss.example.com），配置后将自动平滑替换预签名直链的主机地址
     */
    protected String publicUrl;

    /**
     * 判断底座客户端是否已成功初始化
     */
    public boolean isInitialized() {
        return this.minioClient != null;
    }

    /**
     * 初始化 MinioClient
     */
    protected void initClient(String url, Integer port, String accessKey, String secretKey, String region,
            Boolean secure, String publicUrl) {
        this.url = url;
        this.port = port;
        this.secure = secure;
        this.publicUrl = publicUrl;
        if (StringUtils.isBlank(url) || port == null || StringUtils.isBlank(accessKey)
                || StringUtils.isBlank(secretKey)) {
            log.warn("[S3Storage] 缺少必要的连接参数 (url={}, port={}, accessKey={})，跳过客户端初始化", url, port, accessKey);
            return;
        }
        boolean isSecure = Boolean.TRUE.equals(secure);
        MinioClient.Builder builder = MinioClient.builder()
                .endpoint(url, port, isSecure)
                .credentials(accessKey, secretKey);
        if (StringUtils.isNotBlank(region)) {
            builder.region(region);
        }
        this.minioClient = builder.build();
        log.info("[S3Storage] 存储客户端初始化成功: endpoint={}:{}, secure={}, region={}, publicUrl={}",
                url, port, isSecure, region, publicUrl);
    }

    /**
     * 确保存储桶存在（带本地内存缓存，消除每次写文件时的重复网络探活）
     */
    public void existBucket(String name) {
        if (!isInitialized()) {
            throw new IllegalStateException("S3 存储客户端未正确初始化，请检查对应存储配置");
        }
        if (StringUtils.isBlank(name)) {
            return;
        }
        if (knownBuckets.contains(name)) {
            return;
        }
        try {
            boolean exist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(name).build());
            if (!exist) {
                makeBucket(name);
            }
            knownBuckets.add(name);
        } catch (ErrorResponseException e) {
            if ("Forbidden".equals(e.errorResponse().code()) || e.errorResponse().code().contains("AccessDenied")) {
                log.warn("[S3Storage] 无法检查 bucket 是否存在（权限受限，尝试直接信任操作）：bucket={}", name);
                knownBuckets.add(name);
            } else {
                log.error("[S3Storage] 检查 bucket 异常：", e);
            }
        } catch (Exception e) {
            log.error("[S3Storage] 检查 bucket 异常：", e);
        }
    }

    public void makeBucket(String bucketName) {
        if (!isInitialized()) {
            throw new IllegalStateException("S3 存储客户端未初始化");
        }
        try {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            knownBuckets.add(bucketName);
        } catch (Exception e) {
            log.error("[S3Storage] 创建 bucket 异常：", e);
        }
    }

    public Boolean removeBucket(String bucketName) {
        if (!isInitialized()) {
            throw new IllegalStateException("S3 存储客户端未初始化");
        }
        try {
            minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
            knownBuckets.remove(bucketName);
            return true;
        } catch (Exception e) {
            log.error("[S3Storage] 删除 bucket 异常：", e);
            return false;
        }
    }

    /**
     * 流式上传（推荐：明确传入 objectSize，严禁使用 inputStream.available()）
     */
    public Map<String, String> upload(String bucketName, String filename, InputStream inputStream, long objectSize,
            String contentType) throws Exception {
        existBucket(bucketName);
        ObjectWriteResponse response = minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(filename)
                .contentType(contentType)
                .stream(inputStream, objectSize, -1)
                .build());
        log.info("[S3Storage] 上传文件成功：bucket={}, file={}, etag={}", bucketName, filename, response.etag());
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("url", filename);
        return resultMap;
    }

    /**
     * 向后兼容老接口（不带 size 时降级为 -1 分片读取）
     */
    public Map<String, String> upload(String bucketName, String filename, InputStream inputStream, String contentType)
            throws Exception {
        return upload(bucketName, filename, inputStream, -1, contentType);
    }

    /**
     * 文件流式下载（返回开放的 InputStream，消费端负责使用完后关闭）
     * 修复：严禁在 finally 中关闭流返回！
     */
    public InputStream fileDownload(String bucketName, String fileName) {
        if (!isInitialized()) {
            throw new IllegalStateException("S3 存储客户端未初始化");
        }
        if (StringUtils.isBlank(fileName)) {
            return null;
        }
        try {
            return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(fileName).build());
        } catch (ServerException | InternalException | XmlParserException | InvalidResponseException
                | InvalidKeyException | NoSuchAlgorithmException | IOException | ErrorResponseException
                | InsufficientDataException e) {
            log.error("[S3Storage] 下载文件流异常：bucket={}, file={}", bucketName, fileName, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 文件流式下载至 HttpServletResponse
     */
    public void fileDownload(String bucketName, String fileName, Boolean delete, HttpServletResponse response) {
        if (!isInitialized()) {
            throw new IllegalStateException("S3 存储客户端未初始化");
        }
        if (StringUtils.isBlank(fileName)) {
            writeDownloadError(response, "文件名不能为空");
            return;
        }
        try (InputStream inputStream = minioClient
                .getObject(GetObjectArgs.builder().bucket(bucketName).object(fileName).build());
                OutputStream outputStream = response.getOutputStream()) {
            response.reset();
            String downloadName = fileName.substring(fileName.lastIndexOf("/") + 1);
            response.setHeader("Content-Disposition", "attachment;filename=" +
                    URLEncoder.encode(downloadName, StandardCharsets.UTF_8));
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("UTF-8");

            byte[] buf = new byte[8192];
            int length;
            while ((length = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, length);
            }
            outputStream.flush();

            if (BooleanUtils.isTrue(delete)) {
                minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(fileName).build());
            }
        } catch (Throwable ex) {
            log.error("[S3Storage] 下载文件至响应异常：bucket={}, file={}", bucketName, fileName, ex);
            writeDownloadError(response, "文件下载失败");
        }
    }

    private void writeDownloadError(HttpServletResponse response, String msg) {
        try {
            response.setHeader("Content-type", "text/html;charset=UTF-8");
            response.getOutputStream().write(msg.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("[S3Storage] 写入下载错误响应异常：", e);
        }
    }

    /**
     * 查看 Bucket 文件对象列表
     */
    public List<ObjectItem> listObjects(String bucketName) {
        if (!isInitialized()) {
            throw new IllegalStateException("S3 存储客户端未初始化");
        }
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).build());
        List<ObjectItem> objectItems = new ArrayList<>();
        try {
            for (Result<Item> result : results) {
                Item item = result.get();
                ObjectItem objectItem = new ObjectItem();
                objectItem.setObjectName(item.objectName());
                objectItem.setSize(item.size());
                objectItems.add(objectItem);
            }
        } catch (Exception e) {
            log.error("[S3Storage] 查看文件对象异常：bucket={}", bucketName, e);
            return Collections.emptyList();
        }
        return objectItems;
    }

    /**
     * 批量删除文件对象
     */
    public Map<String, String> removeObjects(String bucketName, List<String> objects) throws Exception {
        if (!isInitialized()) {
            throw new IllegalStateException("S3 存储客户端未初始化");
        }
        Map<String, String> resultMap = new HashMap<>();
        List<DeleteObject> dos = objects.stream().map(DeleteObject::new).toList();
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                        .bucket(bucketName)
                        .objects(dos)
                        .build());
        for (Result<DeleteError> result : results) {
            DeleteError error = result.get();
            log.error("[S3Storage] 删除文件对象失败: bucket={}, object={}, error={}", bucketName, error.objectName(),
                    error.message());
        }
        resultMap.put("mes", "删除成功");
        return resultMap;
    }

    /**
     * 获取预览直链：
     * 1. 若显式指定 isPublic == true，或未指定 (isPublic == null) 且当前桶为公开只读桶（如 user-bucket, goods-bucket），
     *    直接生成持久免签直链（支持 publicUrl/CDN 映射），彻底根除短期签名过期导致的 400 异常；
     * 2. 若显式指定 isPublic == false，或当前桶为私有业务桶，生成临时预签名直链（1小时有效期，支持 publicUrl/CDN 域名自动映射替换）。
     */
    public String preview(String bucketName, String objectKey, Boolean isPublic)
            throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException,
            NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {
        if (!isInitialized()) {
            throw new IllegalStateException("S3 存储客户端未初始化");
        }
        if (StringUtils.isBlank(objectKey)) {
            return null;
        }

        // 核心规约：仅当显式传入 isPublic == true 时生成免签持久直链；
        // 当 isPublic 不是 true（即 false 或 null 未传）时，一律生成带过期时效的 S3 预签名直链
        boolean effectivePublic = Boolean.TRUE.equals(isPublic);

        // 1. 显式指定公开时返回免签直链，永久有效且 CDN/浏览器强缓存友好
        if (effectivePublic) {
            return buildPublicDirectUrl(bucketName, objectKey);
        }

        // 2. 其余情况（包括未传或声明为 false）一律生成带时效签名的预签名直链
        String presignedUrl = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(objectKey)
                        .expiry(60 * 60, TimeUnit.SECONDS)
                        .build());

        if (StringUtils.isNotBlank(publicUrl)) {
            try {
                URI originUri = URI.create(presignedUrl);
                URI targetUri = URI
                        .create(publicUrl.endsWith("/") ? publicUrl.substring(0, publicUrl.length() - 1) : publicUrl);
                String originHostPart = originUri.getScheme() + "://" + originUri.getAuthority();
                String targetHostPart = targetUri.getScheme() + "://" + targetUri.getAuthority();
                presignedUrl = presignedUrl.replaceFirst(originHostPart, targetHostPart);
            } catch (Exception e) {
                log.warn("[S3Storage] 替换 publicUrl 异常，沿用原始签名链接: origin={}, publicUrl={}", presignedUrl, publicUrl, e);
            }
        }
        return presignedUrl;
    }

    /**
     * 获取预览直链（遵循存储桶默认公私策略）
     */
    public String preview(String bucketName, String objectKey)
            throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException,
            NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {
        return preview(bucketName, objectKey, null);
    }

    /**
     * 构造公开桶持久免签访问直链
     */
    public String buildPublicDirectUrl(String bucketName, String objectKey) {
        String base;
        if (StringUtils.isNotBlank(publicUrl)) {
            base = publicUrl.endsWith("/") ? publicUrl.substring(0, publicUrl.length() - 1) : publicUrl;
        } else if (StringUtils.isNotBlank(url)) {
            boolean isSecure = Boolean.TRUE.equals(secure);
            base = (isSecure ? "https://" : "http://") + url
                    + (port != null && port != 80 && port != 443 ? ":" + port : "");
        } else {
            base = "";
        }
        String cleanKey = objectKey != null && objectKey.startsWith("/") ? objectKey.substring(1) : objectKey;
        String encodedKey = encodePathKey(cleanKey);
        if (StringUtils.isBlank(base)) {
            return "/" + bucketName + "/" + (encodedKey != null ? encodedKey : "");
        }
        return base + "/" + bucketName + "/" + (encodedKey != null ? encodedKey : "");
    }

    /**
     * 对对象路径按段执行 URL 编码（避免双重编码，并将空格转为 %20）
     */
    public static String encodePathKey(String pathKey) {
        if (StringUtils.isBlank(pathKey)) {
            return pathKey;
        }
        return Arrays.stream(pathKey.split("/"))
                .map(segment -> {
                    try {
                        String raw = URLDecoder.decode(segment, StandardCharsets.UTF_8.name());
                        return URLEncoder.encode(raw, StandardCharsets.UTF_8.name())
                                .replace("+", "%20");
                    } catch (Exception e) {
                        return segment;
                    }
                })
                .collect(Collectors.joining("/"));
    }

    /**
     * 生成图片缩略图并上传
     */
    public Map<String, String> thumbnail(String bucketName, String filename, InputStream inputStream,
            String contentType) throws Exception {
        existBucket(bucketName);
        ByteArrayOutputStream thumbnailStream = new ByteArrayOutputStream();
        BufferedImage bufferedImage = ImageIO.read(inputStream);
        if (bufferedImage != null) {
            Thumbnails.of(bufferedImage)
                    .size(200, 200)
                    .outputFormat("jpg")
                    .toOutputStream(thumbnailStream);
        } else {
            throw new UnsupportedFormatException("Invalid image format.");
        }

        String thumbnailFilename;
        int index = filename.lastIndexOf(".");
        if (index != -1) {
            thumbnailFilename = filename.substring(0, index) + "_thumbnail" + filename.substring(index);
        } else {
            thumbnailFilename = filename + "_thumbnail";
        }

        byte[] thumbnailBytes = thumbnailStream.toByteArray();
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(thumbnailFilename)
                        .contentType(contentType)
                        .stream(new ByteArrayInputStream(thumbnailBytes), thumbnailBytes.length, -1)
                        .build());
        log.info("[S3Storage] 上传缩略图成功：bucket={}, file={}", bucketName, thumbnailFilename);

        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("url", thumbnailFilename);
        return resultMap;
    }
}
