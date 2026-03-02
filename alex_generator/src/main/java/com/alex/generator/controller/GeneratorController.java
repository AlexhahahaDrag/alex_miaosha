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
            @ApiImplicitParam(value = "java路径名称", name = "javaPathName", defaultValue = "财务管理"),
            @ApiImplicitParam(value = "java路径", name = "javaPath", defaultValue = "finance"),
            @ApiImplicitParam(value = "表格列表", name = "tableNames", defaultValue = "t_user"),
            @ApiImplicitParam(value = "表格列表名称", name = "tableNameInfo", defaultValue = "用户管理"),
            @ApiImplicitParam(value = "作者", name = "author", defaultValue = "alex")})
    public Result<Boolean> generate(
            // 将默认值放到 @RequestParam(defaultValue=...)，便于 doc.html 默认回填展示
            @RequestParam(value = "moduleName", required = false, defaultValue = "alex_miaosha_finance") String moduleName,
            @RequestParam(value = "javaPathName", required = false, defaultValue = "财务管理") String javaPathName,
            @RequestParam(value = "javaPath", required = false, defaultValue = "finance") String javaPath,
            // Spring 对数组参数支持逗号分隔，默认值写成单值/逗号分隔皆可
            @RequestParam(value = "tableNames", required = false, defaultValue = "t_user") String[] tableNames,
            @RequestParam(value = "tableNameInfo", required = false, defaultValue = "用户管理") String[] tableNameInfo,
            @RequestParam(value = "author", required = false, defaultValue = "alex") String author
    ) throws Exception {
        return Result.success(generatorService.generator(moduleName, javaPathName, javaPath, tableNames, tableNameInfo, author));
    }
}
