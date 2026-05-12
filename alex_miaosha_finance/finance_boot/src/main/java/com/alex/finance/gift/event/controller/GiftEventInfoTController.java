package com.alex.finance.gift.event.controller;

import com.alex.api.finance.gift.event.query.GiftEventQuery;
import com.alex.api.finance.gift.event.vo.GiftEventInfoTVo;
import com.alex.base.common.Result;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import com.alex.finance.gift.event.service.GiftEventInfoTService;
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

import java.util.List;

@ApiSort(132)
@Api(value = "gift event api", tags = {"gift event api"})
@RestController
@RequiredArgsConstructor
@RequestMapping("${api.version:/api/v1}/gift-event-info-t")
public class GiftEventInfoTController {

    private final GiftEventInfoTService giftEventInfoTService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "gift event page", response = Result.class)
    @PostMapping(value = "/page")
    public Result<Page<GiftEventInfoTVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                                  @RequestParam(value = "pageSize", required = false) Long pageSize,
                                                  @RequestBody(required = false) GiftEventQuery query) {
        return Result.success(giftEventInfoTService.getPage(pageNum, pageSize, query));
    }

    @ApiOperationSupport(order = 15, author = "alex")
    @ApiOperation(value = "gift event list", response = Result.class)
    @PostMapping(value = "/list")
    public Result<List<GiftEventInfoTVo>> getList(@RequestBody(required = false) GiftEventQuery query) {
        return Result.success(giftEventInfoTService.getList(query));
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "gift event detail", response = Result.class)
    @GetMapping
    public Result<GiftEventInfoTVo> query(@RequestParam(value = "id") Long id) {
        return Result.success(giftEventInfoTService.queryGiftEventInfoT(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "add gift event", response = Result.class)
    @PostMapping
    public Result<GiftEventInfoTVo> add(@Validated({Insert.class}) @RequestBody GiftEventInfoTVo giftEventInfoTVo) {
        return Result.success(giftEventInfoTService.addGiftEventInfoT(giftEventInfoTVo));
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "update gift event", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody GiftEventInfoTVo giftEventInfoTVo) {
        return Result.success(giftEventInfoTService.updateGiftEventInfoT(giftEventInfoTVo));
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "delete gift event", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(giftEventInfoTService.deleteGiftEventInfoT(ids));
    }
}
