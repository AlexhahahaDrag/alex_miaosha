package com.alex.product.mapper.pmsShopProduct;

import com.alex.product.entity.pmsShopProduct.PmsShopProduct;
import com.alex.api.product.vo.pmsShopProduct.PmsShopProductVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description:  商品网上商品信息 mapper
 * @author:       alex
 * @createDate:   2023-05-15 14:11:10
 * @version:      1.0.0
 */
@Mapper
public interface PmsShopProductMapper extends BaseMapper<PmsShopProduct> {

    Page<PmsShopProductVo> getPage(Page<PmsShopProductVo> page, @Param("pmsShopProductVo") PmsShopProductVo pmsShopProductVo);

    PmsShopProductVo queryPmsShopProduct(@Param("id") String id);

    Page<PmsShopProductVo> getNewestProductPage(Page<PmsShopProductVo> page, @Param("pmsShopProductVo") PmsShopProductVo pmsShopProductVo);
}
