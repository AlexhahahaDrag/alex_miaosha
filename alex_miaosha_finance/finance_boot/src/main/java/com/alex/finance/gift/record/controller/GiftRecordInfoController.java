package com.alex.finance.gift.record.controller;

import com.alex.api.finance.gift.record.query.GiftRecordQuery;
import com.alex.api.finance.gift.record.vo.GiftRecordInfoVo;
import com.alex.api.finance.gift.record.vo.GiftRecordSummaryVo;
import com.alex.base.common.Result;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import com.alex.finance.gift.record.service.GiftRecordInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@ApiSort(133)
@Api(value = "礼金记录管理", tags = { "礼金记录管理" })
@RestController
@RequiredArgsConstructor
@RequestMapping("${api.version:/api/v1}/gift-record-info-t")
public class GiftRecordInfoController {

    private final GiftRecordInfoService giftRecordInfoService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "礼金记录分页查询", response = Result.class)
    @PostMapping(value = "/page")
    public Result<Page<GiftRecordInfoVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
            @RequestParam(value = "pageSize", required = false) Long pageSize,
            @RequestBody(required = false) GiftRecordQuery query) {
        return Result.success(giftRecordInfoService.getPage(pageNum, pageSize, query));
    }

    @ApiOperationSupport(order = 15, author = "alex")
    @ApiOperation(value = "礼金记录列表查询", response = Result.class)
    @PostMapping(value = "/list")
    public Result<List<GiftRecordInfoVo>> getList(@RequestBody(required = false) GiftRecordQuery query) {
        return Result.success(giftRecordInfoService.getList(query));
    }

    @ApiOperationSupport(order = 16, author = "alex")
    @ApiOperation(value = "礼金汇总统计", response = Result.class)
    @PostMapping(value = "/summary")
    public Result<GiftRecordSummaryVo> summary(@RequestBody(required = false) GiftRecordQuery query) {
        return Result.success(giftRecordInfoService.getSummary(query));
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "礼金记录详情", response = Result.class)
    @GetMapping
    public Result<GiftRecordInfoVo> query(@RequestParam(value = "id") Long id) {
        return Result.success(giftRecordInfoService.queryGiftRecordInfo(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增礼金记录", response = Result.class)
    @PostMapping
    public Result<GiftRecordInfoVo> add(
            @Validated({ Insert.class }) @RequestBody GiftRecordInfoVo giftRecordInfoVo) {
        return Result.success(giftRecordInfoService.addGiftRecordInfo(giftRecordInfoVo));
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改礼金记录", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({ Update.class }) @RequestBody GiftRecordInfoVo giftRecordInfoVo) {
        return Result.success(giftRecordInfoService.updateGiftRecordInfo(giftRecordInfoVo));
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "删除礼金记录", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(giftRecordInfoService.deleteGiftRecordInfo(ids));
    }

    @ApiOperationSupport(order = 60, author = "alex")
    @ApiOperation(value = "查询待回礼金额", response = Result.class)
    @GetMapping(value = "/pending-return-amount")
    public Result<BigDecimal> pendingReturnAmount(@RequestParam("receiveRecordId") Long receiveRecordId) {
        return Result.success(giftRecordInfoService.calculatePendingReturnAmount(receiveRecordId));
    }

    @ApiOperationSupport(order = 70, author = "alex")
    @ApiOperation(value = "标记已回礼", response = Result.class)
    @PutMapping(value = "/mark-returned")
    public Result<Boolean> markReturned(@RequestParam("receiveRecordId") Long receiveRecordId) {
        return Result.success(giftRecordInfoService.markReturned(receiveRecordId));
    }

    @ApiOperationSupport(order = 80, author = "alex")
    @ApiOperation(value = "导出礼金记录", response = void.class)
    @PostMapping(value = "/export")
    public void export(@RequestBody(required = false) GiftRecordQuery query,
            javax.servlet.http.HttpServletResponse response) {
        giftRecordInfoService.exportGiftRecordInfo(query, response);
    }
}
