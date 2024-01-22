package com.alex.oss.mapper.fileInfo;

import com.alex.api.oss.vo.fileInfo.FileInfoVo;
import com.alex.api.user.annotation.DataPermission;
import com.alex.oss.entity.fileInfo.FileInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * description:  文件信息表 mapper
 * author:       alex
 * createDate:   2023-01-30 14:08:29
 * version:      1.0.0
 */
@Mapper
public interface FileInfoMapper extends BaseMapper<FileInfo> {

    @DataPermission(table = "file_info")
    Page<FileInfoVo> getPage(Page<FileInfoVo> page, @Param("fileInfoVo") FileInfoVo fileInfoVo);

    FileInfoVo queryFileInfo(@Param("id") Long id);
}
