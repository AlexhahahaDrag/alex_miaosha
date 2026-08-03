package com.alex.api.user.roleInfo.api;

import com.alex.api.user.roleInfo.fallback.RoleInfoFallbackFactory;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.roleInfo.vo.RolePermissionAssignRequest;
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
 * description:  角色信息表controller
 * author:       majf
 * createDate:   2024-01-14 21:56:18
 * version:      1.0.0
 */
@Component
@FeignClient(contextId = "roleInfoApi", name = "alex-user-${spring.profiles.active:dev}", path = "${api.version:/api/v1}/role-info", configuration = FeignConfig.class, fallback = RoleInfoFallbackFactory.class)
public interface RoleInfoApi {

    @ApiOperationSupport(order = 10, author = "majf")
    @ApiOperation(value = "获取角色信息表分页", notes = "获取角色信息表分页", response = Result.class)
    @PostMapping(value = "page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "查询条件", name = "roleInfoVo")}
    )
    Result<Page<RoleInfoVo>> getRoleInfoPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                            @RequestParam(value = "pageSize", required = false) Long pageSize,
                                            @RequestBody(required = false) RoleInfoVo roleInfoVo);

    @ApiOperationSupport(order = 20, author = "majf")
    @ApiOperation(value = "获取角色信息表详情", notes = "获取角色信息表详情", response = Result.class)
    @GetMapping
    Result<RoleInfoVo> queryRoleInfo(@RequestParam(value = "id") String id);

    @ApiOperationSupport(order = 30, author = "majf")
    @ApiOperation(value = "新增角色信息表", notes = "新增角色信息表", response = Result.class)
    @PostMapping
    Result<Boolean> addRoleInfo(@RequestBody RoleInfoVo roleInfoVo);

    @ApiOperationSupport(order = 40, author = "majf")
    @ApiOperation(value = "修改角色信息表", notes = "修改角色信息表", response = Result.class)
    @PutMapping
    Result<Boolean> updateRoleInfo(@RequestBody RoleInfoVo roleInfoVo);

    @ApiOperationSupport(order = 50, author = "majf")
    @ApiOperation(value = "刪除角色信息表", notes = "刪除角色信息表", response = Result.class)
    @DeleteMapping
    Result<Boolean> deleteRoleInfo(@RequestParam("ids") String ids);

    @ApiOperationSupport(order = 70, author = "alex")
    @ApiOperation(value = "角色分配权限", notes = "全量替换角色权限关系", response = Result.class)
    @PostMapping("/assign-permissions")
    Result<Boolean> assignPermissions(@RequestBody RolePermissionAssignRequest request);
}