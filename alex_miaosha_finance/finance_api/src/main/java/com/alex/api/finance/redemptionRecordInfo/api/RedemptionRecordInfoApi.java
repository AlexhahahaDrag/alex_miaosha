package com.alex.api.finance.redemptionRecordInfo.api;

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
import com.alex.api.finance.redemptionRecordInfo.vo.RedemptionRecordInfoVo;

/**
 * @description:  消费券核销记录表 (按数量核销)控制器 api
 * @author:       alex
 * @createDate:   2025-12-17 14:08:55
 * @version:      1.0.0
 */
@Component
// @FeignClient(name = "alex-oss-${spring.profiles.active:dev}", configuration = FeignConfig.class)
public interface RedemptionRecordInfoApi {

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取消费券核销记录表 (按数量核销)分页", notes = "获取消费券核销记录表 (按数量核销)分页", response = Result.class)
    @PostMapping(value = "/api/v1//redemption-record-info/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "查询条件", name = "redemptionRecordInfoVo")}
    )
    Result<Page<RedemptionRecordInfoVo>> getRedemptionRecordInfoPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                            @RequestParam(value = "pageSize", required = false) Long pageSize,
                                            @RequestBody(required = false) RedemptionRecordInfoVo redemptionRecordInfoVo);

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取消费券核销记录表 (按数量核销)详情", notes = "获取消费券核销记录表 (按数量核销)详情", response = Result.class)
    @GetMapping(value = "/api/v1//redemption-record-info")
    Result<RedemptionRecordInfoVo> queryRedemptionRecordInfo(@RequestParam(value = "id") String id);

    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增消费券核销记录表 (按数量核销)", notes = "新增消费券核销记录表 (按数量核销)", response = Result.class)
    @PostMapping("/api/v1//redemption-record-info")
    Result<Boolean> addRedemptionRecordInfo(@RequestBody RedemptionRecordInfoVo redemptionRecordInfoVo);

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改消费券核销记录表 (按数量核销)", notes = "修改消费券核销记录表 (按数量核销)", response = Result.class)
    @PutMapping("/api/v1//redemption-record-info")
    Result<Boolean> updateRedemptionRecordInfo(@RequestBody RedemptionRecordInfoVo redemptionRecordInfoVo);

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除消费券核销记录表 (按数量核销)", notes = "刪除消费券核销记录表 (按数量核销)", response = Result.class)
    @DeleteMapping("/api/v1")
    Result<Boolean> deleteRedemptionRecordInfo(@RequestParam("ids") String ids);
}