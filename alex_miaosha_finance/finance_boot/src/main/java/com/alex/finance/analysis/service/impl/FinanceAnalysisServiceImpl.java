package com.alex.finance.analysis.service.impl;

import com.alex.api.finance.vo.financeAnalysis.AnalysisVo;
import com.alex.api.finance.vo.financeAnalysis.BalanceVo;
import com.alex.finance.analysis.mapper.FinanceAnalysisMapper;
import com.alex.finance.analysis.service.FinanceAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * description:
 * author: alex
 * createDate: 2022/10/22 14:49
 * version: 1.0.0
 */
@RequiredArgsConstructor
@Service
public class FinanceAnalysisServiceImpl implements FinanceAnalysisService {

    private static final String TYPE_INCOME = "income";
    private static final String TYPE_EXPENSE = "expense";

    private final FinanceAnalysisMapper financeAnalysisMapper;

    @Override
    public BalanceVo getBalance(Long belongTo, String searchDate) {
        List<AnalysisVo> currentList = financeAnalysisMapper.getBalance(belongTo, searchDate);
        if (searchDate == null || currentList == null || currentList.isEmpty()) {
            return new BalanceVo().setList(currentList);
        }

        // Calculate MoM and YoY dates safely
        String[] dateParts = searchDate.trim().split("-");
        if (dateParts.length < 2) {
            return new BalanceVo().setList(currentList);
        }
        int year;
        int month;
        try {
            year = Integer.parseInt(dateParts[0]);
            month = Integer.parseInt(dateParts[1]);
        } catch (NumberFormatException e) {
            return new BalanceVo().setList(currentList);
        }

        String momDate = (month <= 1) ? (year - 1) + "-12" : year + "-" + String.format("%02d", month - 1);
        String yoyDate = (year - 1) + "-" + String.format("%02d", month);

        List<AnalysisVo> momList = financeAnalysisMapper.getBalance(belongTo, momDate);
        List<AnalysisVo> yoyList = financeAnalysisMapper.getBalance(belongTo, yoyDate);

        List<AnalysisVo> currentIncomeExpense = financeAnalysisMapper.getIncomeAndExpense(belongTo, searchDate, null);
        List<AnalysisVo> momIncomeExpense = financeAnalysisMapper.getIncomeAndExpense(belongTo, momDate, null);
        List<AnalysisVo> yoyIncomeExpense = financeAnalysisMapper.getIncomeAndExpense(belongTo, yoyDate, null);

        BigDecimal monthIncomeSum = getSum(currentIncomeExpense, TYPE_INCOME);
        BigDecimal monthExpenseSum = getSum(currentIncomeExpense, TYPE_EXPENSE);

        BigDecimal currentTotal = BigDecimal.ZERO;
        BigDecimal momTotal = BigDecimal.ZERO;
        BigDecimal yoyTotal = BigDecimal.ZERO;

        Map<String, AnalysisVo> momMap = momList == null ? new HashMap<>() : 
            momList.stream().collect(Collectors.toMap(AnalysisVo::getTypeCode, Function.identity(), (v1, v2) -> v1));
        Map<String, AnalysisVo> yoyMap = yoyList == null ? new HashMap<>() : 
            yoyList.stream().collect(Collectors.toMap(AnalysisVo::getTypeCode, Function.identity(), (v1, v2) -> v1));

        for (AnalysisVo vo : currentList) {
            BigDecimal currentAmount = vo.getAmount() == null ? BigDecimal.ZERO : vo.getAmount();
            currentTotal = currentTotal.add(currentAmount);

            // Calculate item MoM
            AnalysisVo momVo = momMap.get(vo.getTypeCode());
            BigDecimal momAmount = (momVo == null || momVo.getAmount() == null) ? BigDecimal.ZERO : momVo.getAmount();
            momTotal = momTotal.add(momAmount);
            vo.setMomTrend(calculateTrend(currentAmount, momAmount));

            // Calculate item YoY
            AnalysisVo yoyVo = yoyMap.get(vo.getTypeCode());
            BigDecimal yoyAmount = (yoyVo == null || yoyVo.getAmount() == null) ? BigDecimal.ZERO : yoyVo.getAmount();
            yoyTotal = yoyTotal.add(yoyAmount);
            vo.setYoyTrend(calculateTrend(currentAmount, yoyAmount));
        }

        return new BalanceVo()
                .setList(currentList)
                .setMomTrend(calculateTrend(currentTotal, momTotal))
                .setYoyTrend(calculateTrend(currentTotal, yoyTotal))
                .setMonthIncomeSum(monthIncomeSum)
                .setMonthExpenseSum(monthExpenseSum)
                .setIncomeMomTrend(calculateTrend(monthIncomeSum, getSum(momIncomeExpense, TYPE_INCOME)))
                .setIncomeYoyTrend(calculateTrend(monthIncomeSum, getSum(yoyIncomeExpense, TYPE_INCOME)))
                .setExpenseMomTrend(calculateTrend(monthExpenseSum, getSum(momIncomeExpense, TYPE_EXPENSE)))
                .setExpenseYoyTrend(calculateTrend(monthExpenseSum, getSum(yoyIncomeExpense, TYPE_EXPENSE)));
    }

    private BigDecimal getSum(List<AnalysisVo> list, String type) {
        if (list == null || list.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return list.stream()
                .filter(vo -> type.equals(vo.getIncomeAndExpenses()))
                .map(vo -> vo.getAmount() == null ? BigDecimal.ZERO : vo.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String calculateTrend(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            int cmp = current.compareTo(BigDecimal.ZERO);
            if (cmp == 0) {
                return "0.0%";
            }
            return cmp > 0 ? "+100%" : "-100%";
        }
        BigDecimal change = current.subtract(previous);
        BigDecimal trend = change.divide(previous.abs(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        String prefix = trend.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        return prefix + trend.setScale(1, RoundingMode.HALF_UP) + "%";
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

