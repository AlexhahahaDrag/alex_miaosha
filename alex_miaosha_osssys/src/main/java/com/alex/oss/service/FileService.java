package com.alex.oss.service;

import com.alex.oss.vo.FileInfoVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.List;

/**
 * description:
 * author:       majf
 * createDate:   2023/1/12 14:40
 * version:      1.0.0
 */
public interface FileService {

    FileInfoVo uploadFile(MultipartFile file, String type) throws Exception;

    boolean deleteFile(List<String> filePath, String type) throws Exception;

    InputStream fileDownload(String type, String fileName, Boolean delete, HttpServletResponse response);
}
