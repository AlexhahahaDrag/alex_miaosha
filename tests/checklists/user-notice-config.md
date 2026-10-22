# 用户个性化消息通知渠道配置测试清单

## 一、 领域边界与测试范围
本清单覆盖 `alex_miaosha_finance` 微服务消息与提醒体系（`SysMessageInfo`、`UserNoticeConfig`）的用户个性化多渠道推送开关管控能力。
核心治理与业务目标：
1. **多渠道独立控制**：系统内部站内信（`internal_enabled`，控制消息是否入库并在页面提示展示）与微信服务号模板消息推送（`wechat_enabled`，控制是否调用微信服务号模板消息发送接口），二者正交独立；
2. **零数据膨胀原则 (Ponytail 极简模式)**：未配置开关的新用户，数据库中不存任何冗余记录，服务层与前端默认全量放行（站内信=开启，微信=开启）；仅当用户在前端主动调整配置时才触发单条按需写入（Upsert）；
3. **8 大业务分类字典映射**：拆分为 `GLOBAL`（全局总控）、`GIFT_EVENT`（礼金事件）、`GIFT_RETURN`（人情回礼）、`COUPON`（消费券到期）、`PREPAID_CARD`（预付卡到期）、`BUDGET_OVER`（预算超支）、`FINANCE_REPORT`（财务简报）、`SYSTEM`（系统通知）；
4. **两级开关与一票否决权**：`GLOBAL` 总控具备最高决策权。若用户的 `GLOBAL` 开关关闭，对应渠道的所有具体子业务即便独立开启也必须被一票否决；
5. **用户级强隔离与防越权**：严格由登录态上下文解析 `loginUserId`，禁止越权操作他人开关配置；
6. **全栈 UI 体验契约**：前端采用 100% Tailwind CSS 原子类构建，右侧 560px 抽屉唤出，支持乐观更新、错误回滚与级联禁用状态提示。

---

## 二、 七点法与边界验证矩阵

| 用例编号 | 测试类与方法 | 验证场景与边界输入 | 预期结果与安全断言 | 状态 |
| :--- | :--- | :--- | :--- | :--- |
| **DEF-01** | `UserNoticeDefaultPolicyIT#testNewUserDefaultAllow` | 新用户没有任何 DB 配置记录，遍历 8 大枚举调用 `isInternalEnabled` 与 `isWechatEnabled` | 默认全部恒定放行（返回 `true`），不抛出 NPE 或异常 | ✅ PASS |
| **DEF-02** | `UserNoticeDefaultPolicyIT#testGetMyConfigsReturnsAll8ItemsWithDefaultOpen` | 新用户前端初次加载调用 `getMyConfigs` | 内存合并返回完整的 8 大业务项，所有配置项开关值均为 `true`，`id` 为 `null` | ✅ PASS |
| **ISO-01** | `UserNoticePersonalizedOverrideIT#testUserIsolationAndOverride` | 用户 1001 将 `COUPON` 微信通知设为 0，用户 1002 未设置 | 用户 1001 的 `COUPON` 微信被拦截（返回 `false`），用户 1002 依然默认放行（返回 `true`），用户间完全隔离 | ✅ PASS |
| **JOB-01** | `UserNoticePersonalizedOverrideIT#testRemindJobRespectsUserWechatDisabled` | 消费券巡检到期触发 `notifyWeChatSafely`，用户个性化关闭了 `COUPON` 微信通知 | 内部拦截判定生效，`weChatService.sendRemindTemplateMessage` 零调用（`never()`） | ✅ PASS |
| **JOB-02** | `UserNoticePersonalizedOverrideIT#testRemindJobRespectsUserInternalDisabled` | 巡检触发 `persistCandidates` 批量持久化，用户关闭了 `COUPON` 站内信但开启微信 | 站内信候选过滤跳过，数据库入库插入数为 0（`never().insert()`），而微信通道正交发送 | ✅ PASS |
| **VETO-01** | `UserNoticeGlobalOverrideSecurityIT#testGlobalInternalDisabled_overridesAllChildBizTypes` | 用户配置 `GLOBAL` 站内信关闭，但 `GIFT_EVENT` 站内信开启 | `isInternalEnabled` 严格返回 `false`，一票否决生效；微信渠道正交独立不受影响 | ✅ PASS |
| **VETO-02** | `UserNoticeGlobalOverrideSecurityIT#testGlobalWechatDisabled_overridesAllChildBizTypes` | 用户配置 `GLOBAL` 微信关闭，但 `COUPON` 微信开启 | `isWechatEnabled` 严格返回 `false`，一票否决生效；站内信渠道正交独立不受影响 | ✅ PASS |
| **SEC-01** | `UserNoticeGlobalOverrideSecurityIT#testSaveMyConfig_securityValidations` | 非法传参（`userId=null`、`vo=null`、`noticeCode=ILLEGAL_ATTACK`） | 服务层严格拦截并抛出对应的 `FinanceException`（参数错误或未认证） | ✅ PASS |
| **SEC-02** | `UserNoticeGlobalOverrideSecurityIT#testQuerySafetyFallback` | 查询时传入非法的 null/负数用户 ID 或空白编码 | 防御性安全回退 `true`，防止巡检任务因非法参数中断 | ✅ PASS |

---

## 三、 测试覆盖率与金字塔分层总结
- **Level 2 专项集成测试**：3 大套件（`UserNoticeDefaultPolicyIT`, `UserNoticePersonalizedOverrideIT`, `UserNoticeGlobalOverrideSecurityIT`）共 9 项用例 **100% 绿色通过**；
- **PC 端静态类型与语法校验**：`alex_miaosha_front` 执行 `vue-tsc --noEmit` 0 error，全量通过；
- **移动端单元测试**：`alex_miaosha_mobile` 执行 `npm run test:unit`（Vitest），25 个测试套件共 149 项测试 **100% 绿色通过**（涵盖 `NOTICE_BIZ_META_MAP` 8大项、`isSubNoticeVetoed` 全局一票否决三态判定、雪花 ID BigInt/String 精度防丢失）；
- **移动端生产打包构建**：`npm run build`（Vite + Less + Vant 4 生产压缩打包）**100% 成功（0 errors）**。
