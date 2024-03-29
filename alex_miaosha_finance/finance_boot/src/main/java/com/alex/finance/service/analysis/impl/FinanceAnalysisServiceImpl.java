package com.alex.finance.service.analysis.impl;

import com.alex.api.finance.vo.financeAnalysis.AnalysisVo;
import com.alex.finance.mapper.financeAnalysis.FinanceAnalysisMapper;
import com.alex.finance.service.analysis.FinanceAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * description:
 * author: alex
 * createDate: 2022/10/22 14:49
 * version: 1.0.0
 */
@RequiredArgsConstructor
@Service
public class FinanceAnalysisServiceImpl implements FinanceAnalysisService {

    private final FinanceAnalysisMapper financeAnalysisMapper;

    @Override
    public List<AnalysisVo> getBalance(Long belongTo, String searchDate) {
        return financeAnalysisMapper.getBalance(belongTo, searchDate);
    }

    @Override
    public List<AnalysisVo> getIncomeAndExpense(Long belongTo, String searchDate, String type) {
        return financeAnalysisMapper.getIncomeAndExpense(belongTo, searchDate, type);
    }

    @Override
    public List<AnalysisVo> getDayExpense(Long belongTo, String searchDate) {
        return financeAnalysisMapper.getDayExpense(belongTo, searchDate);
    }

    @Override
    public List<AnalysisVo> getMonthExpense(Long belongTo, String searchDate) {
        return financeAnalysisMapper.getMonthExpense(belongTo, searchDate);
    }
}
