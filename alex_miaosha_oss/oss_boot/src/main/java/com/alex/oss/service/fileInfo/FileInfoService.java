package com.alex.oss.service.fileInfo;

import com.alex.api.oss.vo.fileInfo.FileInfoVo;
import com.alex.oss.entity.fileInfo.FileInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * 文件信息表 服务类
 * @author: alex
 * @createDate: 2023-01-30 14:08:29
 * @description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface FileInfoService extends IService<FileInfo> {

    Page<FileInfoVo> getPage(Long pageNum, Long pageSize, FileInfoVo fileInfoVo);

    FileInfoVo queryFileInfo(Long id);

    FileInfoVo addFileInfo(String type, MultipartFile multipartFile) throws Exception;

    FileInfoVo updateFileInfo(Long id, String type, MultipartFile file) throws Exception;

    Boolean deleteFileInfo(String ids);

    InputStream fileDownload(Long id);

    /*
     * @param fileIdList
     * @description: 根据文件id列表获取文件信息
     * @author:      alex
     * @return:      java.util.List<com.alex.api.oss.vo.fileInfo.FileInfoVo>
    */
    List<FileInfoVo> getFileInfo(List<Long> fileIdList);
}
