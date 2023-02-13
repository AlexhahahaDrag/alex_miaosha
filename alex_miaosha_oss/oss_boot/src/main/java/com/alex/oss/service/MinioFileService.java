package com.alex.oss.service;

import com.alex.api.oss.vo.fileInfo.FileInfoVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * description:
 * author:       majf
 * createDate:   2023/1/12 14:40
 * version:      1.0.0
 */
public interface MinioFileService {

    FileInfoVo uploadFile(MultipartFile file, String type) throws Exception;

    boolean deleteFile(List<String> filePath, String type) throws Exception;
}
