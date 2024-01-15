package com.alex.oss.controller.fileInfo;

import com.alex.api.oss.vo.fileInfo.FileInfoVo;
import com.alex.base.common.Result;
import com.alex.oss.service.fileInfo.FileInfoService;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * description: 文件信息表restApi
 * author: alex
 * createDate: 2023-01-30 14:08:29
 * version: 1.0.0
 */
@ApiSort(20)
@Api(value = "文件信息表相关接口", tags = {"文件信息表相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/file-info")
public class FileInfoController {

    private final FileInfoService fileInfoService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取文件信息表分页", notes = "获取文件信息表分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum"),
            @ApiImplicitParam(value = "每页大小", name = "pageSize"),
            @ApiImplicitParam(value = "查询条件", name = "fileInfoVo")}
    )
    public Result<Page<FileInfoVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                            @RequestParam(value = "pageSize", required = false) Long pageSize,
                                            @RequestBody(required = false) FileInfoVo fileInfoVo) {
        return Result.success(fileInfoService.getPage(pageNum, pageSize, fileInfoVo));
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取文件信息表详情", notes = "获取文件信息表详情", response = Result.class)
    @GetMapping
    public Result<FileInfoVo> query(@RequestParam(value = "id") Long id) {
        return Result.success(fileInfoService.queryFileInfo(id));
    }

    // TODO: 2023/1/12 实现多附件上传
    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增文件信息表", notes = "新增文件信息表", response = Result.class)
    @PostMapping
    public Result<FileInfoVo> add(@RequestParam(value = "type", required = false) String type,
                               @RequestPart(value = "file") MultipartFile file) throws Exception {
        return Result.success(fileInfoService.addFileInfo(type, file));
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改文件信息表", notes = "修改文件信息表", response = Result.class)
    @PutMapping
    public Result<FileInfoVo> update(@RequestParam(value = "id") Long id,
                                  @RequestParam(value = "type", required = false) String type,
                                  @RequestPart(value = "file") MultipartFile file) throws Exception {
        return Result.success(fileInfoService.updateFileInfo(id, type, file));
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除文件信息表", notes = "刪除文件信息表", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(fileInfoService.deleteFileInfo(ids));
    }

    @ApiOperationSupport(order = 60, author = "alex")
    @ApiOperation(value = "文件下载", notes = "文件下载", response = Result.class)
    @GetMapping("/fileDownload")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "id", name = "id", required = true)}
    )
    public Result fileDownload(@RequestParam(value = "id") Long id) {
        return Result.success(fileInfoService.fileDownload(id));
    }

    @ApiOperationSupport(order = 70, author = "alex")
    @ApiOperation(value = "获取文件信息", notes = "获取文件信息", response = Result.class)
    @GetMapping("/getFileInfo")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "文件id列表", name = "fileIdList", required = true)}
    )
    public Result<List<FileInfoVo>> getFileInfo(@RequestParam(value = "fileIdList") List<Long> fileIdList) {
        return Result.success(fileInfoService.getFileInfo(fileIdList));
    }
}
