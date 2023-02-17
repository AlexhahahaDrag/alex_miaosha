package com.alex.api.user.api.tUserLogin;

import com.alex.api.oss.api.fallback.OssFallbackFactory;
import com.alex.api.oss.vo.fileInfo.FileInfoVo;
import com.alex.base.common.Result;
import com.alex.common.config.FeignConfig;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

/**
 * @description:  用户登录表controller
 * @author:       alex
 * @createDate:   2023-02-16 14:11:55
 * @version:      1.0.0
 */
@Component
@FeignClient(name = "alex-oss-dev", fallback = OssFallbackFactory.class, configuration = FeignConfig.class)
public interface TUserLoginApi {

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取文件信息表分页", notes = "获取文件信息表分页", response = Result.class)
    @PostMapping(value = "/api/v1/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum"),
            @ApiImplicitParam(value = "每页大小", name = "pageSize"),
            @ApiImplicitParam(value = "查询条件", name = "fileVo")}
    )
    Result<Page<FileInfoVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                            @RequestParam(value = "pageSize", required = false) Long pageSize,
                                            @RequestBody(required = false) FileInfoVo fileVo);

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取文件信息表详情", notes = "获取文件信息表详情", response = Result.class)
    @GetMapping
    Result<FileInfoVo> query(@RequestParam(value = "id") String id);

    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增文件信息表", notes = "新增文件信息表", response = Result.class)
    @PostMapping("/api/v1")
    Result<Boolean> add(@RequestBody FileInfoVo fileVo);

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改文件信息表", notes = "修改文件信息表", response = Result.class)
    @PutMapping("/api/v1")
    Result<Boolean> update(@RequestBody FileInfoVo fileVo);

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除文件信息表", notes = "刪除文件信息表", response = Result.class)
    @DeleteMapping("/api/v1")
    Result<Boolean> delete(@RequestParam("ids") String ids);
}