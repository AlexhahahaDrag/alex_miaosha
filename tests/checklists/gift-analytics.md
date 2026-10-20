# 🎁 人情往来智能洞察报告与互赠收支平衡矩阵测试 Checklist

> **关联项目**：`alex_miaosha_finance`（财务微服务） + `alex_miaosha_front`（PC端） + `alex_miaosha_mobile`（移动端）  
> **关联文档**：`TESTING_STANDARD.md`、`01-Standards/后端微服务开发规则与运维规约.md`、`02-Features/01-Gift-人情礼往/`  
> **最后更新**：2026-10-20  
> **维护责任**：后端研发组 / 前端研发组 / 测试组

---

## 0. 元信息

| 项 | 内容 |
| :--- | :--- |
| **涵盖模块** | 1. **后端**：`alex_miaosha_finance` 下 `com.alex.finance.gift.analysis`（`GiftAnalysisService`、`GiftAnalysisController`）<br>2. **契约**：`finance_api` 下 `GiftPersonBalanceVo`、`GiftReturnRecommendationReq`、`GiftReturnRecommendationVo`<br>3. **前端**：PC 端 `src/views/finance/gift/analysis` + `GiftReturnCalculatorDrawer.vue`、移动端 `src/views/finance/gift` |
| **测试分层配比** | 单元测试 (70%) + 集成测试 (25%) + AI/E2E 流程测试 (5%) |
| **核心算法与业务** | 亲友收礼与随礼双向差额对账（`netBalance = receive - give`）、三大平衡状态（`SURPLUS` 礼金盈余、`DEFICIT` 礼金借出、`BALANCED` 基本持平）、大额单向未回礼失衡预警算法、AI 智能体回礼吉利金额推荐算法（最低体面档、推荐平衡档、热络加码档） |
| **关键基础设施** | `@DataPermission` 数据权限隔离 + `Long2StringSerializer`（雪花 ID 防精度截断） + Knife4j API 文档 |

---

## 1. 核心高价值测试目标与真实代码漏洞防线

| # | 领域 | 规则与漏洞描述 | 必测 Case | 期望结果 |
| :--- | :--- | :--- | :--- | :--- |
| **GIFT-AN-01** | 双向互赠收支对账 | 穿透 `gift_record_info_t` 汇总指定亲友的 `receiveAmount` 与 `giveAmount`，计算 `netBalance`。 | `test_calculate_balance_matrix_accurate_net_amount` | 准确计算收送总额与差额，差额为负表示送多于收，为正表示收多于送。 |
| **GIFT-AN-02** | 状态机判定边界 | 设定 ±200 元内为 `BALANCED`（持平）；超出 +200 为 `SURPLUS`（多收待回）；低于 -200 为 `DEFICIT`（多送待收）。 | `test_balance_status_threshold_transitions` | 临界值 200.00 为 BALANCED，200.01 晋升为 SURPLUS，-200.01 判定为 DEFICIT。 |
| **GIFT-AN-03** | 人情失衡预警算法 | 当亲友净差额 `netBalance >= 800` 且距最近收礼记录已超过 180 天未有互动，触发失衡预警。 | `test_imbalance_alert_trigger_conditions` | 满足大额+长期无互动双条件触发 `isImbalancedAlert = true`，附带具体预警文案。 |
| **GIFT-AN-04** | AI 回礼吉利档位推荐 | 回礼算法基于对方历史最高礼金、事件类型（结婚/满月/乔迁/寿宴等）与亲疏关系，按民间吉利数字向上取整推荐 3 个档位。 | `test_smart_return_gift_recommendation_tiers` | 输出递增的 3 档金额：`minDecentAmount <= recommendedAmount <= generousAmount`，且均为吉利数字（如 500, 600, 800, 888, 1000 等）。 |
| **GIFT-AN-05** | 大数雪花 ID 序列化 | `personId` 等 Long 主键必须通过 `@JsonSerialize(using = Long2StringSerializer.class)` 序列化为字符串。 | `test_person_balance_vo_id_serialization` | 前端拿到字符串类型 ID，杜绝 JS 64 位大数低位截断。 |
| **GIFT-AN-06** | 空数据与零互动防御 | 当亲友暂无往来记录或系统初次初始化时，矩阵返回空安全列表，金额默认兜底 `0.00`。 | `test_balance_matrix_empty_fallback_safety` | 不发生 NPE 崩溃，返回规范的空列表与零默认值。 |

---

## 2. 字段边界（七点法）与异常处理

| 字段 / 场景 | 极小值/空值 | 典型正常值 | 极大值/超长 | 格式边界/非法字符 | 预期处理 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **亲友 ID (`personId`)** | `null` / `0` | `1847192837192837192L` | `Long.MAX_VALUE` | 非数字字符串 `"abc"` | `null` 抛出 `PARAM_ERROR`；合法雪花 ID 转换为 String 传输。 |
| **礼金差额 (`netBalance`)** | `0.00` | `+500.00`、`-800.00` | `±99999999.99` | 包含多位小数 `500.555` | 强制使用 `BigDecimal` 四舍五入保留两位小数。 |
| **事件类型 (`eventType`)** | 空字符串 `""` | `"WEDDING"`、`"FULL_MONTH"` | 未知类型 `"UNKNOWN_EVENT"` | 特殊符号 `"#$$%"` | 未知类型平滑兜底为通用宴请规则，不抛异常。 |
| **推荐回礼金额** | `100.00` | `600.00`、`888.00` | `10000.00` | 负数 `-500` | 推荐金额下限不低于 200 元，金额严格保持正整数/吉利数。 |

---

## 3. 状态机流转矩阵 (Balance Status Transitions)

```
                       [无记录 / 差额 = 0]
                               │
               ┌───────────────┴───────────────┐
               ▼                               ▼
       netBalance > +200              netBalance < -200
               │                               │
               ▼                               ▼
      [SURPLUS (礼金盈余)]             [DEFICIT (礼金借出)]
    (多收待回 / 人情在对方)          (多送待收 / 人情在我方)
               │                               │
               ├─► 超 180 天且额度 ≥ 800       └─► 持续随礼深化
               ▼
     [IMBALANCED (失衡预警)]
      (提示尽早体面回礼)
```

---

## 4. 权限与租户隔离矩阵

| 角色 / 视角 | 互赠平衡矩阵查询权限 | AI 回礼测算权限 | 数据过滤范围 |
| :--- | :--- | :--- | :--- |
| **超级管理员 (super_super)** | ✅ 允许全量查看 | ✅ 允许测算 | 全局无限制 |
| **机构管理员 (org admin)** | ✅ 允许查看本机构 | ✅ 允许测算 | 严格限定在当前机构内用户流向 |
| **普通用户 (user)** | ✅ 允许查看个人 | ✅ 允许测算 | 仅限本人关联的亲友与流水（`user_id = currentUserId`） |

---

## 5. 不测理由清单 (What We Do NOT Test)

1. **ECharts / SVG 底层 Canvas 渲染流水线**：由浏览器图形引擎保障，单测重点关注数据契约与计算正确性；
2. **民间吉利数字的全部民俗学地域细分穷举**：采用当代最广谱通用的 200/500/600/800/888/1000/1200/1688/2000 阶梯，不过度穷举微观方言谐音；
3. **打印机物理硬件通信驱动**：通过前端模拟打印弹窗行为进行功能验证。
