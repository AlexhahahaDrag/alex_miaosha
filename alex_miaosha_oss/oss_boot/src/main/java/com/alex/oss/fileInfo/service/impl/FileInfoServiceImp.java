package com.alex.oss.fileInfo.service.impl;

import com.alex.api.oss.fileInfo.vo.FileInfoVo;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.FileException;
import com.alex.common.utils.string.StringUtils;
import com.alex.oss.fileInfo.entity.FileInfo;
import com.alex.oss.fileInfo.mapper.FileInfoMapper;
import com.alex.oss.fileInfo.service.FileInfoService;
import com.alex.oss.minio.service.MinioFileService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * <p>
 *
 * description: 文件信息表服务实现类
 * author: alex
 * createDate: 2023-01-30 14:08:29
 * version: 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileInfoServiceImp extends ServiceImpl<FileInfoMapper, FileInfo> implements FileInfoService {

    private final FileInfoMapper fileInfoMapper;

    private final Map<String, MinioFileService> fileServiceMap;

    @Value("${oss.active:minio}")
    private String activeFileSystem;

    private MinioFileService getFileService(String fileSystem) {
        String key = StringUtils.isBlank(fileSystem) ? activeFileSystem : fileSystem;
        MinioFileService service = fileServiceMap.get(key + "FileService");
        return service != null ? service : fileServiceMap.get("minioFileService");
    }

    @Override
    public Page<FileInfoVo> getPage(Long pageNum, Long pageSize, FileInfoVo fileInfoVo) {
        Page<FileInfoVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return fileInfoMapper.getPage(page, fileInfoVo);
    }

    @Override
    public FileInfoVo queryFileInfo(Long id) throws ServerException, InsufficientDataException, ErrorResponseException,
            IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException,
            InternalException {
        FileInfoVo fileInfoVo = fileInfoMapper.queryFileInfo(id);
        if (fileInfoVo != null) {
            if (StringUtils.isNotBlank(fileInfoVo.getUrl())) {
                String url = getFileService(fileInfoVo.getFileSystem()).preview(fileInfoVo.getBucketName(),
                        fileInfoVo.getUrl());
                fileInfoVo.setPreUrl(url);
            }
            if (StringUtils.isNotBlank(fileInfoVo.getThumbnailUrl())) {
                String thumbnailUrl = getFileService(fileInfoVo.getFileSystem()).preview(fileInfoVo.getBucketName(),
                        fileInfoVo.getThumbnailUrl());
                fileInfoVo.setPreThumbnailUrl(thumbnailUrl);
            }
        }
        return fileInfoVo;
    }

    @Override
    public FileInfoVo addFileInfo(String type, MultipartFile file, boolean isThumbnail, boolean isNormal)
            throws Exception {
        if (file == null) {
            throw new FileException(ResultEnum.IMAGE_NO_FOUNT);
        }
        FileInfoVo uploadFile = uploadFile(type, file, isThumbnail, isNormal);
        FileInfo fileInfo = new FileInfo();
        BeanUtils.copyProperties(uploadFile, fileInfo);
        fileInfoMapper.insert(fileInfo);
        return queryFileInfo(fileInfo.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<FileInfoVo> addBatchFileInfo(String type, List<MultipartFile> multipartFiles, boolean isThumbnail,
            boolean isNormal) throws Exception {
        if (multipartFiles == null || multipartFiles.isEmpty()) {
            throw new FileException(ResultEnum.IMAGE_NO_FOUNT);
        }
        List<FileInfoVo> fileInfoVos = new ArrayList<>();
        for (MultipartFile file : multipartFiles) {
            FileInfoVo uploadFile = uploadFile(type, file, isThumbnail, isNormal);
            FileInfo fileInfo = new FileInfo();
            BeanUtils.copyProperties(uploadFile, fileInfo);
            fileInfoMapper.insert(fileInfo);
            BeanUtils.copyProperties(fileInfo, uploadFile);
            fileInfoVos.add(uploadFile);
        }
        return fileInfoVos;
    }

    @Override
    public FileInfoVo updateFileInfo(Long id, String type, MultipartFile file, boolean isThumbnail, boolean isNormal)
            throws Exception {
        FileInfo fileInfo = this.getById(id);
        FileInfoVo uploadFile = null;
        if (file != null) {
            uploadFile = uploadFile(type, file, isThumbnail, isNormal);
            BeanUtils.copyProperties(uploadFile, fileInfo, "id");
        }
        fileInfoMapper.updateById(fileInfo);
        assert uploadFile != null;
        BeanUtils.copyProperties(fileInfo, uploadFile);
        return uploadFile;
    }

    @Override
    public Boolean deleteFileInfo(String ids) {
        if (StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        fileInfoMapper.deleteByIds(idArr);
        return true;
    }

    @Override
    public InputStream fileDownload(Long id) {
        FileInfoVo fileInfo = fileInfoMapper.queryFileInfo(id);
        return getFileService(fileInfo.getFileSystem()).fileDownload(fileInfo);
    }

    private FileInfoVo uploadFile(String type, MultipartFile file, boolean isThumbnail, boolean isNormal)
            throws Exception {
        return getFileService(null).uploadFile(file, type, isThumbnail, isNormal);
    }

    @Override
    public List<FileInfoVo> getFileInfo(List<Long> fileIdList) {
        if (fileIdList == null || fileIdList.isEmpty()) {
            return Lists.newArrayList();
        }
        @SuppressWarnings("null")
        LambdaQueryWrapper<FileInfo> query = Wrappers.<FileInfo>lambdaQuery().in(FileInfo::getId, fileIdList);
        List<FileInfo> fileInfos = fileInfoMapper.selectList(query);
        if (fileInfos == null || fileInfos.isEmpty()) {
            return null;
        }
        return fileInfos.parallelStream().map(item -> {
            FileInfoVo fileInfoVo = new FileInfoVo();
            BeanUtils.copyProperties(item, fileInfoVo);
            try {
                if (StringUtils.isNotBlank(item.getUrl())) {
                    String url = getFileService(item.getFileSystem()).preview(item.getBucketName(), item.getUrl());
                    fileInfoVo.setPreUrl(url);
                }
                if (StringUtils.isNotBlank(item.getThumbnailUrl())) {
                    String thumbnailUrl = getFileService(item.getFileSystem()).preview(item.getBucketName(),
                            item.getThumbnailUrl());
                    fileInfoVo.setPreThumbnailUrl(thumbnailUrl);
                }
            } catch (Exception e) {
                log.info("文件预览失败，文件ID：{}, 错误信息：{}", item.getId(), e.getMessage());
            }
            return fileInfoVo;
        }).toList();
    }
}
