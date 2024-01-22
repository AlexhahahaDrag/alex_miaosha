package com.alex.user.controller.rolePermissionInfo;

import com.alex.api.user.vo.rolePermissionInfo.RolePermissionInfoVo;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import org.springframework.validation.annotation.Validated;
import com.alex.user.entity.rolePermissionInfo.RolePermissionInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import com.alex.base.common.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import com.alex.user.service.rolePermissionInfo.RolePermissionInfoService;
import org.springframework.web.bind.annotation.RestController;

/**
 * description:  角色权限信息表restApi
 * author:       majf
 * createDate:   2024-01-19 14:52:21
 * version:      1.0.0
 */
@ApiSort(105)
@Api(value = "角色权限信息表相关接口", tags = {"角色权限信息表相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/role-permission-info")
public class RolePermissionInfoController {

    private final RolePermissionInfoService rolePermissionInfoService;

    @ApiOperationSupport(order = 10, author = "majf")
    @ApiOperation(value = "获取角色权限信息表分页", notes = "获取角色权限信息表分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum"),
            @ApiImplicitParam(value = "每页大小", name = "pageSize"),
            @ApiImplicitParam(value = "查询条件", name = "rolePermissionInfoVo")}
    )
    public Result<Page<RolePermissionInfoVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                                 @RequestParam(value = "pageSize", required = false) Long pageSize,
                                                 @RequestBody(required = false) RolePermissionInfoVo rolePermissionInfoVo) {
        return Result.success(rolePermissionInfoService.getPage(pageNum, pageSize, rolePermissionInfoVo));
    }

    @ApiOperationSupport(order = 20, author = "majf")
    @ApiOperation(value = "获取角色权限信息表详情", notes = "获取角色权限信息表详情", response = Result.class)
    @GetMapping
    public Result<RolePermissionInfoVo> query(@RequestParam(value = "id") Long id) {
        return Result.success(rolePermissionInfoService.queryRolePermissionInfo(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "majf")
    @ApiOperation(value = "新增角色权限信息表", notes = "新增角色权限信息表", response = Result.class)
    @PostMapping
    public Result<Boolean> add(@Validated({Insert.class}) @RequestBody RolePermissionInfoVo rolePermissionInfoVo) {
        return Result.success(rolePermissionInfoService.addRolePermissionInfo(rolePermissionInfoVo));
    }

    @ApiOperationSupport(order = 40, author = "majf")
    @ApiOperation(value = "修改角色权限信息表", notes = "修改角色权限信息表", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody RolePermissionInfoVo rolePermissionInfoVo) {
        return Result.success(rolePermissionInfoService.updateRolePermissionInfo(rolePermissionInfoVo));
    }

    @ApiOperationSupport(order = 50, author = "majf")
    @ApiOperation(value = "刪除角色权限信息表", notes = "刪除角色权限信息表", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(rolePermissionInfoService.deleteRolePermissionInfo(ids));
    }
}
