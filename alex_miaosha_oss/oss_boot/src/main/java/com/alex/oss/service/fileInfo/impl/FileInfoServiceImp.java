package com.alex.oss.service.fileInfo.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alex.api.oss.vo.fileInfo.FileInfoVo;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.FileException;
import com.alex.common.utils.string.StringUtils;
import com.alex.oss.entity.fileInfo.FileInfo;
import com.alex.oss.mapper.fileInfo.FileInfoMapper;
import com.alex.oss.service.fileInfo.FileInfoService;
import com.alex.oss.service.minio.MinioFileService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *
 * @description: 文件信息表服务实现类
 * @author: alex
 * @createDate: 2023-01-30 14:08:29
 * @version: 1.0.0
 */
@Service
@RequiredArgsConstructor
public class FileInfoServiceImp extends ServiceImpl<FileInfoMapper, FileInfo> implements FileInfoService {

    private final FileInfoMapper fileInfoMapper;

    private final MinioFileService minioFileService;

    @Override
    public Page<FileInfoVo> getPage(Long pageNum, Long pageSize, FileInfoVo fileInfoVo) {
        Page<FileInfoVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return fileInfoMapper.getPage(page, fileInfoVo);
    }

    @Override
    public FileInfoVo queryFileInfo(Long id) {
        return fileInfoMapper.queryFileInfo(id);
    }

    @Override
    public FileInfoVo addFileInfo(String type, MultipartFile file) throws Exception {
        if (file == null) {
            throw new FileException(ResultEnum.IMAGE_NO_FOUNT);
        }
        FileInfoVo uploadFile = uploadFile(type, file);
        FileInfo fileInfo = new FileInfo();
        BeanUtil.copyProperties(uploadFile, fileInfo);
        fileInfoMapper.insert(fileInfo);
        BeanUtil.copyProperties(fileInfo, uploadFile);
        return uploadFile;
    }

    @Override
    public FileInfoVo updateFileInfo(Long id, String type, MultipartFile file) throws Exception {
        FileInfo fileInfo = this.getById(id);
        FileInfoVo uploadFile = null;
        if (file != null) {
            uploadFile = uploadFile(type, file);
            BeanUtil.copyProperties(uploadFile, fileInfo, "id");
        }
        fileInfoMapper.updateById(fileInfo);
        BeanUtil.copyProperties(fileInfo, uploadFile);
        return uploadFile;
    }

    @Override
    public Boolean deleteFileInfo(String ids) {
        if (StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        fileInfoMapper.deleteBatchIds(idArr);
        return true;
    }

    @Override
    public InputStream fileDownload(Long id) {
        FileInfoVo fileInfo = fileInfoMapper.queryFileInfo(id);
        return minioFileService.fileDownload(fileInfo);
    }

    private FileInfoVo uploadFile(String type, MultipartFile file) throws Exception {
        FileInfoVo fileInfoVo = minioFileService.uploadFile(file, type);
        return fileInfoVo;
    }

    @Override
    public List<FileInfoVo> getFileInfo(List<Long> fileIdList) {
        if (fileIdList == null || fileIdList.isEmpty()) {
            return Lists.newArrayList();
        }
        LambdaQueryWrapper<FileInfo> query = Wrappers.<FileInfo>lambdaQuery().in(FileInfo::getId, fileIdList);
        List<FileInfo> fileInfos = fileInfoMapper.selectList(query);
        if (fileInfos == null || fileInfos.isEmpty()) {
            return null;
        }
        Map<Long, String> map = new HashMap<>();
        fileInfos.forEach(item -> {
            try {
                String url = minioFileService.preview(item.getBucketName(), item.getUrl());
                map.put(item.getId(), Optional.ofNullable(url).map(ii -> ii.replace("http://minio:9000", "http://123.249.83.33:9000")).orElse(""));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidResponseException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (ServerException e) {
                e.printStackTrace();
            } catch (ErrorResponseException e) {
                e.printStackTrace();
            } catch (XmlParserException e) {
                e.printStackTrace();
            } catch (InsufficientDataException e) {
                e.printStackTrace();
            } catch (InternalException e) {
                e.printStackTrace();
            }
        });
        return fileInfos.parallelStream().map(item -> {
            FileInfoVo fileInfoVo = new FileInfoVo();
            BeanUtil.copyProperties(item, fileInfoVo);
            fileInfoVo.setPreUrl(map.get(item.getId()));
            return fileInfoVo;
        }).collect(Collectors.toList());
    }
}
