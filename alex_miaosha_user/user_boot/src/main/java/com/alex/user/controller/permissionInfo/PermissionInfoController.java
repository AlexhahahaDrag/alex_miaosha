package com.alex.user.controller.permissionInfo;

import com.alex.api.user.vo.permissionInfo.PermissionInfoVo;
import com.alex.base.common.Result;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import com.alex.user.service.permissionInfo.PermissionInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * description:  权限信息表restApi
 * author:       majf
 * createDate:   2024-01-16 15:43:56
 * version:      1.0.0
 */
@ApiSort(105)
@Api(value = "权限信息表相关接口", tags = {"权限信息表相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/permission-info")
public class PermissionInfoController {

    private final PermissionInfoService permissionInfoService;

    @ApiOperationSupport(order = 10, author = "majf")
    @ApiOperation(value = "获取权限信息表分页", notes = "获取权限信息表分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum"),
            @ApiImplicitParam(value = "每页大小", name = "pageSize"),
            @ApiImplicitParam(value = "查询条件", name = "permissionInfoVo")}
    )
    public Result<Page<PermissionInfoVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                                 @RequestParam(value = "pageSize", required = false) Long pageSize,
                                                 @RequestBody(required = false) PermissionInfoVo permissionInfoVo) {
        return Result.success(permissionInfoService.getPage(pageNum, pageSize, permissionInfoVo));
    }

    @ApiOperationSupport(order = 20, author = "majf")
    @ApiOperation(value = "获取权限信息表详情", notes = "获取权限信息表详情", response = Result.class)
    @GetMapping
    public Result<PermissionInfoVo> query(@RequestParam(value = "id") Long id) {
        return Result.success(permissionInfoService.queryPermissionInfo(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "majf")
    @ApiOperation(value = "新增权限信息表", notes = "新增权限信息表", response = Result.class)
    @PostMapping
    public Result<PermissionInfoVo> add(@Validated({Insert.class}) @RequestBody PermissionInfoVo permissionInfoVo) {
        return Result.success(permissionInfoService.addPermissionInfo(permissionInfoVo));
    }

    @ApiOperationSupport(order = 40, author = "majf")
    @ApiOperation(value = "修改权限信息表", notes = "修改权限信息表", response = Result.class)
    @PutMapping
    public Result<PermissionInfoVo> update(@Validated({Update.class}) @RequestBody PermissionInfoVo permissionInfoVo) {
        return Result.success(permissionInfoService.updatePermissionInfo(permissionInfoVo));
    }

    @ApiOperationSupport(order = 50, author = "majf")
    @ApiOperation(value = "刪除权限信息表", notes = "刪除权限信息表", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(permissionInfoService.deletePermissionInfo(ids));
    }

    @ApiOperationSupport(order = 60, author = "alex")
    @ApiOperation(value = "获取权限管理列表", notes = "获取权限管理列表", response = Result.class)
    @PostMapping(value = "/list")
    public Result<List<PermissionInfoVo>> getList(@RequestBody(required = false) PermissionInfoVo permissionInfoVo) {
        return Result.success(permissionInfoService.getList(permissionInfoVo));
    }
}
