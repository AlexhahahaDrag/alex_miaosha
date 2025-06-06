package com.alex.finance.prepaidConsumeRecordT.controller;

import com.alex.api.finance.prepaidConsumeRecordT.vo.PrepaidConsumeRecordTVo;
import com.alex.finance.prepaidConsumeRecordT.service.PrepaidConsumeRecordTService;
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

/**
 * description:  消费卡交易记录表restApi
 * author:       alex
 * createDate:   2025-04-30 08:21:48
 * version:      1.0.0
 */
@ApiSort(105)
@Api(value = "消费卡交易记录表相关接口", tags = {"消费卡交易记录表相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/prepaid-consume-record-t")
public class PrepaidConsumeRecordTController {

    private final PrepaidConsumeRecordTService prepaidConsumeRecordTService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取消费卡交易记录表分页", notes = "获取消费卡交易记录表分页", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "查询条件", name = "prepaidConsumeRecordTVo", dataTypeClass = PrepaidConsumeRecordTVo.class)}
    )
    public Result<Page<PrepaidConsumeRecordTVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                             @RequestParam(value = "pageSize", required = false) Long pageSize,
                                             @RequestBody(required = false) PrepaidConsumeRecordTVo prepaidConsumeRecordTVo) {
        return Result.success(prepaidConsumeRecordTService.getPage(pageNum, pageSize, prepaidConsumeRecordTVo));
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取消费卡交易记录表详情", notes = "获取消费卡交易记录表详情", response = Result.class)
    @GetMapping
    public Result<PrepaidConsumeRecordTVo> query(@RequestParam(value = "id") Long id) {
        return Result.success(prepaidConsumeRecordTService.queryPrepaidConsumeRecordT(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增消费卡交易记录表", notes = "新增消费卡交易记录表", response = Result.class)
    @PostMapping
    public Result<Boolean> add(@Validated({Insert.class}) @RequestBody PrepaidConsumeRecordTVo prepaidConsumeRecordTVo) {
        return Result.success(prepaidConsumeRecordTService.addPrepaidConsumeRecordT(prepaidConsumeRecordTVo));
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改消费卡交易记录表", notes = "修改消费卡交易记录表", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody PrepaidConsumeRecordTVo prepaidConsumeRecordTVo) {
        return Result.success(prepaidConsumeRecordTService.updatePrepaidConsumeRecordT(prepaidConsumeRecordTVo));
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除消费卡交易记录表", notes = "刪除消费卡交易记录表", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(prepaidConsumeRecordTService.deletePrepaidConsumeRecordT(ids));
    }
}
