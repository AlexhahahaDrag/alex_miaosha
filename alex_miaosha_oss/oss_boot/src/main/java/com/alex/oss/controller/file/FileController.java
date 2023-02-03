package com.alex.oss.controller.file;

import com.alex.api.oss.vo.file.FileInfoVo;
import com.alex.base.common.Result;
import com.alex.oss.service.FileService;
import com.alex.utils.annotations.AvoidRepeatableCommit;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * description:  文件
 * author:       majf
 * createDate:   2023/1/12 14:46
 * version:      1.0.0
 */
@ApiSort(10)
@Api(value = "文件相关接口", tags = {"文件相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/file")
public class FileController {

    private final FileService fileService;

    // TODO: 2023/1/12 s实现多附件上传
    @AvoidRepeatableCommit(message = "请不要重复上传文件")
    @ApiOperationSupport(order = 10, author = "alex")
    @PostMapping
    @ApiOperation(value = "文件上传", notes = "文件上传", response = Result.class)
    public Result<FileInfoVo> uploadFile(@RequestPart(value = "file") MultipartFile file,
                                         @RequestParam(value = "type", required = false) String type) throws Exception {
        FileInfoVo fileInfoVo = fileService.uploadFile(file, type);
        return Result.success(fileInfoVo);
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @DeleteMapping
    @ApiOperation(value = "文件删除", notes = "文件删除", response = Result.class)
    @ApiImplicitParams({
            @ApiImplicitParam(value = "路径", name = "filePath", required = true),
            @ApiImplicitParam(value = "类型", name = "type")}
    )
    public Result deleteFile(@RequestParam(value = "filePath") List<String> filePath,
                             @RequestParam(value = "type", required = false) String type) throws Exception {
        return Result.success(fileService.deleteFile(filePath, type));
    }

//    @ApiOperationSupport(order = 30, author = "alex")
//    @ApiOperation(value = "文件下载", notes = "文件下载", response = Result.class)
//    @GetMapping
//    @ApiImplicitParams({
//            @ApiImplicitParam(value = "文件名", name = "fileName", required = true),
//            @ApiImplicitParam(value = "类型", name = "type"),
//            @ApiImplicitParam(value = "是否删除", name = "delete")}
//    )
//    public Result deleteFile(@RequestParam(value = "fileName") String fileName,
//                             @RequestParam(value = "type", required = false) String type,
//                             @RequestParam(value = "delete", required = false) Boolean delete, HttpServletResponse response) {
//        return Result.success(fileService.fileDownload(type, fileName, delete, response));
//    }
}
