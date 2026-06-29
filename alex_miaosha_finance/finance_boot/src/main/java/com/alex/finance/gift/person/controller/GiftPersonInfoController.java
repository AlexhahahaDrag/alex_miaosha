package com.alex.finance.gift.person.controller;

import com.alex.api.finance.gift.person.query.GiftPersonQuery;
import com.alex.api.finance.gift.person.vo.GiftPersonBusinessVo;
import com.alex.api.finance.gift.person.vo.GiftPersonInfoVo;
import com.alex.api.finance.gift.person.vo.GiftPersonProfileVo;
import com.alex.api.finance.gift.person.vo.GiftPersonSummaryVo;
import com.alex.base.common.Result;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import com.alex.finance.gift.person.service.GiftPersonInfoService;
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

@ApiSort(130)
@Api(value = "礼金往来人管理", tags = {"礼金往来人管理"})
@RestController
@RequiredArgsConstructor
@RequestMapping("${api.version:/api/v1}/gift-person-info-t")
public class GiftPersonInfoController {

    private final GiftPersonInfoService giftPersonInfoService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "礼金往来人分页查询", response = Result.class)
    @PostMapping(value = "/page")
    public Result<Page<GiftPersonInfoVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                                   @RequestParam(value = "pageSize", required = false) Long pageSize,
                                                   @RequestBody(required = false) GiftPersonQuery query) {
        return Result.success(giftPersonInfoService.getPage(pageNum, pageSize, query));
    }

    @ApiOperationSupport(order = 15, author = "alex")
    @ApiOperation(value = "礼金往来人列表查询", response = Result.class)
    @PostMapping(value = "/list")
    public Result<List<GiftPersonInfoVo>> getList(@RequestBody(required = false) GiftPersonQuery query) {
        return Result.success(giftPersonInfoService.getList(query));
    }

    @ApiOperationSupport(order = 16, author = "alex")
    @ApiOperation(value = "礼金往来人概览统计", response = Result.class)
    @GetMapping(value = "/summary")
    public Result<GiftPersonSummaryVo> summary() {
        return Result.success(giftPersonInfoService.getSummary());
    }

    @ApiOperationSupport(order = 17, author = "alex")
    @ApiOperation(value = "礼金往来人业务分页", response = Result.class)
    @PostMapping(value = "/business-page")
    public Result<Page<GiftPersonBusinessVo>> businessPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                                           @RequestParam(value = "pageSize", required = false) Long pageSize,
                                                           @RequestBody(required = false) GiftPersonQuery query) {
        return Result.success(giftPersonInfoService.getBusinessPage(pageNum, pageSize, query));
    }

    @ApiOperationSupport(order = 18, author = "alex")
    @ApiOperation(value = "礼金往来人个人档案", response = Result.class)
    @GetMapping(value = "/profile")
    public Result<GiftPersonProfileVo> profile(@RequestParam(value = "id") Long id) {
        return Result.success(giftPersonInfoService.getProfile(id));
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "礼金往来人详情", response = Result.class)
    @GetMapping
    public Result<GiftPersonInfoVo> query(@RequestParam(value = "id") Long id) {
        return Result.success(giftPersonInfoService.queryGiftPersonInfo(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增礼金往来人", response = Result.class)
    @PostMapping
    public Result<GiftPersonInfoVo> add(@Validated({Insert.class}) @RequestBody GiftPersonInfoVo giftPersonInfoVo) {
        return Result.success(giftPersonInfoService.addGiftPersonInfo(giftPersonInfoVo));
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改礼金往来人", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody GiftPersonInfoVo giftPersonInfoVo) {
        return Result.success(giftPersonInfoService.updateGiftPersonInfo(giftPersonInfoVo));
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "删除礼金往来人", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(giftPersonInfoService.deleteGiftPersonInfo(ids));
    }
}
