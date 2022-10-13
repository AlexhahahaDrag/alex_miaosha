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
            @ApiImplicitParam(value = "文件名称", name = "fileName", defaultValue = "test"),
            @ApiImplicitParam(value = "父级包名称", name = "parentPackage"),
            @ApiImplicitParam(value = "表格列表", name = "tableNames"),
            @ApiImplicitParam(value = "作者(Alex)", name = "author"),
            @ApiImplicitParam(value = "自定义controller路径", name = "myControllerPath"),
            @ApiImplicitParam(value = "自定义service路径", name = "myServicePath"),
            @ApiImplicitParam(value = "自定义mapper路径", name = "myMapperPath"),
            @ApiImplicitParam(value = "自定义entity路径", name = "myEntityPath"),
            @ApiImplicitParam(value = "自定义vo路径", name = "myVoPath"),
            @ApiImplicitParam(value = "自定义client路径", name = "myClientPath")})
    public Result<Boolean> generate(@RequestParam(value = "moduleName") String moduleName,
                                    @RequestParam(value = "javaPath") String javaPath,
                                    @RequestParam(value = "fileName") String fileName,
                                    @RequestParam(value = "parentPackage") String parentPackage,
                                    @RequestParam(value = "tableNames") String[] tableNames,
                                    @RequestParam(value = "author", required = false) String author,
                                    @RequestParam(value = "myControllerPath", required = false) String myControllerPath,
                                    @RequestParam(value = "myServicePath", required = false) String myServicePath,
                                    @RequestParam(value = "myMapperPath", required = false) String myMapperPath,
                                    @RequestParam(value = "myEntityPath", required = false) String myEntityPath,
                                    @RequestParam(value = "myVoPath", required = false) String myVoPath,
                                    @RequestParam(value = "myClientPath", required = false) String myClientPath) {
        return Result.success(generatorService.generator(moduleName, javaPath, fileName, parentPackage, tableNames, author,
                myControllerPath, myServicePath, myMapperPath, myEntityPath, myVoPath, myClientPath));
    }

    // TODO: 2022/10/12 修改vo模板集成baseVo 
}
