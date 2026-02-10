package com.alex.api.finance.cpnCouponInfo.api;

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
import com.alex.api.finance.cpnCouponInfo.vo.CpnCouponInfoVo;

/**
 * @description:  消费券信息表控制器 api
 * @author:       alex
 * @createDate:   2025-12-17 17:54:42
 * @version:      1.0.0
 */
@Component
@RequestMapping("${api.version:/api/v1}/cpn-coupon-info")
// @FeignClient(name = "alex-oss-${spring.profiles.active:dev}", configuration = FeignConfig.class)
public interface CpnCouponInfoApi {

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取消费券信息表分页", notes = "获取消费券信息表分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "查询条件", name = "cpnCouponInfoVo")}
    )
    Result<Page<CpnCouponInfoVo>> getCpnCouponInfoPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                            @RequestParam(value = "pageSize", required = false) Long pageSize,
                                            @RequestBody(required = false) CpnCouponInfoVo cpnCouponInfoVo);

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取消费券信息表详情", notes = "获取消费券信息表详情", response = Result.class)
    @GetMapping
    Result<CpnCouponInfoVo> queryCpnCouponInfo(@RequestParam(value = "id") String id);

    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增消费券信息表", notes = "新增消费券信息表", response = Result.class)
    @PostMapping
    Result<Boolean> addCpnCouponInfo(@RequestBody CpnCouponInfoVo cpnCouponInfoVo);

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改消费券信息表", notes = "修改消费券信息表", response = Result.class)
    @PutMapping
    Result<Boolean> updateCpnCouponInfo(@RequestBody CpnCouponInfoVo cpnCouponInfoVo);

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除消费券信息表", notes = "刪除消费券信息表", response = Result.class)
    @DeleteMapping
    Result<Boolean> deleteCpnCouponInfo(@RequestParam("ids") String ids);
}
