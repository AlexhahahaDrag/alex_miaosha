# Gift（礼尚往来）模块测试 Checklist

> **关联项目**：`alex_miaosha_finance`（后端） + `alex_miaosha_front`（PC） + `alex_miaosha_mobile`（移动端）
> **关联文档**：根目录 `TESTING_STANDARD.md`
> **样板属性**：本文件作为**所有功能 checklist 的样板**，其他功能 copy 本文件改即可
> **最后更新**：2026-05-28

---

## 0. 元信息

| 项 | 内容 |
|---|---|
| 模块名 | gift（礼尚往来） |
| 业务负责人 | @alex |
| 测试负责人 | @alex |
| 关联需求 | `FEATURE.md#gift`、`feature.md#gift` |
| 关联代码（后端） | `alex_miaosha_finance/finance_api/src/main/java/com/alex/api/finance/personalGift/`（VO 层已有，实现待补） |
| 关联代码（PC） | `src/views/finance/gift/`（`config.ts` / `api/index.ts` / `dashboard|person|event|record|analysis/index.vue`） |
| 关联代码（移动） | `src/views/finance/gift/`（同 PC 结构 + `components/GiftRecordCard.vue`） |
| 关联 graphify 节点 | 待 `graphify query "gift module"` 后填写 |
| 后端 API 前缀 | `/gift-person-info-t` / `/gift-event-info-t` / `/gift-record-info-t` / `/gift-analysis` |

---

## 1. 业务实体与字段边界分析

### 1.1 GiftPersonInfo（亲友）

| 字段 | 类型 | 合法范围 | 必测边界值 | 边界依据 |
|---|---|---|---|---|
| `id` | String（前端）/ Long（后端） | 主键 | `null`、`"0"`、`"-1"`、不存在、超过 `Long.MAX_SAFE_INTEGER` 的字符串 | AGENTS.md 约束：前端 string，后端 Long |
| `personName` | String | 1-50 字 | `null`、`""`、`" "`、`"张"`、50 字、51 字、emoji `"张🎁"`、SQL 注入 `"'; DROP--"` | DB `VARCHAR(50)` |
| `phone` | String | 11 位手机号或固话 | `null`、`""`、`"1"`、`"13800000000"`、`"99999999999"`（非法前缀）、`"+86-138-0000-0000"`（含分隔符） | 业务正则 |
| `relationType` | String | 字典枚举 | `null`、合法值、`"INVALID"`、`""` | 字典表 |
| `remark` | String | 0-500 字 | `null`、`""`、500 字、501 字、含换行、含 emoji | DB `VARCHAR(500)` |

### 1.2 GiftEventInfo（事由）

| 字段 | 类型 | 合法范围 | 必测边界值 | 边界依据 |
|---|---|---|---|---|
| `id` | String/Long | 主键 | 同 GiftPersonInfo | — |
| `eventName` | String | 1-100 字 | `null`、`""`、1 字、100 字、101 字、含 emoji | DB |
| `eventType` | String | 字典枚举 | 所有合法值 + `null` + 非法 | 字典 |
| `eventTime` | LocalDateTime | 业务时间 | `null`、`1970-01-01`、未来时间、`9999-12-31`、闰年 `2024-02-29`、跨时区 | 业务 |
| `hostPersonId` | String/Long | 关联 GiftPersonInfo | `null`、`"0"`、不存在的 ID、跨机构 ID | 外键 |

### 1.3 GiftRecordInfo（礼金记录，最核心）

| 字段 | 类型 | 合法范围 | 必测边界值 | 边界依据 |
|---|---|---|---|---|
| `id` | String/Long | 主键 | 同 GiftPersonInfo | — |
| `eventId` | String/Long | 关联事由 | `null`（部分场景允许）、不存在、跨机构 | 外键 |
| `giverPersonId` | String/Long | 送礼人 | `null`、`"0"`、不存在 | 外键 |
| `receiverPersonId` | String/Long | 收礼人 | `null`、`"0"`、不存在 | 外键 |
| `relatedRecordId` | String/Long | 自关联（回礼场景） | `null`、自己关联自己（**非法**）、关联非 GIVE 记录（**非法**）、循环关联 A→B→A（**非法**） | 业务规则 |
| `direction` | enum | `GIVE` / `RECEIVE` / `RETURN` | 三值各一 + `null` + `"INVALID"` | 业务枚举 |
| `amount` | BigDecimal | 0.01 ~ 9999999.99 | **七点法见下** | DB `DECIMAL(12,2)` |
| `payTime` | LocalDateTime | 业务时间 | 同 eventTime | — |
| `returnedFlag` | Integer | 0 / 1 | 0、1、`null`、`2`（非法） | 业务枚举 |
| `remark` | String | 0-500 字 | 同 GiftPersonInfo.remark | — |

### 1.4 `amount` 七点法（核心精度边界）

| 点 | 值 | 期望 | 测试位置 |
|---|---|---|---|
| far-low | `-99999` | 后端拒绝 + 前端校验失败 | 后端单测 + 前端单测 |
| min-1 | `-0.01` | 拒绝 | 同上 |
| min | `0` | **业务决定**：是否允许 0 元礼？（建议拒绝） | 业务确认 |
| min+1 | `0.01` | 通过，且**精度无丢失** | 全栈 |
| typical | `200`、`888`、`1314` | 通过 | 全栈 |
| max | `9999999.99` | 通过，精度无丢失 | 全栈 |
| max+1 | `10000000` | 后端拒绝（DECIMAL(12,2) 上限） | 后端单测 |
| precision_overflow | `0.001` | 后端拒绝或截断 | 后端单测 |
| **JS 精度坑** | `Number.MAX_SAFE_INTEGER + 1` 的字符串 | 前端 normalizeGiftIds 转 string 不丢精度 | **前端单测必测** |

---

## 2. 状态机分析

### 2.1 `direction` × `returnedFlag` 状态机

```
   ┌─────┐   送礼   ┌─────────┐   标记已回   ┌─────────┐
   │ 新建 │ ───────→│ GIVE    │ ──────────→│ GIVE     │
   └─────┘          │ flag=0  │            │ flag=1   │
                    └─────────┘            └──────────┘
                                                  │
                                                  │ 自动创建关联 record
                                                  ▼
                                          ┌──────────────┐
                                          │ RETURN       │
                                          │ relatedId=⬆ │
                                          └──────────────┘

   ┌─────┐   收礼   ┌─────────┐
   │ 新建 │ ───────→│ RECEIVE │ ──── ✗ 不能直接 mark-returned
   └─────┘          │ flag=0  │       （要新建一条 GIVE 作为"回礼"）
                    └─────────┘
```

### 2.2 必测的合法转换

| # | 转换 | 期望 |
|---|---|---|
| L1 | `null → GIVE(flag=0)` | 通过 |
| L2 | `null → RECEIVE(flag=0)` | 通过 |
| L3 | `GIVE(flag=0) → GIVE(flag=1)` | 通过，自动创建 RETURN record |
| L4 | 自动创建的 `RETURN` 有正确的 `relatedRecordId` | 必校验 |
| L5 | 多次 mark-returned 但前者已回礼 | 幂等 |

### 2.3 必测的非法转换

| # | 转换 | 期望 |
|---|---|---|
| I1 | `RECEIVE → mark-returned` | 抛业务异常 |
| I2 | `RETURN → mark-returned` | 抛业务异常 |
| I3 | `GIVE(flag=1) → mark-returned` 再次 | 幂等（不重复创建 RETURN）|
| I4 | 标记一笔不属于自己的 GIVE | 权限拒绝 |
| I5 | `relatedRecordId` 关联自己 | 抛业务异常 |
| I6 | `relatedRecordId` 关联非 GIVE 的记录 | 抛业务异常 |
| I7 | `direction=RETURN` 但 `relatedRecordId=null` | 抛业务异常 |

---

## 3. 权限矩阵

数据权限通过 `@DataPermission` + `DataPermissionHandlerImpl` 实现。

| 操作 | 超管（super_super） | 机构管理员（admin） | 普通用户（self） | 普通用户（other） |
|---|---|---|---|---|
| 创建 Person | ✅ 所有机构 | ✅ 本机构 | ✅ 仅自己 | ❌ |
| 查看 Person | ✅ 所有 | ✅ 本机构 | ✅ 仅自己 | ❌ |
| 修改 Person | ✅ 所有 | ✅ 本机构 | ✅ 仅自己 | ❌ |
| 删除 Person | ✅ 所有 | ✅ 本机构 | ✅ 仅自己 | ❌ |
| 创建 Event | ✅ | ✅ | ✅ | ❌ |
| 查看 Event | ✅ 所有 | ✅ 本机构 | ✅ 仅自己 | ❌ |
| 创建 Record | ✅ | ✅ | ✅ | ❌ |
| 查看 Record | ✅ 所有 | ✅ 本机构 | ✅ 仅自己 | ❌ |
| **mark-returned** | ✅ | ✅ 本机构内 | ✅ 仅自己 | ❌ |
| **导出 Excel** | ✅ | ✅ | ❌（按钮不显示） | ❌ |
| 查看 analysis | ✅ 全局聚合 | ✅ 本机构聚合 | ✅ 仅自己聚合 | ❌ |

**前端按钮显隐对应**（已有 case 覆盖，需补全）：

| 按钮 | super | admin | self |
|---|---|---|---|
| `gift-record-btn-export` | ✓ | ✓ | ✗ |
| `gift-person-btn-export` | ✓ | ✓ | ✗ |
| `gift-analysis-btn-export` | ✓ | ✓ | ✗ |
| `gift-analysis-btn-print` | ✓ | ✓ | ✗ |

---

## 4. 测试用例规划

按金字塔分层，每个 ☐ 必填，完成后改 ☑。

### 4.1 后端单元测试（Vitest 不适用 → JUnit + Mockito）

- [ ] `GiftRecordServiceImplTest`
    - [ ] `should_validate_amount_boundary`（@ParameterizedTest 七点法）
    - [ ] `should_reject_when_amount_negative`
    - [ ] `should_reject_when_amount_exceeds_max`
    - [ ] `should_truncate_or_reject_precision_overflow`
- [ ] `GiftRecordStateMachineTest`
    - [ ] `give_can_be_marked_returned` (L3)
    - [ ] `receive_cannot_be_marked_returned` (I1)
    - [ ] `return_cannot_be_marked_returned` (I2)
    - [ ] `mark_returned_is_idempotent` (I3, L5)
    - [ ] `return_record_has_correct_related_id` (L4)
- [ ] `GiftRecordRelationConstraintTest`
    - [ ] `cannot_relate_to_self` (I5)
    - [ ] `cannot_relate_to_non_give_record` (I6)
    - [ ] `return_must_have_related_id` (I7)
- [ ] `GiftPersonServiceImplTest`
    - [ ] `personName` 字符串七点法
    - [ ] `phone` 格式校验
- [ ] `GiftAnalysisServiceImplTest`
    - [ ] 聚合金额正确性：Σ(GIVE) - Σ(RETURN 已抵扣)
    - [ ] 跨年统计正确
    - [ ] 时区处理正确（用户在 UTC+8 提交，统计按 UTC+8 分桶）

### 4.2 后端集成测试（MockMvc + @SpringBootTest）

- [ ] `GiftRecordControllerIT`
    - [ ] `POST /gift-record-info-t` 各种入参组合
    - [ ] `PUT /gift-record-info-t/mark-returned` 流程
    - [ ] `GET /gift-record-info-t/pending-return-amount` 返回值精度
- [ ] `GiftPermissionIT`（数据权限矩阵，12 个 case）
    - [ ] 4 身份 × 3 操作（查看/创建/删除）
- [ ] `GiftConcurrencyIT`
    - [ ] 并发标记同一笔已回礼（乐观锁/幂等）
    - [ ] 并发删除 + 查询竞态

### 4.3 PC 前端单元测试（Vitest + @vue/test-utils）

- [ ] `src/views/finance/gift/api/index.spec.ts`
    - [ ] `normalizeGiftIds_should_convert_bigint_to_string`
    - [ ] `normalizeGiftIds_should_recursively_process_nested`
    - [ ] `normalizeGiftIds_should_keep_non_id_fields`
- [ ] `src/views/finance/gift/config.spec.ts`
    - [ ] `formatMoney` 精度边界
    - [ ] `directionText` 枚举映射
    - [ ] `directionClass` CSS class 映射
- [ ] `GiftRecordCard.spec.ts`（如果有共享组件）
    - [ ] Loading / Empty / Error / Success 四态

### 4.4 PC 前端 E2E（Playwright + Midscene，已有部分）

- [x] `GIFT-PC-PERSON-CRUD-001`（已有）
- [x] `GIFT-PC-EVENT-CRUD-001`（已有）
- [x] `GIFT-PC-RECORD-CRUD-001`（已有）
- [x] `GIFT-PC-EXPORT-001`（已有）
- [x] `GIFT-BUTTON-*-ADMIN/USER-001` 系列（已有）
- [ ] `GIFT-FLOW-RETURN-001`（**新增**：GIVE → mark-returned → 验证 RETURN 自动创建 → analysis 待回礼 -1）
- [ ] `GIFT-FLOW-PRECISION-001`（**新增**：amount=`9999999.99` 创建 → 列表显示无精度丢失）
- [ ] `GIFT-FLOW-PERMISSION-001`（**新增**：manager 创建 record → readonly 切换身份可见行但无详情按钮）
- [ ] `GIFT-FLOW-CONCURRENT-MARK-001`（**新增**：两 tab 同时 mark-returned，只生成一条 RETURN）

### 4.5 移动端 E2E（已有 3 个，扩到 10+）

- [x] `GIFT-MOBILE-001`（页面加载顺序）
- [x] `GIFT-MOBILE-002`（快速记礼弹窗 + Haptic）
- [x] `GIFT-MOBILE-003`（列表、空状态、刷新加载）
- [ ] `GIFT-MOBILE-RECORD-CRUD-001`（移动端 CRUD 闭环）
- [ ] `GIFT-MOBILE-GESTURE-PULL-REFRESH-001`（下拉刷新七点法）
- [ ] `GIFT-MOBILE-GESTURE-INFINITE-SCROLL-001`（上拉加载边界）
- [ ] `GIFT-MOBILE-GESTURE-SWIPE-DELETE-001`（左滑删除）
- [ ] `GIFT-MOBILE-OFFLINE-001`（断网时降级提示 + 本地缓存）
- [ ] `GIFT-MOBILE-LARGE-LIST-001`（1000 条 mock 验证虚拟滚动）
- [ ] `GIFT-MOBILE-VIEWPORT-SE-001`（iPhone SE 320px 不溢出）
- [ ] `GIFT-MOBILE-PERF-LCP-001`（首屏 LCP < 2.5s）
- [ ] `GIFT-MOBILE-HAPTIC-COVERAGE-001`（关键操作均触发 vibrate）

---

## 5. 不测理由（必填，防漏测）

| 项 | 不测理由 | 由谁兜底 |
|---|---|---|
| Vant 组件本身的交互（如 `<van-popup>` 关闭动画） | 第三方库自测 | Vant 团队 |
| Ant Design Vue 组件本身 | 同上 | AntV 团队 |
| MyBatis-Plus 分页插件本身 | 同上 | Baomidou 团队 |
| 微服务 Nacos 注册中心连接 | 由基础设施保障 | 运维 |
| Redis 网络层 | 由基础设施保障 | 运维 |
| **PNG 图片本身像素**（如默认头像） | 视觉回归成本高于价值 | 手工肉眼 |

---

## 6. 覆盖率目标

| 范围 | Line | Branch | Mutation Score | 备注 |
|---|---|---|---|---|
| 后端 `giftRecord/service/` | ≥ 80% | ≥ 70% | ≥ 70% | 核心业务 |
| 后端 `giftPerson/service/` | ≥ 75% | ≥ 65% | ≥ 65% | |
| 后端 `giftEvent/service/` | ≥ 75% | ≥ 65% | ≥ 65% | |
| 后端 `giftAnalysis/service/` | ≥ 70% | ≥ 60% | ≥ 60% | 聚合计算 |
| 前端 PC `views/finance/gift/api/` | ≥ 85% | ≥ 70% | — | 含 normalizeGiftIds |
| 前端 PC `views/finance/gift/config.ts` | ≥ 80% | ≥ 70% | — | 工具函数 |
| 前端移动 同上 | 同上 | 同上 | — | |

---

## 7. 性能基线（移动端必填）

| 指标 | 基线 | 告警阈值 |
|---|---|---|
| Gift Dashboard 首屏 LCP | < 1.5s | > 2.5s |
| Gift Record 列表加载（10 条） | < 800ms | > 1.5s |
| 快速记礼弹窗打开 | < 200ms | > 500ms |
| 大列表（1000 条）滚动 FPS | > 50 | < 30 |

---

## 8. 测试数据准备

### 8.1 Fixture 用户

| persona | 角色 | 测试 ID（建议） |
|---|---|---|
| `super_super` | 超管 | 通过 `RBAC_SUPER_USER` 环境变量 |
| `rbac_user_manager` | 机构管理员 | `RBAC_MANAGER_USER` |
| `rbac_readonly` | 普通用户 | `RBAC_READONLY_USER` |
| `cross_org_user` | 跨机构对照用户（**新增**） | `RBAC_CROSS_ORG_USER` |

### 8.2 测试数据隔离策略

- 所有创建的数据**必须**用 `${Date.now()}` + 前缀（如 `codex-pc-record-`）做唯一标识
- 测试结束必须在 `finally` 中通过 API 删除
- 永远不要直接操作 dev DB
- CI 环境用 testcontainers 起独立 MySQL/Redis

### 8.3 大数据集准备

`GIFT-MOBILE-LARGE-LIST-001` 需要 1000 条数据：

- 方案 A：测试前调用 `/test-helper/gift-record/seed?count=1000`（仅 dev/test 环境）
- 方案 B：Playwright 拦截分页 API，返回 mock 数据（不污染 DB）

推荐方案 B。

---

## 9. 进度跟踪

| 阶段 | 状态 | 完成日期 |
|---|---|---|
| Checklist 编制完成 | ✅ | 2026-05-28 |
| 字段边界分析评审 | ☐ | |
| 后端单测启动 | ☐ | |
| 后端覆盖率达 70% | ☐ | |
| 前端单测启动 | ☐ | |
| 前端覆盖率达 70% | ☐ | |
| AI 测试 case 全部完成 | ☐ | |
| CI 接入 | ☐ | |
| **样板完工**（其他模块可 copy） | ☐ | |

---

## 10. 修订记录

| 版本 | 日期 | 修改人 | 内容 |
|---|---|---|---|
| v1.0 | 2026-05-28 | alex | 首版，作为其他功能 checklist 的样板 |
