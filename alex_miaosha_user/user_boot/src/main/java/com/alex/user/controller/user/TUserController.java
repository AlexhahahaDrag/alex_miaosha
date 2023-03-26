package com.alex.user.controller.user;

import com.alex.api.user.vo.user.TUserVo;
import com.alex.base.common.Result;
import com.alex.common.annotations.user.AccessLimit;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import com.alex.user.entity.user.TUser;
import com.alex.user.service.user.TUserService;
import com.alex.utils.annotations.AvoidRepeatableCommit;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @description: 管理员表restApi
 * @author: alex
 * @createDate: 2022-12-26 17:20:38
 * @version: 1.0.0
 */
@ApiSort(10)
@Api(value = "管理员表相关接口", tags = {"管理员表相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class TUserController {

    private final TUserService tUserService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取管理员表分页", notes = "获取管理员表分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum"),
            @ApiImplicitParam(value = "每页大小", name = "pageSize"),
            @ApiImplicitParam(value = "查询条件", name = "tUserVo")}
    )
    public Result<Page<TUserVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                         @RequestParam(value = "pageSize", required = false) Long pageSize,
                                         @RequestBody(required = false) TUserVo tUserVo) {
        return Result.success(tUserService.getPage(pageNum, pageSize, tUserVo));
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取管理员表详情", notes = "获取管理员表详情", response = Result.class)
    @GetMapping
    public Result<TUserVo> query(@RequestParam(value = "id") String id) {
        return Result.success(tUserService.queryTUser(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增管理员表", notes = "新增管理员表", response = Result.class)
    @PostMapping
    public Result<TUser> add(@RequestBody @Validated({Insert.class}) TUserVo tUserVo) {
        return Result.success(tUserService.addTUser(tUserVo));
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改管理员表", notes = "修改管理员表", response = Result.class)
    @PutMapping
    public Result<TUser> update(@RequestBody @Validated({Update.class}) TUserVo tUserVo) {
        return Result.success(tUserService.updateTUser(tUserVo));
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除管理员表", notes = "刪除管理员表", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(tUserService.deleteTUser(ids));
    }

    @ApiOperationSupport(order = 60, author = "alex")
    @AccessLimit()
    @PostMapping("/login")
    @ApiOperation(value = "登录")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "用户名称", name = "username", type = "String.class"),
            @ApiImplicitParam(value = "密码", name = "password", type = "String.class"),
            @ApiImplicitParam(value = "是否记住我", name = "isRememberMe", type = "Boolean.class")
    })
    public Result<Object> doLogin(HttpServletRequest request,
                                  @RequestParam(value = "username", required = false) String username,
                                  @RequestParam(value = "password", required = false) String password,
                                  @RequestParam(value = "isRememberMe", required = false) Boolean isRememberMe) throws Exception {
        return tUserService.login(request, username, password, isRememberMe);
    }

    @ApiOperationSupport(order = 65, author = "alex")
    @PostMapping("/logout")
    @ApiOperation(value = "登出")
    public Result<Boolean> logout() {
        return tUserService.logout();
    }

    @ApiOperationSupport(order = 70, author = "alex")
    @ApiOperation(value = "获取管理员列表", notes = "获取管理员列表", response = Result.class)
    @PostMapping(value = "/list")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "查询条件", name = "tUserVo")}
    )
    public Result<List<TUserVo>> getList(@RequestBody(required = false) TUserVo tUserVo) {
        return Result.success(tUserService.getList(tUserVo));
    }

    @ApiOperationSupport(order = 80, author = "alex")
    @ApiOperation(value = "获取用户详情", notes = "获取用户详情", response = Result.class)
    @GetMapping(value = "/getUserInfo")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "用户名", name = "username")}
    )
    public TUserVo getUserByUsername(@RequestParam(value = "username") String username) {
        return tUserService.getUserByUsername(username);
    }

    @ApiOperationSupport(order = 90, author = "alex")
    @ApiOperation(value = "根据token校验用户权限", notes = "根据token校验用户权限", response = Result.class)
    @GetMapping(value = "/authToken")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "token", name = "token")}
    )
    public Result<Boolean> authToken(@RequestParam(value = "token") String token) {
        return Result.success(tUserService.authToken(token)) ;
    }
}
