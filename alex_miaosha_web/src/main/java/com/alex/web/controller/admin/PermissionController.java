package com.alex.web.controller.admin;

import com.alex.base.common.Result;
import com.alex.web.pojo.dto.PermissionDTO;
import com.alex.web.service.PermissionService;
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

import java.util.List;

/**
 * description:
 * author: majf
 * createDate: 2022/8/11 15:36
 * version: 1.0.0
 */
@Api(value = "权限模块", tags = {"权限模块"})
@ApiSort(85)
@ApiSupport(order = 85, author = "alex")
@RestController
@RequiredArgsConstructor
@RequestMapping("/permission")
public class PermissionController {

    private final PermissionService permissionService;

    @ApiOperation(value = "查询权限列表")
    @ApiOperationSupport(order = 1, author = "alex")
    @GetMapping
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页数", name = "page", type = "Long.clas"),
            @ApiImplicitParam(value = "页面大小", name = "pageSize", type = "Long.clas"),
            @ApiImplicitParam(value = "查询", name = "search", type = "Long.clas")
    }
    )
    public Result<Page<PermissionDTO>> findByPermissions(@RequestParam(value = "page", required = false) Long page,
                                                         @RequestParam(value = "pageSize", required = false) Long pageSize,
                                                         @RequestParam(value = "search", required = false) String search) {
        return Result.success(permissionService.findPermissions(page, pageSize, search));
    }

    @ApiOperation(value = "新增权限")
    @ApiOperationSupport(order = 5, author = "alex")
    @PutMapping
    @ApiImplicitParams({
            @ApiImplicitParam(value = "权限dto", name = "permissionDTO", type = "PermissionDTO.clas", required = true)
    }
    )
    public Result createPermission(@RequestBody PermissionDTO permissionDTO) {
        permissionService.createPermission(permissionDTO);
        return Result.success();
    }

    @ApiOperation(value = "修改权限")
    @ApiOperationSupport(order = 15, author = "alex")
    @PostMapping
    @ApiImplicitParams({
            @ApiImplicitParam(value = "权限dto", name = "permissionDTO", type = "PermissionDTO.clas", required = true)
    }
    )
    public Result updatePermission(@RequestBody PermissionDTO permissionDTO) {
        permissionService.updatePermission(permissionDTO);
        return Result.success();
    }

    @ApiOperation(value = "删除权限")
    @ApiOperationSupport(order = 35, author = "alex")
    @DeleteMapping
    @ApiImplicitParams({
            @ApiImplicitParam(value = "权限ids", name = "ids", type = "String.clas")
    }
    )
    public Result deletePermission(@RequestParam(value = "ids", required = false) String ids) {
        permissionService.deletes(ids);
        return Result.success();
    }

    @ApiOperation(value = "查询所有权限")
    @ApiOperationSupport(order = 45, author = "alex")
    @GetMapping("/findAll")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "查询", name = "search", type = "String.clas")
    }
    )
    public Result<List<PermissionDTO>> findAll(@RequestParam(value = "search", required = false) String search) {
        return Result.success(permissionService.findAll(search));
    }
}
