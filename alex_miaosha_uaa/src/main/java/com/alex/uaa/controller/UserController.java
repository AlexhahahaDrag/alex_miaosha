package com.alex.uaa.controller;

import com.alex.base.common.Result;
import com.alex.common.annotations.uaa.AccessLimit;
import com.alex.uaa.pojo.vo.LoginParam;
import com.alex.uaa.pojo.vo.RegisterParam;
import com.alex.uaa.pojo.vo.UpdatePasswordParam;
import com.alex.uaa.service.UserService;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @description:  user控制器
 * @author:       majf
 * @createDate:   2022/8/8 15:42
 * @version:      1.0.0
 */
@ApiSort(195)
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/user")
@Api(value = "用户接口", tags = {"用户"})
public class UserController {

    private final UserService userService;

    @AccessLimit()
    @PostMapping("doLogin")
    @ApiOperation(value = "登录")
    @ApiImplicitParams(
            @ApiImplicitParam(value = "loginParam", name = "登录参数", required = true)
    )
    public Result<String> doLogin(@RequestBody LoginParam loginParam) {

        return userService.doLogin(loginParam);
    }

    @PostMapping("doLgout")
    @ApiOperation(value = "注销")
    public Result<String> doLgout(HttpServletRequest request) {
        return userService.doLogout(request);
    }

    // TODO: 2022/8/8 添加防止重复提交
    @PostMapping("doRegister")
    @ApiOperation(value = "注册")
    @ApiImplicitParams(
            @ApiImplicitParam(value = "registerParam", name = "注册参数", required = true)
    )
    public Result doRegister(@RequestBody RegisterParam registerParam) {
        return userService.doRegister(registerParam);
    }

    @PostMapping("updatePassword")
    @ApiOperation(value = "更换密码")
    @ApiImplicitParams(
            @ApiImplicitParam(value = "updatePasswordParam", name = "更换密码", required = true)
    )
    public Result updatePassword(@RequestBody UpdatePasswordParam updatePasswordParam, HttpServletRequest request) {
        return userService.updatePassword(updatePasswordParam, request);
    }
}
