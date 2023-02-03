package com.alex.oss.service.fileInfo.impl;

import com.alex.common.enums.BucketNameEnum;
import com.alex.oss.entity.fileInfo.FileInfo;
import com.alex.api.oss.vo.fileInfo.FileInfoVo;
import com.alex.oss.mapper.fileInfo.FileInfoMapper;
import com.alex.oss.service.fileInfo.FileInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.io.InputStream;
import java.util.List;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import cn.hutool.core.bean.BeanUtil;
import com.alex.common.utils.string.StringUtils;

import javax.servlet.http.HttpServletResponse;

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
    public Boolean addFileInfo(FileInfoVo fileInfoVo) {
        FileInfo fileInfo = new FileInfo();
        BeanUtil.copyProperties(fileInfoVo, fileInfo);
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
