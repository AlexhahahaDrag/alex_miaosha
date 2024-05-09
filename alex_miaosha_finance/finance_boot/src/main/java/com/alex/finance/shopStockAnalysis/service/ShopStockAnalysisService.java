package com.alex.finance.shopStockAnalysis.service;

import com.alex.api.finance.shopStockAnalysis.vo.ShopStockAmountVo;
import com.alex.api.finance.shopStockAnalysis.vo.ShopStockAnalysisVo;

import java.util.List;

/**
 * description:  商品库存分析服务类
 * author:       majf
 * createDate:   2024/5/6 18:03
 * version:      1.0.0
 */
public interface ShopStockAnalysisService{

    /**
     * description: 获取所有商品库存信息
     * author:      majf
     * return:      ShopStockAnalysisVo
    */
    ShopStockAnalysisVo getAllShopStockInfo();

    /**
     * description: 获取所有现金分布信息
     * author:      majf
     * return:      com.alex.api.finance.shopStockAnalysis.vo.ShopStockAmountVo
    */
    List<ShopStockAmountVo> getAllAmountInfo();

    /**
     * description: 获取现金库存信息
     * author:      majf
     * return:      com.alex.api.finance.shopStockAnalysis.vo.ShopStockAmountVo
    */
    ShopStockAmountVo getCashAmountInfo();
}
