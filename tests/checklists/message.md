# Message（系统消息与到期提醒中心）模块测试 Checklist

> **关联项目**：`alex_miaosha_finance`（后端） + `alex_miaosha_front`（PC） + `alex_miaosha_mobile`（移动端）
> **关联文档**：根目录 `TESTING_STANDARD.md`
> **参考样板**：`tests/checklists/gift.md`
> **最后更新**：2026-10-19

---

## 0. 元信息

| 项 | 内容 |
| :--- | :--- |
| 模块名 | message（系统消息与多业务到期提醒中心） |
| 业务负责人 | @alex |
| 测试负责人 | @alex |
| 后端代码路径 | `alex_miaosha_finance/finance_boot/src/main/java/com/alex/finance/message/` |
| 后端 VO 路径 | `alex_miaosha_finance/finance_api/src/main/java/com/alex/api/finance/message/vo/` |
| PC 代码路径 | `src/views/message/` + `src/layout/components/my-right-info/components/MessageCenterDrawer.vue` |
| 移动端代码路径 | `src/views/message/` (`index.vue` + `api/index.ts` + `config/index.ts`) |
| 后端 API 前缀 | `/sys-message-info` |
| 数据库表 | `sys_message_info_t`（关联扫描 `gift_event_info_t`、`cpn_coupon_info_t`、`account_record_info`、`prepaid_card_info_t`） |
| 关键依赖 | `UserUtils.getLoginUser()`（注入登录态） + `DateUtils.now()`（时区安全时间） + `Long2StringSerializer`（ID 序列化） |

---

## 1. 核心业务规则与高价值测试目标

| # | 规则分类 | 规则描述 | 必测 Case | 期望结果 |
| :--- | :--- | :--- | :--- | :--- |
| M1 | 礼尚往来事件提醒 | 事件日期等于今天（`D_DAY`）或明天（`D_MINUS_1`）时自动生成提醒；后天或已过去事件不生成 | `sync_should_generate_gift_event_reminders_for_today_and_tomorrow_only` | 仅 D-0(`HIGH`) 与 D-1(`MEDIUM`) 生成提醒 |
| M2 | 消费券到期提醒 | 仅当 `remainingQuantity > 0` 且距离 `endDate` 在 `0~3` 天内时生成提醒；已核销完或 >3 天不提醒 | `sync_should_generate_coupon_expire_reminders_within_3_days_when_remaining_positive` | D-0(`HIGH`)、D-1(`MEDIUM`)、D-2~3(`NORMAL`) 准确生成，剩余为 0 不生成 |
| M3 | 猫超卡/账户记录到期提醒 | `account_record_info.avli_date` 在 `0~3` 天内且金额有效时生成 `CARD_EXPIRE` 提醒 | `sync_should_generate_account_record_expire_reminders_within_3_days` | 生成带账户名与金额的 `ACCOUNT_RECORD` 提醒 |
| M4 | 预付卡到期提醒 | `prepaid_card_info_t.expire_date` 在 `0~3` 天内且 `currentBalance > 0` 时生成 `CARD_EXPIRE` 提醒 | `sync_should_generate_prepaid_card_expire_reminders_when_balance_positive` | 余额 > 0 且 ≤ 3 天生成提醒；余额为 0 不生成 |
| M5 | 幂等防重与防复活 | 同一 `dedup_key` 在全表（含已读、已逻辑删除记录）已存在时，重复调用 `sync` 不得二次插入 | `sync_should_be_idempotent_and_not_resurrect_deleted_or_read_messages` | 第二次 `sync` 新增条数为 0 |
| M6 | 归属与越权防护 | 用户 A 不能标记已读或删除用户 B 的私有消息 | `markRead_or_delete_should_reject_when_message_belongs_to_other_user` | 抛出无权操作异常 |
| M7 | ID 精度安全契约 | 后端 `id / orgId / userId` 序列化为 String，前端 `normalizeMessageIds` 将所有 `*Id` 递归转 String | `normalizeMessageIds_should_convert_all_id_fields_to_string` | 大数雪花 ID 精度零丢失 |
| M8 | 微信模板消息联动与降级 | XXL-Job 巡检新插入提醒时自动触发 `WeChatService.sendRemindTemplateMessage`；微信异常不回滚站内信入库 | `testWeChatFailureDoesNotRollbackMessageInsert` | 站内信入库成功且捕获微信异常不抛出 |
| M9 | 人情往来失衡与回礼备忘提醒 | 针对净盈余 `netBalance >= 800` 且超 180 天未互动亲友，按月幂等生成 `GIFT_RETURN` 备忘消息 | `test_generate_gift_return_imbalance_reminder_and_dedup` | 生成标题为回礼备忘、链接跳转平衡矩阵的消息，同月内不重复打扰 |


---

## 2. 字段边界分析（七点法）

### 2.1 `SysMessageInfo` 核心字段边界

| 字段 | 类型 | 校验规则 | 七点法边界值 | 期望行为 |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `Long` -> JSON `String` | MyBatis-Plus ASSIGN_ID | `null`、`-1`、`0`、`1`、`typical`、`Long.MAX_SAFE_INTEGER`、`Long.MAX_VALUE` | 前端始终收到 `string` 类型 ID |
| `title` | `String (VARCHAR 200)` | 必填，非空白，最大 200 字符 | `null`、`""`、`"   "`、`"A"`（min=1）、`"正常标题"`、200 字符、201 字符（max+1） | 空或超长拒绝，1~200 字符通过 |
| `content` | `String (VARCHAR 1000)` | 必填，非空白，最大 1000 字符 | `null`、`""`、`"   "`、`"1"`（min=1）、`"正常正文"`、1000 字符、1001 字符（max+1） | 空或超长拒绝，1~1000 字符通过 |
| `msgType` | `String` 枚举 | `SYSTEM` / `GIFT_EVENT` / `COUPON_EXPIRE` / `CARD_EXPIRE` | `null`、`""`、`"INVALID"`、4 个合法枚举值 | 发布系统消息缺省默认 `SYSTEM`，非法枚举过滤忽略 |
| `priority` | `String` 枚举 | `HIGH` / `MEDIUM` / `NORMAL` | `null`、`""`、`"UNKNOWN"`、`HIGH`、`MEDIUM`、`NORMAL` | 缺省默认 `NORMAL` |
| `remainingDays`（到期天数差） | `long` 计算值 | 礼金：`[0, 1]`；券卡：`[0, 3]` | `-30`（远期过期）、`-1`（昨天过期）、`0`（今天 min）、`1`（明天）、`2`（典型）、`3`（券卡 max）、`4`（max+1） | 礼金仅 `0,1` 触发；券卡仅 `0,1,2,3` 触发；负数与 `>max` 不触发 |

---

## 3. 状态机流转矩阵

### 3.1 消息生命周期状态机

```
[待巡检业务记录]
      │
      ▼ (executeRemindJob: dedup_key 不存在 -> insert + notifyWeChatSafely)
[未读 UNREAD (read_status=0, is_delete=0)]
      │
      ├──► markRead(id) / markAllRead() ──► [已读 READ (read_status=1, read_time=now)]
      │                                              │
      │                                              ├──► 重复 markRead() ──► 幂等保持 READ
      │                                              │
      ▼                                              ▼
  deleteMessages(ids)                        deleteMessages(ids)
      │                                              │
      └──────────────────► [已删除 DELETED (is_delete=1)] ◄──┘
                                     │
                                     └──► 再次触发 executeRemindJob() ──► 因 dedup_key 已存在，严禁复活或重发微信
```

| 起始状态 | 触发动作 | 目标状态 | 断言重点 |
| :--- | :--- | :--- | :--- |
| 无记录 | `executeRemindJob`（满足临期条件） | `read_status=0, is_delete=0` | `unreadTotal` +1，生成对应 `dedup_key` 并触发一次微信模板消息 |
| `read_status=0` | `markRead(id)` | `read_status=1, read_time!=null` | `unreadTotal` -1 |
| `read_status=1` | `markRead(id)`（重复已读） | `read_status=1` | 幂等返回 true，`unreadTotal` 不变为负数 |
| `read_status=0/1` | `deleteMessages(id)` | `is_delete=1` | 分页列表不可见；再次 `executeRemindJob` 不会重新插入 |

---

## 4. 权限矩阵（RBAC 数据隔离）

| 角色 Persona | 查询/汇总 (`page`/`summary`) | XXL-Job 后台巡检 (`executeRemindJob`) | 标为已读 (`read`/`read-all`) | 发布系统通知 (`POST /sys-message-info`) | 删除消息 (`DELETE`) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **超级管理员 (`super`)** | 本人消息 + 全局广播消息 | 按业务记录归属人全量扫描投递 | 仅限本人可见消息 | 允许发布（支持个人或全局广播） | 允许删除本人消息或系统广播 |
| **机构管理员 (`admin`)** | 本人消息 + 本机构共享提醒 + 全局广播 | 按业务记录归属人全量扫描投递 | 本人/本机构消息 | 允许在本机构内发布通知 | 仅限本机构/本人消息，跨机构拒绝 |
| **普通用户 (`user`)** | 仅本人消息 + 本机构共享礼金提醒 + 全局广播 | 按业务记录归属人全量扫描投递 | 仅限本人可见消息，跨用户拒绝 | 允许创建个人提醒事项 | 仅限本人消息，跨用户拒绝 |

---

## 5. 测试金字塔分层与"不测理由"

| 测试层级 | 占比目标 | 覆盖内容 | 执行命令 |
| :--- | :--- | :--- | :--- |
| **后端单元测试** | 60% | `SysMessageInfoServiceTest`：四大消息源 XXL-Job 巡检规则、七点法天数边界、`dedup_key` 幂等、微信模板推送联动与异常隔离、已读/全部已读、越权拦截 | `mvn test -pl alex_miaosha_finance/finance_boot -am -Dtest=SysMessageInfoServiceTest` |
| **前端单元测试 (PC & Mobile)** | 25% | `normalizeMessageIds` 大数精度保护、分类徽章样式映射、双端 `jumpUrl` 路由解析 | `pnpm test:unit` |
| **AI / E2E 自动化冒烟** | 10% | • PC：`MESSAGE-PC-SMOKE-001` (铃铛/抽屉/Tab)、`MESSAGE-PC-SMOKE-002` (新增弹窗表单)<br/>• Mobile：`MESSAGE-MOBILE-SMOKE-001` (TabBar/卡片)、`MESSAGE-MOBILE-SMOKE-002` (胶囊/仅未读/列表) | • PC: `node scripts/midscene/run-smoke.mjs`<br/>• Mobile: `node scripts/midscene/run-gift-smoke.mjs` |
| **构建与静态门禁** | 5% | TypeScript 编译检查、ESLint 零错误、Vite 生产打包 | `pnpm lint && pnpm build` |

### 明确不测项及理由（Out of Scope）
1. **微信公网服务器真实 HTTP 通信 (`WxMpService.getTemplateMsgService().sendTemplateMsg`)**：依赖外部微信公网 AccessToken 与 OpenID 配额，单测通过 Mock `WeChatService` 验证触发次数、入参组装与异常隔离逻辑，不向腾讯公网发包。
2. **MyBatis-Plus 框架内置 `BaseMapper.insert` SQL 语法**：由框架自身保证，单测聚焦业务规则与 Mapper 接口交互契约。
