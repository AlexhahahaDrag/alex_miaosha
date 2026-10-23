# Checklist: Role-Aware Dashboard & Multi-Role Portal Hardening (第四十阶段：全栈真数据大盘与微服务探针)

**Module:** `alex_miaosha_front` & `alex_miaosha_mobile` & `alex_miaosha_user` & `alex_miaosha_finance`  
**Date:** 2026-10-23  
**Standard:** `TESTING_STANDARD.md` (Level 1 Unit & Level 2 Component Contract Tests)

---

## 1. 角色判定与自适应分流矩阵

| # | 测试项 | 覆盖端 / 文件 | 验证断言 | 状态 |
|---|---|---|---|:---:|
| 1 | 超管角色强判定 | PC: `homeDashboardUser.test.ts`<br/>Mobile: `mobileHomeDashboard.test.ts` | `superAdmin: true` 或 `super_super` 必收敛为 `super_admin`（PC 渲染超管控制台，Mobile 渲染紫色平台总管徽章） | ✅ PASS |
| 2 | 机构/垂直管理员启发式判定 | PC: `homeDashboardUser.test.ts`<br/>Mobile: `mobileHomeDashboard.test.ts` | 角色代码含 `admin` 或角色名称包含 `管理员`/`主管` 必定收敛为 `org_admin`（PC 渲染机构大盘，Mobile 渲染蓝色机构主管徽章） | ✅ PASS |
| 3 | 普通用户/家庭成员兜底判定 | PC: `homeDashboardUser.test.ts`<br/>Mobile: `mobileHomeDashboard.test.ts` | 其余普通角色必定收敛为 `user` 角色视角（PC 渲染个人资产工作台，Mobile 渲染绿色家庭随礼账本徽章） | ✅ PASS |

---

## 2. 真实人情礼金资产聚合与防穿透缓存矩阵

| # | 测试项 | 覆盖端 / 文件 | 验证断言 | 状态 |
|---|---|---|---|:---:|
| 4 | 礼金结余计算公式契约 | PC: `homeDashboardUser.test.ts`<br/>Mobile: `mobileHomeDashboard.test.ts` | 净结余 `netBalance` 严格等于 `累计收礼 (receivedAmount) - 随礼支出 (giveAmount + returnAmount)`，彻底杜绝负号颠倒或遗漏回礼 | ✅ PASS |
| 5 | 工作台轻量级 SWR 缓存有效性 | PC: `homeDashboardUser.test.ts` (`dashboardCache.ts`) | 60 秒 TTL 周期内连续请求直接命中内存快照，避免侧边栏高频切换导致后端接口穿透 | ✅ PASS |
| 6 | 缓存超期自动失效 | PC: `homeDashboardUser.test.ts` (`dashboardCache.ts`) | 超过 60 秒后缓存自动失效并返回 null，促发后台异步静默重新拉取 | ✅ PASS |
| 7 | 缓存按键清理与全量清空 | PC: `homeDashboardUser.test.ts` (`dashboardCache.ts`) | 支持针对特定 Key 或全量清空缓存 | ✅ PASS |
| 8 | 真实趋势图空数据优雅降级 | PC: `UserDashboard.vue` | 当近半年暂无人情流水时，图表月份与 0 轴正常绘制，不发生未捕获异常或控制台白屏 | ✅ PASS |

---

## 3. 待办日程、消息中心打通与 AI 记账联动矩阵

| # | 测试项 | 覆盖端 / 文件 | 验证断言 | 状态 |
|---|---|---|---|:---:|
| 9 | PC 待办日程与未读消息联动 | PC: `PersonalTodoCard.vue` | 前 5 条未读系统提醒（`GIFT_EVENT`/`GIFT_RETURN`）与本地自定义待办融合展示，打勾直接调用 `markSysMessageRead` | ✅ PASS |
| 10 | PC 快捷服务直达 AI 助手抽屉 | PC: `QuickActionGrid.vue` & `GlobalAiAssistantDrawer.vue` | 点击 `AI 智能记账` 通过 `open-global-ai-drawer` 事件一键滑出全局 AI 抽屉并预置提示语 | ✅ PASS |
| 11 | 移动端一键唤起 AI 智能记账 ActionSheet | Mobile: `home/index.vue` & `FinanceAiSheet.vue` | 点击 `AI 智能流式记账` 触发 `light` 触觉反馈并调起多场景记账抽屉，保存成功自动刷新资产卡片 | ✅ PASS |
| 12 | 移动端随礼与待办触觉微交互 | Mobile: `mobileHomeDashboard.test.ts` | 点击快捷随礼与待办提醒触发 10ms `selection` 微振动，增强触觉交互体感 | ✅ PASS |
| 13 | 移动端全时段动态问候语 | Mobile: `mobileHomeDashboard.test.ts` | 凌晨、上午、中午、下午、晚上 5 大时段精准自适应匹配亲切文案 | ✅ PASS |

---

## 4. 全栈真数据聚合大盘与微服务集群在线探针矩阵 (新增)

| # | 测试项 | 覆盖端 / 文件 | 验证断言 | 状态 |
|---|---|---|---|:---:|
| 14 | 超管大盘非授权越权隔离防护 | 后端: `DashboardServiceTest.java` | 非超管用户请求 `GET /api/v1/dashboard/super/overview` 抛出 `SystemException(UNAUTHORIZED, 403)`，彻底杜绝数据越权 | ✅ PASS |
| 15 | 超管全景 KPI 与 7 天动态日期态势 | 后端: `DashboardServiceTest.java`<br/>PC: `dashboardRealData.test.ts` | 聚合全网真实用户数、有效机构数、交易总额；动态生成包含 7 天 `MM-dd` 刻度的 QPS 与单量数据 | ✅ PASS |
| 16 | 机构管理员在册成员与组织概览 | 后端: `DashboardServiceTest.java`<br/>PC: `dashboardRealData.test.ts`<br/>Mobile: `mobileDashboardAggregate.test.ts` | 根据上下文精准查询管辖机构在册成员数，`orgId` 严格序列化为 `string`，杜绝前端精度截断 | ✅ PASS |
| 17 | 6 大核心微服务集群健康探测 | 后端: `DashboardServiceTest.java`<br/>PC: `dashboardRealData.test.ts`<br/>Mobile: `mobileDashboardAggregate.test.ts` | 真实 Socket 探测 30001 (Gateway)、30006 (User)、30007 (Product)、30008 (Finance)、30009 (OSS)、30010 (AI) 在线状态与毫秒级时延 | ✅ PASS |
| 18 | 系统安全与审计核心动态时间线 | 后端: `DashboardServiceTest.java`<br/>PC: `dashboardRealData.test.ts` | 真实聚合包含风控限流、机构隔离、财务核算与 XXL-Job 定时巡检动态事件流 | ✅ PASS |

---

## 5. 不测理由与排除清单（Out of Scope）

1. **真实物理高并发 ECharts 渲染性能压测**：单元测试聚焦于图表数据绑定、空状态兜底与生命周期销毁，GPU 硬件加速与压测由生产端到端负责；
2. **移动端物理硬件马达震动力度感知**：单测通过 Mock `navigator.vibrate` 断言振动时长与模式参数，不依赖实体手机电磁线圈；
3. **秒杀模块排除**：按照用户明确指令，严格排除秒杀模块。
