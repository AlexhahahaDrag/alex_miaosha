package com.alex.oss.service.fileInfo;

import com.alex.api.oss.vo.fileInfo.FileInfoVo;
import com.alex.oss.entity.fileInfo.FileInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

/**
 * 文件信息表 服务类
 * @author: alex
 * @createDate: 2023-01-30 14:08:29
 * @description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface FileInfoService extends IService<FileInfo> {

    Page<FileInfoVo> getPage(Long pageNum, Long pageSize, FileInfoVo fileInfoVo);

    FileInfoVo queryFileInfo(String id);

    Boolean addFileInfo(String type, MultipartFile multipartFile) throws Exception;

    Boolean updateFileInfo(FileInfoVo fileInfoVo);

    Boolean deleteFileInfo(String ids);

    InputStream fileDownload(String type, String fileName, Boolean delete, HttpServletResponse response);
}
