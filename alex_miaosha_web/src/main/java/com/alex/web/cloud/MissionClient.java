package com.alex.web.cloud;

import com.alex.base.common.Result;
import com.alex.common.pojo.dto.GoodsDTO;
import com.alex.web.cloud.callback.MissionClientFallback;
import com.alex.web.config.FeignConfig;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@FeignClient(name = "alex-mission", fallback = MissionClientFallback.class, configuration = FeignConfig.class)
@RequestMapping("/cloud")
public interface MissionClient {

    @GetMapping(value = "/goods")
    Result<Page<GoodsDTO>> goodsIndex(@RequestParam(value = "page", defaultValue = "1") Long page,
                                      @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize,
                                      @RequestParam(value = "goodsName", required = false) String goodsName);

    @PostMapping(value = "/goods")
    Result createGoods(@RequestBody @Valid GoodsDTO goodsDTO);

    @PutMapping(value = "/goods")
    Result updateGoods(@RequestBody @Valid GoodsDTO goodsDTO);

    @GetMapping(value = "/goods/{id}/edit")
    Result<GoodsDTO> editGoods(@PathVariable(value = "id") Long id);

    @GetMapping(value = "/goods/{id}/updateUsing")
    Result updateGoodsUsing(@PathVariable(value = "id") Long id);

    @DeleteMapping(value = "/goods/{id}")
    Result deleteGoods(@PathVariable(value = "id") Long id);

    @DeleteMapping(value = "/goods/deletes")
    Result deleteGoodss(@RequestParam(value = "ids") String ids);

    @GetMapping(value = "/order")
    Result<Page<GoodsDTO>> orderIndex(@RequestParam(value = "page", defaultValue = "1") Long page,
                                      @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize,
                                      @RequestParam(value = "id", required = false) Long id);

    @GetMapping(value = "/seckill")
    Result<Page<GoodsDTO>> seckillIndex(@RequestParam(value = "page", defaultValue = "1") Long page,
                                        @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize,
                                        @RequestParam(value = "goodsId", required = false) Long goodsId);

    @PostMapping("/uplaod")
    Result<Page<GoodsDTO>> uploadPicture(@RequestParam(value = "file") MultipartFile file,
                                  @RequestParam(value = "type") Integer type);

    @DeleteMapping("/upload")
    Result<Page<GoodsDTO>> deletePicture(@RequestParam(value = "key") String key);

    @DeleteMapping("/upload/deletes")
    Result<Page<GoodsDTO>> deletes(@RequestParam(value = "keys") String keys);

    @ApiOperation(value = "统计")
    @ApiOperationSupport(order = 1, author = "alex")
    @GetMapping("/welcome")
    Result<Page<GoodsDTO>> welcome();
}