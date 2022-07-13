package com.alex.mission.controller;

import com.alex.common.common.Result;
import com.alex.common.pojo.dto.GoodsDTO;
import com.alex.mission.pojo.vo.GoodsDetailVo;
import com.alex.mission.service.GoodsService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/goods")
public class GoodsController {

    final GoodsService goodsService;

    @GetMapping(value = "/getGoodsList")
    @ApiOperation(value = "获得商品列表", tags = "获得商品列表")
    public Result<List<GoodsDetailVo>> getGoodsList(){
        return goodsService.getGoodsList();
    }

    @GetMapping(value = "/getDetail")
    @ApiOperation(value = "获得具体商品详情信息", tags = "获得具体商品详情信息")
    public Result<GoodsDetailVo> getDetail(@ApiParam(value = "goodsId", name = "货物id", required = true) @PathVariable(value = "goodsId") Long goodsId) {
        return goodsService.getDetail(goodsId);
    }
}
