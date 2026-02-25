package com.alex.api.finance.cpnUserCouponInfo.api;

import com.alex.base.common.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import com.alex.api.finance.cpnUserCouponInfo.vo.CpnUserCouponInfoVo;
import com.alex.api.finance.cpnUserCouponInfo.vo.CpnUserCouponRedeemReq;

/**
 * @description:  用户消费券库存表 (按数量核销)控制器 api
 * @author:       alex
 * @createDate:   2025-12-17 17:55:32
 * @version:      1.0.0
 */
@Component
@RequestMapping("${api.version:/api/v1}/cpn-user-coupon-info")
// @FeignClient(name = "alex-oss-${spring.profiles.active:dev}", configuration = FeignConfig.class)
public interface CpnUserCouponInfoApi {

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取用户消费券库存表 (按数量核销)分页", notes = "获取用户消费券库存表 (按数量核销)分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "查询条件", name = "cpnUserCouponInfoVo")}
    )
    Result<Page<CpnUserCouponInfoVo>> getCpnUserCouponInfoPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                            @RequestParam(value = "pageSize", required = false) Long pageSize,
                                            @RequestBody(required = false) CpnUserCouponInfoVo cpnUserCouponInfoVo);

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取用户消费券库存表 (按数量核销)详情", notes = "获取用户消费券库存表 (按数量核销)详情", response = Result.class)
    @GetMapping
    Result<CpnUserCouponInfoVo> queryCpnUserCouponInfo(@RequestParam(value = "id") String id);

    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增用户消费券库存表 (按数量核销)", notes = "新增用户消费券库存表 (按数量核销)", response = Result.class)
    @PostMapping
    Result<Boolean> addCpnUserCouponInfo(@RequestBody CpnUserCouponInfoVo cpnUserCouponInfoVo);

    @ApiOperationSupport(order = 35, author = "alex")
    @ApiOperation(value = "消费券核销数量（按数量核销）", notes = "按 userId + couponId 进行数量核销，同时写入核销历史记录", response = Result.class)
    @PostMapping("/redeem")
    Result<Boolean> redeem(@RequestBody CpnUserCouponRedeemReq req);

    @ApiOperationSupport(order = 36, author = "alex")
    @ApiOperation(value = "取消核销（按数量核销）", notes = "根据 userCouponId 取消核销，同时写入取消核销历史记录", response = Result.class)
    @PostMapping("/redeem/cancel")
    Result<Boolean> cancelRedeem(@RequestBody CpnUserCouponRedeemReq req);

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改用户消费券库存表 (按数量核销)", notes = "修改用户消费券库存表 (按数量核销)", response = Result.class)
    @PutMapping
    Result<Boolean> updateCpnUserCouponInfo(@RequestBody CpnUserCouponInfoVo cpnUserCouponInfoVo);

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除用户消费券库存表 (按数量核销)", notes = "刪除用户消费券库存表 (按数量核销)", response = Result.class)
    @DeleteMapping
    Result<Boolean> deleteCpnUserCouponInfo(@RequestParam("ids") String ids);
}
