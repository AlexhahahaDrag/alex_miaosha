package com.alex.finance.finance.controller;

import com.alex.api.finance.vo.finance.FinanceBudgetSaveReq;
import com.alex.api.finance.vo.finance.FinanceBudgetStatusVo;
import com.alex.base.common.Result;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.alex.finance.finance.service.FinanceBudgetInfoService;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * description: 月度零花钱预算与消费统计控制器
 * author: alex
 * createDate: 2026-10-08
 * version: 1.0.0
 */
@ApiSort(18)
@Api(value = "月度零花钱预算与消费统计", tags = {"月度零花钱预算相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("${api.version:/api/v1}/finance-budget")
public class FinanceBudgetInfoController {

    private final FinanceBudgetInfoService financeBudgetInfoService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取指定月份零花钱预算与消费进度状态", notes = "当月未设置时自动继承最近历史月配置", response = Result.class)
    @GetMapping("/status")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "预算月份(YYYY-MM，如 2026-10，为空默认当月)", name = "budgetMonth", dataTypeClass = String.class),
            @ApiImplicitParam(value = "年月(兼容老参数)", name = "yearMonth", dataTypeClass = String.class),
            @ApiImplicitParam(value = "家庭组/机构ID(可选，为空自动获取登录用户所在机构)", name = "orgId", dataTypeClass = Long.class),
            @ApiImplicitParam(value = "归属用户ID(可选，用于过滤指定成员)", name = "belongTo", dataTypeClass = Long.class)
    })
    public Result<FinanceBudgetStatusVo> getBudgetStatus(
            @RequestParam(value = "budgetMonth", required = false) String budgetMonth,
            @RequestParam(value = "yearMonth", required = false) String yearMonth,
            @RequestParam(value = "orgId", required = false) Long orgId,
            @RequestParam(value = "belongTo", required = false) Long belongTo) {
        String targetMonth = StringUtils.isNotBlank(budgetMonth) ? budgetMonth : yearMonth;
        return Result.success(financeBudgetInfoService.getMonthlyBudgetStatus(targetMonth, orgId, belongTo));
    }

    @ApiOperationSupport(order = 15, author = "alex")
    @ApiOperation(value = "获取近两月已有记账分类", notes = "获取上月及本月已有记账分类", response = Result.class)
    @GetMapping("/categories")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "预算月份(YYYY-MM，如 2026-10，为空默认当月)", name = "budgetMonth", dataTypeClass = String.class),
            @ApiImplicitParam(value = "年月(兼容老参数)", name = "yearMonth", dataTypeClass = String.class),
            @ApiImplicitParam(value = "归属用户ID(可选，为空自动获取登录用户)", name = "belongTo", dataTypeClass = Long.class)
    })
    public Result<List<String>> getCategories(
            @RequestParam(value = "budgetMonth", required = false) String budgetMonth,
            @RequestParam(value = "yearMonth", required = false) String yearMonth,
            @RequestParam(value = "belongTo", required = false) Long belongTo) {
        String targetMonth = StringUtils.isNotBlank(budgetMonth) ? budgetMonth : yearMonth;
        return Result.success(financeBudgetInfoService.getRecentCategories(targetMonth, belongTo));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "保存或更新月度零花钱预算及分类配置", notes = "仅影响当前及后续未显式配置的月份", response = Result.class)
    @PostMapping("/save")
    public Result<Boolean> saveMonthlyBudget(@Validated @RequestBody FinanceBudgetSaveReq req) {
        return Result.success(financeBudgetInfoService.saveMonthlyBudget(req));
    }
}
