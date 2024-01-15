package com.alex.user.controller.tUserLogin;

import com.alex.api.user.vo.tUserLogin.TUserLoginVo;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import org.springframework.validation.annotation.Validated;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import com.alex.base.common.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import com.alex.user.service.tUserLogin.TUserLoginService;
import org.springframework.web.bind.annotation.RestController;

/**
 * description:  用户登录表restApi
 * author:       alex
 * createDate:   2023-02-16 14:11:55
 * version:      1.0.0
 */
@ApiSort(20)
@Api(value = "用户登录表相关接口", tags = {"用户登录表相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/userLoginInfo")
public class TUserLoginController {

    private final TUserLoginService tUserLoginService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取用户登录表分页", notes = "获取用户登录表分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum"),
            @ApiImplicitParam(value = "每页大小", name = "pageSize"),
            @ApiImplicitParam(value = "查询条件", name = "tUserLoginVo")}
    )
    public Result<Page<TUserLoginVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                                 @RequestParam(value = "pageSize", required = false) Long pageSize,
                                                 @RequestBody(required = false) TUserLoginVo tUserLoginVo) {
        return Result.success(tUserLoginService.getPage(pageNum, pageSize, tUserLoginVo));
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取用户登录表详情", notes = "获取用户登录表详情", response = Result.class)
    @GetMapping
    public Result<TUserLoginVo> query(@RequestParam(value = "id") String id) {
        return Result.success(tUserLoginService.queryTUserLogin(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增用户登录表", notes = "新增用户登录表", response = Result.class)
    @PostMapping
    public Result<Boolean> add(@Validated({Insert.class}) @RequestBody TUserLoginVo tUserLoginVo) {
        return Result.success(tUserLoginService.addTUserLogin(tUserLoginVo));
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改用户登录表", notes = "修改用户登录表", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody TUserLoginVo tUserLoginVo) {
        return Result.success(tUserLoginService.updateTUserLogin(tUserLoginVo));
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除用户登录表", notes = "刪除用户登录表", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(tUserLoginService.deleteTUserLogin(ids));
    }
}
