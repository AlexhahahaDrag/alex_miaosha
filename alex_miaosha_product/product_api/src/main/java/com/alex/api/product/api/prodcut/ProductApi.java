package com.alex.api.product.api.prodcut;

import com.alex.api.product.vo.pmsAttr.PmsAttrVo;
import com.alex.api.product.vo.pmsBrand.PmsBrandVo;
import com.alex.base.common.Result;
import com.alex.common.config.FeignConfig;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

/**
 * @description:  商品属性controller
 * @author:       alex
 * @createDate:   2023-03-02 15:04:46
 * @version:      1.0.0
 */
@Component
@FeignClient(name = "alex-oss-${spring.profiles.active:dev}", configuration = FeignConfig.class)
public interface ProductApi {

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取商品属性详情", notes = "获取商品属性详情", response = Result.class)
    @GetMapping(value = "/api/v1/pms-attr")
    Result<PmsAttrVo> queryPmsAttr(@RequestParam(value = "id") String id);

    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取品牌详情", notes = "获取品牌详情", response = Result.class)
    @GetMapping(value = "/api/v1//pms-brand")
    Result<PmsBrandVo> queryPmsBrand(@RequestParam(value = "id") String id);
}