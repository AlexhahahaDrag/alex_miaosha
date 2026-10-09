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
| API | `GET /finance-budget/status?budgetMonth={YYYY-MM}&belongTo={userId}`<br>`POST /finance-budget/save` |
| 数据库表 | `finance_budget_info`（列名 `budget_month`，含标准 BaseEntity 审计字段及唯一索引） |

---

## 1. 字段边界（七点法）

| # | 字段/契约 | 边界/测试场景 | 预期行为 |
| --- | --- | --- | --- |
| F1 | `budgetAmount` | 零值（0.00） | 预算额为0，进度为0%，不触发除零异常（Safe Zero-Division） |
| F2 | `budgetAmount` | 负数（如 -100） | 前端拦截提示 + 后端 Bean Validation 校验拒绝 |
| F3 | `budgetAmount` | 正数超支（消费 1200 > 预算 1000） | `isOverBudget = true`，`remainingAmount = -200`，进度条变红且显示超支状态 |
| F4 | `categoryCodes` | 空集合 / null / 未配置 | 默认统计全部支出（或收入）（排除内部转账 `type_code = '转账'`） |
| F5 | `categoryCodes` | 单一分类（如 `["餐饮"]`） | 仅累计指定分类的支出，其他类别（如“交通”）不计入零花钱 |
| F6 | `categoryCodes` | 多分类去重与空格（如 `["餐饮", "餐饮 ", "零食"]`） | 逗号拼接去重并规范化为 `餐饮,零食` |
| F7 | `budgetMonth` | 格式非法（如 `2026-13` 或空串） | 后端严格校验拦截，抛出友好提示（兼顾兼容老参数 `yearMonth`） |
| F8 | `incomeAndExpenses` | `expense` / `income` / `expense,income` | 独立字段承载收支方向，支持单选（仅支出/仅收入）与多选（支出+收入），多选时求和（`totalExpense + totalIncome`） |
| F9 | `categoryCodes` 脏数据自愈 | 包含 `"支出"`、`"收入"`、`"expense"`、`"income"` | 读写链路自动清洗剥离，避免误匹配类别导致有效消费漏算为 0 |

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
  - 动态计算微指标标签：`已用` (仅支出)、`已入` (仅收入)、`已计` (支出+收入)；未选类别时微标签自适应 `全部支出` / `全部收入` / `全部收支`；
  - 宽屏（>=1300px）并排呈现，窄屏（<1300px）通过 CSS 弹性自动折行降级。

---

## 6. 零花钱预算弹窗 Tailwind 双卡片分层与收支多选规范

- **Tailwind 双卡片分层设计**：
  - 卡片 1（额度）：`bg-slate-50/80 rounded-xl p-4 border border-solid border-slate-200/80`，月份徽标 + 额度输入框；
  - 卡片 2（范围与分类）：`bg-slate-50/40 rounded-xl p-4 border border-solid border-slate-200`，收支类型与业务分类一体化配置；
  - 修复 `<div>` 标签在部分浏览器无 preflight 下边框缺失问题（显式声明 `border-solid`），统一精致圆角药丸 Chip；
  - 选中态：`bg-blue-50 border-blue-500 text-blue-600 shadow-xs ring-1 ring-blue-500/20`，左侧带小圆点指示，悬浮态带渐变浅蓝反馈。
- **收支类型多选交互**：
  - 支出与收入不再互斥，支持自由单选或同时勾选；
  - 至少保留一项的防呆保护；
  - 弹窗动态提示文案根据所选收支方向与分类数量精准变化。

---

## 7. 移动端 UI 交互与触控胶囊规范 (Mobile Haptic & Chip Standard)

- **移动端触控胶囊 (Touch Chip Group)**：
  - 调整预算弹窗采用原生移动端适配的圆角胶囊药丸（Chip）形态替代传统复选框列表，支持轻触快速高亮切换；
  - 激活态提供微缩放（`:active transform: scale(0.96)`）与轻微触感震动反馈（`navigator?.vibrate?.(10)`），符合移动端触控直觉；
  - 类别按「收支类型」大类 Chip 与「动态提取账目类别」分块排列，动态提取类别池超长时限制高度并支持纵向滚动；
  - 提供快捷「全选 / 清空」按钮以及动态已选计数 badge（如 `(已选 3 项)`）。
- **零花钱卡片呈现**：
  - 保留独立折叠/展开预算微概览卡片，清晰呈现三列指标（月预算、本月消费、剩余可用/超支警告）；
  - 动态展示当前纳入统计的分类标签（收支大类与个性化类别），未配置时显示“全部支出”。

---

## 8. PC 端财务信息页交互升级规范 (Task-Skill & Impact-Table P0)

- **快速记账与连续记账 (`finance-manager-detail/index.vue`)**：
  - [x] 宽度调优：680px 适度紧凑视界，减少留白与认知漂移；
  - [x] 类别药丸快速点选：从近期两月记账提取的类别药丸，点击 1 步填入 `typeCode`；
  - [x] 智能环境预填：默认带入当前登录用户、微信支付（`wx`）、当前时间（`dayjs()`）、支出（`expense`）及有效（`1`）；
  - [x] 连续记录支持：底部提供「保存并再记一笔」按钮，保存成功后保留环境预设，清空名称与金额继续输入。
- **预算卡片 ➔ 账单明细一键联动穿透 (`index.vue`)**：
  - [x] 点击预算卡片分类标签（`cat-pill`）或当月已用金额（`clickable-sub-stat`），自动联动下方表格筛选本月该范围账单；
  - [x] 表格上方展示交互式激活横幅 `linkage-active-banner`，并支持「清除联动，查看全量」；
  - [x] 触发重置时自动清空联动状态。
- **快捷周期置顶 (`finance-manager-filter/index.vue`)**：
  - [x] 筛选栏置顶快捷胶囊：`[本月]` `[上月]` `[近30天]` `[全部]`；
  - [x] 1 击即时切换并触发列表与汇总数据查询；
  - [x] 联动外部传参或自定义时间段平滑切换。
- **表格视觉细节升级 (`index.vue`)**：
  - [x] 类别列使用确定性哈希配色胶囊展示（`category-pill`），并支持点击类别胶囊快捷过滤；
  - [x] 金额列与收支类型保持清晰语义对比。

