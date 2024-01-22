package com.alex.mission.controller;

import com.alex.base.common.Result;
import com.alex.common.pojo.dto.GoodsDTO;
import com.alex.common.pojo.dto.OrderDTO;
import com.alex.common.pojo.dto.SeckillGoodsDTO;
import com.alex.common.pojo.vo.WelcomeVo;
import com.alex.mission.service.GoodsService;
import com.alex.mission.service.OrderService;
import com.alex.mission.service.SeckillGoodsService;
import com.alex.mission.service.WelcomeService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 *description:  feign接口
 *author:       majf
 *createDate:   2022/7/15 11:40
 *version:      1.0.0
 */
@ApiSort(195)
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/cloud")
@Api(value = "cloud feign接口", tags = {"cloud feign接口"})
public class CloudController {

    private final GoodsService goodsService;

    private final OrderService orderService;

    private final SeckillGoodsService seckillGoodsService;

    private final WelcomeService welcomeService;

    @GetMapping(value = "/goods")
    @ApiOperation(value = "获得商品列表", notes = "获得商品列表")
    public Result<Page<GoodsDTO>> goodsIndex(@ApiParam(value = "页数", name = "page", required = true) @RequestParam(value = "page") Long page,
                                             @ApiParam(value = "页面大小", name = "pageSize", required = true) @RequestParam(value = "pageSize") Long pageSize,
                                             @ApiParam(value = "商品名称", name = "goodsName") @RequestParam(value = "goodsName", required = false) String goodsName){
        return goodsService.findGoods(page, pageSize, goodsName);
    }

    @PostMapping(value = "/goods")
    @ApiOperation(value = "新增商品", notes = "新增商品")
    public Result<GoodsDTO> createGoods(@ApiParam(value = "商品信息", name = "goodsDTO", required = true) @Valid @RequestBody GoodsDTO goodsDTO){
        return goodsService.create(goodsDTO);
    }

    @PutMapping(value = "/goods")
    @ApiOperation(value = "修改商品", notes = "修改商品")
    public Result<GoodsDTO> updateGoods (@ApiParam(value = "商品信息", name = "goodsDTO", required = true) @Valid @RequestBody GoodsDTO goodsDTO){
        return goodsService.update(goodsDTO);
    }

    @GetMapping(value = "/goods/{id}/edit")
    @ApiOperation(value = "获取商品详情", notes = "获取商品详情")
    public Result<GoodsDTO> goodsDetail(@ApiParam(value = "商品id", name = "id", required = true) @PathVariable(value = "id") Long goodsId){
        return Result.success(goodsService.selectById(goodsId));
    }

    @GetMapping(value = "/goods/{id}/updateUsing")
    @ApiOperation(value = "修改是否可用", notes = "修改是否可用")
    public Result<Boolean> updateUsing(@ApiParam(value = "商品id", name = "id", required = true) @PathVariable(value = "id") Long goodsId){
        return Result.success(goodsService.updateUsingById(goodsId));
    }

    @DeleteMapping(value = "/goods/{id}")
    @ApiOperation(value = "删除商品", notes = "删除商品")
    public Result<Boolean> deleteGoods(@ApiParam(value = "商品id", name = "id", required = true) @PathVariable(value = "id") Long goodsId){
        return Result.success(goodsService.delete(goodsId));
    }

    @DeleteMapping(value = "/goods/deletes")
    @ApiOperation(value = "删除商品", notes = "删除商品")
    public Result<Boolean> deleteGoodss(@ApiParam(value = "商品id列表", name = "ids", required = true) @RequestParam(value = "ids") String ids){
        return Result.success(goodsService.deletes(ids));
    }

    @GetMapping(value = "/order")
    @ApiOperation(value = "分页查询订单", notes = "分页查询订单")
    public Result<Page<OrderDTO>> orderIndex(@ApiParam(value = "页数", name = "page", required = true) @RequestParam(value = "page") Integer page,
                                             @ApiParam(value = "页面大小", name = "pageSize", required = true) @RequestParam(value = "pageSize") Integer pageSize,
                                             @ApiParam(value = "订单id", name = "orderId") @RequestParam(value = "orderId", required = false) Long orderId) {
        return orderService.findByOrderId(page, pageSize, orderId);
    }

    @GetMapping(value = "/seckillGoods")
    @ApiOperation(value = "分页查询秒杀商品", notes = "分页查询秒杀商品")
    public Result<Page<SeckillGoodsDTO>> seckillGodosIndex(@ApiParam(value = "页数", name = "page", required = true) @RequestParam(value = "page") Integer page,
                                                           @ApiParam(value = "页面大小", name = "pageSize", required = true) @RequestParam(value = "pageSize") Integer pageSize,
                                                           @ApiParam(value = "商品id", name = "goodsId") @RequestParam(value = "goodsId", required = false) Long goodsId) {
        return seckillGoodsService.findSeckill(page, pageSize, goodsId);
    }

    @GetMapping(value = "/welcome")
    @ApiOperation(value = "统计", notes = "统计")
    public Result<WelcomeVo> welcome() {
        return Result.success(welcomeService.welcomeCount());
    }

//    @PostMapping(value = "/upload")
//    @ApiOperation(value = "上传图片", notes  = "上传图片")
//    public Result<ImageKit> upload(@ApiParam(value = "文件", name = "file", required = true) @RequestParam(value = "file")MultipartFile file,
//                                   @ApiParam(value = "上传类型 0：商品，1：主页", name = "type", required = true) @RequestParam(value = "type") String type) {
//        return Result.success(imageService.upload(file, type));
//    }
//
//    @DeleteMapping(value = "/upload")
//    @ApiOperation(value = "删除图片", notes  = "删除图片")
//    public Result<Boolean> delete(@ApiParam(value = "key", name = "key", required = true) @RequestParam(value = "key") String key) {
//        return Result.success(imageService.delete(key));
//    }
//
//    @DeleteMapping(value = "/upload/deletes")
//    @ApiOperation(value = "批量删除图片", notes = "批量删除图片")
//    public Result<Boolean> deletes(@ApiParam(value = "key集合", name = "keys", required = true) @RequestParam(value = "keys") String[] keys) {
//        return Result.success(imageService.deletes(keys));
//    }
}
