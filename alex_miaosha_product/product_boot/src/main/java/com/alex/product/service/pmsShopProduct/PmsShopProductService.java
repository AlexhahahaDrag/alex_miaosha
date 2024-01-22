package com.alex.product.service.pmsShopProduct;

import com.alex.api.product.vo.pmsShopProduct.PmsShopProductVo;
import com.alex.product.entity.pmsShopProduct.PmsShopProduct;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 商品网上商品信息 服务类
 * author: alex
 * createDate: 2023-05-15 14:11:10
 * description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface PmsShopProductService extends IService<PmsShopProduct> {

    Page<PmsShopProductVo> getPage(Long pageNum, Long pageSize, PmsShopProductVo pmsShopProductVo);

    PmsShopProductVo queryPmsShopProduct(String id);

    Boolean addPmsShopProduct(PmsShopProductVo pmsShopProductVo);

    Boolean updatePmsShopProduct(PmsShopProductVo pmsShopProductVo);

    Boolean deletePmsShopProduct(String ids);

    Page<PmsShopProductVo> getNewestProductPage(Long pageNum, Long pageSize, PmsShopProductVo pmsShopProductVo);

    List<PmsShopProductVo> getProductHisInfo(String skuId, String startTime);

    List<PmsShopProductVo>  getCompareInfo(String key, String searchTime);

    Boolean updateCompareInfo(String skuId, Long chooseId);
}
