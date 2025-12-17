package com.alex.api.finance.cpnUserCouponInfo.api;

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
import com.alex.api.finance.cpnUserCouponInfo.vo.CpnUserCouponInfoVo;

/**
 * @description:  用户消费券库存表 (按数量核销)控制器 api
 * @author:       alex
 * @createDate:   2025-12-17 17:55:32
 * @version:      1.0.0
 */
@Component
// @FeignClient(name = "alex-oss-${spring.profiles.active:dev}", configuration = FeignConfig.class)
public interface CpnUserCouponInfoApi {

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取用户消费券库存表 (按数量核销)分页", notes = "获取用户消费券库存表 (按数量核销)分页", response = Result.class)
    @PostMapping(value = "/api/v1//cpn-user-coupon-info/page")
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
    @GetMapping(value = "/api/v1//cpn-user-coupon-info")
    Result<CpnUserCouponInfoVo> queryCpnUserCouponInfo(@RequestParam(value = "id") String id);

    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增用户消费券库存表 (按数量核销)", notes = "新增用户消费券库存表 (按数量核销)", response = Result.class)
    @PostMapping("/api/v1//cpn-user-coupon-info")
    Result<Boolean> addCpnUserCouponInfo(@RequestBody CpnUserCouponInfoVo cpnUserCouponInfoVo);

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改用户消费券库存表 (按数量核销)", notes = "修改用户消费券库存表 (按数量核销)", response = Result.class)
    @PutMapping("/api/v1//cpn-user-coupon-info")
    Result<Boolean> updateCpnUserCouponInfo(@RequestBody CpnUserCouponInfoVo cpnUserCouponInfoVo);

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除用户消费券库存表 (按数量核销)", notes = "刪除用户消费券库存表 (按数量核销)", response = Result.class)
    @DeleteMapping("/api/v1")
    Result<Boolean> deleteCpnUserCouponInfo(@RequestParam("ids") String ids);
}