package com.alex.product.mapper.pmsShopProduct;

import com.alex.api.product.vo.pmsShopProduct.PmsShopProductVo;
import com.alex.product.entity.pmsShopProduct.PmsShopProduct;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * description:  商品网上商品信息 mapper
 * author:       alex
 * createDate:   2023-05-15 14:11:10
 * version:      1.0.0
 */
@Mapper
public interface PmsShopProductMapper extends BaseMapper<PmsShopProduct> {

    Page<PmsShopProductVo> getPage(Page<PmsShopProductVo> page, @Param("pmsShopProductVo") PmsShopProductVo pmsShopProductVo);

    PmsShopProductVo queryPmsShopProduct(@Param("id") String id);

    Page<PmsShopProductVo> getNewestProductPage(Page<PmsShopProductVo> page, @Param("pmsShopProductVo") PmsShopProductVo pmsShopProductVo);

    List<PmsShopProductVo> getProductHisInfo(@Param("skuId") String skuId, @Param("startTime") String startTime);

    List<PmsShopProductVo> getCompareInfo(@Param("key") String key, @Param("searchTime") String searchTime);
}
