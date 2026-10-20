package com.alex.finance.finance.service.impl;

import com.alex.api.finance.vo.dict.DictInfoVo;
import com.alex.api.finance.vo.finance.FinanceBudgetSaveReq;
import com.alex.api.finance.vo.finance.FinanceBudgetStatusVo;
import com.alex.api.finance.vo.finance.FinanceInfoVo;
import com.alex.api.finance.vo.finance.FinanceSummaryVo;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.api.user.userInfo.UserUtils;
import com.alex.base.constants.SysConf;
import com.alex.common.exception.FinanceException;
import com.alex.common.utils.date.DateUtils;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * description: 月度零花钱预算服务实现
 * author: alex
 * createDate: 2026-10-08
 * version: 2.0.0
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

    private YearMonth validateAndParseBudgetMonth(String budgetMonth) {
        if (StringUtils.isBlank(budgetMonth)) {
            return YearMonth.from(DateUtils.now());
        }
        try {
            return YearMonth.parse(budgetMonth.trim());
        } catch (Exception e) {
            throw FinanceException.param("预算月份格式不正确，标准格式为 YYYY-MM（如 2026-10）");
        }
    }

    @Override
    public FinanceBudgetStatusVo getMonthlyBudgetStatus(String budgetMonth, Long orgId, Long belongTo) {
        YearMonth ym = validateAndParseBudgetMonth(budgetMonth);
        String targetBudgetMonth = ym.toString();
        Long targetOrgId = resolveTargetOrgId(orgId);

        FinanceBudgetInfo current = financeBudgetInfoMapper.selectByMonth(targetOrgId, targetBudgetMonth);
        FinanceBudgetInfo history = (current == null) ? financeBudgetInfoMapper.selectLatestBefore(targetOrgId, targetBudgetMonth) : null;
        boolean isInherited = (history != null);
        FinanceBudgetInfo budget = (current != null) ? current : history;

        BigDecimal budgetAmount = BigDecimal.ZERO;
        String categoryCodesStr = null;
        String incomeAndExpenses = SysConf.EXPENSE_STATUS;
        Long recordId = (current != null) ? current.getId() : null;

        if (budget != null) {
            budgetAmount = budget.getBudgetAmount() != null ? budget.getBudgetAmount() : BigDecimal.ZERO;
            categoryCodesStr = budget.getCategoryCodes();
            if (StringUtils.isNotBlank(budget.getIncomeAndExpenses())) {
                incomeAndExpenses = budget.getIncomeAndExpenses();
            }
        }

        List<String> categoryCodes = parseCategoryCodes(categoryCodesStr);
        Set<String> directions = parseIncomeAndExpenses(incomeAndExpenses);

        FinanceInfoVo queryVo = new FinanceInfoVo();
        queryVo.setBelongTo(belongTo);
        queryVo.setInfoDateStart(ym.atDay(1));
        queryVo.setInfoDateEnd(ym.atEndOfMonth());
        queryVo.setIsValid("1");
        if (!categoryCodes.isEmpty()) {
            queryVo.setTypeCodes(categoryCodes);
        }

        BigDecimal actualExpense = calculateActualExpense(queryVo, directions);
        BigDecimal remainingAmount = budgetAmount.subtract(actualExpense);
        BigDecimal usagePercent = calculateUsagePercent(budgetAmount, actualExpense);
        boolean isOverBudget = budgetAmount.compareTo(BigDecimal.ZERO) > 0 && actualExpense.compareTo(budgetAmount) > 0;

        return FinanceBudgetStatusVo.builder()
                .id(recordId)
                .orgId(targetOrgId)
                .belongTo(belongTo)
                .budgetMonth(targetBudgetMonth)
                .incomeAndExpenses(String.join(",", directions))
                .budgetAmount(budgetAmount)
                .categoryCodes(categoryCodes)
                .categoryNames(mapCategoryNames(categoryCodes))
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
            throw FinanceException.param("预算月份不能为空！");
        }
        YearMonth ym = validateAndParseBudgetMonth(budgetMonth);
        String targetBudgetMonth = ym.toString();

        if (req.getBudgetAmount() != null && req.getBudgetAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw FinanceException.param("预算金额不能为负数！");
        }

        assertCanEditBudget();
        Long targetOrgId = resolveTargetOrgId(req.getOrgId());

        Long belongTo = req.getBelongTo();
        if (belongTo == null && userUtils != null) {
            belongTo = userUtils.getUserId();
        }

        Set<String> directions = parseIncomeAndExpenses(req.getIncomeAndExpenses());
        String incomeAndExpenses = String.join(",", directions);

        List<String> cleanCodes = cleanCategoryCodes(req.getCategoryCodes());
        String categoryCodesStr = cleanCodes.isEmpty() ? null : String.join(",", cleanCodes);

        FinanceBudgetInfo exist = financeBudgetInfoMapper.selectByMonth(targetOrgId, targetBudgetMonth);
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
            newBudget.setBudgetMonth(targetBudgetMonth);
            newBudget.setIncomeAndExpenses(incomeAndExpenses);
            newBudget.setBudgetAmount(req.getBudgetAmount());
            newBudget.setCategoryCodes(categoryCodesStr);
            newBudget.setIsValid("1");
            financeBudgetInfoMapper.insert(newBudget);
        }
        return true;
    }

    private Long resolveTargetOrgId(Long orgId) {
        Long userOrgId = userUtils != null ? userUtils.getOrgId() : null;
        if (userUtils != null) {
            TUserVo user = userUtils.getLoginUser();
            if (user != null && !isSuper(user)) {
                if (orgId != null && userOrgId != null && !userOrgId.equals(orgId)) {
                    throw FinanceException.forbidden("无权访问其他机构数据");
                }
            }
        }
        if (orgId != null) {
            return orgId;
        }
        return userOrgId != null ? userOrgId : 20L;
    }

    private void assertCanEditBudget() {
        if (userUtils == null) {
            return;
        }
        TUserVo user = userUtils.getLoginUser();
        if (user == null) {
            return;
        }
        if (isSuper(user) || isFamilyAdmin(user) || hasBudgetEditPermission(user)) {
            return;
        }
        throw FinanceException.forbidden("无权保存预算设定，仅家庭管理员可操作");
    }

    private boolean isSuper(TUserVo user) {
        return hasRoleContaining(user, "super");
    }

    private boolean isFamilyAdmin(TUserVo user) {
        return hasRoleContaining(user, "admin");
    }

    private boolean hasRoleContaining(TUserVo user, String keyword) {
        if (user == null || user.getRoleInfoVoList() == null) {
            return false;
        }
        return user.getRoleInfoVoList().stream()
                .filter(r -> r != null && r.getRoleCode() != null)
                .anyMatch(r -> r.getRoleCode().toLowerCase().contains(keyword));
    }

    private boolean hasBudgetEditPermission(TUserVo user) {
        if (user == null) {
            return false;
        }
        List<String> codes = user.getPermissionCodes();
        if (codes != null && codes.contains("finance:financeManager:budget:edit")) {
            return true;
        }
        List<String> btnCodes = user.getButtonPermissionCodes();
        return btnCodes != null && btnCodes.contains("finance:financeManager:budget:edit");
    }

    private List<String> parseCategoryCodes(String categoryCodesStr) {
        if (StringUtils.isBlank(categoryCodesStr)) {
            return Collections.emptyList();
        }
        return cleanCategoryCodes(Arrays.asList(categoryCodesStr.split(",")));
    }

    private List<String> cleanCategoryCodes(Collection<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return Collections.emptyList();
        }
        return codes.stream()
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .filter(c -> !c.equalsIgnoreCase("支出") && !c.equalsIgnoreCase("收入")
                        && !c.equalsIgnoreCase(SysConf.EXPENSE_STATUS) && !c.equalsIgnoreCase(SysConf.INCOME_STATUS))
                .distinct()
                .collect(Collectors.toList());
    }

    private BigDecimal calculateActualExpense(FinanceInfoVo queryVo, Set<String> directions) {
        boolean hasExp = directions.contains(SysConf.EXPENSE_STATUS);
        boolean hasInc = directions.contains(SysConf.INCOME_STATUS);
        if (hasExp && hasInc) {
            queryVo.setIncomeAndExpenses(null);
        } else {
            queryVo.setIncomeAndExpenses(hasInc ? SysConf.INCOME_STATUS : SysConf.EXPENSE_STATUS);
        }

        FinanceSummaryVo summary = financeInfoService.getFinanceSummary(queryVo);
        if (summary == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal exp = summary.getTotalExpense() != null ? summary.getTotalExpense() : BigDecimal.ZERO;
        BigDecimal inc = summary.getTotalIncome() != null ? summary.getTotalIncome() : BigDecimal.ZERO;
        if (hasExp && hasInc) {
            return exp.subtract(inc);
        }
        return hasInc ? inc : exp;
    }

    private BigDecimal calculateUsagePercent(BigDecimal budgetAmount, BigDecimal actualExpense) {
        if (budgetAmount == null || actualExpense == null || budgetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }
        return actualExpense.divide(budgetAmount, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(1, RoundingMode.HALF_UP);
    }

    private Set<String> parseIncomeAndExpenses(String directionStr) {
        Set<String> result = new LinkedHashSet<>();
        if (StringUtils.isNotBlank(directionStr)) {
            for (String part : directionStr.split(",")) {
                String trimmed = part.trim().toLowerCase();
                if (SysConf.EXPENSE_STATUS.equals(trimmed) || "支出".equals(trimmed)) {
                    result.add(SysConf.EXPENSE_STATUS);
                } else if (SysConf.INCOME_STATUS.equals(trimmed) || "收入".equals(trimmed)) {
                    result.add(SysConf.INCOME_STATUS);
                }
            }
        }
        if (result.isEmpty()) {
            result.add(SysConf.EXPENSE_STATUS);
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
        YearMonth ym = validateAndParseBudgetMonth(budgetMonth);
        LocalDate startDate = ym.minusMonths(1).atDay(1);
        LocalDate endDate = ym.atEndOfMonth();
        List<String> list = financeInfoMapper.selectRecentCategories(startDate, endDate, belongTo);
        return list != null ? list : Collections.emptyList();
    }
}
