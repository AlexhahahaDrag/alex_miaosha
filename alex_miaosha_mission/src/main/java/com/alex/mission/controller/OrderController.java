package com.alex.mission.controller;

import com.alex.base.common.Result;
import com.alex.mission.pojo.vo.OrderDetailVo;
import com.alex.mission.service.OrderService;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 *description:  订单控制类
 *author:       majf
 *createDate:   2022/7/15 9:41
 *version:      1.0.0
 */
@ApiSort(205)
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/order")
@Api(value = "订单相关接口", tags = {"订单相关接口"})
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @ApiOperation(value = "返回客户的所有订单数据", notes = "返回客户的所有订单数据")
    public Result<List<OrderDetailVo>> getOrderList(HttpServletRequest request){
        return orderService.getOrderList(request);
    }
}
