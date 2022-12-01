package com.alex.finance.service.analysis;

import com.alex.finance.vo.financeAnalysis.AnalysisVo;

import java.util.List;

/**
 * 财务分析 服务类
 *
 * @author: majf
 * @createDate: 2022-10-10 18:02:03
 * @description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface FinanceAnalysisService {

    /**
     * @param belongTo
     * @param searchDate
     * @description:
     * @author: alex
     * @return: java.util.List<com.alex.finance.vo.financeAnalysis.AnalysisVo>
     */
    List<AnalysisVo> getBalance(Long belongTo, String searchDate);

    /**
     * @param belongTo
     * @param searchDate
     * @param type
     * @description:
     * @author: alex
     * @return: java.util.List<com.alex.finance.vo.financeAnalysis.AnalysisVo>
     */
    List<AnalysisVo> getIncomeAndExpense(Long belongTo, String searchDate, String type);

    /**
     * @param belongTo
     * @param searchDate
     * @description: 获取天明细
     * @author:      alex
     * @return:      java.util.List<com.alex.finance.vo.financeAnalysis.AnalysisVo>
    */
    List<AnalysisVo> getDayExpense(Long belongTo, String searchDate);

    /**
     * @param belongTo
     * @param searchDate
     * @description: 获取月明细数据
     * @author:      alex
     * @return:      java.util.List<com.alex.finance.vo.financeAnalysis.AnalysisVo>
    */
    List<AnalysisVo> getMonthExpense(Long belongTo, String searchDate);
}
