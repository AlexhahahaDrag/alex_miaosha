package com.alex.product.mapper.pmsShopWantProduct;

import com.alex.api.product.vo.pmsShopWantProduct.PmsShopWantProductVo;
import com.alex.product.entity.pmsShopWantProduct.PmsShopWantProduct;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * description:  商品想买网上商品信息 mapper
 * author:       alex
 * createDate:   2023-05-25 16:18:10
 * version:      1.0.0
 */
@Mapper
public interface PmsShopWantProductMapper extends BaseMapper<PmsShopWantProduct> {

    Page<PmsShopWantProductVo> getPage(Page<PmsShopWantProductVo> page, @Param("pmsShopWantProductVo") PmsShopWantProductVo pmsShopWantProductVo);

    List<PmsShopWantProductVo> getList(@Param("pmsShopWantProductVo") PmsShopWantProductVo pmsShopWantProductVo);

    PmsShopWantProductVo queryPmsShopWantProduct(@Param("id") String id);
}
