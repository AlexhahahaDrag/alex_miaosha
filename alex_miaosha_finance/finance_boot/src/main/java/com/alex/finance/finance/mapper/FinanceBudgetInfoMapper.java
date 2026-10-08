package com.alex.finance.finance.mapper;

import com.alex.finance.finance.entity.FinanceBudgetInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * description: 月度零花钱预算表 Mapper
 * author: alex
 * createDate: 2026-10-08
 * version: 1.0.0
 */
public interface FinanceBudgetInfoMapper extends BaseMapper<FinanceBudgetInfo> {

    /**
     * 查询指定月份配置
     */
    @Select("SELECT * FROM finance_budget_info WHERE belong_to = #{belongTo} AND year_month = #{yearMonth} AND (is_delete = '0' OR is_delete = 0) LIMIT 1")
    FinanceBudgetInfo selectByMonth(@Param("belongTo") Long belongTo, @Param("yearMonth") String yearMonth);

    /**
     * 查询指定月份之前的最近一条历史配置 (继承基准)
     */
    @Select("SELECT * FROM finance_budget_info WHERE belong_to = #{belongTo} AND year_month < #{yearMonth} AND (is_delete = '0' OR is_delete = 0) ORDER BY year_month DESC LIMIT 1")
    FinanceBudgetInfo selectLatestBefore(@Param("belongTo") Long belongTo, @Param("yearMonth") String yearMonth);
}
