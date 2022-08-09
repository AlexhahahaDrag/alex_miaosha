package com.alex.web.controller.web;

import com.alex.base.common.Result;
import com.alex.common.pojo.dto.GoodsDTO;
import com.alex.web.cloud.MissionClient;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "货物相关接口", tags = {"货物相关接口"})
@RestController
@RequestMapping("/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final MissionClient missionClient;

    @GetMapping
    @ApiOperation(value = "分页查询货物", tags = "分页查询货物")
    public Result<Page<GoodsDTO>> goodsIndex(@ApiParam(value = "page", name = "页数", required = true) @RequestParam(value = "page", defaultValue = "1") Integer page,
                                             @ApiParam(value = "pageSize", name = "页面大小", required = true) @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                                             @ApiParam(value = "goodsName", name = "页数") @RequestParam(value = "page",  required = false) String goodsName){
        return missionClient.goodsIndex(page, pageSize, goodsName);
    }
}
