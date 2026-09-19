package com.alex.oss.fileInfo.service;

import com.alex.api.oss.fileInfo.vo.FileInfoVo;
import com.alex.common.exception.FileException;
import com.alex.oss.fileInfo.entity.FileInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.minio.errors.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * 文件信息表 服务类
 * author: alex
 * createDate: 2023-01-30 14:08:29
 * description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface FileInfoService extends IService<FileInfo> {

    Page<FileInfoVo> getPage(Long pageNum, Long pageSize, FileInfoVo fileInfoVo);

    FileInfoVo queryFileInfo(Long id, Boolean isPublic) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;

    default FileInfoVo queryFileInfo(Long id) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        return queryFileInfo(id, null);
    }

    FileInfoVo addFileInfo(String type, MultipartFile multipartFile, boolean isThumbnail, boolean isNormal) throws FileException;

    List<FileInfoVo> addBatchFileInfo(String type, List<MultipartFile> multipartFiles, boolean isThumbnail, boolean isNormal) throws FileException;

    /**
     * 多附件并发上传（含扩展名白名单校验、并发加速、自动装配预签名直链与异常补偿清理）
     */
    List<FileInfoVo> uploadMultipleFiles(String type, List<MultipartFile> multipartFiles, boolean isThumbnail, boolean isNormal) throws FileException;

    FileInfoVo updateFileInfo(Long id, String type, MultipartFile file, boolean isThumbnail, boolean isNormal) throws FileException;

    Boolean deleteFileInfo(String ids);

    InputStream fileDownload(Long id);

    void fileDownload(Long id, javax.servlet.http.HttpServletResponse response);

    /**
     * 根据文件id列表获取文件信息
     *
     * @param fileIdList 文件id列表
     * @param isPublic   是否公开预览直链 (true-免签直链, false-带时效签名直链, null-遵循存储桶预置策略)
     * @return 文件信息列表
     * @author alex
     */
    List<FileInfoVo> getFileInfo(List<Long> fileIdList, Boolean isPublic);

    default List<FileInfoVo> getFileInfo(List<Long> fileIdList) {
        return getFileInfo(fileIdList, null);
    }
}
