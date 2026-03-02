package com.alex.api.oss.fileInfo.fallback;

import com.alex.api.oss.fileInfo.vo.FileInfoVo;
import com.alex.base.common.Result;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.SystemException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import com.alex.api.oss.fileInfo.api.OssApi;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@Component
@Slf4j
public class OssFallbackFactory implements FallbackFactory<OssApi> {
    @Override
    public OssApi create(Throwable cause) {
        log.error("ossApi fallback; reason:{}", cause.getMessage());
        return new OssApi() {
            @Override
            public Result<Page<FileInfoVo>> getPage(Long pageNum, Long pageSize, FileInfoVo fileInfoVo) {
                throw new SystemException(ResultEnum.SYSTEM_NO_AVAILABLE, "oss");
            }

            @Override
            public Result<FileInfoVo> query(Long id) {
                throw new SystemException(ResultEnum.SYSTEM_NO_AVAILABLE, "oss");
            }

            @Override
            public Result<FileInfoVo> add(String type, MultipartFile file) throws Exception {
                throw new SystemException(ResultEnum.SYSTEM_NO_AVAILABLE, "oss");
            }

            @Override
            public Result<List<FileInfoVo>> addBatch(String type, List<MultipartFile> files) throws Exception {
                throw new SystemException(ResultEnum.SYSTEM_NO_AVAILABLE, "oss");
            }

            @Override
            public Result<FileInfoVo> update(Long id, String type, MultipartFile file) throws Exception {
                throw new SystemException(ResultEnum.SYSTEM_NO_AVAILABLE, "oss");
            }

            @Override
            public Result<Boolean> delete(String ids) {
                throw new SystemException(ResultEnum.SYSTEM_NO_AVAILABLE, "oss");
            }

            @Override
            public Result<InputStream> fileDownload(Long id) {
                throw new SystemException(ResultEnum.SYSTEM_NO_AVAILABLE, "oss");
            }

            @Override
            public Result<List<FileInfoVo>> getFileInfo(List<Long> fileIdList) {
                throw new SystemException(ResultEnum.SYSTEM_NO_AVAILABLE, "oss");
            }

            @Override
            public Result<FileInfoVo> addThumbnail(String type, MultipartFile file) throws Exception {
                throw new SystemException(ResultEnum.SYSTEM_NO_AVAILABLE, "oss");
            }
        };
    }
}
