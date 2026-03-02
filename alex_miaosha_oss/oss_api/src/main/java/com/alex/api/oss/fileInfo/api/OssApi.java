package com.alex.api.oss.fileInfo.api;

import com.alex.api.oss.fileInfo.fallback.OssFallbackFactory;
import com.alex.api.oss.fileInfo.vo.FileInfoVo;
import com.alex.base.common.Result;
import com.alex.common.config.FeignConfig;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * description:  文件管理控制器 api
 * author:       majf
 * createDate:   2023/1/13 13:58
 * version:      1.0.0
 */
@Component
@FeignClient(contextId = "ossApi", name = "alex-oss-${spring.profiles.active:dev}", path = "${api.version:/api/v1}/file-info", fallback = OssFallbackFactory.class, configuration = FeignConfig.class)
public interface OssApi {

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取文件信息表分页", notes = "获取文件信息表分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "查询条件", name = "fileInfoVo", dataTypeClass = FileInfoVo.class)}
    )
    Result<Page<FileInfoVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                     @RequestParam(value = "pageSize", required = false) Long pageSize,
                                     @RequestBody(required = false) FileInfoVo fileInfoVo);

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取文件信息表详情", notes = "获取文件信息表详情", response = Result.class)
    @GetMapping
    Result<FileInfoVo> query(@RequestParam(value = "id") Long id);

    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增文件信息表", notes = "新增文件信息表", response = Result.class)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Result<FileInfoVo> add(@RequestParam(value = "type", required = false) String type,
                           @RequestPart(value = "file") MultipartFile file) throws Exception;

    @ApiOperationSupport(order = 35, author = "alex")
    @ApiOperation(value = "批量新增文件信息", notes = "批量新增文件信息", response = Result.class)
    @PostMapping(value = "/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Result<List<FileInfoVo>> addBatch(@RequestParam(value = "type", required = false) String type,
                                      @RequestPart(value = "file") List<MultipartFile> files) throws Exception;

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改文件信息表", notes = "修改文件信息表", response = Result.class)
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Result<FileInfoVo> update(@RequestParam(value = "id") Long id,
                              @RequestParam(value = "type", required = false) String type,
                              @RequestPart(value = "file") MultipartFile file) throws Exception;

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除文件信息表", notes = "刪除文件信息表", response = Result.class)
    @DeleteMapping
    Result<Boolean> delete(@RequestParam("ids") String ids);

    @ApiOperationSupport(order = 60, author = "alex")
    @ApiOperation(value = "文件下载", notes = "文件下载", response = Result.class)
    @GetMapping("/fileDownload")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "id", name = "id", required = true, dataTypeClass = Long.class)}
    )
    Result<InputStream> fileDownload(@RequestParam(value = "id") Long id);

    @ApiOperationSupport(order = 70, author = "alex")
    @ApiOperation(value = "获取文件信息列表", notes = "获取文件信息列表", response = Result.class)
    @GetMapping(value = "/getFileInfo")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "文件id列表", name = "fileIdList", required = true, dataTypeClass = List.class)}
    )
    Result<List<FileInfoVo>> getFileInfo(@RequestParam("fileIdList") List<Long> fileIdList);

    @ApiOperationSupport(order = 80, author = "alex")
    @ApiOperation(value = "新增缩略图文件信息表", notes = "新增缩略图文件信息表", response = Result.class)
    @PostMapping(value = "addThumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Result<FileInfoVo> addThumbnail(@RequestParam(value = "type", required = false) String type,
                                    @RequestPart(value = "file") MultipartFile file) throws Exception;
}
