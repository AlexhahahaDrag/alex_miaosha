package com.alex.user.controller.menuInfo;

import com.alex.api.user.vo.menuInfo.MenuInfoVo;
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
import com.alex.user.entity.menuInfo.MenuInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import com.alex.base.common.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import com.alex.user.service.menuInfo.MenuInfoService;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:  菜单管理表restApi
 * @author:       alex
 * @createDate:   2023-12-19 17:34:23
 * @version:      1.0.0
 */
@ApiSort(105)
@Api(value = "菜单管理表相关接口", tags = {"菜单管理表相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/menu-info")
public class MenuInfoController {

    private final MenuInfoService menuInfoService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取菜单管理表分页", notes = "获取菜单管理表分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum"),
            @ApiImplicitParam(value = "每页大小", name = "pageSize"),
            @ApiImplicitParam(value = "查询条件", name = "menuInfoVo")}
    )
    public Result<Page<MenuInfoVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                                 @RequestParam(value = "pageSize", required = false) Long pageSize,
                                                 @RequestBody(required = false) MenuInfoVo menuInfoVo) {
        return Result.success(menuInfoService.getPage(pageNum, pageSize, menuInfoVo));
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取菜单管理表详情", notes = "获取菜单管理表详情", response = Result.class)
    @GetMapping
    public Result<MenuInfoVo> query(@RequestParam(value = "id") String id) {
        return Result.success(menuInfoService.queryMenuInfo(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增菜单管理表", notes = "新增菜单管理表", response = Result.class)
    @PostMapping
    public Result<Boolean> add(@Validated({Insert.class}) @RequestBody MenuInfoVo menuInfoVo) {
        return Result.success(menuInfoService.addMenuInfo(menuInfoVo));
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改菜单管理表", notes = "修改菜单管理表", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody MenuInfoVo menuInfoVo) {
        return Result.success(menuInfoService.updateMenuInfo(menuInfoVo));
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除菜单管理表", notes = "刪除菜单管理表", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(menuInfoService.deleteMenuInfo(ids));
    }
}
