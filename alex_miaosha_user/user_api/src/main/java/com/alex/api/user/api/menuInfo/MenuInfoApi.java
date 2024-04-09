package com.alex.api.user.api.menuInfo;

import com.alex.api.user.vo.menuInfo.MenuInfoVo;
import com.alex.base.common.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

/**
 * description:  菜单管理表controller
 * author:       alex
 * createDate:   2023-12-19 17:34:23
 * version:      1.0.0
 */
@Component
// @FeignClient(name = "alex-oss-${spring.profiles.active:dev}", configuration = FeignConfig.class)
public interface MenuInfoApi {

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取菜单管理表分页", notes = "获取菜单管理表分页", response = Result.class)
    @PostMapping(value = "/api/v1//menu-info/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "查询条件", name = "menuInfoVo", dataTypeClass = Integer.class)}
    )
    Result<Page<MenuInfoVo>> getMenuInfoPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                            @RequestParam(value = "pageSize", required = false) Long pageSize,
                                            @RequestBody(required = false) MenuInfoVo menuInfoVo);

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取菜单管理表详情", notes = "获取菜单管理表详情", response = Result.class)
    @GetMapping(value = "/api/v1//menu-info")
    Result<MenuInfoVo> queryMenuInfo(@RequestParam(value = "id") String id);

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改菜单管理表", notes = "修改菜单管理表", response = Result.class)
    @PutMapping("/api/v1//menu-info")
    Result<Boolean> updateMenuInfo(@RequestBody MenuInfoVo menuInfoVo);

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除菜单管理表", notes = "刪除菜单管理表", response = Result.class)
    @DeleteMapping("/api/v1")
    Result<Boolean> deleteMenuInfo(@RequestParam("ids") String ids);
}