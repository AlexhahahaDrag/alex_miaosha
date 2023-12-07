package com.alex.api.user.api.orgInfo;

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
import com.alex.api.user.vo.orgInfo.OrgInfoVo;

/**
 * @description:  机构表controller
 * @author:       alex
 * @createDate:   2023-12-07 16:57:00
 * @version:      1.0.0
 */
@Component
// @FeignClient(name = "alex-oss-${spring.profiles.active:dev}", configuration = FeignConfig.class)
public interface OrgInfoApi {

    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取机构表分页", notes = "获取机构表分页", response = Result.class)
    @PostMapping(value = "/api/v1//org-info/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum"),
            @ApiImplicitParam(value = "每页大小", name = "pageSize"),
            @ApiImplicitParam(value = "查询条件", name = "orgInfoVo")}
    )
    Result<Page<OrgInfoVo>> getOrgInfoPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
                                            @RequestParam(value = "pageSize", required = false) Long pageSize,
                                            @RequestBody(required = false) OrgInfoVo orgInfoVo);

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取机构表详情", notes = "获取机构表详情", response = Result.class)
    @GetMapping(value = "/api/v1//org-info")
    Result<OrgInfoVo> queryOrgInfo(@RequestParam(value = "id") String id);

    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增机构表", notes = "新增机构表", response = Result.class)
    @PostMapping("/api/v1//org-info")
    Result<Boolean> addOrgInfo(@RequestBody OrgInfoVo orgInfoVo);

    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改机构表", notes = "修改机构表", response = Result.class)
    @PutMapping("/api/v1//org-info")
    Result<Boolean> updateOrgInfo(@RequestBody OrgInfoVo orgInfoVo);

    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "刪除机构表", notes = "刪除机构表", response = Result.class)
    @DeleteMapping("/api/v1")
    Result<Boolean> deleteOrgInfo(@RequestParam("ids") String ids);
}