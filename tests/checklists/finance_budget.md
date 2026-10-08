# Finance Budget (零花钱预算) Checklist

> **关联项目**：`alex_miaosha_finance`（后端）+ `alex_miaosha_front`（PC）+ `alex_miaosha_mobile`（移动端）
> **关联文档**：`TESTING_STANDARD.md`、`doc/sql/alex_finance.sql`、`my_alex_brain/02-Features/05-SelfFinance-个人资产财务/TechSpec-零花钱预算与分类消费契约.md`
> **最后更新**：2026-10-08

---

## 0. 元信息

| 项 | 内容 |
| --- | --- |
| 模块名 | finance_budget（零花钱预算与分类消费统计） |
| 后端路径 | `finance_api`：`FinanceBudgetStatusVo`, `FinanceBudgetSaveReq`<br>`finance_boot`：`FinanceBudgetInfo`, `FinanceBudgetInfoService`, `FinanceBudgetInfoController` |
| PC 路径 | `alex_miaosha_front`：`src/views/finance/financeManager/` |
| 移动端路径 | `alex_miaosha_mobile`：`src/views/finance/financeManager/` |
| API | `GET /finance-budget/status?yearMonth={YYYY-MM}&belongTo={userId}`<br>`POST /finance-budget/save` |
| 数据库表 | `finance_budget_info`（含标准 BaseEntity 审计字段及 `uk_belong_month` 唯一索引） |

---

## 1. 字段边界（七点法）

| # | 字段/契约 | 边界/测试场景 | 预期行为 |
| --- | --- | --- | --- |
| F1 | `budgetAmount` | 零值（0.00） | 预算额为0，进度为0%，不触发除零异常（Safe Zero-Division） |
| F2 | `budgetAmount` | 负数（如 -100） | 前端拦截提示 + 后端 Bean Validation 校验拒绝 |
| F3 | `budgetAmount` | 正数超支（消费 1200 > 预算 1000） | `isOverBudget = true`，`remainingAmount = -200`，进度条变红且显示超支状态 |
| F4 | `categoryCodes` | 空集合 / null / 未配置 | 默认统计全部支出（排除内部转账 `type_code = '转账'`） |
| F5 | `categoryCodes` | 单一分类（如 `["餐饮"]`） | 仅累计指定分类的支出，其他类别（如“交通”）不计入零花钱 |
| F6 | `categoryCodes` | 多分类去重与空格（如 `["餐饮", "餐饮 ", "零食"]`） | 逗号拼接去重并规范化为 `餐饮,零食` |
| F7 | `yearMonth` | 格式非法（如 `2026-13` 或空串） | 后端严格校验拦截，抛出友好提示 |

---

## 2. 状态机与继承逻辑（0-Cron Job Fallback）

| 场景 | 状态演进 | 期望结果 |
| --- | --- | --- |
| 首次使用（无任何配置） | 当前月无记录 & 历史月无记录 | 返回默认 0 预算，`isInherited = false` |
| 次月继承（Ponytail 查询回溯） | 上月设为 2000，当月未单独设置 | 查询自动回溯最近月记录，返回 2000，`isInherited = true` |
| 当月个性化修改 | 当月保存新预算 3000 | 仅更新/插入当月快照，历史月份数据不受任何影响，`isInherited = false` |
| 隔月跳跃继承 | 2026-05 设置，2026-06/07 未设置，2026-08 查询 | 2026-08 自动向上继承 2026-05 的配置 |

---

## 3. 权限与隔离矩阵

| Persona | 当前用户自身预算 | 跨用户查询与设置 | 期望行为 |
| --- | --- | --- | --- |
| 普通用户 A | 可读写归属于 A 的零花钱配置 | 传入 B 的 belongTo | 隔离保护，默认限定当前登录上下文或所属归属人 |
| 超级管理员 | 可为指定用户设定/查询预算 | 指定任意有效 `belongTo` | 正确读取/保存指定用户预算状态 |

---

## 4. 不测理由与风险收敛 (Why We Don't Test)

1. **为什么不需要定时任务测试？**
   - 本方案遵循 **Ponytail** 极简原则，坚决不引入每月1号跑批的 Quartz/XXL-JOB 定时任务。
   - 所有继承行为在读链路通过 `selectLatestBefore` 回溯解决，写链路只持久化实际修改月，无跨月漏跑、补跑数据不一致风险。
2. **为什么不需要单独的 category 多对多中间表测试？**
   - 类别码为轻量配置，以逗号分隔存储于 `category_codes` 字段并传参 `typeCodes IN (...)`，避免多表关联连接开销。

---

## 5. PC 端 UI 布局与视口自适应 (UI Layout Standard)

- **一体化概览卡片 (`finance-overview-card`)**：
  - PC 端日常记账页面已将原先上下独立堆叠的“全量账单统计”和“零花钱预算”两条横幅（原高 ~100px）合并为单行左右一体化卡片（高 ~68px）；
  - 左侧展示账单收支统计（总支出、总收入、净结余、笔数），右侧展示零花钱预算（剩余/超支核心值、上限/已用微指标、进度条、分类标签、调整预算快捷入口）；
  - 纵向节省约 32px+ 高度直接反哺下方账单表格明细（Table），表格可视行数与首屏空间显著扩充；
  - 宽屏（>=1300px）并排呈现，窄屏（<1300px）通过 CSS 弹性自动折行降级。

---

## 6. 零花钱预算弹窗 Tailwind 风格与动态类别提取规范

- **Tailwind 现代风格弹窗**：
  - 弹窗采用卡片式分层架构，内部分为基础额度设置卡片（`bg-slate-50/80 rounded-xl`）与消费分类配置卡片（`rounded-xl border border-slate-200`）；
  - 分类选择全面采用可交互胶囊 Chip（未选中为极简白底灰边，选中为高亮浅蓝底并带指示圆点与微阴影），视觉与交互现代、通透。
- **近两月已有类别动态提取**：
  - 取消硬编码的静态预设分类列表；
  - 通过专属接口 `GET /finance-budget/categories?yearMonth={yearMonth}&belongTo={belongTo}`，动态提取当前预算月「上月 1 号至本月末」内真实入账的非空有效 `type_code`（排除转账）；
  - 保留「支出、收入」大类 Chip，支持一键「全选 / 清空」快捷操作；
  - 已选类别安全兼容，即便历史选中的个性化类别在近两月无流水也不遗漏。
