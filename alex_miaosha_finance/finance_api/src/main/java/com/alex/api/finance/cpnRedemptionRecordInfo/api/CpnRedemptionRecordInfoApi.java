package com.alex.api.finance.cpnRedemptionRecordInfo.api;

import com.alex.base.common.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import com.alex.api.finance.cpnRedemptionRecordInfo.vo.CpnRedemptionRecordInfoVo;

/**
 * @description:  消费券核销记录表 (按数量核销)控制器 api
 * @author:       alex
 * @createDate:   2025-12-17 17:54:00
 * @version:      1.0.0
 */
@Component
@RequestMapping("${api.version:/api/v1}/cpn-redemption-record-info")
// @FeignClient(name = "alex-oss-${spring.profiles.active:dev}", configuration = FeignConfig.class)
public interface CpnRedemptionRecordInfoApi {

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取消费券核销记录表 (按数量核销)分页", notes = "获取消费券核销记录表 (按数量核销)分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "查询条件", name = "cpnRedemptionRecordInfoVo")}
    )
    Result<Page<CpnRedemptionRecordInfoVo>> getCpnRedemptionRecordInfoPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                            @RequestParam(value = "pageSize", required = false) Long pageSize,
                                            @RequestBody(required = false) CpnRedemptionRecordInfoVo cpnRedemptionRecordInfoVo);

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取消费券核销记录表 (按数量核销)详情", notes = "获取消费券核销记录表 (按数量核销)详情", response = Result.class)
    @GetMapping
    Result<CpnRedemptionRecordInfoVo> queryCpnRedemptionRecordInfo(@RequestParam(value = "id") String id);

    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增消费券核销记录表 (按数量核销)", notes = "新增消费券核销记录表 (按数量核销)", response = Result.class)
    @PostMapping
    Result<Boolean> addCpnRedemptionRecordInfo(@RequestBody CpnRedemptionRecordInfoVo cpnRedemptionRecordInfoVo);

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改消费券核销记录表 (按数量核销)", notes = "修改消费券核销记录表 (按数量核销)", response = Result.class)
    @PutMapping
    Result<Boolean> updateCpnRedemptionRecordInfo(@RequestBody CpnRedemptionRecordInfoVo cpnRedemptionRecordInfoVo);

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除消费券核销记录表 (按数量核销)", notes = "刪除消费券核销记录表 (按数量核销)", response = Result.class)
    @DeleteMapping
    Result<Boolean> deleteCpnRedemptionRecordInfo(@RequestParam("ids") String ids);
}
