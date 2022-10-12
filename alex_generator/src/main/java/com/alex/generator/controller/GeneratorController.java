package com.alex.generator.controller;

import com.alex.base.common.Result;
import com.alex.common.annotations.user.AccessLimit;
import com.alex.generator.service.GenerateService;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * description:  generator控制器
 * author:       majf
 * createDate:   2022/10/11 14:44
 * version:      1.0.0
 */
@ApiSort(10)
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/generator")
@Api(value = "generator接口", tags = {"generator"})
public class GeneratorController {

    private final GenerateService generateService;

    @ApiOperationSupport(order = 1, author = "alex")
    @AccessLimit()
    @PostMapping("/generate")
    @ApiOperation(value = "自动生成", notes = "自动生成代码")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum"),
            @ApiImplicitParam(value = "每页大小", name = "pageSize"),
            @ApiImplicitParam(value = "查询条件", name = "financeInfoVo")}
    )
    public Result<Boolean> generate() {
        return Result.success(generateService.generate());
    }

    // TODO: 2022/10/12 修改vo模板集成baseVo 
}
