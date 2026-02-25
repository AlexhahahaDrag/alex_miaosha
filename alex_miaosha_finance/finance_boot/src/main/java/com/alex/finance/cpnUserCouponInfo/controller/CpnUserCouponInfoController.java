package com.alex.finance.cpnUserCouponInfo.controller;

import com.alex.api.finance.cpnUserCouponInfo.vo.CpnUserCouponInfoVo;
import com.alex.api.finance.cpnUserCouponInfo.vo.CpnUserCouponRedeemReq;
import com.alex.base.common.Result;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.alex.common.annotations.LogRestRequest;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import com.alex.finance.cpnUserCouponInfo.service.CpnUserCouponInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户消费券库存表 (按数量核销) 控制器
 * 
 * @author alex
 * @since 2025-12-17 17:55:32
 * @version 1.0.0
 */
@ApiSort(105)
@Api(value = "用户消费券库存表 (按数量核销)相关接口", tags = {"用户消费券库存表 (按数量核销)相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("${api.version:/api/v1}/cpn-user-coupon-info")
public class CpnUserCouponInfoController {

    /**
     * 用户消费券库存表 (按数量核销) 服务
     */
    private final CpnUserCouponInfoService cpnUserCouponInfoService;

    /**
     * 分页查询用户消费券库存表 (按数量核销)
     * 
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param cpnUserCouponInfoVo 查询条件
     * @return 分页结果
     */
    @LogRestRequest(apiName = "获取用户消费券库存表 (按数量核销)分页")
    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取用户消费券库存表 (按数量核销)分页", notes = "分页查询用户消费券库存表 (按数量核销)列表", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class, example = "1"),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class, example = "10"),
            @ApiImplicitParam(value = "查询条件", name = "cpnUserCouponInfoVo", dataTypeClass = CpnUserCouponInfoVo.class)}
    )
    public Result<Page<CpnUserCouponInfoVo>> getPage(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Long pageNum,
                                                  @RequestParam(value = "pageSize", required = false, defaultValue = "10") Long pageSize,
                                                  @RequestBody(required = false) CpnUserCouponInfoVo cpnUserCouponInfoVo) {
        return Result.success(cpnUserCouponInfoService.getPage(pageNum, pageSize, cpnUserCouponInfoVo));
    }

    /**
     * 根据ID查询用户消费券库存表 (按数量核销)详情
     * 
     * @param id 主键 ID
     * @return 用户消费券库存表 (按数量核销)详情
     */
    @LogRestRequest(apiName = "获取用户消费券库存表 (按数量核销)详情")
    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取用户消费券库存表 (按数量核销)详情", notes = "根据ID查询用户消费券库存表 (按数量核销)详细信息", response = Result.class)
    @GetMapping
    @ApiImplicitParam(value = "主键 ID", name = "id", required = true, dataTypeClass = Long.class, paramType = "query")
    public Result<CpnUserCouponInfoVo> query(@RequestParam Long id) {
        return Result.success(cpnUserCouponInfoService.queryCpnUserCouponInfo(id));
    }

    /**
     * 新增用户消费券库存表 (按数量核销)
     * 
     * @param cpnUserCouponInfoVo 用户消费券库存表 (按数量核销)信息
     * @return 操作结果
     */
    @LogRestRequest(apiName = "新增用户消费券库存表 (按数量核销)")
    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增用户消费券库存表 (按数量核销)", notes = "创建新的用户消费券库存表 (按数量核销)记录", response = Result.class)
    @PostMapping
    public Result<Boolean> add(@Validated({Insert.class}) @RequestBody CpnUserCouponInfoVo cpnUserCouponInfoVo) {
        return Result.success(cpnUserCouponInfoService.addCpnUserCouponInfo(cpnUserCouponInfoVo));
    }

    /**
     * 消费券核销数量（按数量核销）
     * <p>
     * 这里只做参数接收与转发，所有业务逻辑在 ServiceImpl 内完成（含事务与历史记录写入）。
     *
     * @param req 核销请求
     * @return 操作结果
     */
    @LogRestRequest(apiName = "消费券核销数量（按数量核销）")
    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 35, author = "alex")
    @ApiOperation(value = "消费券核销数量（按数量核销）", notes = "按 userId + couponId 进行数量核销，同时写入核销历史记录", response = Result.class)
    @PostMapping("/redeem")
    public Result<Boolean> redeem(@Validated @RequestBody CpnUserCouponRedeemReq req) {
        return Result.success(cpnUserCouponInfoService.redeem(req));
    }

    /**
     * 取消核销
     * <p>
     * 这里只做参数接收与转发，所有业务逻辑在 ServiceImpl 内完成（含事务与历史记录写入）。
     *
     * @param req 取消核销请求
     * @return 操作结果
     */
    @LogRestRequest(apiName = "取消核销（按数量核销）")
    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 36, author = "alex")
    @ApiOperation(value = "取消核销（按数量核销）", notes = "根据 userCouponId 取消核销，同时写入取消核销历史记录", response = Result.class)
    @PostMapping("/redeem/cancel")
    public Result<Boolean> cancelRedeem(@Validated @RequestBody CpnUserCouponRedeemReq req) {
        return Result.success(cpnUserCouponInfoService.cancelRedeem(req));
    }

    /**
     * 修改用户消费券库存表 (按数量核销)
     * 
     * @param cpnUserCouponInfoVo 用户消费券库存表 (按数量核销)信息
     * @return 操作结果
     */
    @LogRestRequest(apiName = "修改用户消费券库存表 (按数量核销)")
    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改用户消费券库存表 (按数量核销)", notes = "更新用户消费券库存表 (按数量核销)信息", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody CpnUserCouponInfoVo cpnUserCouponInfoVo) {
        return Result.success(cpnUserCouponInfoService.updateCpnUserCouponInfo(cpnUserCouponInfoVo));
    }

    /**
     * 删除用户消费券库存表 (按数量核销)
     * 
     * @param ids 主键ID列表，多个ID用逗号分隔
     * @return 操作结果
     */
    @LogRestRequest(apiName = "删除用户消费券库存表 (按数量核销)")
    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "删除用户消费券库存表 (按数量核销)", notes = "根据ID列表批量删除用户消费券库存表 (按数量核销)", response = Result.class)
    @DeleteMapping
    @ApiImplicitParam(value = "主键ID列表，多个ID用逗号分隔", name = "ids", required = true, dataTypeClass = String.class, example = "1,2,3")
    public Result<Boolean> delete(@RequestParam String ids) {
        return Result.success(cpnUserCouponInfoService.deleteCpnUserCouponInfo(ids));
    }
}
