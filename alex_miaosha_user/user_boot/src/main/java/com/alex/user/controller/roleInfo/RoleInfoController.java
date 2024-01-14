package com.alex.user.controller.roleInfo;

import com.alex.api.user.vo.roleInfo.RoleInfoVo;
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
import com.alex.user.entity.roleInfo.RoleInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import com.alex.base.common.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import com.alex.user.service.roleInfo.RoleInfoService;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:  角色信息表restApi
 * @author:       majf
 * @createDate:   2024-01-14 21:56:18
 * @version:      1.0.0
 */
@ApiSort(105)
@Api(value = "角色信息表相关接口", tags = {"角色信息表相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/role-info")
public class RoleInfoController {

    private final RoleInfoService roleInfoService;

    @ApiOperationSupport(order = 10, author = "majf")
    @ApiOperation(value = "获取角色信息表分页", notes = "获取角色信息表分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum"),
            @ApiImplicitParam(value = "每页大小", name = "pageSize"),
            @ApiImplicitParam(value = "查询条件", name = "roleInfoVo")}
    )
    public Result<Page<RoleInfoVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                                 @RequestParam(value = "pageSize", required = false) Long pageSize,
                                                 @RequestBody(required = false) RoleInfoVo roleInfoVo) {
        return Result.success(roleInfoService.getPage(pageNum, pageSize, roleInfoVo));
    }

    @ApiOperationSupport(order = 20, author = "majf")
    @ApiOperation(value = "获取角色信息表详情", notes = "获取角色信息表详情", response = Result.class)
    @GetMapping
    public Result<RoleInfoVo> query(@RequestParam(value = "id") String id) {
        return Result.success(roleInfoService.queryRoleInfo(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "majf")
    @ApiOperation(value = "新增角色信息表", notes = "新增角色信息表", response = Result.class)
    @PostMapping
    public Result<Boolean> add(@Validated({Insert.class}) @RequestBody RoleInfoVo roleInfoVo) {
        return Result.success(roleInfoService.addRoleInfo(roleInfoVo));
    }

    @ApiOperationSupport(order = 40, author = "majf")
    @ApiOperation(value = "修改角色信息表", notes = "修改角色信息表", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody RoleInfoVo roleInfoVo) {
        return Result.success(roleInfoService.updateRoleInfo(roleInfoVo));
    }

    @ApiOperationSupport(order = 50, author = "majf")
    @ApiOperation(value = "刪除角色信息表", notes = "刪除角色信息表", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(roleInfoService.deleteRoleInfo(ids));
    }
}
