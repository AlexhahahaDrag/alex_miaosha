package com.alex.oss.service.fileInfo.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alex.api.oss.vo.fileInfo.FileInfoVo;
import com.alex.base.enums.ResultEnum;
import com.alex.common.enums.BucketNameEnum;
import com.alex.common.exception.FileException;
import com.alex.common.utils.string.StringUtils;
import com.alex.oss.entity.fileInfo.FileInfo;
import com.alex.oss.mapper.fileInfo.FileInfoMapper;
import com.alex.oss.service.MinioFileService;
import com.alex.oss.service.fileInfo.FileInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * @description:  文件信息表服务实现类
 * @author:       alex
 * @createDate:   2023-01-30 14:08:29
 * @version:      1.0.0
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
    public FileInfoVo queryFileInfo(String id) {
        return fileInfoMapper.queryFileInfo(id);
    }

    @Override
    public Boolean addFileInfo(String type, MultipartFile file) throws Exception {
        if (file == null) {
            throw new FileException(ResultEnum.IMAGE_NO_FOUNT);
        }
        // TODO: 2023/2/13 添加系统配置文件，可以选择不同的文件系统
        FileInfoVo uploadFile = minioFileService.uploadFile(file, type);
        FileInfo fileInfo = new FileInfo();
        BeanUtil.copyProperties(uploadFile, fileInfo);
        fileInfoMapper.insert(fileInfo);
        return true;
    }

    @Override
    public Boolean updateFileInfo(FileInfoVo fileInfoVo) {
        FileInfo fileInfo = new FileInfo();
        BeanUtil.copyProperties(fileInfoVo, fileInfo);
        fileInfoMapper.updateById(fileInfo);
        return true;
    }

    @Override
    public Boolean deleteFileInfo(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        fileInfoMapper.deleteBatchIds(idArr);
        return true;
    }

    @Override
    public InputStream fileDownload(String type, String fileName, Boolean delete, HttpServletResponse response) {
//        minioTemplate.fileDownload(getBucket(type), fileName, delete, response);
        return null;
    }

    private String getBucket(String type) {
        String bucketName;
        switch (type) {
            case "user" :
                bucketName = BucketNameEnum.USER_BUCKET.getValue();
                break;
            case "goods" :
                bucketName = BucketNameEnum.GOODS_BUCKET.getValue();
                break;
            default:
                bucketName = BucketNameEnum.COMMON_BUCKET.getValue();
        }
        return bucketName;
    }
}
