package com.alex.finance.service.analysis.impl;

import com.alex.finance.mapper.financeAnalysis.FinanceAnalysisMapper;
import com.alex.finance.service.analysis.FinanceAnalysisService;
import com.alex.finance.vo.financeAnalysis.AnalysisVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description:
 * @author: alex
 * @createDate: 2022/10/22 14:49
 * @version: 1.0.0
 */
@RequiredArgsConstructor
@Service
public class FinanceAnalysisServiceImpl implements FinanceAnalysisService {

    private final FinanceAnalysisMapper financeAnalysisMapper;

    @Override
    public List<AnalysisVo> getBalance(Long belongTo, String searchDate) {
        return financeAnalysisMapper.getBalance(belongTo, searchDate);
    }
}
