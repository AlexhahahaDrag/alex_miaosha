package com.alex.product.service.pmsShopWantProduct;

import com.alex.api.product.vo.pmsShopWantProduct.PmsShopWantProductVo;
import com.alex.product.entity.pmsShopWantProduct.PmsShopWantProduct;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 商品想买网上商品信息 服务类
 * author: alex
 * createDate: 2023-05-25 16:18:10
 * description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface PmsShopWantProductService extends IService<PmsShopWantProduct> {

    Page<PmsShopWantProductVo> getPage(Long pageNum, Long pageSize, PmsShopWantProductVo pmsShopWantProductVo);

    List<PmsShopWantProductVo> getList(PmsShopWantProductVo pmsShopWantProductVo);

    PmsShopWantProductVo queryPmsShopWantProduct(String id);

    Boolean addPmsShopWantProduct(PmsShopWantProductVo pmsShopWantProductVo);

    Boolean updatePmsShopWantProduct(PmsShopWantProductVo pmsShopWantProductVo);

    Boolean deletePmsShopWantProduct(String ids);
}
