package com.alex.finance.service.analysis;

import com.alex.finance.vo.financeAnalysis.AnalysisVo;

import java.util.List;

/**
 * 财务分析 服务类
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
     * @author:      alex
     * @return:      java.util.List<com.alex.finance.vo.financeAnalysis.AnalysisVo>
    */
    List<AnalysisVo> getBalance(Long belongTo, String searchDate);
}
