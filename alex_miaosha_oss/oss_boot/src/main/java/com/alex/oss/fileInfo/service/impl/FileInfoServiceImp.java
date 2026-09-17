package com.alex.oss.fileInfo.service.impl;

import com.alex.api.oss.fileInfo.vo.FileInfoVo;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.FileException;
import com.alex.common.utils.string.StringUtils;
import com.alex.oss.fileInfo.entity.FileInfo;
import com.alex.oss.fileInfo.mapper.FileInfoMapper;
import com.alex.oss.fileInfo.service.FileInfoService;
import com.alex.oss.storage.service.FileStorageService;
import com.alex.oss.storage.service.impl.AbstractS3FileService;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

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

    private static final int MAX_BATCH_FILE_COUNT = 9;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg",
            "pdf", "xlsx", "xls", "docx", "doc", "txt", "zip", "rar", "csv");

    private final FileInfoMapper fileInfoMapper;

    private final Map<String, FileStorageService> fileServiceMap;

    @Value("${oss.active:minio}")
    private String activeFileSystem;

    private FileStorageService getFileService(String fileSystem) {
        String key = StringUtils.isBlank(fileSystem) ? activeFileSystem : fileSystem;
        FileStorageService service = fileServiceMap.get(key + "FileService");
        if (service != null) {
            return service;
        }
        FileStorageService fallback = fileServiceMap.get(activeFileSystem + "FileService");
        if (fallback != null) {
            return fallback;
        }
        if (fileServiceMap.get("garageFileService") != null) {
            return fileServiceMap.get("garageFileService");
        }
        if (fileServiceMap.get("minioFileService") != null) {
            return fileServiceMap.get("minioFileService");
        }
        return fileServiceMap.values().stream().findFirst()
                .orElseThrow(() -> new FileException(ResultEnum.SYSTEM_NO_AVAILABLE, "未找到可用的文件存储服务实现"));
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
            FileStorageService fileService = getFileService(fileInfoVo.getFileSystem());
            if (StringUtils.isNotBlank(fileInfoVo.getUrl())) {
                String url = fileService.preview(fileInfoVo.getBucketName(), fileInfoVo.getUrl());
                fileInfoVo.setPreUrl(url);
            }
            if (StringUtils.isNotBlank(fileInfoVo.getThumbnailUrl())) {
                String thumbnailUrl = fileService.preview(fileInfoVo.getBucketName(), fileInfoVo.getThumbnailUrl());
                fileInfoVo.setPreThumbnailUrl(thumbnailUrl);
            }
        }
        return fileInfoVo;
    }

    @Override
    public FileInfoVo addFileInfo(String type, MultipartFile file, boolean isThumbnail, boolean isNormal) throws FileException {
        if (file == null) {
            throw new FileException(ResultEnum.IMAGE_NO_FOUNT);
        }
        try {
            FileInfoVo uploadFile = uploadFile(type, file, isThumbnail, isNormal);
            FileInfo fileInfo = new FileInfo();
            BeanUtils.copyProperties(uploadFile, fileInfo);
            fileInfoMapper.insert(fileInfo);
            return queryFileInfo(fileInfo.getId());
        } catch (FileException e) {
            throw e;
        } catch (Exception e) {
            log.error("新增文件异常：", e);
            throw new FileException(ResultEnum.IMAGE_UPLOAD_FAIL, e.getMessage());
        }
    }

    @Override
    public List<FileInfoVo> addBatchFileInfo(String type, List<MultipartFile> multipartFiles, boolean isThumbnail,
            boolean isNormal) throws FileException {
        return uploadMultipleFiles(type, multipartFiles, isThumbnail, isNormal);
    }

    @Override
    public List<FileInfoVo> uploadMultipleFiles(String type, List<MultipartFile> multipartFiles, boolean isThumbnail,
            boolean isNormal) throws FileException {
        if (multipartFiles == null || multipartFiles.isEmpty()) {
            throw new FileException(ResultEnum.IMAGE_NO_FOUNT);
        }
        if (multipartFiles.size() > MAX_BATCH_FILE_COUNT) {
            log.warn("多附件批量上传超过上限限制：数量={}", multipartFiles.size());
            throw new FileException(ResultEnum.PARAM_ERROR, "单次最多支持上传 " + MAX_BATCH_FILE_COUNT + " 个附件");
        }

        // 严格后缀白名单校验
        for (MultipartFile file : multipartFiles) {
            if (file == null || file.isEmpty()) {
                throw new FileException(ResultEnum.IMAGE_NO_FOUNT);
            }
            String originalFilename = file.getOriginalFilename();
            String suffix = StringUtils.isBlank(originalFilename) || !originalFilename.contains(".")
                    ? ""
                    : originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(suffix)) {
                log.warn("检测到不支持或违规的文件格式：fileName={}", originalFilename);
                throw new FileException(ResultEnum.PARAM_ERROR, "不支持的文件格式: ." + suffix);
            }
        }

        // 异步并发上传 S3 + Saga 补偿清理机制
        List<String> uploadedPaths = Collections.synchronizedList(new ArrayList<>());
        List<FileInfoVo> uploadedVos = new ArrayList<>();
        try {
            List<CompletableFuture<FileInfoVo>> futures = multipartFiles.stream()
                    .map(file -> CompletableFuture.supplyAsync(() -> {
                        try {
                            FileInfoVo vo = uploadFile(type, file, isThumbnail, isNormal);
                            if (vo.getUrl() != null) {
                                uploadedPaths.add(vo.getUrl());
                            }
                            if (vo.getThumbnailUrl() != null) {
                                uploadedPaths.add(vo.getThumbnailUrl());
                            }
                            return vo;
                        } catch (Exception e) {
                            throw new CompletionException(e);
                        }
                    }))
                    .toList();

            for (CompletableFuture<FileInfoVo> future : futures) {
                uploadedVos.add(future.join());
            }

            // 批量落库
            for (FileInfoVo uploadFile : uploadedVos) {
                FileInfo fileInfo = new FileInfo();
                BeanUtils.copyProperties(uploadFile, fileInfo);
                fileInfoMapper.insert(fileInfo);
                uploadFile.setId(fileInfo.getId());

                // 补全预签名预览直链
                fillPreUrlsSafely(uploadFile);
            }
            return uploadedVos;
        } catch (Exception e) {
            log.error("多附件批量上传异常，触发 Saga 补偿清理：paths={}", uploadedPaths, e);
            rollbackUploadedFilesSafely(uploadedPaths, type);
            Throwable cause = e instanceof CompletionException ? e.getCause() : e;
            if (cause instanceof FileException fileEx) {
                throw fileEx;
            }
            throw new FileException(ResultEnum.IMAGE_UPLOAD_FAIL, cause != null ? cause.getMessage() : e.getMessage());
        }
    }

    private void fillPreUrlsSafely(FileInfoVo uploadFile) {
        try {
            FileStorageService fileService = getFileService(uploadFile.getFileSystem());
            if (StringUtils.isNotBlank(uploadFile.getUrl())) {
                uploadFile.setPreUrl(fileService.preview(uploadFile.getBucketName(), uploadFile.getUrl()));
            }
            if (StringUtils.isNotBlank(uploadFile.getThumbnailUrl())) {
                uploadFile.setPreThumbnailUrl(fileService.preview(uploadFile.getBucketName(), uploadFile.getThumbnailUrl()));
            }
        } catch (Exception e) {
            log.warn("装配预签名直链异常：url={}", uploadFile.getUrl(), e);
        }
    }

    private void rollbackUploadedFilesSafely(List<String> uploadedPaths, String type) {
        if (uploadedPaths == null || uploadedPaths.isEmpty()) {
            return;
        }
        try {
            FileStorageService service = getFileService(null);
            if (service != null) {
                service.deleteFile(new ArrayList<>(uploadedPaths), type);
            }
        } catch (Exception ex) {
            log.error("Saga 补偿清理文件异常：paths={}", uploadedPaths, ex);
        }
    }

    @Override
    public FileInfoVo updateFileInfo(Long id, String type, MultipartFile file, boolean isThumbnail, boolean isNormal) throws FileException {
        FileInfo fileInfo = this.getById(id);
        if (fileInfo == null) {
            throw new FileException(ResultEnum.SYSTEM_NO_AVAILABLE, "文件记录不存在");
        }
        FileInfoVo uploadFile = null;
        if (file != null) {
            try {
                uploadFile = uploadFile(type, file, isThumbnail, isNormal);
                BeanUtils.copyProperties(uploadFile, fileInfo, "id");
            } catch (FileException e) {
                throw e;
            } catch (Exception e) {
                log.error("更新文件上传异常：", e);
                throw new FileException(ResultEnum.IMAGE_UPLOAD_FAIL, e.getMessage());
            }
        }
        fileInfoMapper.updateById(fileInfo);
        if (uploadFile == null) {
            uploadFile = new FileInfoVo();
        }
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

    @Override
    public void fileDownload(Long id, javax.servlet.http.HttpServletResponse response) {
        FileInfoVo fileInfo = fileInfoMapper.queryFileInfo(id);
        if (fileInfo == null) {
            try {
                response.setStatus(javax.servlet.http.HttpServletResponse.SC_NOT_FOUND);
                response.setHeader("Content-type", "text/html;charset=UTF-8");
                response.getOutputStream().write("文件不存在".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            } catch (IOException e) {
                log.error("写入 404 响应异常：", e);
            }
            return;
        }
        FileStorageService service = getFileService(fileInfo.getFileSystem());
        if (service instanceof AbstractS3FileService s3Service) {
            s3Service.getTemplate().fileDownload(fileInfo.getBucketName(), fileInfo.getUrl(), false, response);
        } else if (service != null) {
            try (InputStream inputStream = service.fileDownload(fileInfo);
                    java.io.OutputStream outputStream = response.getOutputStream()) {
                if (inputStream == null) {
                    response.setStatus(javax.servlet.http.HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                response.reset();
                String fileName = fileInfo.getUrl();
                String downloadName = fileName.substring(fileName.lastIndexOf("/") + 1);
                response.setHeader("Content-Disposition", "attachment;filename=" +
                        java.net.URLEncoder.encode(downloadName, java.nio.charset.StandardCharsets.UTF_8));
                response.setContentType("application/octet-stream");
                byte[] buf = new byte[8192];
                int length;
                while ((length = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, length);
                }
                outputStream.flush();
            } catch (IOException e) {
                log.error("下载流输出异常：", e);
            }
        }
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
            return Lists.newArrayList();
        }
        return fileInfos.parallelStream().map(item -> {
            FileInfoVo fileInfoVo = new FileInfoVo();
            BeanUtils.copyProperties(item, fileInfoVo);
            try {
                FileStorageService fileService = getFileService(item.getFileSystem());
                if (StringUtils.isNotBlank(item.getUrl())) {
                    String url = fileService.preview(item.getBucketName(), item.getUrl());
                    fileInfoVo.setPreUrl(url);
                }
                if (StringUtils.isNotBlank(item.getThumbnailUrl())) {
                    String thumbnailUrl = fileService.preview(item.getBucketName(), item.getThumbnailUrl());
                    fileInfoVo.setPreThumbnailUrl(thumbnailUrl);
                }
            } catch (Exception e) {
                log.info("文件预览失败，文件ID：{}, 错误信息：{}", item.getId(), e.getMessage());
            }
            return fileInfoVo;
        }).toList();
    }
}
