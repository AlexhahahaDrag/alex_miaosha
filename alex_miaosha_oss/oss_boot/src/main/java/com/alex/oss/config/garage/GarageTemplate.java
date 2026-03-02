package com.alex.oss.config.garage;

import com.alex.common.utils.string.StringUtils;
import com.alex.oss.minio.vo.ObjectItem;
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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * description:  garage模板类
 * author:       alex
 * createDate:   2026/03/02
 * version:      1.0.0
 */
@Configuration
@Slf4j
@EnableConfigurationProperties({GarageProperties.class})
@Data
public class GarageTemplate implements InitializingBean {

    private final GarageProperties garageProperties;

    private MinioClient minioClient;

    @Autowired
    public GarageTemplate(GarageProperties garageProperties) {
        Assert.notNull(garageProperties, "GarageProperties must not be null!");
        this.garageProperties = garageProperties;
    }

    @Override
    public void afterPropertiesSet() {
        String url = garageProperties.getUrl();
        Integer port = garageProperties.getPort();
        String accessKey = garageProperties.getAccessKey();
        String secretKey = garageProperties.getSecretKey();
        Assert.notNull(url, "garage url can't be null!");
        Assert.notNull(port, "garage port can't be null!");
        Assert.notNull(accessKey, "garage accessKey can't be null!");
        Assert.notNull(secretKey, "garage secretKey can't be null!");
        minioClient = MinioClient.builder()
                .endpoint(url, port, false)
                .credentials(accessKey, secretKey)
                .build();
    }

    public void existBucket(String name) {
        try {
            boolean exist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(name).build());
            if (!exist) {
                makeBucket(name);
            }
        } catch (Exception e) {
            log.error("检查bucket是否存在异常：", e);
        }
    }

    public void makeBucket(String bucketName) {
        try {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            log.error("创建bucket异常：", e);
        }
    }

    public Boolean removeBucket(String bucketName) {
        try {
            minioClient.removeBucket(RemoveBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            log.error("删除bucket异常：", e);
            return false;
        }
        return true;
    }

    public Map<String, String> upload(String bucketName, String filename, InputStream inputStream, String contentType) throws Exception {
        existBucket(bucketName);
        ObjectWriteResponse objectWriteResponse = minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(filename)
                .contentType(contentType)
                .stream(inputStream, inputStream.available(), -1)
                .build());
        log.info("上传文件结果：{}", JSONObject.toJSONString(objectWriteResponse));
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("url", filename);
        return resultMap;
    }

    public void fileDownload(String bucketName, String fileName, Boolean delete, HttpServletResponse response) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            if (StringUtils.isBlank(fileName)) {
                response.setHeader("Content-type", "text/html;charset=UTF-8");
                String data = "文件下载失败";
                OutputStream ps = response.getOutputStream();
                ps.write(data.getBytes(StandardCharsets.UTF_8));
                return;
            }
            outputStream = response.getOutputStream();
            inputStream = minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(fileName).build());
            byte[] buf = new byte[1024];
            int length;
            response.reset();
            response.setHeader("Content-Disposition", "attachment;filename=" +
                    URLEncoder.encode(fileName.substring(fileName.lastIndexOf("/") + 1), StandardCharsets.UTF_8));
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("UTF-8");
            while ((length = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, length);
            }
            inputStream.close();
            if (BooleanUtils.isTrue(delete)) {
                minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(fileName).build());
            }
        } catch (Throwable ex) {
            response.setHeader("Content-type", "text/html;charset=UTF-8");
            String data = "文件下载失败";
            try {
                OutputStream ps = response.getOutputStream();
                ps.write(data.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                log.error("文件下载异常 - 向响应写入错误信息失败：", e);
            }
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.error("关闭文件流异常：", e);
            }
        }
    }

    public InputStream fileDownload(String bucketName, String fileName) {
        InputStream inputStream = null;
        try {
            if (StringUtils.isBlank(fileName)) {
                return null;
            }
            inputStream = minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(fileName).build());
        } catch (ServerException | InternalException | XmlParserException | InvalidResponseException |
                 InvalidKeyException | NoSuchAlgorithmException | IOException | ErrorResponseException |
                 InsufficientDataException e) {
            log.error("下载文件流异常：", e);
            throw new RuntimeException(e);
        }
        return inputStream;
    }

    public List<ObjectItem> listObjects(String bucketName) {
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
            log.error("查看文件对象异常：", e);
            return null;
        }
        return objectItems;
    }

    public Map<String, String> removeObjects(String bucketName, List<String> objects) throws Exception {
        Map<String, String> resultMap = new HashMap<>();
        List<DeleteObject> dos = objects.stream().map(DeleteObject::new).collect(Collectors.toList());
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                        .bucket(bucketName)
                        .objects(dos)
                        .build());
        for (Result<DeleteError> result : results) {
            DeleteError error = result.get();
            log.error("Error in deleting object {}; {}", error.objectName(), error.message());
        }
        resultMap.put("mes", "删除成功");
        return resultMap;
    }

    public String preview(String bucketName, String objectKey) throws IOException, InvalidKeyException, InvalidResponseException, InsufficientDataException, NoSuchAlgorithmException, ServerException, InternalException, XmlParserException, ErrorResponseException {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(objectKey)
                        .expiry(60 * 60, TimeUnit.SECONDS)
                        .build());
    }

    public Map<String, String> thumbnail(String bucketName, String filename, InputStream inputStream, String contentType) throws Exception {
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
        ObjectWriteResponse objectWriteResponse = minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object("thumbnail_" + filename)
                        .contentType(contentType)
                        .stream(new ByteArrayInputStream(thumbnailStream.toByteArray()), thumbnailStream.size(), -1)
                        .build()
        );
        log.info("上传缩略图结果：{}", JSONObject.toJSONString(objectWriteResponse));
        Map<String, String> resultMap = new HashMap<>();
        int index = filename.lastIndexOf(".");
        if (index != -1) {
            filename = filename.substring(0, index) + "_thumbnail" + filename.substring(index);
        } else {
            filename = filename + "_thumbnail";
        }
        resultMap.put("url", filename);
        return resultMap;
    }
}
