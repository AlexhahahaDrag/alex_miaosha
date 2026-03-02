package com.alex.api.oss.fileInfo.api;

import com.alex.api.oss.fileInfo.fallback.OssFallbackFactory;
import com.alex.api.oss.fileInfo.vo.FileInfoVo;
import com.alex.base.common.Result;
import com.alex.common.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * description:
 * author:       majf
 * createDate:   2023/1/13 13:58
 * version:      1.0.0
 */
@Component
@FeignClient(name = "alex-oss-${spring.profiles.active:dev}", fallback = OssFallbackFactory.class, configuration = FeignConfig.class)
public interface OssApi {

    @GetMapping(value = "${api.version:/api/v1}/file-info/getFileInfo")
    Result<List<FileInfoVo>> getFileInfo(@RequestParam("fileIdList") List<Long> fileIdList);
}
