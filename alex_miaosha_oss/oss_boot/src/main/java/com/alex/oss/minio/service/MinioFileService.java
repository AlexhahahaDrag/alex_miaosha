package com.alex.oss.minio.service;

import io.minio.errors.*;
import org.springframework.web.multipart.MultipartFile;

import com.alex.api.oss.fileInfo.vo.FileInfoVo;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * description:  minio文件服务类
 * author:       majf
 * createDate:   2023/1/12 14:40
 * version:      1.0.0
 */
public interface MinioFileService {

    FileInfoVo uploadFile(MultipartFile file, String type) throws Exception;

    boolean deleteFile(List<String> filePath, String type) throws Exception;

    InputStream fileDownload(FileInfoVo fileInfo);

    String preview(String bucketName, String objectName) throws IOException, InvalidResponseException, InvalidKeyException, NoSuchAlgorithmException, ServerException, ErrorResponseException, XmlParserException, InsufficientDataException, InternalException;

    FileInfoVo thumbnail(MultipartFile file, String type) throws Exception;
}
