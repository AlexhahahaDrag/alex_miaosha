package com.alex.web.controller.admin;

import com.alex.base.common.Result;
import com.alex.web.pojo.dto.RoleDTO;
import com.alex.web.service.RoleService;
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
 * @description: controller
 * @author: majf
 * @createDate: 2022/8/9 11:54
 * @version: 1.0.0
 */
@Api(value = "角色模块", tags = {"角色模块"})
@ApiSort(25)
@ApiSupport(order = 25, author = "alex")
@RestController
@RequiredArgsConstructor
@RequestMapping("/role")
public class RoleController {

    private final RoleService roleService;

    @ApiOperation(value = "查询角色列表")
    @ApiOperationSupport(order = 1, author = "alex")
    @GetMapping
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页数", name = "page", type = "Long.clas"),
            @ApiImplicitParam(value = "页面大小", name = "pageSize", type = "Long.clas"),
            @ApiImplicitParam(value = "查询", name = "search", type = "Long.clas")
    }
    )
    public Result<Page<RoleDTO>> findByRoles(@RequestParam(value = "page", required = false) Long page,
                                             @RequestParam(value = "pageSize", required = false) Long pageSize,
                                             @RequestParam(value = "search", required = false) String search) {
        return Result.success(roleService.findRoles(page, pageSize, search));
    }

    @ApiOperation(value = "新增角色")
    @ApiOperationSupport(order = 5, author = "alex")
    @PutMapping
    @ApiImplicitParams({
            @ApiImplicitParam(value = "角色dto", name = "roleDTO", type = "RoleDTO.clas", required = true)
    }
    )
    public Result createRole(@RequestBody RoleDTO roleDTO) {
        roleService.createRole(roleDTO);
        return Result.success();
    }

    @ApiOperation(value = "修改角色")
    @ApiOperationSupport(order = 15, author = "alex")
    @PostMapping
    @ApiImplicitParams({
            @ApiImplicitParam(value = "角色dto", name = "roleDTO", type = "RoleDTO.clas", required = true)
    }
    )
    public Result updateRole(@RequestBody RoleDTO roleDTO) {
        roleService.updateRole(roleDTO);
        return Result.success();
    }

    @ApiOperation(value = "删除角色")
    @ApiOperationSupport(order = 35, author = "alex")
    @DeleteMapping
    @ApiImplicitParams({
            @ApiImplicitParam(value = "角色ids", name = "ids", type = "String.clas")
    }
    )
    public Result deleteRole(@RequestParam(value = "ids", required = false) String ids) {
        roleService.deletes(ids);
        return Result.success();
    }

    @ApiOperation(value = "查询所有角色")
    @ApiOperationSupport(order = 45, author = "alex")
    @GetMapping("/findAll")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "查询", name = "search", type = "String.clas")
    }
    )
    public Result<List<RoleDTO>> findAll(@RequestParam(value = "search", required = false) String search) {
        return Result.success(roleService.findAll(search));
    }
}
