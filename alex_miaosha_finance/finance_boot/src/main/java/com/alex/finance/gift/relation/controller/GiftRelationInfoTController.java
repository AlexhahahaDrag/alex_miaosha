package com.alex.finance.gift.relation.controller;

import com.alex.api.finance.gift.relation.query.GiftRelationQuery;
import com.alex.api.finance.gift.relation.vo.GiftRelationInfoTVo;
import com.alex.base.common.Result;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import com.alex.finance.gift.relation.service.GiftRelationInfoTService;
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

@ApiSort(131)
@Api(value = "gift relation api", tags = {"gift relation api"})
@RestController
@RequiredArgsConstructor
@RequestMapping("${api.version:/api/v1}/gift-relation-info-t")
public class GiftRelationInfoTController {

    private final GiftRelationInfoTService giftRelationInfoTService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "gift relation page", response = Result.class)
    @PostMapping(value = "/page")
    public Result<Page<GiftRelationInfoTVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                                     @RequestParam(value = "pageSize", required = false) Long pageSize,
                                                     @RequestBody(required = false) GiftRelationQuery query) {
        return Result.success(giftRelationInfoTService.getPage(pageNum, pageSize, query));
    }

    @ApiOperationSupport(order = 15, author = "alex")
    @ApiOperation(value = "gift relation list", response = Result.class)
    @PostMapping(value = "/list")
    public Result<List<GiftRelationInfoTVo>> getList(@RequestBody(required = false) GiftRelationQuery query) {
        return Result.success(giftRelationInfoTService.getList(query));
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "gift relation detail", response = Result.class)
    @GetMapping
    public Result<GiftRelationInfoTVo> query(@RequestParam(value = "id") Long id) {
        return Result.success(giftRelationInfoTService.queryGiftRelationInfoT(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "add gift relation", response = Result.class)
    @PostMapping
    public Result<GiftRelationInfoTVo> add(@Validated({Insert.class}) @RequestBody GiftRelationInfoTVo giftRelationInfoTVo) {
        return Result.success(giftRelationInfoTService.addGiftRelationInfoT(giftRelationInfoTVo));
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "update gift relation", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody GiftRelationInfoTVo giftRelationInfoTVo) {
        return Result.success(giftRelationInfoTService.updateGiftRelationInfoT(giftRelationInfoTVo));
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "delete gift relation", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(giftRelationInfoTService.deleteGiftRelationInfoT(ids));
    }
}
