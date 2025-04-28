package com.alex.api.finance.prepaidConsumeRecordT.api;

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
import com.alex.api.finance.prepaidConsumeRecordT.vo.PrepaidConsumeRecordTVo;

/**
 * description:  消费卡交易记录表controller
 * author:       alex
 * createDate:   2025-04-28 20:58:14
 * version:      1.0.0
 */
@Component
// @FeignClient(name = "alex-oss-${spring.profiles.active:dev}", configuration = FeignConfig.class)
public interface PrepaidConsumeRecordTApi {

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取消费卡交易记录表分页", notes = "获取消费卡交易记录表分页", response = Result.class)
    @PostMapping(value = "/api/v1//prepaid-consume-record-t/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "查询条件", name = "prepaidConsumeRecordTVo")}
    )
    Result<Page<PrepaidConsumeRecordTVo>> getPrepaidConsumeRecordTPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                            @RequestParam(value = "pageSize", required = false) Long pageSize,
                                            @RequestBody(required = false) PrepaidConsumeRecordTVo prepaidConsumeRecordTVo);

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取消费卡交易记录表详情", notes = "获取消费卡交易记录表详情", response = Result.class)
    @GetMapping(value = "/api/v1//prepaid-consume-record-t")
    Result<PrepaidConsumeRecordTVo> queryPrepaidConsumeRecordT(@RequestParam(value = "id") String id);

    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增消费卡交易记录表", notes = "新增消费卡交易记录表", response = Result.class)
    @PostMapping("/api/v1//prepaid-consume-record-t")
    Result<Boolean> addPrepaidConsumeRecordT(@RequestBody PrepaidConsumeRecordTVo prepaidConsumeRecordTVo);

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改消费卡交易记录表", notes = "修改消费卡交易记录表", response = Result.class)
    @PutMapping("/api/v1//prepaid-consume-record-t")
    Result<Boolean> updatePrepaidConsumeRecordT(@RequestBody PrepaidConsumeRecordTVo prepaidConsumeRecordTVo);

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除消费卡交易记录表", notes = "刪除消费卡交易记录表", response = Result.class)
    @DeleteMapping("/api/v1")
    Result<Boolean> deletePrepaidConsumeRecordT(@RequestParam("ids") String ids);
}