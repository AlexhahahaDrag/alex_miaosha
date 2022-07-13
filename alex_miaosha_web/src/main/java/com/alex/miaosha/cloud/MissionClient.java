package com.alex.miaosha.cloud;

import com.alex.common.common.Result;
import com.alex.common.pojo.dto.GoodsDTO;
import com.alex.miaosha.cloud.callback.MissionClientFallback;
import com.alex.miaosha.config.FeignConfig;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@FeignClient(name = "alex-mission", fallback = MissionClientFallback.class, configuration = FeignConfig.class)
public interface MissionClient {

    @GetMapping(value = "/cloud/goods")
    Result<Page<GoodsDTO>> goodsIndex(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                      @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                                      @RequestParam(value = "page", required = false) String goodsName);

    @PostMapping(value = "/cloud/goods")
    Result<Object> goodsCreate(@RequestBody @Valid GoodsDTO goodsDTO);

    @PutMapping(value = "/cloud/goods")
    Result<Object> goodsUpdate(@RequestBody @Valid GoodsDTO goodsDTO);

    @GetMapping(value = "/cloud/goods/{id}/edit")
    Result<GoodsDTO> goodsEdit(@PathVariable(value = "id") Long id);

    @GetMapping(value = "/cloud/goods/updateUsing/{id}")
    Result<Object> goodsUsing(@PathVariable(value = "id") Long id);

    @DeleteMapping(value = "/cloud/goods/{id}")
    Result<Object> goodsDelete(@PathVariable(value = "id") Long id);

    @DeleteMapping(value = "/cloud/goods/deletes")
    Result<Object> goodsDeletes(@RequestParam(value = "ids") String ids);
}