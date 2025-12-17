package com.alex.api.finance.userCouponInfo.api;

import com.alex.base.common.Result;
import com.alex.common.config.FeignConfig;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import com.alex.api.finance.userCouponInfo.vo.UserCouponInfoVo;
import com.alex.api.finance.userCouponInfo.vo.UserCouponRedeemReq;

/**
 * @description:  用户消费券库存表 (按数量核销)控制器 api
 * @author:       alex
 * @createDate:   2025-12-17 14:08:13
 * @version:      1.0.0
 */
@Component
// @FeignClient(name = "alex-oss-${spring.profiles.active:dev}", configuration = FeignConfig.class)
public interface UserCouponInfoApi {

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取用户消费券库存表 (按数量核销)分页", notes = "获取用户消费券库存表 (按数量核销)分页", response = Result.class)
    @PostMapping(value = "/api/v1//user-coupon-info/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "查询条件", name = "userCouponInfoVo")}
    )
    Result<Page<UserCouponInfoVo>> getUserCouponInfoPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                            @RequestParam(value = "pageSize", required = false) Long pageSize,
                                            @RequestBody(required = false) UserCouponInfoVo userCouponInfoVo);

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取用户消费券库存表 (按数量核销)详情", notes = "获取用户消费券库存表 (按数量核销)详情", response = Result.class)
    @GetMapping(value = "/api/v1//user-coupon-info")
    Result<UserCouponInfoVo> queryUserCouponInfo(@RequestParam(value = "id") String id);

    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增用户消费券库存表 (按数量核销)", notes = "新增用户消费券库存表 (按数量核销)", response = Result.class)
    @PostMapping("/api/v1//user-coupon-info")
    Result<Boolean> addUserCouponInfo(@RequestBody UserCouponInfoVo userCouponInfoVo);

    /**
     * AI Agent: 核销用户消费券（按数量核销）
     */
    @ApiOperationSupport(order = 25, author = "alex")
    @ApiOperation(value = "核销用户消费券(按数量)", notes = "新增用户券实例并生成核销历史记录", response = Result.class)
    @PostMapping("/api/v1//user-coupon-info/redeem")
    Result<Boolean> redeem(@RequestBody UserCouponRedeemReq req);

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改用户消费券库存表 (按数量核销)", notes = "修改用户消费券库存表 (按数量核销)", response = Result.class)
    @PutMapping("/api/v1//user-coupon-info")
    Result<Boolean> updateUserCouponInfo(@RequestBody UserCouponInfoVo userCouponInfoVo);

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除用户消费券库存表 (按数量核销)", notes = "刪除用户消费券库存表 (按数量核销)", response = Result.class)
    @DeleteMapping("/api/v1")
    Result<Boolean> deleteUserCouponInfo(@RequestParam("ids") String ids);
}