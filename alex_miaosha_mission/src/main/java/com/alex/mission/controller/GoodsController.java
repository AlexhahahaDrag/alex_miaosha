package com.alex.mission.controller;

import com.alex.common.common.Result;
import com.alex.mission.pojo.vo.GoodsDetailVo;
import com.alex.mission.service.GoodsService;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@ApiSort(200)
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/goods")
@Api(value = "商品相关接口", tags = {"商品相关接口"})
public class GoodsController {

    private final GoodsService goodsService;

    @ApiOperationSupport(author = "majf", order = 30)
    @GetMapping(value = "/getGoodsList")
    @ApiOperation(value = "获得商品列表", notes = "获得商品列表")
    public Result<List<GoodsDetailVo>> getGoodsList(){
        return goodsService.getGoodsList();
    }

    @ApiOperationSupport(order = 40)
    @GetMapping(value = "/getDetail")
    @ApiOperation(value = "获得具体商品详情信息", notes = "获得具体商品详情信息")
    public Result<GoodsDetailVo> getDetail(@ApiParam(value = "goodsId", name = "货物id", required = true) @PathVariable(value = "goodsId") Long goodsId) {
        return goodsService.getDetail(goodsId);
    }
}
