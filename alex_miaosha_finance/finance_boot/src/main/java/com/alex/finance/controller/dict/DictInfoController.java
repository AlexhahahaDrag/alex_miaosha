package com.alex.finance.controller.dict;

import com.alex.base.common.Result;
import com.alex.finance.entity.dict.DictInfo;
import com.alex.finance.service.dict.DictInfoService;
import com.alex.finance.vo.dict.DictInfoVo;
import com.alex.utils.annotations.AvoidRepeatableCommit;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @description:  字典表restApi
 * @author:       alex
 * @createDate:   2022-10-13 17:47:15
 * @version:      1.0.0
 */
@ApiSort(105)
@Api(value = "字典表相关接口", tags = {"字典表相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/dict-info")
public class DictInfoController {

    private final DictInfoService dictInfoService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取字典表分页", notes = "获取字典表分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum"),
            @ApiImplicitParam(value = "每页大小", name = "pageSize"),
            @ApiImplicitParam(value = "查询条件", name = "dictInfoVo")}
    )
    public Result<Page<DictInfoVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                                 @RequestParam(value = "pageSize", required = false) Long pageSize,
                                                 @RequestBody(required = false) DictInfoVo dictInfoVo) {
        return Result.success(dictInfoService.getPage(pageNum, pageSize, dictInfoVo));
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取字典表详情", notes = "获取字典表详情", response = Result.class)
    @GetMapping
    public Result<DictInfoVo> query(@RequestParam(value = "id") String id) {
        return Result.success(dictInfoService.queryDictInfo(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增字典表", notes = "新增字典表", response = Result.class)
    @PostMapping
    public Result<DictInfo> add(@RequestBody DictInfoVo dictInfoVo) {
        return Result.success(dictInfoService.addDictInfo(dictInfoVo));
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改字典表", notes = "修改字典表", response = Result.class)
    @PutMapping
    public Result<DictInfo> update(@RequestBody DictInfoVo dictInfoVo) {
        return Result.success(dictInfoService.updateDictInfo(dictInfoVo));
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除字典表", notes = "刪除字典表", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(dictInfoService.deleteDictInfo(ids));
    }

    @ApiOperationSupport(order = 60, author = "alex")
    @ApiOperation(value = "获取字典表列表", notes = "获取字典表列表", response = Result.class)
    @GetMapping(value = "/listByBelong")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "分类", name = "belongTo")}
    )
    public Result<List<DictInfoVo>> listByBelong(@RequestParam(value = "belongTo", required = false) String belongTo) {
        return Result.success(dictInfoService.listByBelong(belongTo));
    }
}
