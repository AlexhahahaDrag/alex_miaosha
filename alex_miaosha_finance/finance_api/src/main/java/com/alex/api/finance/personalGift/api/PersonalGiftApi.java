package com.alex.api.finance.personalGift.api;

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
import com.alex.api.finance.personalGift.vo.PersonalGiftVo;

/**
 * description:  个人随礼信息表controller
 * author:       alex
 * createDate:   2024-07-10 10:01:28
 * version:      1.0.0
 */
@Component
// @FeignClient(name = "alex-oss-${spring.profiles.active:dev}", configuration = FeignConfig.class)
public interface PersonalGiftApi {

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取个人随礼信息表分页", notes = "获取个人随礼信息表分页", response = Result.class)
    @PostMapping(value = "/api/v1//personal-gift/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class),
            @ApiImplicitParam(value = "查询条件", name = "personalGiftVo")}
    )
    Result<Page<PersonalGiftVo>> getPersonalGiftPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                            @RequestParam(value = "pageSize", required = false) Long pageSize,
                                            @RequestBody(required = false) PersonalGiftVo personalGiftVo);

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取个人随礼信息表详情", notes = "获取个人随礼信息表详情", response = Result.class)
    @GetMapping(value = "/api/v1//personal-gift")
    Result<PersonalGiftVo> queryPersonalGift(@RequestParam(value = "id") String id);

    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增个人随礼信息表", notes = "新增个人随礼信息表", response = Result.class)
    @PostMapping("/api/v1//personal-gift")
    Result<Boolean> addPersonalGift(@RequestBody PersonalGiftVo personalGiftVo);

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改个人随礼信息表", notes = "修改个人随礼信息表", response = Result.class)
    @PutMapping("/api/v1//personal-gift")
    Result<Boolean> updatePersonalGift(@RequestBody PersonalGiftVo personalGiftVo);

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除个人随礼信息表", notes = "刪除个人随礼信息表", response = Result.class)
    @DeleteMapping("/api/v1")
    Result<Boolean> deletePersonalGift(@RequestParam("ids") String ids);
}