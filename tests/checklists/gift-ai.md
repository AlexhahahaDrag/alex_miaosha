# Gift AI（礼尚往来 AI 解读）P0–P1 测试 Checklist

> **关联项目**：`alex_miaosha_ai`（网关 `/api/am-ai`）+ `alex_miaosha_front`（PC）+ `alex_miaosha_mobile`（移动端）  
> **关联文档**：根目录 `TESTING_STANDARD.md`；`docs/superpowers/plans/2026-09-01-gift-ai-p0-p1.md`；`docs/superpowers/specs/2026-09-01-gift-ai-opportunity-roadmap-design.md`  
> **关联 checklist**：SSE 基建见 [`ai-analyze-stream.md`](./ai-analyze-stream.md)；gift CRUD/权限见 [`gift.md`](./gift.md)  
> **样板属性**：结构沿用 `gift.md`（字段边界 / 状态 / 权限 / 不测理由）  
> **范围**：**仅 P0–P1**（双端 GiftAiClient + Dashboard/Analysis「AI 解读」只读展示）；P2+ / ASR / recommend-amount **不在本清单**  
> **最后更新**：2026-09-01

---

## 0. 元信息

| 项 | 内容 |
| --- | --- |
| 模块名 | gift-ai（礼尚往来 AI 解读） |
| 业务负责人 | @alex |
| 测试负责人 | @alex |
| 关联需求 | `2026-09-01-gift-ai-opportunity-roadmap-design.md`（P0–P1） |
| AI 服务 | `alex_miaosha_ai` → `POST /ai/chat`、`POST /ai/chat/stream` |
| 网关前缀 | `/api/am-ai`（JWT Authorization） |
| PC 代码路径 | `alex_miaosha_front/src/views/finance/gift/ai/` + `gift-dashboard/index.vue`、`analysis/index.vue` |
| Mobile 代码路径 | `alex_miaosha_mobile/src/views/finance/gift/ai/` + `dashboard/index.vue`、`analysis/index.vue` |
| bizType | `gift-analysis`（固定） |
| 关键约束 | AI **永不**调用 gift CRUD / 不写库；context 中 ID 为 **string**；手机号默认不进 context |
| 入口权限 | `gift:view`（无则整段 UI 不展示） |
| testid 前缀 | `gift-ai-insight-*` |

---

## 1. P0–P1 高价值测试目标

### 🔴 P0 · 鉴权与通道

| # | 场景 | 必测 case | 期望结果 | 覆盖层 |
| --- | --- | --- | --- | --- |
| A1 | 未登录 / token 为空调 stream | `chatGiftAiStream_without_token_should_onError_401` | `onError({ code:'401', message:'请先登录' })`，**不发** fetch | PC/Mobile 单测 mock store |
| A2 | token 无效 / 过期经网关 | `stream_with_invalid_jwt_should_http_401_or_403` | HTTP 非 2xx → 面板 `gift-ai-insight-error` 展示；不写入 gift 表 | 集成 / E2E |
| A3 | 整包 `/ai/chat` 鉴权失败 | `chatGiftAi_unauthorized_should_fail` | `code !== '200'`，前端解构报错 | 单测 mock postData |
| A4 | SSE 半包/粘包解析 | `consumeSseBuffer_partial_meta_delta` | meta/delta/done 正确拆分 | ✅ `sseParse.spec.ts` |
| A5 | HTTP 500 / 空 body | `stream_http_error_should_onError` | `onError`，loading 结束 | `api.spec.ts` |

### 🔴 P0 · SSE 中断（Abort）

| # | 场景 | 必测 case | 期望结果 | 覆盖层 |
| --- | --- | --- | --- | --- |
| S1 | 流式进行中点「停止」 | `abort_during_stream_stops_loading` | `AbortController.abort()`；`loading=false`；**不**再追加 delta；**不**弹失败 toast（aborted 静默） | 组件单测 / E2E |
| S2 | abort 后无 onDone 副作用 | `abort_should_not_set_summary_from_partial` | 可选：保留已流式 partial 或清空（当前实现保留 `streamText` 直至 unmount abort） | 行为快照 |
| S3 | 组件 unmount 自动 abort | `onUnmounted_aborts_inflight_stream` | 无泄漏请求、无 setState after unmount | 组件单测 |
| S4 | fetch 抛错且 signal.aborted | `network_error_when_aborted_silent` | 不调用 `onError` | `api.ts` 分支 |

### 🟠 P1 · 入口与空数据

| # | 场景 | 必测 case | 期望结果 | 覆盖层 |
| --- | --- | --- | --- | --- |
| E1 | overview 全零 / 无记录 | `empty_overview_disables_run_button` | PC：`showAiInsight=false` **或** 面板 `disabled`；Mobile：`runDisabled=true`；`gift-ai-insight-run` 不可点 | E2E / 单测 |
| E2 | overview 有任一非零指标 | `non_empty_overview_enables_insight` | 展示面板且按钮可点 | E2E |
| E3 | Mobile 自行拉 overview | `mobile_loadContext_uses_analysis_api_not_dashboard_page` | 请求 `/gift-analysis/overview`，**禁止**用 Dashboard record-page 截断加总 | 代码审查 + mock IT |
| E4 | 流式成功收尾 | `stream_done_fills_summary_and_keypoints` | `gift-ai-insight-summary` / `gift-ai-insight-keypoints` 有内容 | E2E / mock SSE |

### 🟠 P1 · 权限与隐私

| # | 场景 | 必测 case | 期望结果 | 覆盖层 |
| --- | --- | --- | --- | --- |
| P1 | 无 `gift:view` | `no_gift_view_hides_ai_panel` | DOM 无 `gift-ai-insight-panel` / `gift-ai-insight-card`；**不**发 `/ai/chat/stream` | Midscene rbac persona |
| P2 | 有 `gift:view` | `gift_view_shows_panel_when_overview_ok` | 面板可见 | rbac smoke |
| P3 | context 剔除 phone | `sanitize_omits_phone_mobile_keys` | `JSON.stringify(context)` 不含手机号；`*Id`/`id` 为 string | ✅ `buildAnalysisContext.spec.ts` |
| P4 | 请求体 bizType | `request_bizType_gift_analysis` | `bizType === 'gift-analysis'`，`depth === 2` | 单测 |

### 🔴 P0 · AI 不写库（横切不变式）

| # | 场景 | 必测 case | 期望结果 | 覆盖层 |
| --- | --- | --- | --- | --- |
| W1 | 解读前后 gift 表行数 | `ai_insight_does_not_insert_gift_records` | `gift_record_info_t` / `gift_person_info_t` / `gift_event_info_t` count **不变** | 集成（前后 SQL count） |
| W2 | 解读不触发 gift 写 API | `ai_flow_no_gift_crud_http` | 网络仅 `/ai/chat/stream`（+ analysis 读接口）；无 POST/PUT/DELETE gift-* | E2E network 断言 |
| W3 | 前端无 gift 写封装调用 | `gift_ai_module_no_import_gift_mutations` | `src/views/finance/gift/ai/**` 不 import record/person/event 的 add/update/delete | 静态 grep / graphify |
| W4 | AI 服务无 gift mapper | `ai_boot_no_gift_datasource` | `alex_miaosha_ai` 不依赖 finance gift 模块 | 架构审查 |

---

## 2. 字段边界（`buildGiftAnalysisAiRequest` / context）

### 2.1 请求体 `AiAnalyzeReq`

| 字段 | 类型 | 必测边界 | 期望 |
| --- | --- | --- | --- |
| `bizType` | string | 固定 `gift-analysis` | 拒绝其它 bizType（若后端校验） |
| `content` | string | 非空固定提示语 | 正常透传 |
| `depth` | number | `2` | 固定 |
| `context.overview` | object | `{}`、全零、含 BigInt 级 `*Id` | ID string 化；可空 overview 仍构建请求 |
| `context.trend` 等 | optional | undefined / 空数组 / 嵌套 person | 递归 sanitize |

### 2.2 `sanitizeForAiContext` 七点法

| 点 | 输入 | 期望 |
| --- | --- | --- |
| phone 顶层 | `{ phone: '138...' }` | key 删除 |
| mobile 变体 | `{ mobilePhone, hostMobile }` | key 删除 |
| id 数字 | `{ id: 42, personId: 9007199254740991 }` | `'42'`, `'9007199254740991'` |
| 嵌套数组 | `personRanking: [{ phone, id }]` | 递归处理 |
| 非对象 | `null`, `undefined`, `100` | 原样返回 |
| 合法金额 | `{ receiveAmount: 888.88 }` | 保留 |
| 序列化安全 | 含 emoji 姓名 | 不抛错 |

---

## 3. 状态机（SSE 解读流程）

```
[ idle ]
   │ 点击「AI 解读」（需 gift:view + overview 非空 + 非 disabled）
   ▼
[ loading ] ──Abort──► [ idle ]（静默，loading=false）
   │ onDelta*
   │ onDone ─────────► [ done ]（summary + keyPoints，loading=false）
   │ onError ────────► [ error ]（面板 error + toast，loading=false）
   │ HTTP≠2xx ───────► [ error ]
   │ token 空 ───────► [ error ] code 401
```

| # | 转移 | 必测 |
| --- | --- | --- |
| L1 | idle → loading | run 后 run 按钮 loading/disabled |
| L2 | loading → idle（abort） | S1 |
| L3 | loading → done | summary 展示 |
| L4 | loading → error | error testid + AntD/Vant toast |
| I1 | disabled 时 run 无效 | `runInsight` early return |
| I2 | 重复 run 先 abort 旧流 | 新 requestId，无并发双写 UI |

---

## 4. 权限矩阵

| 身份 | `gift:view` | overview 有数据 | AI 面板 | 「AI 解读」按钮 | `/ai/chat/stream` |
| --- | --- | --- | --- | --- | --- |
| rbac_readonly（有 view） | ✅ | ✅ | 可见 | 可点 | ✅（只读 AI） |
| rbac_readonly（有 view） | ✅ | ❌ 全零 | PC 隐藏 / Mobile 卡片在但 disabled | disabled | ❌ |
| 无 gift:view | ❌ | ✅ | **隐藏** | — | ❌ |
| 未登录 | — | — | 隐藏或 401 | — | ❌ 401 |

**Midscene persona**：沿用 `super_super` / `rbac_user_manager` / `rbac_readonly`；只读账号必须有 `gift:view` 才测 AI 正例。

---

## 5. 测试用例规划

### 5.1 前端单元测试（Vitest，已有 + 待补）

| 文件 | case | 状态 |
| --- | --- | --- |
| `sseParse.spec.ts` | 半包 meta/delta | ✅ |
| `buildAnalysisContext.spec.ts` | phone 剔除、Id string、可选段 | ✅ |
| `api.spec.ts` | 无 token、HTTP 错误、mock SSE done | ✅ / 待补 abort |
| `GiftAiInsightPanel.spec.ts`（待补） | abort、disabled、unmount | ☐ |
| `GiftAiInsightCard.spec.ts`（待补） | runDisabled、loadContext fail | ☐ |

### 5.2 前端 E2E（Midscene + Playwright）

| ID | 场景 | testid |
| --- | --- | --- |
| `GIFT-AI-SMOKE-001` | 有 view + 有数据 → 解读 → summary 出现 | `gift-ai-insight-run`, `gift-ai-insight-summary` |
| `GIFT-AI-ABORT-001` | 流式中点停止 → loading 结束 | `gift-ai-insight-abort` |
| `GIFT-AI-PERM-001` | 无 `gift:view` → 无面板 | 面板 testid 不存在 |
| `GIFT-AI-EMPTY-001` | 空 overview → 按钮 disabled 或面板不挂载 | `gift-ai-insight-run[disabled]` |
| `GIFT-AI-NO-WRITE-001` | 解读前后 gift record 列表条数不变 | API 断言 |

### 5.3 后端 / 网关（回归，非 gift 模块代码）

| case | 说明 | 引用 |
| --- | --- | --- |
| 网关 SSE 不加密 | stream 经 `/api/am-ai` 透传 | `ai-analyze-stream.md` §6 |
| JWT 缺失 403 | 与 ai-chat 一致 | 网关 IT |
| `bizType` 透传 | AI 服务不要求 gift 专用 prompt | 可选 IT |

### 5.4 集成不变式（finance + ai）

- [ ] `GIFT-AI-IT-NO-DB-WRITE`：`@SpringBootTest` 或 Testcontainers：登录用户触发 AI mock 前后 `SELECT COUNT(*)` from gift 三表不变  
- [ ] Mock `AiAnalyzeApi` / WireMock stream，避免 CI 打外网 LLM

---

## 6. 不测理由（必填）

| 项 | 不测理由 | 由谁兜底 |
| --- | --- | --- |
| 真实 DeepSeek / SenseNova 外网质量 | 费用、不稳定 | 预发手工 + rule-based fallback |
| LLM 文案语义正确性 | 非确定性 | 产品抽检 |
| `recommend-amount` 联动 | P2+ backlog | 后续 checklist |
| 语音 ASR 输入 | backlog | — |
| `tools/ai-chat` 与 gift-ai 代码重复 | 本阶段不强制抽公共包 | 后续 refactor |
| Ant Design / Vant 按钮 loading 动画 | 第三方 | 上游 |
| AI 服务 prompt 微调 | P1 通用 bizType 已够 | 运维配置 |
| 移动端 Haptic 解读按钮 | 已实现 vibrate，非 P0 阻断 | `GIFT-MOBILE-HAPTIC-*` |
| SSE 帧格式细节 | 已由 `ai-analyze-stream.md` 覆盖 | 该 checklist |
| gift CRUD 数据权限 | 不属于 AI 模块 | `gift.md` |

---

## 7. 覆盖率目标

| 范围 | Line | Branch | 备注 |
| --- | --- | --- | --- |
| PC `gift/ai/sseParse.ts` | ≥ 90% | ≥ 85% | 纯函数 |
| PC `gift/ai/buildAnalysisContext.ts` | ≥ 90% | ≥ 85% | sanitize 全分支 |
| PC `gift/ai/api.ts` | ≥ 80% | ≥ 70% | mock fetch |
| Mobile 同路径 | 同上 | 同上 | 契约对齐 PC |
| `GiftAiInsightPanel/Card` | ≥ 70% | ≥ 60% | abort/disabled |

---

## 8. 进度跟踪

| 阶段 | 状态 | 完成日期 |
| --- | --- | --- |
| P0–P1 checklist 编制 | ✅ | 2026-09-01 |
| sseParse + buildAnalysisContext 单测 | ✅ | 2026-09-01 |
| api.spec abort / 401 补全 | ☐ | |
| Insight 组件单测 | ☐ | |
| Midscene GIFT-AI-* E2E | ☐ | |
| NO-DB-WRITE 集成 IT | ☐ | |

---

## 9. 修订记录

| 版本 | 日期 | 修改人 | 内容 |
| --- | --- | --- | --- |
| v1.0 | 2026-09-01 | alex | Task 7：Gift AI P0–P1 checklist 首版 |
