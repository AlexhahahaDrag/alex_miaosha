package com.alex.user.controller.roleUserInfo;

import com.alex.api.user.vo.rolePermissionInfo.RolePermissionInfoVo;
import com.alex.api.user.vo.roleUserInfo.RoleUserInfoVo;
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
import com.alex.user.entity.roleUserInfo.RoleUserInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import com.alex.base.common.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import com.alex.user.service.roleUserInfo.RoleUserInfoService;
import org.springframework.web.bind.annotation.RestController;

/**
 * description:  用户角色信息表restApi
 * author:       majf
 * createDate:   2024-01-15 15:12:07
 * version:      1.0.0
 */
@ApiSort(105)
@Api(value = "用户角色信息表相关接口", tags = {"用户角色信息表相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/role-user-info")
public class RoleUserInfoController {

    private final RoleUserInfoService roleUserInfoService;

    @ApiOperationSupport(order = 10, author = "majf")
    @ApiOperation(value = "获取用户角色信息表分页", notes = "获取用户角色信息表分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "查询条件", name = "roleUserInfoVo", dataTypeClass = RoleUserInfoVo.class)}
    )
    public Result<Page<RoleUserInfoVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                                 @RequestParam(value = "pageSize", required = false) Long pageSize,
                                                 @RequestBody(required = false) RoleUserInfoVo roleUserInfoVo) {
        return Result.success(roleUserInfoService.getPage(pageNum, pageSize, roleUserInfoVo));
    }

    @ApiOperationSupport(order = 20, author = "majf")
    @ApiOperation(value = "获取用户角色信息表详情", notes = "获取用户角色信息表详情", response = Result.class)
    @GetMapping
    public Result<RoleUserInfoVo> query(@RequestParam(value = "id") String id) {
        return Result.success(roleUserInfoService.queryRoleUserInfo(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "majf")
    @ApiOperation(value = "新增用户角色信息表", notes = "新增用户角色信息表", response = Result.class)
    @PostMapping
    public Result<Boolean> add(@Validated({Insert.class}) @RequestBody RoleUserInfoVo roleUserInfoVo) {
        return Result.success(roleUserInfoService.addRoleUserInfo(roleUserInfoVo));
    }

    @ApiOperationSupport(order = 40, author = "majf")
    @ApiOperation(value = "修改用户角色信息表", notes = "修改用户角色信息表", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody RoleUserInfoVo roleUserInfoVo) {
        return Result.success(roleUserInfoService.updateRoleUserInfo(roleUserInfoVo));
    }

    @ApiOperationSupport(order = 50, author = "majf")
    @ApiOperation(value = "刪除用户角色信息表", notes = "刪除用户角色信息表", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(roleUserInfoService.deleteRoleUserInfo(ids));
    }
}
