package com.alex.finance.finance.mapper;

import com.alex.finance.finance.entity.FinanceBudgetInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * description: 月度零花钱预算表 Mapper
 * author: alex
 * createDate: 2026-10-08
 * version: 1.0.0
 */
public interface FinanceBudgetInfoMapper extends BaseMapper<FinanceBudgetInfo> {

    /**
     * 查询指定月份配置 (按家庭组机构)
     */
    FinanceBudgetInfo selectByMonth(@Param("orgId") Long orgId, @Param("budgetMonth") String budgetMonth);

    /**
     * 查询指定月份之前的最近一条历史配置 (继承基准，按家庭组机构)
     */
    FinanceBudgetInfo selectLatestBefore(@Param("orgId") Long orgId, @Param("budgetMonth") String budgetMonth);
}
