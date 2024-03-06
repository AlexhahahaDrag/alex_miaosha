package com.alex.finance.mapper.shopFinance;

import com.alex.api.finance.vo.shopFinanceAnalysis.ShopFinanceAnalysisVo;
import com.alex.api.finance.vo.shopFinanceAnalysis.ShopFinanceChainYearVo;
import com.alex.finance.entity.shopFinance.ShopFinance;
import com.alex.api.finance.vo.shopFinance.ShopFinanceVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import com.alex.api.user.annotation.DataPermission;

import java.util.List;

/**
 * description:  商店财务表 mapper
 * author:       majf
 * createDate:   2024-02-23 21:19:49
 * version:      1.0.0
 */
@Mapper
public interface ShopFinanceMapper extends BaseMapper<ShopFinance> {

    @DataPermission(table = "t_shop_finance")
    Page<ShopFinanceVo> getPage(Page<ShopFinanceVo> page, @Param("shopFinanceVo") ShopFinanceVo shopFinanceVo);

    ShopFinanceVo queryShopFinance(@Param("id") Long id);

    List<ShopFinanceAnalysisVo> getDayShopFinanceInfo(@Param("searchDate") String searchDate);

    List<ShopFinanceAnalysisVo> getMonthShopFinanceInfo(@Param("searchDate") String searchDate);

    List<ShopFinanceAnalysisVo> getPayWayInfo(@Param("searchDate") String searchDate);

    List<ShopFinanceAnalysisVo> getShopNameInfo(@Param("searchDate") String searchDate);

    ShopFinanceChainYearVo getChainAndYear(@Param("searchDate") String searchDate);
}
