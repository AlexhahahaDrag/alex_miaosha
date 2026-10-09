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
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public FinanceBudgetStatusVo getMonthlyBudgetStatus(String budgetMonth, Long orgId, Long belongTo) {
        if (StringUtils.isBlank(budgetMonth)) {
            budgetMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        Long targetOrgId = orgId;
        if (targetOrgId == null && userUtils != null) {
            targetOrgId = userUtils.getOrgId();
        }
        if (targetOrgId == null) {
            targetOrgId = 20L;
        }

        boolean isExplicitMember = (belongTo != null);

        // 1. 先查当月独立配置 (按家庭组机构)
        FinanceBudgetInfo current = financeBudgetInfoMapper.selectByMonth(targetOrgId, budgetMonth);
        BigDecimal budgetAmount = BigDecimal.ZERO;
        String categoryCodesStr = null;
        String incomeAndExpenses = "expense";
        boolean isInherited = false;
        Long recordId = null;

        if (current != null) {
            budgetAmount = current.getBudgetAmount() != null ? current.getBudgetAmount() : BigDecimal.ZERO;
            categoryCodesStr = current.getCategoryCodes();
            if (StringUtils.isNotBlank(current.getIncomeAndExpenses())) {
                incomeAndExpenses = current.getIncomeAndExpenses();
            }
            recordId = current.getId();
        } else {
            // 2. 当月无配置时，向上回退查最近的历史月份（自然继承上个月的配置，按家庭组机构）
            FinanceBudgetInfo history = financeBudgetInfoMapper.selectLatestBefore(targetOrgId, budgetMonth);
            if (history != null) {
                budgetAmount = history.getBudgetAmount() != null ? history.getBudgetAmount() : BigDecimal.ZERO;
                categoryCodesStr = history.getCategoryCodes();
                if (StringUtils.isNotBlank(history.getIncomeAndExpenses())) {
                    incomeAndExpenses = history.getIncomeAndExpenses();
                }
                isInherited = true;
            }
        }

        // 3. 解析分类列表 (纯净真实分类，过滤掉混入的收支类型关键字)
        List<String> categoryCodes = new ArrayList<>();
        if (StringUtils.isNotBlank(categoryCodesStr)) {
            categoryCodes = Arrays.stream(categoryCodesStr.split(","))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .filter(c -> !c.equalsIgnoreCase("支出") && !c.equalsIgnoreCase("收入")
                            && !c.equalsIgnoreCase("expense") && !c.equalsIgnoreCase("income"))
                    .distinct()
                    .collect(Collectors.toList());
        }

        // 4. 统计当月在指定分类下的实际收支 (家庭组模式下 belongTo 保持 null，由 @DataPermission 自动按机构全员聚合)
        YearMonth ym = YearMonth.parse(budgetMonth);
        LocalDate monthStart = ym.atDay(1);
        LocalDate monthEnd = ym.atEndOfMonth();

        Set<String> directions = parseIncomeAndExpenses(incomeAndExpenses);
        String normalizedDirection = String.join(",", directions);

        FinanceInfoVo queryVo = new FinanceInfoVo();
        if (isExplicitMember) {
            queryVo.setBelongTo(belongTo);
        } else {
            queryVo.setBelongTo(null);
        }
        queryVo.setInfoDateStart(monthStart);
        queryVo.setInfoDateEnd(monthEnd);
        queryVo.setIsValid("1");
        if (!categoryCodes.isEmpty()) {
            queryVo.setTypeCodes(categoryCodes);
        }

        BigDecimal actualExpense = BigDecimal.ZERO;
        if (directions.contains("expense") && directions.contains("income")) {
            // 支出与收入均选中: 查询所有符合分类的收支流水并求和
            queryVo.setIncomeAndExpenses(null);
            FinanceSummaryVo summary = financeInfoService.getFinanceSummary(queryVo);
            if (summary != null) {
                BigDecimal exp = summary.getTotalExpense() != null ? summary.getTotalExpense() : BigDecimal.ZERO;
                BigDecimal inc = summary.getTotalIncome() != null ? summary.getTotalIncome() : BigDecimal.ZERO;
                actualExpense = exp.add(inc);
            }
        } else if (directions.contains("income")) {
            queryVo.setIncomeAndExpenses("income");
            FinanceSummaryVo summary = financeInfoService.getFinanceSummary(queryVo);
            if (summary != null) {
                actualExpense = summary.getTotalIncome() != null ? summary.getTotalIncome() : BigDecimal.ZERO;
            }
        } else {
            queryVo.setIncomeAndExpenses("expense");
            FinanceSummaryVo summary = financeInfoService.getFinanceSummary(queryVo);
            if (summary != null) {
                actualExpense = summary.getTotalExpense() != null ? summary.getTotalExpense() : BigDecimal.ZERO;
            }
        }

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
                .orgId(targetOrgId)
                .belongTo(belongTo)
                .budgetMonth(budgetMonth)
                .incomeAndExpenses(normalizedDirection)
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
        String budgetMonth = req != null ? req.getBudgetMonth() : null;
        if (req == null || StringUtils.isBlank(budgetMonth)) {
            throw new IllegalArgumentException("预算月份不能为空！");
        }
        Long targetOrgId = req.getOrgId();
        if (targetOrgId == null && userUtils != null) {
            targetOrgId = userUtils.getOrgId();
        }
        if (targetOrgId == null) {
            targetOrgId = 20L;
        }

        Long belongTo = req.getBelongTo() != null ? req.getBelongTo() : (userUtils != null ? userUtils.getUserId() : null);

        Set<String> directions = parseIncomeAndExpenses(req.getIncomeAndExpenses());
        String incomeAndExpenses = String.join(",", directions);

        String categoryCodesStr = null;
        if (req.getCategoryCodes() != null && !req.getCategoryCodes().isEmpty()) {
            categoryCodesStr = req.getCategoryCodes().stream()
                    .filter(StringUtils::isNotBlank)
                    .map(String::trim)
                    .filter(c -> !c.equalsIgnoreCase("支出") && !c.equalsIgnoreCase("收入")
                            && !c.equalsIgnoreCase("expense") && !c.equalsIgnoreCase("income"))
                    .distinct()
                    .collect(Collectors.joining(","));
            if (StringUtils.isBlank(categoryCodesStr)) {
                categoryCodesStr = null;
            }
        }

        FinanceBudgetInfo exist = financeBudgetInfoMapper.selectByMonth(targetOrgId, budgetMonth);
        if (exist != null) {
            exist.setOrgId(targetOrgId);
            exist.setBelongTo(belongTo);
            exist.setBudgetAmount(req.getBudgetAmount());
            exist.setIncomeAndExpenses(incomeAndExpenses);
            exist.setCategoryCodes(categoryCodesStr);
            exist.setIsValid("1");
            financeBudgetInfoMapper.updateById(exist);
        } else {
            FinanceBudgetInfo newBudget = new FinanceBudgetInfo();
            newBudget.setOrgId(targetOrgId);
            newBudget.setBelongTo(belongTo);
            newBudget.setBudgetMonth(budgetMonth);
            newBudget.setIncomeAndExpenses(incomeAndExpenses);
            newBudget.setBudgetAmount(req.getBudgetAmount());
            newBudget.setCategoryCodes(categoryCodesStr);
            newBudget.setIsValid("1");
            financeBudgetInfoMapper.insert(newBudget);
        }
        return true;
    }

    private Set<String> parseIncomeAndExpenses(String directionStr) {
        Set<String> result = new LinkedHashSet<>();
        if (StringUtils.isNotBlank(directionStr)) {
            for (String part : directionStr.split(",")) {
                String trimmed = part.trim().toLowerCase();
                if ("expense".equals(trimmed) || "支出".equals(trimmed)) {
                    result.add("expense");
                } else if ("income".equals(trimmed) || "收入".equals(trimmed)) {
                    result.add("income");
                }
            }
        }
        if (result.isEmpty()) {
            result.add("expense");
        }
        return result;
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
    public List<String> getRecentCategories(String budgetMonth, Long belongTo) {
        if (StringUtils.isBlank(budgetMonth)) {
            budgetMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        // belongTo 为 null 时，由 @DataPermission 自然提取全家庭组/机构已有分类
        if (financeInfoMapper == null) {
            return Collections.emptyList();
        }
        try {
            YearMonth ym = YearMonth.parse(budgetMonth);
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
