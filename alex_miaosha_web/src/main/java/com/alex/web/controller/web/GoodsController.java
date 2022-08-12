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
import org.springframework.web.bind.annotation.*;

@Api(value = "货物模块", tags = {"货物模块"})
@ApiSort(100)
@ApiSupport(order = 100, author = "alex")
@RestController
@RequiredArgsConstructor
@RequestMapping("/goods")
public class GoodsController {

    private final MissionClient missionClient;

    @ApiOperation(value = "分页查询货物")
    @ApiOperationSupport(order = 1, author = "alex")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页数", name = "page", type = "Long.clas"),
            @ApiImplicitParam(value = "页面大小", name = "pageSize", type = "Long.clas"),
            @ApiImplicitParam(value = "商品名称", name = "goodsName", type = "String.clas")
    }
    )
    @GetMapping
    public Result<Page<GoodsDTO>> goodsIndex(@RequestParam(value = "page", defaultValue = "1", required = false) Long page,
                                             @RequestParam(value = "pageSize", defaultValue = "10", required = false) Long pageSize,
                                             @RequestParam(value = "goodsName", required = false) String goodsName) {
        return missionClient.goodsIndex(page, pageSize, goodsName);
    }

    @ApiOperation(value = "新增货物")
    @ApiOperationSupport(order = 5, author = "alex")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "商品", name = "goodsDTO", type = "GoodsDTO.clas")
    }
    )
    @PostMapping
    public Result<Page<GoodsDTO>> createGoods(@RequestBody GoodsDTO goodsDTO) {
        return missionClient.createGoods(goodsDTO);
    }

    @ApiOperation(value = "修改货物")
    @ApiOperationSupport(order = 10, author = "alex")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "商品", name = "goodsDTO", type = "GoodsDTO.clas")
    }
    )
    @PutMapping
    public Result<Page<GoodsDTO>> updateGoods(@RequestBody GoodsDTO goodsDTO) {
        return missionClient.updateGoods(goodsDTO);
    }

    @ApiOperation(value = "选择单个货物")
    @ApiOperationSupport(order = 20, author = "alex")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "商品id", name = "id", type = "Long.clas")
    }
    )
    @GetMapping("/{id}/edit")
    public Result<GoodsDTO> editGoods(@RequestParam(value = "id", required = false) Long id) {
        return missionClient.editGoods(id);
    }

    @ApiOperation(value = "修改是否可用")
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "商品id", name = "id", type = "Long.clas")
    }
    )
    @GetMapping("/{id}/updateUsing")
    public Result updateGoodsUsing(@PathVariable(value = "id") Long id) {
        return missionClient.updateGoodsUsing(id);
    }

    @ApiOperation(value = "单个删除货物")
    @ApiOperationSupport(order = 40, author = "alex")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "商品id", name = "id", type = "Long.clas")
    }
    )
    @DeleteMapping("/{id}")
    public Result<GoodsDTO> deleteGoods(@PathVariable(value = "id") Long id) {
        return missionClient.deleteGoods(id);
    }

    @ApiOperation(value = "批量删除货物")
    @ApiOperationSupport(order = 50, author = "alex")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "商品ids", name = "id", type = "Long.clas")
    }
    )
    @DeleteMapping()
    public Result<GoodsDTO> deleteGoodss(@RequestParam(value = "ids", required = false) String ids) {
        return missionClient.deleteGoodss(ids);
    }
}
