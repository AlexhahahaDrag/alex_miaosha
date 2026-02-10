package com.alex.finance.prepaidCardInfoT.controller;

import com.alex.api.finance.prepaidCardInfoT.vo.PrepaidCardConsumeVo;
import com.alex.api.finance.prepaidCardInfoT.vo.PrepaidCardInfoTVo;
import com.alex.finance.prepaidCardInfoT.service.PrepaidCardInfoTService;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import org.springframework.validation.annotation.Validated;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import com.alex.base.common.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alex.api.finance.prepaidCardInfoT.vo.PrepaidDashboardOverviewVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @description:  消费卡信息表restApi
 * @author:       alex
 * @createDate:   2025-04-30 08:21:48
 * @version:      1.0.0
 */
@ApiSort(105)
@Api(value = "消费卡信息表相关接口", tags = {"消费卡信息表相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("${api.version:/api/v1}/prepaid-card-info-t")
public class PrepaidCardInfoTController {

    private static final Logger log = LoggerFactory.getLogger(PrepaidCardInfoTController.class);

    private final PrepaidCardInfoTService prepaidCardInfoTService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取消费卡信息表分页", notes = "获取消费卡信息表分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "查询条件", name = "prepaidCardInfoTVo", dataTypeClass = PrepaidCardInfoTVo.class)}
    )
    public Result<Page<PrepaidCardInfoTVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                             @RequestParam(value = "pageSize", required = false) Long pageSize,
                                             @RequestBody(required = false) PrepaidCardInfoTVo prepaidCardInfoTVo) {
        return Result.success(prepaidCardInfoTService.getPage(pageNum, pageSize, prepaidCardInfoTVo));
    }

    @ApiOperationSupport(order = 15, author = "alex")
    @ApiOperation(value = "获取消费卡信列表", notes = "获取消费卡信列表", response = Result.class)
    @PostMapping(value = "/list")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "查询条件", name = "prepaidCardInfoTVo", dataTypeClass = PrepaidCardInfoTVo.class)}
    )
    public Result<List<PrepaidCardInfoTVo>> getList(@RequestBody(required = false) PrepaidCardInfoTVo prepaidCardInfoTVo) {
        return Result.success(prepaidCardInfoTService.getList(prepaidCardInfoTVo));
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取消费卡信息表详情", notes = "获取消费卡信息表详情", response = Result.class)
    @GetMapping
    public Result<PrepaidCardInfoTVo> query(@RequestParam(value = "id") Long id) {
        return Result.success(prepaidCardInfoTService.queryPrepaidCardInfoT(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增消费卡信息表", notes = "新增消费卡信息表", response = Result.class)
    @PostMapping
    public Result<PrepaidCardInfoTVo> add(@Validated({Insert.class}) @RequestBody PrepaidCardInfoTVo prepaidCardInfoTVo) throws Exception {
        return Result.success(prepaidCardInfoTService.addPrepaidCardInfoT(prepaidCardInfoTVo));
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改消费卡信息表", notes = "修改消费卡信息表", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody PrepaidCardInfoTVo prepaidCardInfoTVo) {
        return Result.success(prepaidCardInfoTService.updatePrepaidCardInfoT(prepaidCardInfoTVo));
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除消费卡信息表", notes = "刪除消费卡信息表", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(prepaidCardInfoTService.deletePrepaidCardInfoT(ids));
    }

    @ApiOperationSupport(order = 60, author = "alex")
    @ApiOperation(value = "消费/充值金额", notes = "消费/充值金额", response = Result.class, tags = {"消费卡信息表相关接口"})
    @PostMapping(value = "consumeAndRecharge")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "消费/充值信息", name = "prepaidCardConsumeVo", dataTypeClass = PrepaidCardConsumeVo.class, required = true)
    })
    public Result<Boolean> consumeAndRecharge(@RequestBody @Validated PrepaidCardConsumeVo prepaidCardConsumeVo) throws Exception {
        try {
            if (prepaidCardConsumeVo == null) {
                log.warn("消费/充值请求参数为空");
                return Result.error("400", "请求参数不能为空");
            }
            
            // 检查必要字段
            if (prepaidCardConsumeVo.getType() == null || prepaidCardConsumeVo.getType().trim().isEmpty()) {
                log.warn("消费/充值类型为空");
                return Result.error("400", "消费/充值类型不能为空");
            }
            
            Boolean result = prepaidCardInfoTService.consumeAndRecharge(prepaidCardConsumeVo);
            return Result.success(result != null ? result : false);
        } catch (Exception e) {
            log.error("消费/充值操作时发生异常: {}", e.getMessage(), e);
            return Result.error("500", "消费/充值操作失败: " + e.getMessage());
        }
    }

    @ApiOperationSupport(order = 70, author = "alex")
    @ApiOperation(value = "仪表盘总览", notes = "返回总卡数、总余额、本月消费与充值及环比", response = Result.class, tags = {"消费卡信息表相关接口"})
    @GetMapping(value = "/dashboard/overview")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "用户ID", name = "userId", dataTypeClass = Long.class, required = false)
    })
    public Result<PrepaidDashboardOverviewVo> dashboard(@RequestParam(value = "userId", required = false) Long userId) {
        try {
            PrepaidDashboardOverviewVo result = prepaidCardInfoTService.dashboardOverview(userId);
            if (result == null) {
                // 如果返回null，创建一个默认的空结果
                result = new PrepaidDashboardOverviewVo()
                        .setTotalCards(0)
                        .setTotalCardsMoM(0)
                        .setTotalBalance(java.math.BigDecimal.ZERO)
                        .setTotalBalanceMoM(java.math.BigDecimal.ZERO)
                        .setMonthExpense(java.math.BigDecimal.ZERO)
                        .setMonthExpenseMoM(java.math.BigDecimal.ZERO)
                        .setMonthRecharge(java.math.BigDecimal.ZERO)
                        .setMonthRechargeMoM(java.math.BigDecimal.ZERO);
            }
            return Result.success(result);
        } catch (Exception e) {
            // 记录异常日志
            log.error("获取仪表盘总览数据时发生异常: {}", e.getMessage(), e);
            // 返回默认的空结果而不是抛出异常
            PrepaidDashboardOverviewVo defaultResult = new PrepaidDashboardOverviewVo()
                    .setTotalCards(0)
                    .setTotalCardsMoM(0)
                    .setTotalBalance(java.math.BigDecimal.ZERO)
                    .setTotalBalanceMoM(java.math.BigDecimal.ZERO)
                    .setMonthExpense(java.math.BigDecimal.ZERO)
                    .setMonthExpenseMoM(java.math.BigDecimal.ZERO)
                    .setMonthRecharge(java.math.BigDecimal.ZERO)
                    .setMonthRechargeMoM(java.math.BigDecimal.ZERO);
            return Result.success(defaultResult);
        }
    }
}
