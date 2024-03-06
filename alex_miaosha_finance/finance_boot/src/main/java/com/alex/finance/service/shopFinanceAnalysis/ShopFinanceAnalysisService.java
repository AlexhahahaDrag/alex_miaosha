package com.alex.finance.service.shopFinanceAnalysis;

import com.alex.api.finance.vo.shopFinanceAnalysis.ShopFinanceAnalysisVo;
import com.alex.api.finance.vo.shopFinanceAnalysis.ShopFinanceChainYearVo;

import java.util.List;

/**
 * author: majf
 * createDate: 2024/3/2 20:48
 * description: 店铺财务分析管理
 * version: 1.0.0
 */
public interface ShopFinanceAnalysisService {

    List<ShopFinanceAnalysisVo> getDayShopFinanceInfo(String searchDate);

    List<ShopFinanceAnalysisVo> getMonthShopFinanceInfo(String searchDate);

    List<ShopFinanceAnalysisVo> getPayWayInfo(String searchDate);

    List<ShopFinanceAnalysisVo> getShopNameInfo(String searchDate);

    ShopFinanceChainYearVo getChainAndYear(String searchDate);
}
