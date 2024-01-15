package com.alex.web.controller.admin;

import com.alex.base.common.Result;
import com.alex.web.service.AdminUserService;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * description:
 * author: majf
 * createDate: 2022/8/11 15:36
 * version: 1.0.0
 */
@Api(value = "菜单模块", tags = {"菜单模块"})
@ApiSort(95)
@ApiSupport(order = 95, author = "alex")
@RestController
@RequiredArgsConstructor
@RequestMapping("/menu")
public class MenuController {

    private final AdminUserService adminUserService;

    @ApiOperation(value = "根据用户名称查询菜单信息")
    @ApiOperationSupport(order = 1, author = "alex")
    @GetMapping
    @ApiImplicitParams({
            @ApiImplicitParam(value = "用户名", name = "username", type = "String.clas")
    }
    )
    public Result findMenuByUsername(@RequestParam(value = "username") String username) {
        return Result.success(adminUserService.findMenuByUsername(username));
    }
}
