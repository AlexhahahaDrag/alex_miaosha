package com.alex.finance.finance.service;

import com.alex.api.finance.vo.finance.FinanceBudgetSaveReq;
import com.alex.api.finance.vo.finance.FinanceBudgetStatusVo;
import com.alex.finance.finance.entity.FinanceBudgetInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * description: 月度零花钱预算服务接口
 * author: alex
 * createDate: 2026-10-08
 * version: 1.0.0
 */
public interface FinanceBudgetInfoService extends IService<FinanceBudgetInfo> {

    /**
     * 获取指定月份的零花钱预算与消费实时统计状态
     * @param budgetMonth 预算月份 (如 2026-10)，为空默认当月
     * @param orgId 家庭组/机构ID (为空自动从登录人解析)
     * @param belongTo 归属用户ID (可选，用于过滤单人实际支出)
     * @return 零花钱预算状态
     */
    FinanceBudgetStatusVo getMonthlyBudgetStatus(String budgetMonth, Long orgId, Long belongTo);

    /**
     * 兼容重载
     */
    default FinanceBudgetStatusVo getMonthlyBudgetStatus(String budgetMonth, Long belongTo) {
        return getMonthlyBudgetStatus(budgetMonth, null, belongTo);
    }

    /**
     * 保存或更新指定月份的零花钱预算及分类配置
     * @param req 保存参数
     * @return 是否成功
     */
    Boolean saveMonthlyBudget(FinanceBudgetSaveReq req);

    /**
     * 获取上月及当月已有记账分类列表
     * @param budgetMonth 预算月份
     * @param belongTo 归属用户ID
     * @return 类别名称列表
     */
    List<String> getRecentCategories(String budgetMonth, Long belongTo);
}
