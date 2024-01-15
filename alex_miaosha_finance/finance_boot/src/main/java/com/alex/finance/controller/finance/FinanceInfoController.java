package com.alex.finance.controller.finance;

import com.alex.api.finance.vo.finance.FinanceInfoVo;
import com.alex.base.common.Result;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import com.alex.finance.entity.finance.FinanceInfo;
import com.alex.finance.service.finance.FinanceInfoService;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * description: 财务信息表restApi
 * author: majf
 * createDate: 2022-10-10 18:02:03
 * version: 1.0.0
 */
@ApiSort(10)
@Api(value = "财务信息表相关接口", tags = {"财务信息表相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/finance-info")
public class FinanceInfoController {

    private final FinanceInfoService financeInfoService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取财务信息表分页", notes = "获取财务信息表分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum"),
            @ApiImplicitParam(value = "每页大小", name = "pageSize"),
            @ApiImplicitParam(value = "查询条件", name = "financeInfoVo")}
    )
    public Result<IPage<FinanceInfoVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                                @RequestParam(value = "pageSize", required = false) Long pageSize,
                                                @RequestBody(required = false) FinanceInfoVo financeInfoVo) {
        return Result.success(financeInfoService.getPage(pageNum, pageSize, financeInfoVo));
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取财务信息表列表", notes = "获取财务信息表列表", response = Result.class)
    @PostMapping(value = "/list")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "查询条件", name = "financeInfoVo")}
    )
    public Result<List<FinanceInfoVo>> getList(@RequestBody FinanceInfoVo financeInfoVo) {
        return Result.success(financeInfoService.getList(financeInfoVo));
    }

    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "获取财务信息表详情", notes = "获取财务信息表详情", response = Result.class)
    @GetMapping
    public Result<FinanceInfoVo> query(@RequestParam(value = "id") String id) {
        return Result.success(financeInfoService.queryFinanceInfo(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "新增财务信息表", notes = "新增财务信息表", response = Result.class)
    @PostMapping
    public Result<FinanceInfo> add(@Validated({Insert.class}) @RequestBody FinanceInfoVo financeInfoVo) {
        return Result.success(financeInfoService.addFinanceInfo(financeInfoVo));
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "修改财务信息表", notes = "修改财务信息表", response = Result.class)
    @PutMapping
    public Result<FinanceInfo> update(@Validated({Update.class}) @RequestBody FinanceInfoVo financeInfoVo) {
        return Result.success(financeInfoService.updateFinanceInfo(financeInfoVo));
    }

    @ApiOperationSupport(order = 60, author = "alex")
    @ApiOperation(value = "刪除财务信息表", notes = "刪除财务信息表", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(financeInfoService.deleteFinanceInfo(ids));
    }

    @ApiOperationSupport(order = 70, author = "alex")
    @PostMapping(value = "/importFinance")
    @ApiOperation(value = "导入财务信息表", notes = "导入财务信息表", response = Result.class)
    public Result<Boolean> importFinance(@RequestPart("file") MultipartFile file) throws Exception {
        return Result.success(financeInfoService.importFinance(file));
    }
}
