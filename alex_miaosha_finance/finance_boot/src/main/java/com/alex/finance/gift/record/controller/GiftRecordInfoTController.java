package com.alex.finance.gift.record.controller;

import com.alex.api.finance.gift.record.query.GiftRecordQuery;
import com.alex.api.finance.gift.record.vo.GiftRecordInfoTVo;
import com.alex.base.common.Result;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import com.alex.finance.gift.record.service.GiftRecordInfoTService;
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
@Api(value = "gift record api", tags = {"gift record api"})
@RestController
@RequiredArgsConstructor
@RequestMapping("${api.version:/api/v1}/gift-record-info-t")
public class GiftRecordInfoTController {

    private final GiftRecordInfoTService giftRecordInfoTService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "gift record page", response = Result.class)
    @PostMapping(value = "/page")
    public Result<Page<GiftRecordInfoTVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                                   @RequestParam(value = "pageSize", required = false) Long pageSize,
                                                   @RequestBody(required = false) GiftRecordQuery query) {
        return Result.success(giftRecordInfoTService.getPage(pageNum, pageSize, query));
    }

    @ApiOperationSupport(order = 15, author = "alex")
    @ApiOperation(value = "gift record list", response = Result.class)
    @PostMapping(value = "/list")
    public Result<List<GiftRecordInfoTVo>> getList(@RequestBody(required = false) GiftRecordQuery query) {
        return Result.success(giftRecordInfoTService.getList(query));
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "gift record detail", response = Result.class)
    @GetMapping
    public Result<GiftRecordInfoTVo> query(@RequestParam(value = "id") Long id) {
        return Result.success(giftRecordInfoTService.queryGiftRecordInfoT(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "add gift record", response = Result.class)
    @PostMapping
    public Result<GiftRecordInfoTVo> add(@Validated({Insert.class}) @RequestBody GiftRecordInfoTVo giftRecordInfoTVo) {
        return Result.success(giftRecordInfoTService.addGiftRecordInfoT(giftRecordInfoTVo));
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "update gift record", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody GiftRecordInfoTVo giftRecordInfoTVo) {
        return Result.success(giftRecordInfoTService.updateGiftRecordInfoT(giftRecordInfoTVo));
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "delete gift record", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(giftRecordInfoTService.deleteGiftRecordInfoT(ids));
    }

    @ApiOperationSupport(order = 60, author = "alex")
    @ApiOperation(value = "pending return amount", response = Result.class)
    @GetMapping(value = "/pending-return-amount")
    public Result<BigDecimal> pendingReturnAmount(@RequestParam("receiveRecordId") Long receiveRecordId) {
        return Result.success(giftRecordInfoTService.calculatePendingReturnAmount(receiveRecordId));
    }

    @ApiOperationSupport(order = 70, author = "alex")
    @ApiOperation(value = "mark returned", response = Result.class)
    @PutMapping(value = "/mark-returned")
    public Result<Boolean> markReturned(@RequestParam("receiveRecordId") Long receiveRecordId) {
        return Result.success(giftRecordInfoTService.markReturned(receiveRecordId));
    }
}
