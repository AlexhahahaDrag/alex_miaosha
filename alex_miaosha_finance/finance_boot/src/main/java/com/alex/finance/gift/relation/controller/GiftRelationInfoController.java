package com.alex.finance.gift.relation.controller;

import com.alex.api.finance.gift.relation.query.GiftRelationQuery;
import com.alex.api.finance.gift.relation.vo.GiftRelationInfoVo;
import com.alex.base.common.Result;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import com.alex.finance.gift.relation.service.GiftRelationInfoService;
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
@Api(value = "礼金关系管理", tags = {"礼金关系管理"})
@RestController
@RequiredArgsConstructor
@RequestMapping("${api.version:/api/v1}/gift-relation-info-t")
public class GiftRelationInfoController {

    private final GiftRelationInfoService giftRelationInfoService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "礼金关系分页查询", response = Result.class)
    @PostMapping(value = "/page")
    public Result<Page<GiftRelationInfoVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                                     @RequestParam(value = "pageSize", required = false) Long pageSize,
                                                     @RequestBody(required = false) GiftRelationQuery query) {
        return Result.success(giftRelationInfoService.getPage(pageNum, pageSize, query));
    }

    @ApiOperationSupport(order = 15, author = "alex")
    @ApiOperation(value = "礼金关系列表查询", response = Result.class)
    @PostMapping(value = "/list")
    public Result<List<GiftRelationInfoVo>> getList(@RequestBody(required = false) GiftRelationQuery query) {
        return Result.success(giftRelationInfoService.getList(query));
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "礼金关系详情", response = Result.class)
    @GetMapping
    public Result<GiftRelationInfoVo> query(@RequestParam(value = "id") Long id) {
        return Result.success(giftRelationInfoService.queryGiftRelationInfo(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增礼金关系", response = Result.class)
    @PostMapping
    public Result<GiftRelationInfoVo> add(@Validated({Insert.class}) @RequestBody GiftRelationInfoVo giftRelationInfoVo) {
        return Result.success(giftRelationInfoService.addGiftRelationInfo(giftRelationInfoVo));
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改礼金关系", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody GiftRelationInfoVo giftRelationInfoVo) {
        return Result.success(giftRelationInfoService.updateGiftRelationInfo(giftRelationInfoVo));
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "删除礼金关系", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(giftRelationInfoService.deleteGiftRelationInfo(ids));
    }
}
