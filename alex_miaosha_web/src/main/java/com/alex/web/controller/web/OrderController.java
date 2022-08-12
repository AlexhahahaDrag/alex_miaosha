package com.alex.web.controller.web;

import com.alex.base.common.Result;
import com.alex.common.pojo.dto.GoodsDTO;
import com.alex.web.cloud.MissionClient;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author:       majf
 * @createDate:   2022/8/11 16:25
 * @version:      1.0.0
 */
@Api(value = "订单模块", tags = {"订单模块"})
@ApiSort(110)
@ApiSupport(order = 110, author = "alex")
@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {

    private final MissionClient missionClient;

    @ApiOperation(value = "分页查询订单")
    @ApiOperationSupport(order = 1, author = "alex")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页数", name = "page", type = "Long.clas"),
            @ApiImplicitParam(value = "页面大小", name = "pageSize", type = "Long.clas"),
            @ApiImplicitParam(value = "id", name = "id", type = "Long.clas")
    }
    )
    @GetMapping
    public Result<Page<GoodsDTO>> goodsIndex(@RequestParam(value = "page", defaultValue = "1", required = false) Long page,
                                             @RequestParam(value = "pageSize", defaultValue = "10", required = false) Long pageSize,
                                             @RequestParam(value = "id", required = false) Long id) {
        return missionClient.orderIndex(page, pageSize, id);
    }
}
