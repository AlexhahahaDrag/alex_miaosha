package com.alex.finance.mapper.financeAnalysis;

import com.alex.finance.vo.financeAnalysis.AnalysisVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @description: 财务信息表 mapper
 * @author: majf
 * @createDate: 2022-10-10 18:02:03
 * @version: 1.0.0
 */
public interface FinanceAnalysisMapper {

    /**
     * @param belongTo
     * @description: 获取截账户余额
     * @author:      alex
     * @return:      java.util.List<com.alex.finance.vo.financeAnalysis.AnalysisVo>
    */

    List<AnalysisVo> getBalance(@Param("belongTo") Long belongTo, @Param("searchDate") String searchDate);
}
