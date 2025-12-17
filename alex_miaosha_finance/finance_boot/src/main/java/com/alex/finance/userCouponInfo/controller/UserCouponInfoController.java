package com.alex.finance.userCouponInfo.controller;

import com.alex.api.finance.userCouponInfo.vo.UserCouponInfoVo;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.alex.common.annotations.LogRestRequest;
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
import com.alex.finance.userCouponInfo.service.UserCouponInfoService;
import org.springframework.web.bind.annotation.RestController;
import com.alex.api.finance.userCouponInfo.vo.UserCouponRedeemReq;

/**
 * 用户消费券库存表 (按数量核销) 控制器
 * 
 * @author alex
 * @since 2025-12-17 14:08:13
 * @version 1.0.0
 */
@ApiSort(105)
@Api(value = "用户消费券库存表 (按数量核销)相关接口", tags = {"用户消费券库存表 (按数量核销)相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user-coupon-info")
public class UserCouponInfoController {

    /**
     * 用户消费券库存表 (按数量核销) 服务
     */
    private final UserCouponInfoService userCouponInfoService;

    /**
     * 分页查询用户消费券库存表 (按数量核销)
     * 
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param userCouponInfoVo 查询条件
     * @return 分页结果
     */
    @LogRestRequest(apiName = "获取用户消费券库存表 (按数量核销)分页")
    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取用户消费券库存表 (按数量核销)分页", notes = "分页查询用户消费券库存表 (按数量核销)列表", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class, example = "1"),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class, example = "10"),
            @ApiImplicitParam(value = "查询条件", name = "userCouponInfoVo", dataTypeClass = UserCouponInfoVo.class)}
    )
    public Result<Page<UserCouponInfoVo>> getPage(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Long pageNum,
                                                  @RequestParam(value = "pageSize", required = false, defaultValue = "10") Long pageSize,
                                                  @RequestBody(required = false) UserCouponInfoVo userCouponInfoVo) {
        return Result.success(userCouponInfoService.getPage(pageNum, pageSize, userCouponInfoVo));
    }

    /**
     * 根据ID查询用户消费券库存表 (按数量核销)详情
     * 
     * @param id 主键ID
     * @return 用户消费券库存表 (按数量核销)详情
     */
    @LogRestRequest(apiName = "获取用户消费券库存表 (按数量核销)详情")
    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取用户消费券库存表 (按数量核销)详情", notes = "根据ID查询用户消费券库存表 (按数量核销)详细信息", response = Result.class)
    @GetMapping(value = "/{id}")
    @ApiImplicitParam(value = "主键ID", name = "id", required = true, dataTypeClass = Long.class, paramType = "path")
    public Result<UserCouponInfoVo> getById(@PathVariable Long id) {
        return Result.success(userCouponInfoService.queryUserCouponInfo(id));
    }

    /**
     * AI Agent
     * 核销数量：先新增一条用户券实例（调用 addUserCouponInfo），再写入核销历史记录（redemption_record_info_t）
     * 说明：当前 user_coupon_info_t 未包含“库存数量”字段，因此“核销数量”以 redemption_record_info_t.redemption_quantity 记录为准。
     */
    @LogRestRequest(apiName = "核销用户消费券(按数量)")
    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 25, author = "alex")
    @ApiOperation(value = "核销用户消费券(按数量)", notes = "新增用户券实例并生成核销历史记录", response = Result.class)
    @PostMapping(value = "/redeem")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "核销请求", name = "req", dataTypeClass = UserCouponRedeemReq.class)
    })
    public Result<Boolean> redeem(@RequestBody UserCouponRedeemReq req) {
        // AI Agent: 业务逻辑下沉至 ServiceImpl，Controller 仅负责入参/出参封装
        return Result.success(userCouponInfoService.redeem(req));
    }

    /**
     * 新增用户消费券库存表 (按数量核销)
     * 
     * @param userCouponInfoVo 用户消费券库存表 (按数量核销)信息
     * @return 操作结果
     */
    @LogRestRequest(apiName = "新增用户消费券库存表 (按数量核销)")
    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增用户消费券库存表 (按数量核销)", notes = "创建新的用户消费券库存表 (按数量核销)记录", response = Result.class)
    @PostMapping
    public Result<Boolean> add(@Validated({Insert.class}) @RequestBody UserCouponInfoVo userCouponInfoVo) {
        return Result.success(userCouponInfoService.addUserCouponInfo(userCouponInfoVo));
    }

    /**
     * 修改用户消费券库存表 (按数量核销)
     * 
     * @param userCouponInfoVo 用户消费券库存表 (按数量核销)信息
     * @return 操作结果
     */
    @LogRestRequest(apiName = "修改用户消费券库存表 (按数量核销)")
    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改用户消费券库存表 (按数量核销)", notes = "更新用户消费券库存表 (按数量核销)信息", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody UserCouponInfoVo userCouponInfoVo) {
        return Result.success(userCouponInfoService.updateUserCouponInfo(userCouponInfoVo));
    }

    /**
     * 删除用户消费券库存表 (按数量核销)
     * 
     * @param ids 主键ID列表，多个ID用逗号分隔
     * @return 操作结果
     */
    @LogRestRequest(apiName = "删除用户消费券库存表 (按数量核销)")
    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "删除用户消费券库存表 (按数量核销)", notes = "根据ID列表批量删除用户消费券库存表 (按数量核销)", response = Result.class)
    @DeleteMapping
    @ApiImplicitParam(value = "主键ID列表，多个ID用逗号分隔", name = "ids", required = true, dataTypeClass = String.class, example = "1,2,3")
    public Result<Boolean> delete(@RequestParam String ids) {
        return Result.success(userCouponInfoService.deleteUserCouponInfo(ids));
    }
}
