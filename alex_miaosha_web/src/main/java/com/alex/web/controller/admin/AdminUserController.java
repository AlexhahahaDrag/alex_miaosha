package com.alex.web.controller.admin;

import com.alex.base.common.Result;
import com.alex.web.pojo.dto.AdminUserDTO;
import com.alex.web.pojo.dto.AdminUserPermissionDTO;
import com.alex.web.service.AdminUserService;
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

/**
 * description: 管理员相关 controller
 * author: majf
 * createDate: 2022/8/9 11:54
 * version: 1.0.0
 */
@Api(value = "管理员模块", tags = {"管理员模块"})
@ApiSort(1)
@ApiSupport(order = 1, author = "alex")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminUserController {

    // TODO: 2022/8/11 添加权限
    private final AdminUserService adminUserService;

    @ApiOperation(value = "查询管理员列表")
    @ApiOperationSupport(order = 1, author = "alex")
    @GetMapping
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页数", name = "page", type = "Long.clas"),
            @ApiImplicitParam(value = "页面大小", name = "pageSize", type = "Long.clas"),
            @ApiImplicitParam(value = "查询", name = "search", type = "Long.clas")
    }
    )
    public Result<Page<AdminUserDTO>> findByAdminUsers(@RequestParam(value = "page", required = false) Long page,
                                                       @RequestParam(value = "pageSize", required = false) Long pageSize,
                                                       @RequestParam(value = "search", required = false) String search) {
        return Result.success(adminUserService.findAdminUsers(page, pageSize, search));
    }

    @ApiOperation(value = "新增管理员")
    @ApiOperationSupport(order = 5, author = "alex")
    @PutMapping
    @ApiImplicitParams({
            @ApiImplicitParam(value = "管理员req", name = "adminUserDTO", type = "AdminUserDTO.clas", required = true)
    }
    )
    public Result createAdminUser(@RequestBody AdminUserDTO adminUserDTO) {
        adminUserService.createAdminUser(adminUserDTO);
        return Result.success();
    }

    @ApiOperation(value = "修改管理员")
    @ApiOperationSupport(order = 15, author = "alex")
    @PostMapping
    @ApiImplicitParams({
            @ApiImplicitParam(value = "管理员req", name = "adminUserDTO", type = "AdminUserDTO.clas", required = true)
    }
    )
    public Result updateAdminUser(@RequestBody AdminUserDTO adminUserDTO) {
        adminUserService.updateAdminUser(adminUserDTO);
        return Result.success();
    }

    @ApiOperation(value = "修改管理员权限")
    @ApiOperationSupport(order = 25, author = "alex")
    @PostMapping("patchPermission")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "管理员权限DTO", name = "adminUserPermissionDTO", type = "AdminUserPermissionDTO.clas", required = true)
    }
    )
    public Result patchAdminUserPermission(@RequestBody AdminUserPermissionDTO adminUserPermissionDTO) {
        adminUserService.patchAdminUserPermission(adminUserPermissionDTO);
        return Result.success();
    }

    @ApiOperation(value = "删除管理员")
    @ApiOperationSupport(order = 35, author = "alex")
    @DeleteMapping
    @ApiImplicitParams({
            @ApiImplicitParam(value = "管理员ids", name = "ids", type = "String.clas")
    }
    )
    public Result deleteAdminUser(@RequestParam(value = "ids", required = false) String ids) {
        adminUserService.deletes(ids);
        return Result.success();
    }

    @ApiOperation(value = "启/禁用管理员")
    @ApiOperationSupport(order = 45, author = "alex")
    @GetMapping("/{id}")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "管理员id", name = "id", type = "Long.clas", required = true)
    }
    )
    public Result deleteAdminUser(@PathVariable("id") Long id) {
        adminUserService.switchIsBan(id);
        return Result.success();
    }
}
