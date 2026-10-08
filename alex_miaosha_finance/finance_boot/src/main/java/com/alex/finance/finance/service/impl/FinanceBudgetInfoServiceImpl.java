package com.alex.finance.finance.service.impl;

import com.alex.api.finance.vo.dict.DictInfoVo;
import com.alex.api.finance.vo.finance.FinanceBudgetSaveReq;
import com.alex.api.finance.vo.finance.FinanceBudgetStatusVo;
import com.alex.api.finance.vo.finance.FinanceInfoVo;
import com.alex.api.finance.vo.finance.FinanceSummaryVo;
import com.alex.api.user.user.UserUtils;
import com.alex.finance.dict.service.DictInfoService;
import com.alex.finance.finance.entity.FinanceBudgetInfo;
import com.alex.finance.finance.mapper.FinanceBudgetInfoMapper;
import com.alex.finance.finance.mapper.FinanceInfoMapper;
import com.alex.finance.finance.service.FinanceBudgetInfoService;
import com.alex.finance.finance.service.FinanceInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * description: 月度零花钱预算服务实现
 * author: alex
 * createDate: 2026-10-08
 * version: 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FinanceBudgetInfoServiceImpl extends ServiceImpl<FinanceBudgetInfoMapper, FinanceBudgetInfo>
        implements FinanceBudgetInfoService {

    private final FinanceBudgetInfoMapper financeBudgetInfoMapper;
    private final FinanceInfoService financeInfoService;

    @Autowired(required = false)
    private FinanceInfoMapper financeInfoMapper;

    @Autowired(required = false)
    private DictInfoService dictInfoService;

    @Autowired(required = false)
    private UserUtils userUtils;

    @Override
    public FinanceBudgetStatusVo getMonthlyBudgetStatus(String yearMonth, Long belongTo) {
        if (StringUtils.isBlank(yearMonth)) {
            yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        if (belongTo == null && userUtils != null) {
            try {
                ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attrs != null) {
                    belongTo = userUtils.getUserId(attrs.getRequest());
                }
            } catch (Exception e) {
                log.debug("未能从当前上下文自动解析登录用户: {}", e.getMessage());
            }
        }

        // 1. 先查当月独立配置
        FinanceBudgetInfo current = financeBudgetInfoMapper.selectByMonth(belongTo, yearMonth);
        BigDecimal budgetAmount = BigDecimal.ZERO;
        String categoryCodesStr = null;
        boolean isInherited = false;
        Long recordId = null;

        if (current != null) {
            budgetAmount = current.getBudgetAmount() != null ? current.getBudgetAmount() : BigDecimal.ZERO;
            categoryCodesStr = current.getCategoryCodes();
            isInherited = false;
            recordId = current.getId();
        } else {
            // 2. 当月无配置时，向上回退查最近的历史月份（自然继承上个月的配置）
            FinanceBudgetInfo history = financeBudgetInfoMapper.selectLatestBefore(belongTo, yearMonth);
            if (history != null) {
                budgetAmount = history.getBudgetAmount() != null ? history.getBudgetAmount() : BigDecimal.ZERO;
                categoryCodesStr = history.getCategoryCodes();
                isInherited = true;
            }
        }

        // 3. 解析分类列表
        List<String> categoryCodes = new ArrayList<>();
        if (StringUtils.isNotBlank(categoryCodesStr)) {
            categoryCodes = Arrays.stream(categoryCodesStr.split(","))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
        }

        // 4. 统计当月在指定分类下的实际支出
        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate monthStart = ym.atDay(1);
        LocalDate monthEnd = ym.atEndOfMonth();

        FinanceInfoVo queryVo = new FinanceInfoVo();
        queryVo.setBelongTo(belongTo);
        queryVo.setInfoDateStart(monthStart);
        queryVo.setInfoDateEnd(monthEnd);
        queryVo.setIncomeAndExpenses("expense");
        queryVo.setIsValid("1");
        if (!categoryCodes.isEmpty()) {
            queryVo.setTypeCodes(categoryCodes);
        }

        FinanceSummaryVo summary = financeInfoService.getFinanceSummary(queryVo);
        BigDecimal actualExpense = (summary != null && summary.getTotalExpense() != null)
                ? summary.getTotalExpense() : BigDecimal.ZERO;

        // 5. 计算剩余与百分比
        BigDecimal remainingAmount = budgetAmount.subtract(actualExpense);
        BigDecimal usagePercent = BigDecimal.ZERO;
        if (budgetAmount.compareTo(BigDecimal.ZERO) > 0) {
            usagePercent = actualExpense.divide(budgetAmount, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(1, RoundingMode.HALF_UP);
        }
        boolean isOverBudget = budgetAmount.compareTo(BigDecimal.ZERO) > 0 && actualExpense.compareTo(budgetAmount) > 0;

        // 6. 组装类别名称
        List<String> categoryNames = mapCategoryNames(categoryCodes);

        return FinanceBudgetStatusVo.builder()
                .id(recordId)
                .belongTo(belongTo)
                .yearMonth(yearMonth)
                .budgetAmount(budgetAmount)
                .categoryCodes(categoryCodes)
                .categoryNames(categoryNames)
                .actualExpense(actualExpense)
                .remainingAmount(remainingAmount)
                .usagePercent(usagePercent)
                .isOverBudget(isOverBudget)
                .isInherited(isInherited)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveMonthlyBudget(FinanceBudgetSaveReq req) {
        if (req == null || StringUtils.isBlank(req.getYearMonth())) {
            throw new IllegalArgumentException("年份月份不能为空！");
        }
        Long belongTo = req.getBelongTo();
        if (belongTo == null && userUtils != null) {
            try {
                ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attrs != null) {
                    belongTo = userUtils.getUserId(attrs.getRequest());
                }
            } catch (Exception e) {
                log.debug("未能自动获取归属人: {}", e.getMessage());
            }
        }
        if (belongTo == null) {
            throw new IllegalArgumentException("归属人ID不能为空！");
        }

        String categoryCodesStr = null;
        if (req.getCategoryCodes() != null && !req.getCategoryCodes().isEmpty()) {
            categoryCodesStr = req.getCategoryCodes().stream()
                    .filter(StringUtils::isNotBlank)
                    .map(String::trim)
                    .distinct()
                    .collect(Collectors.joining(","));
        }

        FinanceBudgetInfo exist = financeBudgetInfoMapper.selectByMonth(belongTo, req.getYearMonth());
        if (exist != null) {
            exist.setBudgetAmount(req.getBudgetAmount());
            exist.setCategoryCodes(categoryCodesStr);
            exist.setIsValid("1");
            financeBudgetInfoMapper.updateById(exist);
        } else {
            FinanceBudgetInfo newBudget = new FinanceBudgetInfo();
            newBudget.setBelongTo(belongTo);
            newBudget.setYearMonth(req.getYearMonth());
            newBudget.setBudgetAmount(req.getBudgetAmount());
            newBudget.setCategoryCodes(categoryCodesStr);
            newBudget.setIsValid("1");
            financeBudgetInfoMapper.insert(newBudget);
        }
        return true;
    }

    private List<String> mapCategoryNames(List<String> categoryCodes) {
        if (categoryCodes == null || categoryCodes.isEmpty() || dictInfoService == null) {
            return categoryCodes;
        }
        try {
            Map<String, String> dictMap = new HashMap<>();
            List<DictInfoVo> dictList = dictInfoService.listByBelong("finance_type");
            if (dictList != null) {
                for (DictInfoVo vo : dictList) {
                    if (vo.getTypeCode() != null && vo.getTypeName() != null) {
                        dictMap.put(vo.getTypeCode(), vo.getTypeName());
                    }
                }
            }
            return categoryCodes.stream()
                    .map(code -> dictMap.getOrDefault(code, code))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.debug("解析分类字典名称异常: {}", e.getMessage());
            return categoryCodes;
        }
    }

    @Override
    public List<String> getRecentCategories(String yearMonth, Long belongTo) {
        if (StringUtils.isBlank(yearMonth)) {
            yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        if (belongTo == null && userUtils != null) {
            try {
                ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attrs != null) {
                    belongTo = userUtils.getUserId(attrs.getRequest());
                }
            } catch (Exception e) {
                log.debug("未能自动获取归属人: {}", e.getMessage());
            }
        }
        if (financeInfoMapper == null) {
            return Collections.emptyList();
        }
        try {
            YearMonth ym = YearMonth.parse(yearMonth);
            LocalDate startDate = ym.minusMonths(1).atDay(1);
            LocalDate endDate = ym.atEndOfMonth();
            List<String> list = financeInfoMapper.selectRecentCategories(startDate, endDate, belongTo);
            return list != null ? list : Collections.emptyList();
        } catch (Exception e) {
            log.error("获取近两月记账分类异常: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
