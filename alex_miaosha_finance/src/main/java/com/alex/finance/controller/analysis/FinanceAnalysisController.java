package com.alex.finance.controller.analysis;

import com.alex.base.common.Result;
import com.alex.finance.service.analysis.FinanceAnalysisService;
import com.alex.finance.vo.financeAnalysis.AnalysisVo;
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

import java.util.List;

/**
 * @description: 财务分析控制类
 * @author: alex
 * @createDate: 2022/10/22 14:42
 * @version: 1.0.0
 */
@ApiSort(30)
@Api(value = "财务分析相关接口", tags = {"财务分析相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/finance-analysis")
public class FinanceAnalysisController {

    private final FinanceAnalysisService financeAnalysisService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取余额", notes = "获取余额", response = Result.class)
    @GetMapping(value = "/getBalance")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "人员", name = "belongTo"),
            @ApiImplicitParam(value = "时间(yyyy-mm)", name = "searchDate")}
    )
    public Result<List<AnalysisVo>> getTypeCodeSum(@RequestParam(value = "belongTo", required = false) Long belongTo,
                                                   @RequestParam(value = "searchDate", required = false) String searchDate) {
        return Result.success(financeAnalysisService.getBalance(belongTo, searchDate));
    }
}
