package com.alex.finance.shopStockAnalysis.service.impl;

import com.alex.api.finance.vo.shopFinanceAnalysis.ShopStockAnalysisVo;
import com.alex.finance.shopStockAnalysis.mapper.ShopStockAnalysisMapper;
import com.alex.finance.shopStockAnalysis.service.ShopStockAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * description:  商品库存分析服务实现类
 * author:       majf
 * createDate:   2024/5/6 18:03
 * version:      1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShopStockAnalysisServiceImp implements ShopStockAnalysisService {

    private final ShopStockAnalysisMapper shopStockAnalysisMapper;

    @Override
    public ShopStockAnalysisVo getAllShopStockInfo() {
        return shopStockAnalysisMapper.getAllShopStockInfo();
    }
}
