package com.alex.user.user.controller;

import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.base.common.Result;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.alex.common.annotations.LogRestRequest;
import com.alex.common.annotations.user.AccessLimit;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import com.alex.user.user.entity.TUser;
import com.alex.user.user.service.TUserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * description: 管理员表restApi
 * author: alex
 * createDate: 2022-12-26 17:20:38
 * version: 1.0.0
 */
@ApiSort(10)
@Api(value = "管理员表相关接口", tags = {"管理员表相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class TUserController {

    private final TUserService tUserService;

    @LogRestRequest(apiName = "获取管理员表分页")
    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取管理员表分页", notes = "获取管理员表分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "查询条件", name = "tUserVo", dataTypeClass = TUserVo.class)}
    )
    public Result<Page<TUserVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                         @RequestParam(value = "pageSize", required = false) Long pageSize,
                                         @RequestBody(required = false) TUserVo tUserVo) throws Exception {
        return Result.success(tUserService.getPage(pageNum, pageSize, tUserVo));
    }

    @LogRestRequest(apiName = "获取管理员表详情")
    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取管理员表详情", notes = "获取管理员表详情", response = Result.class)
    @GetMapping
    public Result<TUserVo> query(@RequestParam(value = "id") String id) {
        return Result.success(tUserService.queryTUser(id));
    }

    @LogRestRequest(apiName = "新增管理员表")
    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增管理员表", notes = "新增管理员表", response = Result.class)
    @PostMapping
    public Result<TUser> add(@RequestBody @Validated({Insert.class}) TUserVo tUserVo) {
        return Result.success(tUserService.addTUser(tUserVo));
    }

    @LogRestRequest(apiName = "修改管理员表")
    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改管理员表", notes = "修改管理员表", response = Result.class)
    @PutMapping
    public Result<TUser> update(@RequestBody @Validated({Update.class}) TUserVo tUserVo) {
        return Result.success(tUserService.updateTUser(tUserVo));
    }

    /**
     * RBAC-BE-USER-003: 用户启停专用接口。
     * 支持 query 或 body 传入 id/status；仅更新 status，不改其它字段。
     */
    @LogRestRequest(apiName = "更新用户状态")
    @ApiOperationSupport(order = 45, author = "alex")
    @ApiOperation(value = "更新用户状态", notes = "仅更新用户启停状态（1/0）", response = Result.class)
    @PutMapping("/status")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "用户ID", name = "id", dataTypeClass = String.class),
            @ApiImplicitParam(value = "状态(1有效/0无效)", name = "status", dataTypeClass = String.class)
    })
    public Result<Boolean> updateStatus(@RequestParam(value = "id", required = false) String id,
                                        @RequestParam(value = "status", required = false) String status,
                                        @RequestBody(required = false) TUserVo body) {
        String resolvedId = id;
        String resolvedStatus = status;
        if (body != null) {
            if ((resolvedId == null || resolvedId.isEmpty()) && body.getId() != null) {
                resolvedId = String.valueOf(body.getId());
            }
            if ((resolvedStatus == null || resolvedStatus.isEmpty()) && body.getStatus() != null) {
                resolvedStatus = body.getStatus();
            }
        }
        Long userId = resolvedId == null || resolvedId.isEmpty() ? null : Long.valueOf(resolvedId);
        return Result.success(tUserService.updateUserStatus(userId, resolvedStatus));
    }

    @LogRestRequest(apiName = "刪除管理员表")
    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除管理员表", notes = "刪除管理员表", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(tUserService.deleteTUser(ids));
    }

    @LogRestRequest(apiName = "用户登录")
    @ApiOperationSupport(order = 60, author = "alex")
    @AccessLimit()
    @PostMapping("/login")
    @ApiOperation(value = "登录")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "用户名称", name = "username", dataTypeClass = String.class),
            @ApiImplicitParam(value = "密码", name = "password", dataTypeClass = String.class),
            @ApiImplicitParam(value = "是否记住我", name = "isRememberMe", dataTypeClass = String.class)
    })
    public Result<Map<String, Object>> doLogin(HttpServletRequest request,
                                               @RequestParam(value = "username", required = false) String username,
                                               @RequestParam(value = "password", required = false) String password,
                                               @RequestParam(value = "isRememberMe", required = false) Boolean isRememberMe) throws Exception {
        return Result.success(tUserService.login(request, username, password, isRememberMe));
    }

    @LogRestRequest(apiName = "第三方登录渲染")
    @GetMapping("/third/{appName}")
    public void renderAuth(@PathVariable String appName, HttpServletResponse response) throws IOException {
        AuthRequest authRequest = tUserService.getAuthRequest(appName);
        response.sendRedirect(authRequest.authorize(AuthStateUtils.createState()));
    }

    @LogRestRequest(apiName = "第三方登录回调")
    @GetMapping("/callback")
    public Object login(AuthCallback callback) {
        AuthRequest authRequest = tUserService.getAuthRequest("baidu");
        return authRequest.login(callback);
    }

    @LogRestRequest(apiName = "用户登出")
    @ApiOperationSupport(order = 65, author = "alex")
    @PostMapping("/logout")
    @ApiOperation(value = "登出")
    public Result<Boolean> logout(HttpServletRequest request) {
        return tUserService.logout(request);
    }

    @LogRestRequest(apiName = "获取管理员列表")
    @ApiOperationSupport(order = 70, author = "alex")
    @ApiOperation(value = "获取管理员列表", notes = "获取管理员列表", response = Result.class)
    @PostMapping(value = "/list")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "查询条件", name = "tUserVo", dataTypeClass = TUserVo.class)}
    )
    public Result<List<TUserVo>> getList(@RequestBody(required = false) TUserVo tUserVo) {
        return Result.success(tUserService.getList(tUserVo));
    }

    @LogRestRequest(apiName = "获取用户详情")
    @ApiOperationSupport(order = 80, author = "alex")
    @ApiOperation(value = "获取用户详情", notes = "获取用户详情", response = Result.class)
    @GetMapping(value = "/getUserInfo")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "用户名", name = "username", dataTypeClass = String.class)}
    )
    public TUserVo getUserByUsername(@RequestParam(value = "username") String username) {
        return tUserService.getUserByUsername(username);
    }

    @LogRestRequest(apiName = "根据token 校验用户权限")
    @ApiOperationSupport(order = 90, author = "alex")
    @ApiOperation(value = "根据token 校验用户权限", notes = "根据token 校验用户权限", response = Result.class)
    @GetMapping(value = "/authToken")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "token", name = "token", dataTypeClass = String.class)}
    )
    public Result<Boolean> authToken(@RequestParam(value = "token") String token) {
        return Result.success(tUserService.authToken(token));
    }
}
