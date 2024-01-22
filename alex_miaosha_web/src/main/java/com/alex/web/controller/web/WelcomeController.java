package com.alex.web.controller.web;

import com.alex.base.common.Result;
import com.alex.common.pojo.dto.GoodsDTO;
import com.alex.web.cloud.MissionClient;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * description:
 * author:       majf
 * createDate:   2022/8/11 16:25
 * version:      1.0.0
 */
@Api(value = "欢迎模块", tags = {"OrderController"})
@ApiSort(150)
@ApiSupport(order = 150, author = "alex")
@RestController
@RequiredArgsConstructor
@RequestMapping("/welcome")
public class WelcomeController {

    private final MissionClient missionClient;

    @ApiOperation(value = "统计")
    @ApiOperationSupport(order = 1, author = "alex")
    @GetMapping
    public Result<Page<GoodsDTO>> welcome() {
        return missionClient.welcome();
    }
}
