package com.alex.web.controller.web;

import com.alex.base.common.Result;
import com.alex.common.pojo.dto.GoodsDTO;
import com.alex.web.cloud.MissionClient;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Api(value = "上传模块", tags = {"上传模块"})
@ApiSort(130)
@ApiSupport(order = 130, author = "alex")
@RestController
@RequiredArgsConstructor
@RequestMapping("/upload")
public class UploadController {

    private final MissionClient missionClient;

    @ApiOperation(value = "上传图片")
    @ApiOperationSupport(order = 1, author = "alex")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "文件", name = "file", type = "MultipartFile.clas"),
            @ApiImplicitParam(value = "类型", name = "type", type = "Integer.clas")
    }
    )
    @PostMapping
    public Result<Page<GoodsDTO>> uploadPicture(@RequestParam(value = "file") MultipartFile file,
                                         @RequestParam(value = "type") Integer type) {
        return missionClient.uploadPicture(file, type);
    }

    @ApiOperation(value = "删除图片")
    @ApiOperationSupport(order = 5, author = "alex")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "主键", name = "key", type = "String.clas")
    }
    )
    @DeleteMapping
    public Result<Page<GoodsDTO>> deletePicture(@RequestParam(value = "key") String key) {
        return missionClient.deletePicture(key);
    }

    @ApiOperation(value = "删除图片")
    @ApiOperationSupport(order = 5, author = "alex")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "主键s", name = "keys", type = "String.clas")
    }
    )
    @DeleteMapping("deletes")
    public Result<Page<GoodsDTO>> deletes(@RequestParam(value = "keys") String keys) {
        return missionClient.deletes(keys);
    }
}
