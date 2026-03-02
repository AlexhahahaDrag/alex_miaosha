package com.alex.api.user.permissionInfo.api;

import com.alex.api.user.permissionInfo.fallback.PermissionInfoFallbackFactory;
import com.alex.api.user.permissionInfo.vo.PermissionInfoVo;
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
 * description:  权限信息表controller
 * author:       majf
 * createDate:   2024-01-16 15:43:56
 * version:      1.0.0
 */
@Component
@FeignClient(contextId = "permissionInfoApi", name = "alex-user-${spring.profiles.active:dev}", path = "${api.version:/api/v1}/permission-info", configuration = FeignConfig.class, fallback = PermissionInfoFallbackFactory.class)
public interface PermissionInfoApi {

    @ApiOperationSupport(order = 10, author = "majf")
    @ApiOperation(value = "获取权限信息表分页", notes = "获取权限信息表分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "查询条件", name = "permissionInfoVo")}
    )
    Result<Page<PermissionInfoVo>> getPermissionInfoPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                            @RequestParam(value = "pageSize", required = false) Long pageSize,
                                            @RequestBody(required = false) PermissionInfoVo permissionInfoVo);

    @ApiOperationSupport(order = 20, author = "majf")
    @ApiOperation(value = "获取权限信息表详情", notes = "获取权限信息表详情", response = Result.class)
    @GetMapping
    Result<PermissionInfoVo> queryPermissionInfo(@RequestParam(value = "id") String id);

    @ApiOperationSupport(order = 30, author = "majf")
    @ApiOperation(value = "新增权限信息表", notes = "新增权限信息表", response = Result.class)
    @PostMapping
    Result<Boolean> addPermissionInfo(@RequestBody PermissionInfoVo permissionInfoVo);

    @ApiOperationSupport(order = 40, author = "majf")
    @ApiOperation(value = "修改权限信息表", notes = "修改权限信息表", response = Result.class)
    @PutMapping
    Result<Boolean> updatePermissionInfo(@RequestBody PermissionInfoVo permissionInfoVo);

    @ApiOperationSupport(order = 50, author = "majf")
    @ApiOperation(value = "刪除权限信息表", notes = "刪除权限信息表", response = Result.class)
    @DeleteMapping
    Result<Boolean> deletePermissionInfo(@RequestParam("ids") String ids);
}