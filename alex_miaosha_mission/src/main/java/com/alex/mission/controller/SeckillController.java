package com.alex.mission.controller;

import com.alex.common.annotations.SeckillLimit;
import com.alex.common.common.Result;
import com.alex.common.enums.ResultEnum;
import com.alex.mission.manager.AccessLimitService;
import com.alex.mission.service.SeckillService;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 *description:  秒杀控制类
 *author:       majf
 *createDate:   2022/7/15 9:41
 *version:      1.0.0
 */
@ApiSort(210)
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/seckill")
@Api(value = "秒杀相关接口", tags = {"秒杀相关接口"})
public class SeckillController {

    private final SeckillService seckillService;

    private final AccessLimitService accessLimitService;

    @GetMapping
    @ApiOperation(value = "执行秒杀", notes = "执行秒杀")
    public Result<Integer> doSeckill(@ApiParam(value = "商品id", name = "goodsId", required = true) @RequestParam(value = "goodsId") Long goodsId,
                                     @ApiParam(value = "路径", name = "path", required = true) @RequestParam(value = "path")String path, HttpServletRequest request){
        // TODO: 2022/7/15 校验及设置限流
        if (!accessLimitService.tryAcquireToken()) {
            Result.error(ResultEnum.ACCESS_TOO_MANY.getCode(), ResultEnum.ACCESS_TOO_MANY.getValue());
        }
        return seckillService.doSeckill(goodsId, path, request);
    }

    @GetMapping(value = "result")
    @ApiOperation(value = "客户端轮询查询是否下单成功", notes = "客户端轮询查询是否下单成功, orderId:成功, -1:秒杀失败, 0:排队中")
    public Result<Long> seckillResult(@ApiParam(value = "商品id", name = "goodsId", required = true) @RequestParam(value = "goodsId") Long goodsId,
                                      HttpServletRequest request){
        return seckillService.seckillResult(goodsId, request);
    }

    // TODO: 2022/7/15 测试一个id最多访问几回 
    @GetMapping(value = "getPath")
    @SeckillLimit(seconds = 5, maxCount = 10)
    @ApiOperation(value = "返回一个唯一的path的id", notes = "返回一个唯一的path的id")
    public Result<String> getSeckillPath(@ApiParam(value = "商品id", name = "goodsId", required = true) @RequestParam(value = "goodsId") Long goodsId,
                                      HttpServletRequest request){
        return seckillService.getSeckillPath(goodsId, request);
    }
}
