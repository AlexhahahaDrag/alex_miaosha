package com.alex.oss.config.minio;

import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONUtil;
import com.alex.common.utils.string.StringUtils;
import com.alex.oss.vo.ObjectItem;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * description:
 * author:       majf
 * createDate:   2023/1/12 11:30
 * version:      1.0.0
 */
@Configuration
@Slf4j
@EnableConfigurationProperties({MinioProperties.class})
@Data
public class MinioTemplate implements InitializingBean {

    @Autowired
    private MinioProperties minioProperties;

    private MinioClient minioClient;

    @Override
    public void afterPropertiesSet() {
        String url = minioProperties.getUrl();
        Integer port = minioProperties.getPort();
        String accessKey = minioProperties.getAccessKey();
        String secretKey = minioProperties.getSecretKey();
        Assert.notBlank(url, "minio url can't be null!");
        Assert.notNull(port, "minio port can't be null!");
        Assert.notBlank(accessKey, "minio url can't be null!");
        Assert.notBlank(secretKey, "minio url can't be null!");
        minioClient = MinioClient.builder()
                .endpoint(url, port, false)
                .credentials(accessKey, secretKey)
                .build();
    }

    /**
     * @param name
     * @description: 判断bucket是否存在，不存在则创建
     * @author: majf
     * @return: void
     */
    public void existBucket(String name) {
        try {
            boolean exist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(name).build());
            if (!exist) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(name).build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param bucketName
     * @description: 创建存储bucket
     * @author: majf
     * @return: java.lang.Boolean
     */
    public Boolean makeBucket(String bucketName) {
        try {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 删除存储bucket
     *
     * @param bucketName 存储bucket名称
     * @return Boolean
     */
    public Boolean removeBucket(String bucketName) {
        try {
            minioClient.removeBucket(RemoveBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * @param bucketName
     * @param filename
     * @param inputStream
     * @description: 上传文件到minio
     * @author: majf
     * @return: java.util.Map<java.lang.String, java.lang.String>
     */
    public Map<String, String> upload(String bucketName, String filename, InputStream inputStream, String contentType) throws Exception {
        existBucket(bucketName);
        // 上传到minio服务器
        ObjectWriteResponse objectWriteResponse = minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(filename)
                .contentType(contentType)
                .stream(inputStream, inputStream.available(), -1)
                .build());
        log.info("上传文件结果：{}", JSONUtil.toJsonStr(objectWriteResponse));
        // 返回地址
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("url", filename);
        return resultMap;
    }

    /**
     * 文件下载
     *
     * @param fileName 文件名
     * @param delete   是否删除
     * @throws IOException
     */
    public void fileDownload(String bucketName, String fileName, Boolean delete, HttpServletResponse response) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            if (StringUtils.isBlank(fileName)) {
                response.setHeader("Content-type", "text/html;charset=UTF-8");
                String data = "文件下载失败";
                OutputStream ps = response.getOutputStream();
                ps.write(data.getBytes("UTF-8"));
                return;
            }
            outputStream = response.getOutputStream();
            // 获取文件对象
            inputStream = minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(fileName).build());
            byte buf[] = new byte[1024];
            int length = 0;
            response.reset();
            response.setHeader("Content-Disposition", "attachment;filename=" +
                    URLEncoder.encode(fileName.substring(fileName.lastIndexOf("/") + 1), "UTF-8"));
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("UTF-8");
            // 输出文件
            while ((length = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, length);
            }
            inputStream.close();
            // 判断：下载后是否同时删除minio上的存储文件
            if (BooleanUtils.isTrue(delete)) {
                minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(fileName).build());
            }
        } catch (Throwable ex) {
            response.setHeader("Content-type", "text/html;charset=UTF-8");
            String data = "文件下载失败";
            try {
                OutputStream ps = response.getOutputStream();
                ps.write(data.getBytes("UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            try {
                outputStream.close();
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param bucketName
     * @param fileName
     * @param delete
     * @description: 下载文件流
     * @author:      alex
     * @return:      java.io.InputStream
    */
    public InputStream fileDownload(String bucketName, String fileName, Boolean delete) {
        InputStream inputStream = null;
        try {
            if (StringUtils.isBlank(fileName)) {
                return null;
            }
            // 获取文件对象
            inputStream = minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(fileName).build());
        } catch (ServerException e) {
            throw new RuntimeException(e);
        } catch (InsufficientDataException e) {
            throw new RuntimeException(e);
        } catch (ErrorResponseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (InvalidResponseException e) {
            throw new RuntimeException(e);
        } catch (XmlParserException e) {
            throw new RuntimeException(e);
        } catch (InternalException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return inputStream;
    }

    /**
     * 查看文件对象
     *
     * @param bucketName 存储bucket名称
     * @return 存储bucket内文件对象信息
     */
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
            e.printStackTrace();
            return null;
        }
        return objectItems;
    }

    /**
     * 批量删除文件对象
     *
     * @param bucketName 存储bucket名称
     * @param objects    对象名称集合
     */
    public Map<String, String> removeObjects(String bucketName, List<String> objects) throws Exception {
        Map<String, String> resultMap = new HashMap<>();
        List<DeleteObject> dos = objects.stream().map(e -> new DeleteObject(e)).collect(Collectors.toList());
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
}
