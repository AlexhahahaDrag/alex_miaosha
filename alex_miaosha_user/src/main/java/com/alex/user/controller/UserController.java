package com.alex.user.controller;

import com.alex.base.common.Result;
import com.alex.common.annotations.user.AccessLimit;
import com.alex.user.pojo.vo.LoginParam;
import com.alex.user.pojo.vo.RegisterParam;
import com.alex.user.pojo.vo.UpdatePasswordParam;
import com.alex.user.service.UserService;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @description: user控制器
 * @author: majf
 * @createDate: 2022/8/8 15:42
 * @version: 1.0.0
 */
@ApiSort(10)
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/user")
@Api(value = "用户接口", tags = {"用户"})
public class UserController {

    private final UserService userService;

    @ApiOperationSupport(order = 1)
    @AccessLimit()
    @PostMapping("/doLogin")
    @ApiOperation(value = "登录")
    @ApiImplicitParams(
            @ApiImplicitParam(value = "登录参数", name = "loginParam", required = true, type = "LoginParam.class")
    )
    public Result<String> doLogin(@RequestBody LoginParam loginParam) {
        return userService.doLogin(loginParam);
    }

    @ApiOperationSupport(order = 8)
    @PostMapping("/doLogout")
    @ApiOperation(value = "注销")
    public Result<String> doLogout(HttpServletRequest request) {
        return userService.doLogout(request);
    }

    // TODO: 2022/8/8 添加防止重复提交
    @ApiOperationSupport(order = 3)
    @PostMapping("/doRegister")
    @ApiOperation(value = "注册")
    @ApiImplicitParams(
            @ApiImplicitParam(value = "注册参数", name = "registerParam", required = true, type = "RegisterParam.class")
    )
    // TODO: 2022/8/11 校验编写
    public Result doRegister(@Validated @RequestBody RegisterParam registerParam) {
        return userService.doRegister(registerParam);
    }

    @ApiOperationSupport(order = 0)
    @PostMapping("/updatePassword")
    @ApiOperation(value = "更换密码")
    @ApiImplicitParams(
            @ApiImplicitParam(value = "更换密码", name = "updatePasswordParam", required = true)
    )
    public Result updatePassword(@RequestBody UpdatePasswordParam updatePasswordParam, HttpServletRequest request) {
        return userService.updatePassword(updatePasswordParam, request);
    }
}
