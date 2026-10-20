# Checklist: Finance Budget Lifecycle, Trend Calculation & Member Permission (第三十三阶段)

**Module:** `alex_miaosha_finance` (`finance_boot` & `finance_api`)  
**Date:** 2026-10-20  
**Standard:** `TESTING_STANDARD.md` (Level 2 Integration Tests)

---

## 1. 零花钱预算七点法边界与生命周期状态机矩阵

| # | 测试项 | 覆盖类 / 方法 | 验证断言 | 状态 |
|---|---|---|---|:---:|
| 1 | 非法月份格式前置拦截 | `FinanceBudgetLifecycleAndBoundaryIT#testInvalidMonthFormatThrowsFinanceException` | 非法月份（如 `2026-13`、`2026/10`、`invalid-month`）抛出 `FinanceException.param` (400)，杜绝底层 `DateTimeParseException` 导致的 500 异常 | ✅ PASS |
| 2 | 负数预算金额防御性拦截 | `FinanceBudgetLifecycleAndBoundaryIT#testNegativeBudgetAmountThrowsFinanceException` | 保存预算金额为负数（`-100.00`）时强制拦截，断言不触发数据库插入或更新 | ✅ PASS |
| 3 | 零预算除零安全 | `FinanceBudgetLifecycleAndBoundaryIT#testZeroBudgetAmountSafeZeroDivision` | 预算额为 0.00 时除零安全，使用率恒定为 `0.0%`，`isOverBudget = false`，杜绝前端爆红 | ✅ PASS |
| 4 | 超支状态机准确计算 | `FinanceBudgetLifecycleAndBoundaryIT#testOverBudgetStatusCalculation` | 实际消费 1500 > 预算 1000 时，`isOverBudget = true`，剩余额为 `-500.00`，使用率 `150.0%` | ✅ PASS |
| 5 | 历史月预算回溯继承 | `FinanceBudgetLifecycleAndBoundaryIT#testInheritedBudgetFromHistoryMonth` | 当月未配置时自动向上回溯最近历史月份，`isInherited = true`，继承预算额与分类字典 | ✅ PASS |
| 6 | 脏数据分类清洗自愈 | `FinanceBudgetLifecycleAndBoundaryIT#testCategoryCodesCleaningOnSave` | 保存时自动剥离"支出"、"收入"等关键字并去除重复与空格，存储规范格式 `餐饮,交通,娱乐` | ✅ PASS |

---

## 2. 财务分析环比/同比趋势与边界容错矩阵

| # | 测试项 | 覆盖类 / 方法 | 验证断言 | 状态 |
|---|---|---|---|:---:|
| 7 | 1月份跨年 MoM / YoY 回溯 | `FinanceAnalysisTrendCalculationIT#testBalanceCalculation_JanuaryCrossYearMomCalculation` | 1月份查询时 MoM 自动跨年计算为上年 12 月（`2025-12`），YoY 计算为上年 1 月（`2025-01`），无日期溢出崩溃 | ✅ PASS |
| 8 | 基期为 0 趋势计算安全 | `FinanceAnalysisTrendCalculationIT#testTrendCalculation_ZeroPreviousAmountSafeZeroDivision` | 基期金额为 0 时安全输出 `"+100%"`，杜绝 `ArithmeticException` 或 NaN | ✅ PASS |
| 9 | 人员 ID 参数规范化 | `FinanceAnalysisTrendCalculationIT#testNormalizeBelongTo_PassesNullToMapper` | `belongTo <= 0` 时安全归一化为 null 传入 Mapper，执行家庭全员流水汇总 | ✅ PASS |
| 10 | 收支明细与天/月花费统计 | `FinanceAnalysisTrendCalculationIT#testDayAndMonthExpenseDelegation` | 透传参数由 Mapper 精确处理收支方向，天花费与月花费数据结构高保真 | ✅ PASS |

---

## 3. 财务流水跨家庭成员垂直数据权限隔离矩阵

| # | 测试项 | 覆盖类 / 方法 | 验证断言 | 状态 |
|---|---|---|---|:---:|
| 11 | 普通成员修改他人流水越权拦截 | `FinanceInfoCrossMemberPermissionIT#testUpdateFinanceInfo_CrossMemberModificationBlocked` | 普通成员尝试修改非本人流水时拦截，抛出 `FinanceException.forbidden`，禁止持久化更新 | ✅ PASS |
| 12 | 批量删除包含他人流水安全拦截 | `FinanceInfoCrossMemberPermissionIT#testDeleteFinanceInfo_CrossMemberBatchDeletionBlocked` | 批量删除 ID 列表中只要包含他人流水，全量拦截拒绝并抛出 `FinanceException.forbidden` | ✅ PASS |
| 13 | 普通成员修改自身流水放行 | `FinanceInfoCrossMemberPermissionIT#testUpdateFinanceInfo_OwnRecordAllowed` | 修改自身归属流水正常放行并调用持久化更新 | ✅ PASS |
| 14 | 管理员跨成员管理放行 | `FinanceInfoCrossMemberPermissionIT#testUpdateFinanceInfo_AdminCanManageOtherMembers` | 拥有 admin / super 角色的管理员可跨成员修改并管理全员流水 | ✅ PASS |
| 15 | 新增流水自动归属当前人员 | `FinanceInfoCrossMemberPermissionIT#testAddFinanceInfo_AutoFillsCurrentLoginUserId` | 未指定 `belongTo` 时自动绑定当前登录人员 ID 为流水归属人 | ✅ PASS |

---

## 4. 不测理由

1. **真实物理 MySQL 数据库底层事务隔离级别与磁盘 IO**：测试聚焦于 MyBatis-Plus 业务逻辑、七点法字段边界、状态机演进与权限拦截；物理数据库持久化与死锁恢复由基础设施运维保障；
2. **秒杀模块排除**：遵循用户明确硬指令“秒杀的东西都先不做”，本次测试严格排除秒杀相关接口与业务。
