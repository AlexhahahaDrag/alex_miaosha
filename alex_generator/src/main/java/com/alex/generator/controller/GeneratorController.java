package com.alex.generator.controller;

import com.alex.base.common.Result;
import com.alex.generator.service.GeneratorService;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
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

    private final GeneratorService generatorService;

    @ApiOperationSupport(order = 1, author = "alex")
    @GetMapping
    @ApiOperation(value = "自动生成", notes = "自动生成代码")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "服务名称", name = "moduleName", defaultValue = "alex_miaosha_finance"),
            @ApiImplicitParam(value = "java路径", name = "javaPath", defaultValue = "finance"),
            @ApiImplicitParam(value = "表格列表", name = "tableNames", defaultValue = "t_user"),
            @ApiImplicitParam(value = "作者(Alex)", name = "author", defaultValue = "alex")})
    public Result<Boolean> generate(@RequestParam(value = "moduleName") String moduleName,
                                    @RequestParam(value = "javaPath") String javaPath,
                                    @RequestParam(value = "tableNames") String[] tableNames,
                                    @RequestParam(value = "author", required = false) String author) {
        return Result.success(generatorService.generator(moduleName, javaPath, tableNames, author));
    }

    // TODO: 2022/10/12 修改vo模板集成baseVo 
}
