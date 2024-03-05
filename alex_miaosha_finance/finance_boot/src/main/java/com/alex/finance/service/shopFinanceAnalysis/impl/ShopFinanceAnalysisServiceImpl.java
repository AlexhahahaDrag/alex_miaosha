package com.alex.finance.service.shopFinanceAnalysis.impl;

import com.alex.api.finance.vo.shopFinanceAnalysis.ShopFinanceAnalysisVo;
import com.alex.finance.mapper.shopFinance.ShopFinanceMapper;
import com.alex.finance.service.shopFinanceAnalysis.ShopFinanceAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopFinanceAnalysisServiceImpl implements ShopFinanceAnalysisService {

    private final ShopFinanceMapper shopFinanceMapper;

    @Override
    public List<ShopFinanceAnalysisVo> getDayShopFinanceInfo(String searchDate) {
        // TODO: 2024/3/5 加权限 
        return shopFinanceMapper.getDayShopFinanceInfo(searchDate);
    }

    @Override
    public List<ShopFinanceAnalysisVo> getMonthShopFinanceInfo(String searchDate) {
        // TODO: 2024/3/5 加权限
        return shopFinanceMapper.getMonthShopFinanceInfo(searchDate);
    }
}
