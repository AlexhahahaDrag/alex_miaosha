# Gift（礼尚往来）模块测试 Checklist

> **关联项目**：`alex_miaosha_finance`（后端） + `alex_miaosha_front`（PC） + `alex_miaosha_mobile`（移动端）
> **关联文档**：根目录 `TESTING_STANDARD.md`
> **基于代码版本**：分支 `codex/gift-management-module`（2026-05-28 扫读）
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
| 后端代码路径 | `alex_miaosha_finance/finance_boot/src/main/java/com/alex/finance/gift/` |
| 后端 VO 路径 | `alex_miaosha_finance/finance_api/src/main/java/com/alex/api/finance/gift/` |
| 后端 4 个子模块 | `record` / `person` / `event` / `relation` + `analysis`（聚合） |
| PC 代码路径 | `src/views/finance/gift/`（`config.ts` + `api/index.ts` + `dashboard\|person\|event\|record\|analysis/index.vue`） |
| 移动代码路径 | `src/views/finance/gift/` + `components/GiftRecordCard.vue` |
| 后端 API 前缀 | `/gift-record-info-t` `/gift-person-info-t` `/gift-event-info-t` `/gift-relation-info-t` `/gift-analysis` |
| 数据库表 | `gift_record_info_t` `gift_person_info_t` `gift_event_info_t` `gift_relation_info_t` |
| 关键依赖 | `UserUtils.getLoginUser()`（注入登录态） + `Long2StringSerializer`（ID 序列化） |

---

## 1. 当前实现已发现的"高价值测试目标"（真实代码漏洞清单）

> 这一节是**本次扫读后端代码时发现的真实问题**，每一条都对应明确的测试用例。
> 测试组写完这些 case 后，能直接驱动业务方修复，而不是只验证"happy path 能跑"。

### 🔴 P0 · 安全 / 数据权限漏洞

| # | 位置 | 漏洞描述 | 必测 case | 期望结果 |
|---|---|---|---|---|
| H1 | `GiftRecordInfoTMapper.getPage` 标注了 `@DataPermission`，但 `ServiceImp.getPage` 调用的是 IService 默认 `page(Page, Wrapper)`，**根本不走自定义 mapper 方法** | `@DataPermission` 注解形同虚设 | `should_filter_records_by_org_for_admin_role` | 当前**会失败**，暴露问题 |
| H2 | `markReturned` 不校验记录是否存在、direction 是否为 RECEIVE、是否属于当前用户 | 任何登录用户传任何 receiveRecordId 都可"标记已回" | `markReturned_should_reject_when_record_not_exists`、`markReturned_should_reject_when_direction_is_not_RECEIVE`、`markReturned_should_reject_cross_user` | 当前**会失败** |
| H3 | `updateGiftRecordInfoT` 直接 `updateById`，不校验 record.userId 是否等于当前用户 | 用户 A 拿到用户 B 的 record id 即可修改 | `updateRecord_should_reject_when_not_owner` | 当前**会失败** |
| H4 | `deleteGiftRecordInfoT` 同样无 owner 校验 | 用户 A 可删除用户 B 的记录 | `deleteRecord_should_reject_when_not_owner` | 当前**会失败** |
| H5 | `GiftPersonInfoTServiceImp.getProfile(id)` 无 ownership 校验 + `listGiftRecordsForAggregate()` 拉取**所有用户**的 record | 任何登录用户都能看到任何 person 的完整礼金画像 | `getProfile_should_filter_by_owner` | 当前**会失败** |
| H6 | `update*` / `delete*` 在 person / event / relation 三个 service 中**全部存在**同样的 ownership 缺失 | 同 H3/H4 | 同上，每个实体都补 | 当前**会失败** |

### 🟠 P1 · 业务校验缺失

| # | 位置 | 漏洞描述 | 必测 case | 期望结果 |
|---|---|---|---|---|
| H7 | `validateDirection` 只校验 RETURN 必须有 `relatedRecordId`，但**不校验**关联的 record 是否真存在、是否为 RECEIVE、是否属于自己、是否就是自己 | RETURN 可以关联到任意 id，甚至关联自己 | `addRecord_RETURN_should_reject_when_related_not_RECEIVE`、`addRecord_RETURN_should_reject_when_related_belongs_to_other_user`、`addRecord_RETURN_should_reject_when_related_is_self` | 当前**会失败** |
| H8 | `addGiftRecordInfoT` 不校验 `amount` 必须 > 0 | 可以创建 `amount = 0` 或负数的 record | `addRecord_should_reject_when_amount_zero_or_negative` | 当前**会失败** |
| H9 | `addGiftRecordInfoT` 不校验 `giverPersonId` / `receiverPersonId` 真实存在或属于自己 | 用户可以引用别人的 person 作为送礼人 | `addRecord_should_reject_when_personId_belongs_to_other` | 当前**会失败** |
| H10 | `markReturned` 不幂等（虽然 returnedFlag 设为 1，但多次调用都会成功并刷新 update_time） | mark 第 2 次应该报错或静默 | `markReturned_should_be_idempotent` | 行为待业务确认 |
| H11 | `fillOwner` 在 `userUtils.getLoginUser() == null` 时**静默不抛**，导致 record 被存为 `userId=null, orgId=null` | 未登录态调用产生脏数据 | `addRecord_should_reject_when_not_logged_in` | 当前**会失败** |

### 🟡 P2 · 健壮性 / 数据一致性

| # | 位置 | 描述 | 必测 case |
|---|---|---|---|
| H12 | `deleteGiftRecordInfoT("1,abc,3")` 中 `Long::valueOf("abc")` 抛 `NumberFormatException` 而非业务异常 | `delete_should_reject_invalid_id_with_business_error` |
| H13 | `GiftRelationInfoTServiceImp.delete` 把 `ids.split(",")` 当 `List<String>` 直接 `removeBatchByIds`，没转 Long | 行为依赖 MP 版本，需测试覆盖 |
| H14 | `getSummary.netAmount = receive - give - return` vs `personBusinessVo.netAmount = receive - (give + return)` 公式等价但表达不同 | `summary_netAmount_should_equal_person_netAmount_aggregate` |
| H15 | `GiftAnalysisController.trend` 把 `GIVE + RETURN` 合并为"送出"，但 `getSummary` 把它们分开 | `trend_giveAmount_should_consistent_with_summary` |
| H16 | `getBusinessPage` 在内存中做分页（先 `list()` 再 `subList`），1 万条数据时性能差 | `personBusinessPage_should_complete_within_3s_for_10k_records` |
| H17 | `EventSummary.monthPendingCount` 用 `LocalDateTime.now()` 直接比较，没考虑用户时区 | `monthPendingCount_should_respect_user_timezone` |

---

## 2. 业务实体与字段边界分析

### 2.1 `GiftRecordInfoT`（礼金记录，最核心）

| 字段 | 类型 | 当前校验 | 必测边界值 | 期望行为 |
|---|---|---|---|---|
| `id` | Long → 序列化为 String（`@JsonSerialize(Long2StringSerializer)`） | MP 自动 | `null`、`0`、负数、不存在、`Long.MAX_VALUE` | 前端收到的 JSON 始终是 String |
| `orgId` | Long → String | 由 `fillOwner` 写入 | `null`（未登录）、跨机构 ID | 未登录拒绝、跨机构隔离 |
| `userId` | Long → String | 由 `fillOwner` 写入 | 同 orgId | 同上 |
| `eventId` | Long → String | 无校验 | `null`、不存在的 id、跨用户的 event id | 当前会通过，**应拒绝** |
| `giverPersonId` | Long → String | 无校验 | 同 eventId | 同上 |
| `receiverPersonId` | Long → String | 无校验 | 同 eventId | 同上 |
| `relatedRecordId` | Long → String | RETURN 时非空（其他无校验） | RETURN 时：`null`(拒绝)、自己id、非 RECEIVE 记录、跨用户 RECEIVE、循环关联 | 见 H7 |
| `direction` | String | 必须是 `GIVE/RECEIVE/RETURN`，非空 | `null`、`""`、`"give"`（小写）、`"INVALID"` | 拒绝 |
| `amount` | BigDecimal | **无任何校验** | **见 2.1.1 七点法** | 见 H8 |
| `payTime` | LocalDateTime | 无校验 | `null`、未来时间、`1970-01-01`、跨时区 | 业务确认 |
| `returnedFlag` | Integer | RECEIVE 默认 0，其他无默认 | `0`、`1`、`null`、`2`（非法） | 应只允许 0/1 |
| `remark` | String | 无长度校验 | `null`、`""`、500 字、501 字、emoji、SQL 注入 | DB 截断 |

#### 2.1.1 `amount` 七点法（精度核心）

| 点 | 值 | 当前实际行为 | 期望行为 | 状态 |
|---|---|---|---|---|
| far-low | `BigDecimal("-99999")` | 通过（漏洞 H8） | 拒绝 | 🔴 修 |
| min-1 | `BigDecimal("-0.01")` | 通过 | 拒绝 | 🔴 修 |
| min | `BigDecimal("0")` | 通过 | 业务确认（建议拒绝） | 🟡 |
| min+1 | `BigDecimal("0.01")` | 通过 | 通过，精度不丢 | ✅ |
| typical | `BigDecimal("200")` / `888` / `1314` | 通过 | 通过 | ✅ |
| max | `BigDecimal("9999999.99")` | 通过（DB `DECIMAL(12,2)`） | 通过 | ✅ |
| max+1 | `BigDecimal("10000000")` | DB 抛 SQL 异常 | 应在 service 层拦截并友好报错 | 🟠 |
| precision_overflow | `BigDecimal("0.001")` | DB 自动截断为 0.00 | 应拒绝或四舍五入 | 🟠 |
| **JS 精度** | 前端传 `Number.MAX_SAFE_INTEGER + 1` 的 String | `Long2StringSerializer` 不影响接收，BigDecimal 接收无损 | ✅ | ✅ |

### 2.2 `GiftPersonInfoT`（亲友）

| 字段 | 类型 | 当前校验 | 必测边界 |
|---|---|---|---|
| `personName` | String | 无校验 | `null`、`""`、`" "`、50 字、51 字、emoji、SQL 注入 |
| `phone` | String | 无校验 | `null`、`""`、`"1"`、合法 11 位、`"99999999999"`、含分隔符 |
| `relationType` | String | 无校验（应是字典枚举） | `null`、合法值、`"INVALID"`、`""` |
| `remark` | String | 无校验 | 同 record.remark |

### 2.3 `GiftEventInfoT`（事由）

| 字段 | 类型 | 当前校验 | 必测边界 |
|---|---|---|---|
| `eventName` | String | 无校验 | `null`、`""`、1 字、100 字、含 emoji |
| `eventType` | String | 无校验 | 同 relationType |
| `eventTime` | LocalDateTime | 无校验 | `null`、未来时间、闰年 `2024-02-29`、跨时区 |

### 2.4 `GiftRelationInfoT`（人际关系）

| 字段 | 类型 | 当前校验 | 必测边界 |
|---|---|---|---|
| `personId` | Long | 无校验 | `null`、不存在、跨用户 |
| `relationPersonId` | Long | 无校验 | 同上，**且不能等于 `personId`**（关联自己），当前无此校验 |
| `relationType` | String | 无校验 | 同上 |

---

## 3. 状态机分析

### 3.1 `direction` × `returnedFlag` 状态机

```
   ┌─────┐   送礼   ┌─────────┐
   │ 新建 │ ───────→│ GIVE    │   返回 add VO（returnedFlag 不设置）
   └─────┘          │ flag=?  │   ⚠️ flag 未默认值，导致 NULL
                    └─────────┘

   ┌─────┐   收礼   ┌─────────┐  markReturned ⚠️无校验  ┌─────────┐
   │ 新建 │ ───────→│ RECEIVE │ ───────────────────────→│ RECEIVE │
   └─────┘          │ flag=0  │  （任何人都能 mark）     │ flag=1  │
                    └─────────┘                          └─────────┘

   ┌─────┐   回礼   ┌─────────┐
   │ 新建 │ ───────→│ RETURN  │
   └─────┘          │relatedId│ ⚠️ relatedId 只校验非空，未校验：
                    │ ≠null  │     - 真实存在
                    └─────────┘     - direction 是 RECEIVE
                                    - 属于自己
                                    - 不是自己
```

### 3.2 状态机必测 case 矩阵

| # | 当前直接看代码的行为 | 期望行为 | 优先级 |
|---|---|---|---|
| L1 | `add(GIVE)`：通过 | 通过 | ✅ |
| L2 | `add(RECEIVE)`：自动 `returnedFlag=0` | 通过 | ✅ |
| L3 | `add(RETURN, relatedId=existingReceive)`：通过 | 通过 | ✅ |
| I1 | `add(direction=null)` | 当前抛 `IllegalArgumentException`✅ | ✅ |
| I2 | `add(direction="give")` 小写 | 当前抛✅ | ✅ |
| I3 | `add(RETURN, relatedId=null)` | 当前抛✅ | ✅ |
| **I4** | `add(RETURN, relatedId=自己刚建的GIVE)` | 当前**通过**（漏洞 H7） | 🔴 |
| **I5** | `add(RETURN, relatedId=不存在id)` | 当前**通过**（漏洞 H7） | 🔴 |
| **I6** | `add(RETURN, relatedId=他人的RECEIVE)` | 当前**通过**（漏洞 H7） | 🔴 |
| **I7** | `add(GIVE, amount=-100)` | 当前**通过**（漏洞 H8） | 🔴 |
| **I8** | `markReturned(不存在id)` | 当前返回 false 但无异常（漏洞 H2） | 🔴 |
| **I9** | `markReturned(自己 GIVE id)` | 当前**成功 mark**，但 GIVE 没有"回礼"语义（漏洞 H2） | 🔴 |
| **I10** | `markReturned(他人 RECEIVE id)` | 当前**成功**（漏洞 H2） | 🔴 |
| **I11** | `markReturned(已回过的 id)` | 当前**再次成功**（漏洞 H10） | 🟠 |

---

## 4. 权限矩阵

### 4.1 当前 RBAC + DataPermission 设计意图

| 操作 | 超管（super_super） | 机构管理员（admin） | 普通用户（self） | 普通用户（other） |
|---|---|---|---|---|
| 查看 Record 分页 | 全部 | 本机构 | 仅自己 | 拒绝 |
| 创建 Record | ✅ | ✅ | ✅ | ❌ |
| 修改/删除 Record | 全部 | 本机构 | 仅自己 | ❌ |
| markReturned | 同上 | 同上 | 同上 | ❌ |
| 导出 Excel | ✅ | ✅ | ❌（前端按钮已隐藏） | ❌ |
| 查看 analysis | 全局聚合 | 本机构聚合 | 仅自己聚合 | ❌ |

### 4.2 实测会发现的问题

| 操作 | 设计期望 | 实际行为 | 测试结果 |
|---|---|---|---|
| 普通用户 A 调 `GET /gift-record-info-t?id={B的record}` | 拒绝 | **能查到**（无 ownership 校验） | 🔴 失败 |
| 普通用户 A 调 `POST /gift-record-info-t/page` | 只返回 A 的 | 当前 `@DataPermission` 未生效（漏洞 H1），可能返回全部 | 🔴 失败 |
| 普通用户 A 调 `PUT /gift-record-info-t`（body 含 B 的 id） | 拒绝 | **更新成功**（漏洞 H3） | 🔴 失败 |
| 普通用户 A 调 `DELETE /gift-record-info-t?ids={B的id}` | 拒绝 | **删除成功**（漏洞 H4） | 🔴 失败 |
| 普通用户 A 调 `PUT /gift-record-info-t/mark-returned?receiveRecordId={B的id}` | 拒绝 | **mark 成功**（漏洞 H2） | 🔴 失败 |
| 普通用户 A 调 `GET /gift-person-info-t/profile?id={B的person}` | 拒绝 | **能看完整画像**（漏洞 H5） | 🔴 失败 |

**这 6 条都是要在集成测试中立刻补的高优先级 case**。

---

## 5. 测试用例规划

### 5.1 后端单元测试（JUnit5 + Mockito，60+ case）

#### 5.1.1 `GiftRecordInfoTServiceImpTest`（核心）

- [ ] `addGiftRecordInfoT`
    - [ ] L1 `should_create_GIVE_when_valid`
    - [ ] L2 `should_default_returnedFlag_to_0_when_RECEIVE`
    - [ ] L3 `should_create_RETURN_when_relatedRecordId_exists`
    - [ ] I1 `should_throw_when_direction_is_null`
    - [ ] I2 `should_throw_when_direction_is_lowercase`（边界）
    - [ ] I3 `should_throw_when_RETURN_relatedRecordId_is_null`
    - [ ] **I4** `should_throw_when_RETURN_relatedRecordId_is_self`（覆盖 H7）
    - [ ] **I5** `should_throw_when_RETURN_relatedRecordId_not_exists`（H7）
    - [ ] **I6** `should_throw_when_RETURN_relatedRecordId_belongs_to_other_user`（H7）
    - [ ] **I6b** `should_throw_when_RETURN_relatedRecordId_direction_is_not_RECEIVE`（H7）
    - [ ] **I7a** `should_throw_when_amount_is_negative`（H8）
    - [ ] **I7b** `should_throw_when_amount_is_zero`（H8）
    - [ ] **I7c** `should_truncate_when_amount_precision_overflow`（七点法）
    - [ ] **H11** `should_throw_when_userUtils_returns_null_user`
- [ ] `markReturned`
    - [ ] **I8** `should_throw_when_id_is_null`（已有 ✅）
    - [ ] **I9** `should_throw_when_record_not_exists`（H2）
    - [ ] **I10** `should_throw_when_record_direction_is_not_RECEIVE`（H2）
    - [ ] **I11** `should_throw_when_record_belongs_to_other_user`（H2）
    - [ ] **I12** `should_be_idempotent_when_already_returned`（H10）
- [ ] `updateGiftRecordInfoT`
    - [ ] `should_throw_when_record_belongs_to_other_user`（H3）
    - [ ] `should_keep_orgId_userId_immutable`（防越权篡改 owner）
- [ ] `deleteGiftRecordInfoT`
    - [ ] `should_throw_when_record_belongs_to_other_user`（H4）
    - [ ] `should_skip_silently_when_ids_empty`（已有 ✅）
    - [ ] **`should_throw_business_error_when_id_not_parsable`**（H12 "1,abc,3"）
    - [ ] `should_delete_all_when_ids_are_valid_csv`
- [ ] `calculatePendingReturnAmount`
    - [ ] `should_throw_when_id_is_null`（已有 ✅）
    - [ ] `should_throw_when_record_not_exists`（已有 ✅）
    - [ ] `should_throw_when_direction_is_not_RECEIVE`（已有 ✅）
    - [ ] `should_return_full_amount_when_no_return_records`
    - [ ] `should_return_remaining_when_partial_returned`
    - [ ] `should_return_zero_when_over_returned`（已有 ✅）
    - [ ] `should_handle_null_amount_in_records`
- [ ] `getSummary`
    - [ ] `should_calculate_netAmount_correctly`（公式 H14）
    - [ ] `should_handle_empty_records`
    - [ ] `should_handle_null_amount_records`

#### 5.1.2 `GiftPersonInfoTServiceImpTest`

- [ ] CRUD 七点法 + ownership 校验（同 record）
- [ ] **`getProfile_should_reject_cross_user`**（H5）
- [ ] **`getProfile_should_only_aggregate_owner_records`**（H5）
- [ ] `getBusinessPage_pagination_correctness`（边界：pageNum=0/负/超总页数）

#### 5.1.3 `GiftEventInfoTServiceImpTest`

- [ ] 同上 CRUD + ownership
- [ ] **`monthPendingCount_should_respect_user_timezone`**（H17）

#### 5.1.4 `GiftRelationInfoTServiceImpTest`

- [ ] CRUD + ownership
- [ ] **`should_reject_when_personId_equals_relationPersonId`**（关联自己）
- [ ] **`should_reject_when_personId_not_exists`**
- [ ] **`delete_should_convert_string_ids_to_long`**（H13）

### 5.2 后端集成测试（@SpringBootTest + MockMvc + Testcontainers）

#### 5.2.1 `GiftRecordControllerIT`

- [ ] **`POST /gift-record-info-t/page` should_filter_by_org_for_admin`**（驱动修复 H1）
- [ ] **`POST /gift-record-info-t/page` should_filter_by_user_for_normal`**（H1）
- [ ] **`GET /gift-record-info-t` cross_user should_403`**
- [ ] **`PUT /gift-record-info-t` cross_user should_403`**（H3）
- [ ] **`DELETE /gift-record-info-t?ids=` cross_user should_403`**（H4）
- [ ] **`PUT /mark-returned?receiveRecordId=` cross_user should_403`**（H2）
- [ ] `POST /gift-record-info-t` 同时由两请求标记同一 RECEIVE（并发，验证幂等）
- [ ] `POST /gift-record-info-t` `@AvoidRepeatableCommit` 注解生效（连续两次相同请求被去重）

#### 5.2.2 `GiftPermissionIT`（权限矩阵，4 身份 × 5 操作 = 20 case）

参考已有 `frontend/.../tests/midscene/rbac/cases/smoke.json` 的 persona 设计，对每个 API 跑 4 身份。

#### 5.2.3 `GiftAnalysisIT`

- [ ] **`overview` netAmount 公式与单测一致**（H14）
- [ ] **`trend` GIVE+RETURN 合并语义验证**（H15）
- [ ] `relation-distribution` 排序稳定（同 count 时如何排序）
- [ ] `event-ranking` 空数据时返回兜底"全部事由"
- [ ] **`overview` 性能基线：1 万条记录 < 1s**（H16）

### 5.3 PC 前端单元测试（Vitest）

- [ ] `src/views/finance/gift/api/index.spec.ts`
    - [ ] `normalizeGiftIds_should_convert_bigint_to_string`
    - [ ] `normalizeGiftIds_should_recursively_process_nested`
    - [ ] `normalizeGiftIds_should_handle_null_undefined_empty`
    - [ ] **`normalizeGiftIds_should_preserve_precision_for_Long.MAX_SAFE_INTEGER+1`**
- [ ] `src/views/finance/gift/config.spec.ts`
    - [ ] `formatMoney(999999999.99)` 精度无丢失
    - [ ] `directionText` 三态映射
    - [ ] `directionClass` CSS class 映射

### 5.4 PC 前端 E2E（Playwright + Midscene）

- [x] 已有：`GIFT-PC-PERSON-CRUD-001`、`GIFT-PC-EVENT-CRUD-001`、`GIFT-PC-RECORD-CRUD-001`、`GIFT-PC-EXPORT-001`、`GIFT-BUTTON-*` 系列
- [ ] **`GIFT-FLOW-RETURN-001`**：完整回礼链路
    - 创建 RECEIVE record（amount=500）
    - 调用 `PUT /mark-returned`，验证 `returnedFlag=1`
    - 创建 RETURN record，`relatedRecordId` 指向上一条
    - 调用 `GET /pending-return-amount`，验证返回 0
    - analysis 页 "待回礼" 数量 -1
- [ ] **`GIFT-FLOW-PRECISION-001`**：amount=`9999999.99`，前端列表显示精度无丢失，summary 计算无误
- [ ] **`GIFT-FLOW-PERMISSION-001`**：manager 创建 record，readonly 切换身份后看不到详情按钮但能看到行（驱动修复 H1）
- [ ] **`GIFT-FLOW-MARK-RETURNED-VALIDATION-001`**：UI 触发 markReturned 非 RECEIVE 记录时，期望前端禁用按钮（兜底 H2）
- [ ] **`GIFT-FLOW-CROSS-USER-001`**：登录 A 用 devtools 改请求中的 record id 为 B 的，期望 403（驱动修复 H3/H4）

### 5.5 移动端 E2E（Midscene）

- [x] 已有：`GIFT-MOBILE-001/002/003`（页面加载、快速记礼+Haptic、列表刷新）
- [ ] `GIFT-MOBILE-RECORD-CRUD-001`
- [ ] `GIFT-MOBILE-GESTURE-PULL-REFRESH-001`（下拉刷新七点法）
- [ ] `GIFT-MOBILE-GESTURE-INFINITE-SCROLL-001`（上拉加载边界）
- [ ] `GIFT-MOBILE-GESTURE-SWIPE-DELETE-001`（左滑删除）
- [ ] `GIFT-MOBILE-OFFLINE-001`（断网时降级 UI）
- [ ] `GIFT-MOBILE-LARGE-LIST-001`（1000 条 mock 虚拟滚动）
- [ ] `GIFT-MOBILE-VIEWPORT-SE-001`（iPhone SE 320px 不溢出）
- [ ] `GIFT-MOBILE-PERF-LCP-001`（首屏 LCP < 2.5s）
- [ ] `GIFT-MOBILE-HAPTIC-COVERAGE-001`（关键操作均触发 vibrate）

---

## 6. 不测理由（必填，防漏测）

| 项 | 不测理由 | 由谁兜底 |
|---|---|---|
| MyBatis-Plus 的 `IService.page()` 实现本身 | 第三方库 | Baomidou |
| `@JsonSerialize(using = Long2StringSerializer.class)` 序列化器本身 | 单元测过的 utility | `common` 模块 |
| `@AvoidRepeatableCommit` 注解的拦截逻辑 | 由公共模块测 | `common` 模块 |
| `@DataPermission` 注解的 SQL 解析逻辑 | 由 `user` 模块测 | `user` 模块（但需测它生效在 gift 上 → H1）|
| Vant / Ant Design Vue 组件内部行为 | 第三方库 | 上游 |
| `Long2StringSerializer` 在 emoji JSON 中的行为 | 边缘场景成本太高 | 手工肉眼 |

---

## 7. 覆盖率目标

| 范围 | Line | Branch | Mutation Score | 备注 |
|---|---|---|---|---|
| `gift/record/service/impl/` | ≥ 85% | ≥ 75% | ≥ 75% | 核心业务，最严格 |
| `gift/person/service/impl/` | ≥ 80% | ≥ 70% | ≥ 70% | |
| `gift/event/service/impl/` | ≥ 80% | ≥ 70% | ≥ 70% | |
| `gift/relation/service/impl/` | ≥ 75% | ≥ 65% | ≥ 65% | |
| `gift/analysis/controller/` | ≥ 70% | ≥ 60% | ≥ 60% | 主要是聚合计算 |
| `gift/*/controller/` | ≥ 60% | ≥ 50% | — | 主要走 service 测覆盖 |
| 前端 PC `views/finance/gift/api/` | ≥ 85% | ≥ 70% | — | `normalizeGiftIds` 必锁死 |
| 前端 PC `views/finance/gift/config.ts` | ≥ 80% | ≥ 70% | — | 工具函数 |
| 前端移动 同上 | 同上 | 同上 | — | |

---

## 8. 性能基线

| 指标 | 基线 | 告警阈值 | 测法 |
|---|---|---|---|
| `POST /gift-record-info-t/page` 10 条 | < 200ms | > 500ms | JMeter |
| `POST /gift-record-info-t/page` 1 万条 | < 1s | > 3s | JMeter |
| `GET /gift-analysis/overview` 1 万条 record | < 500ms | > 1.5s | JMeter |
| `GET /gift-person-info-t/business-page` 1k person | < 800ms | > 2s | 注意 H16 内存分页 |
| 移动端 Gift Dashboard 首屏 LCP | < 1.5s | > 2.5s | `performance.getEntriesByType` |
| 移动端 Gift Record 列表（10 条） | < 800ms | > 1.5s | 同上 |
| 移动端快速记礼弹窗打开 | < 200ms | > 500ms | 同上 |

---

## 9. 测试数据准备

### 9.1 Fixture 用户（在 dev 环境预置）

| persona | 角色 | 机构 | 备注 |
|---|---|---|---|
| `super_super` | 超管 | 总部 | 看全量 |
| `org_a_admin` | 机构 A 管理员 | A | 看 A 全部 |
| `org_a_user_1` | 普通用户 1 | A | 只看自己 |
| `org_a_user_2` | 普通用户 2 | A | 跨用户对照 |
| `org_b_admin` | 机构 B 管理员 | B | 跨机构对照 |

### 9.2 测试数据生成器（推荐放 `tests/fixtures/`）

```java
public class GiftRecordFixture {
    public static GiftRecordInfoTVo.builder()
        .userId(USER_A_ID).orgId(ORG_A_ID)
        .direction("GIVE").amount(new BigDecimal("100"))
        .payTime(LocalDateTime.now()).build();
}
```

### 9.3 测试数据隔离

- 所有创建数据用 `codex-test-${模块}-${Date.now()}` 前缀
- AI 测试用 try/finally + API 删除清理
- CI 用 testcontainers 起独立 MySQL 8.0 + Redis 7.0

### 9.4 大数据集（性能测试用）

`GIFT-MOBILE-LARGE-LIST-001` 等需要 1k+ 数据：

- **方案 A（推荐）**：Playwright `context.route()` 拦截 `/page` 返回 mock 数据
- **方案 B**：后端开 `/test-helper/gift/seed?count=1000` 接口（仅 dev 环境，CI 也用此）

---

## 10. 30 天执行计划

### Week 1 · 止血（H1/H2/H3/H4 四个 P0 漏洞驱动修复）
- [ ] Day 1：写 5.1.1 中带 **🔴** 标记的单测，**期望全部失败**（红）
- [ ] Day 2-3：业务方修复 H2/H3/H4（增加 ownership 校验），单测变绿
- [ ] Day 4：修复 H1（让 service 调用 mapper 自定义 `getPage`，使 `@DataPermission` 生效）
- [ ] Day 5：跑 5.2.2 权限矩阵 IT，验证修复

### Week 2 · 边界 + 业务规则（H7~H17）
- [ ] amount / direction / relatedRecordId 校验补全
- [ ] markReturned 状态机校验补全
- [ ] 时区、并发、幂等性测试

### Week 3 · 前端单测 + AI 测试扩展
- [ ] PC 前端 Vitest 接入
- [ ] `normalizeGiftIds` / `formatMoney` 单测
- [ ] 5.4 中 5 个新 AI case
- [ ] 移动端 case 扩到 10+

### Week 4 · CI 自动化 + 度量
- [ ] Jacoco 阈值卡入 CI
- [ ] PIT 突变测试基线
- [ ] PR 评论自动贴覆盖率 / 失败截图
- [ ] 周度审计：用 graphify 比对 service 方法和 case 列表，漏测告警

---

## 11. 进度跟踪

| 阶段 | 状态 | 完成日期 |
|---|---|---|
| Checklist 编制完成（基于真实代码） | ✅ | 2026-05-28 |
| H1-H6 P0 漏洞测试用例落地（红） | ☐ | |
| H1-H6 修复完成（绿） | ☐ | |
| H7-H11 P1 校验补齐 | ☐ | |
| H12-H17 P2 健壮性补齐 | ☐ | |
| 后端 service 覆盖率达 80% | ☐ | |
| 后端 PIT 突变分数达 70% | ☐ | |
| 前端 Vitest 接入 + 工具层覆盖率 80% | ☐ | |
| AI 测试 case 全部完成 | ☐ | |
| CI 接入 | ☐ | |
| **样板完工**（其他模块可 copy 本文件） | ☐ | |

---

## 12. 给业务方的"立即可见价值"清单

> 跟业务方/Leader 汇报时，可以用这一节作为"为什么要测"的证据。

按现在的代码，**普通用户 A** 可以做以下操作：

1. ✅ 查看用户 B 的礼金记录详情（`GET /gift-record-info-t?id=B的id`）
2. ✅ 修改用户 B 的礼金记录（`PUT /gift-record-info-t`，body 含 B 的 id）
3. ✅ 删除用户 B 的礼金记录（`DELETE /gift-record-info-t?ids=B的id`）
4. ✅ 把用户 B 的"收礼"标记为"已回"（`PUT /mark-returned?receiveRecordId=B的id`）
5. ✅ 查看用户 B 的完整礼金画像（`GET /gift-person-info-t/profile?id=B的人脉id`）
6. ✅ 创建礼金记录时输入负数金额、关联到不存在的事由、关联到自己作为"回礼"

按现在的代码，**机构 A 管理员**可以做以下操作：

7. ✅ 查看机构 B 的全部礼金分页（漏洞 H1，DataPermission 未生效）

**这些都是合规风险**。Checklist 的第 1 节列的 17 个测试用例完成后，能从工程层面**强制业务修复**。

---

## 13. 修订记录

| 版本 | 日期 | 修改人 | 内容 |
|---|---|---|---|
| v1.0 | 2026-05-28 | alex | 首版，作为通用样板 |
| v1.1 | 2026-05-28 | alex | 基于 `codex/gift-management-module` 分支真实代码重写，新增 17 个具体漏洞清单（H1-H17）和对应测试用例 |
