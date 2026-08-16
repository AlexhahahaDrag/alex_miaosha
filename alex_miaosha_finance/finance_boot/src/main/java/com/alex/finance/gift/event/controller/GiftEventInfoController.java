package com.alex.finance.gift.event.controller;

import com.alex.api.finance.gift.event.query.GiftEventQuery;
import com.alex.api.finance.gift.event.vo.GiftEventBusinessVo;
import com.alex.api.finance.gift.event.vo.GiftEventInfoVo;
import com.alex.api.finance.gift.event.vo.GiftEventSummaryVo;
import com.alex.api.finance.gift.event.vo.GiftEventTypeOptionsVo;
import com.alex.api.finance.gift.event.vo.GiftRecordRecommendAmountVo;
import com.alex.base.common.Result;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import com.alex.finance.gift.event.service.GiftEventInfoService;
import com.alex.finance.gift.eventoption.service.GiftEventTypeOptionService;
import com.alex.finance.gift.eventoption.entity.GiftEventTypeOption;
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
@Api(value = "礼金事由管理", tags = {"礼金事由管理"})
@RestController
@RequiredArgsConstructor
@RequestMapping("${api.version:/api/v1}/gift-event-info-t")
public class GiftEventInfoController {

    private final GiftEventInfoService giftEventInfoService;
    private final GiftEventTypeOptionService giftEventTypeOptionService;

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "礼金事由分页查询", response = Result.class)
    @PostMapping(value = "/page")
    public Result<Page<GiftEventInfoVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                                  @RequestParam(value = "pageSize", required = false) Long pageSize,
                                                  @RequestBody(required = false) GiftEventQuery query) {
        return Result.success(giftEventInfoService.getPage(pageNum, pageSize, query));
    }

    @ApiOperationSupport(order = 15, author = "alex")
    @ApiOperation(value = "礼金事由列表查询", response = Result.class)
    @PostMapping(value = "/list")
    public Result<List<GiftEventInfoVo>> getList(@RequestBody(required = false) GiftEventQuery query) {
        return Result.success(giftEventInfoService.getList(query));
    }

    @ApiOperationSupport(order = 16, author = "alex")
    @ApiOperation(value = "礼金事由概览统计", response = Result.class)
    @GetMapping(value = "/summary")
    public Result<GiftEventSummaryVo> summary() {
        return Result.success(giftEventInfoService.getSummary());
    }

    @ApiOperationSupport(order = 17, author = "alex")
    @ApiOperation(value = "礼金事由业务分页", response = Result.class)
    @PostMapping(value = "/business-page")
    public Result<Page<GiftEventBusinessVo>> businessPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                                          @RequestParam(value = "pageSize", required = false) Long pageSize,
                                                          @RequestBody(required = false) GiftEventQuery query) {
        return Result.success(giftEventInfoService.getBusinessPage(pageNum, pageSize, query));
    }

    @ApiOperationSupport(order = 18, author = "alex")
    @ApiOperation(value = "事由类型下拉选项（含家庭组共享自定义）", response = Result.class)
    @GetMapping(value = "/event-type-options")
    public Result<GiftEventTypeOptionsVo> eventTypeOptions() {
        return Result.success(giftEventTypeOptionService.listEventTypeOptions());
    }

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "礼金事由详情", response = Result.class)
    @GetMapping
    public Result<GiftEventInfoVo> query(@RequestParam(value = "id") Long id) {
        return Result.success(giftEventInfoService.queryGiftEventInfo(id));
    }

    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增礼金事由", response = Result.class)
    @PostMapping
    public Result<GiftEventInfoVo> add(@Validated({Insert.class}) @RequestBody GiftEventInfoVo giftEventInfoVo) {
        return Result.success(giftEventInfoService.addGiftEventInfo(giftEventInfoVo));
    }

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改礼金事由", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody GiftEventInfoVo giftEventInfoVo) {
        return Result.success(giftEventInfoService.updateGiftEventInfo(giftEventInfoVo));
    }

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "删除礼金事由", response = Result.class)
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam("ids") String ids) {
        return Result.success(giftEventInfoService.deleteGiftEventInfo(ids));
    }

    @ApiOperationSupport(order = 19, author = "alex")
    @ApiOperation(value = "智能推荐金额", response = Result.class)
    @GetMapping(value = "/recommend-amount")
    public Result<GiftRecordRecommendAmountVo> recommendAmount(@RequestParam(value = "personId", required = false) Long personId,
                                                               @RequestParam(value = "eventType") String eventType,
                                                               @RequestParam(value = "direction", required = false) String direction) {
        return Result.success(giftEventTypeOptionService.getRecommendAmount(personId, eventType, direction));
    }

    @ApiOperationSupport(order = 21, author = "alex")
    @ApiOperation(value = "更新事由类型词典选项", response = Result.class)
    @PutMapping(value = "/event-type-option")
    public Result<Boolean> updateEventTypeOption(@RequestBody GiftEventTypeOption option) {
        return Result.success(giftEventTypeOptionService.updateOption(option));
    }
}
