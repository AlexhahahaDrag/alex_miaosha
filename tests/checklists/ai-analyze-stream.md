# AI Analyze Stream（SSE 流式）模块测试 Checklist

> **关联项目**：`alex_miaosha_ai`（`ai_api` + `ai_boot`）+ `alex_miaosha_gateway` + PC `ai-chat`  
> **关联文档**：根目录 `TESTING_STANDARD.md`；`docs/superpowers/specs/2026-08-28-ai-analyze-stream-design.md`  
> **关联 checklist**：整包引擎治理见 [`ai-analyze.md`](./ai-analyze.md)  
> **样板属性**：结构参考 `tests/checklists/gift.md`，聚焦 SSE 双模式（batch + stream）  
> **最后更新**：2026-08-28

---

## 0. 元信息

| 项 | 内容 |
| --- | --- |
| 模块名 | ai-analyze-stream（AI 流式分析 / SSE） |
| 业务负责人 | @alex |
| 测试负责人 | @alex |
| 关联需求 | `2026-08-28-ai-analyze-stream-design.md` |
| 后端代码路径 | `alex_miaosha_ai/ai_boot/src/main/java/com/alex/ai/` |
| 网关路径 | `alex_miaosha_gateway/.../filter/GatewayFilter.java` |
| PC 前端路径 | `alex_miaosha_front/src/views/tools/ai-chat/` |
| 整包 API | `POST /api/v1/ai/chat` → `Result<AiAnalyzeResp>`（**不变**） |
| 流式 API | `POST /api/v1/ai/chat/stream` → `text/event-stream`（**新增**） |
| 配置项 | `ai.stream.read-timeout-ms`（默认 120000）；Nacos 真相源 |
| 关键依赖 | `OpenAiCompatibleClient.chatStream`、`AiSseEventWriter`、`AiEngineRouter.analyzeStream` |

---

## 1. Batch 回归（整包 `/chat` 零破坏）

> 流式为增量能力；整包契约、Feign `AiAnalyzeApi`、现有单测必须保持绿。

| # | case | 期望 | 现有覆盖 |
| --- | --- | --- | --- |
| B1 | `POST /chat` 正常 JSON 请求 | `Result.success(AiAnalyzeResp)`，字段与现网一致 | ✅ 既有 Router/Engine 单测 |
| B2 | Feign `AiAnalyzeApi.chat` | 契约未改，无 stream 方法 | ✅ `ai_api` 未变更 |
| B3 | `OpenAiCompatibleClient.chat`（`stream=false`） | 仍 block 等全文，不走 SSE 解析 | ✅ `OpenAiCompatibleClientTest` |
| B4 | Router fallback 四象限（整包） | 与 stream 前行为一致 | ✅ `AiEngineRouterTest` |
| B5 | 占位 Key / 引擎不可用（整包） | fallback 或 500701/500702 | ✅ `AiEngineRouterTest` + `LlmEngineEnablementTest` |
| B6 | JSON 结构化降级（整包 LLM 响应） | 合法 JSON / 纯文本降级 | ☐ `AbstractOpenAiCompatibleAiEngineTest` 待补 |

**回归门禁**：每轮 stream 改动后，`mvn -pl alex_miaosha_ai/ai_boot -am test` 全绿且 B1–B5 无回归。

---

## 2. SSE 事件顺序（`meta → delta* → done | error`）

帧格式：`event: <name>\ndata: <json>\n\n`

| event | data 形状 | 时机 |
| --- | --- | --- |
| `meta` | `{ "requestId", "engine" }` | 开流后立刻 |
| `delta` | `{ "text": "..." }` | 增量原文，0..N 条 |
| `done` | 完整 `AiAnalyzeResp` | 成功收尾后关闭 |
| `error` | `{ "code", "message" }` | 失败后关闭 |

### 2.1 必测 case

| # | case | 期望事件序列 | 现有覆盖 |
| --- | --- | --- | --- |
| S1 | LLM 真流式（两 chunk + DONE） | `meta` → `delta`×2 → `done` | ✅ Client + Engine 间接 |
| S2 | rule-based 直连 | `meta` → `delta`（可选）→ `done` | ✅ `AiEngineRouterStreamTest` |
| S3 | fallback 切 rule-based | `meta` → `delta` → `done`；`meta.engine=rule-based(fallback)` | ✅ Router stream 单测 |
| S4 | fallback=false 引擎不可用 | 仅 `error`（500701），无 `done` | ✅ Router stream 单测 |
| S5 | fallback=false 调用失败 | 仅 `error`（500702） | ✅ Router stream 单测 |
| S6 | 引擎内部 sink.error + fallback=true | 可能双 `meta`，最终 `done` | ✅ `enabledEngineSinkError_fallbackEnabled` |
| S7 | 空 delta 厂商响应 | `meta` → `done`（delta 可 0 条） | ☐ MockWebServer 待补 |
| S8 | 连接超时 / SseEmitter 超时 | 连接关闭或 `completeWithError` | ☐ Controller IT 待补 |

### 2.2 禁止行为

- 在 `done` 或 `error` 之后继续发 `delta`
- 前端依赖厂商原始 `[DONE]`（应由服务端吸收）
- 无 `meta` 直接发 `delta`

---

## 3. `done` 完整性（完整 `AiAnalyzeResp`）

| 字段 | 要求 |
| --- | --- |
| `requestId` | 与 `meta.requestId` 一致 |
| `engine` | 实际执行引擎（含 `rule-based(fallback)`） |
| `summary` | 与流式拼接全文一致，或 JSON 解析后的规范化结果 |
| `keyPoints` | 来自现有 JSON 解析 / 降级逻辑（非法 JSON → `[]`） |
| `latencyMs` | ≥ 0，合理范围 |

| # | case | 期望 | 现有覆盖 |
| --- | --- | --- | --- |
| D1 | 厂商返回合法 JSON 全文 | `summary` + `keyPoints` 填充 | ☐ Engine 单测待补 |
| D2 | 厂商返回纯文本 | `summary=全文`，`keyPoints=[]` | ☐ Engine 单测待补 |
| D3 | rule-based `done` | 含规则摘要与 engine=`rule-based` | ✅ Router stream fallback |
| D4 | fallback `done` | engine=`rule-based(fallback)`，摘要含回退语义 | ✅ Router stream |
| D5 | `done` 后连接关闭 | `SseEmitter.complete()` 仅一次 | ✅ `AiSseEventWriter` 逻辑 |

---

## 4. Fallback 矩阵（stream 与整包对齐）

默认 `ai.fallback.enabled=true`。

| 场景 | `fallback=true` | `fallback=false` |
| --- | --- | --- |
| 引擎 key 未知 / Bean 不存在 | `meta` + rule-based 流 → `done` | `error` 500701 |
| 目标引擎未启用（占位 Key） | rule-based 流 → `done` | `error` 500701 |
| 远端调用失败 / sink.error | rule-based 流 → `done` | `error` 500702 |
| 目标即 `rule-based` | 直接 rule-based 流 | 同左 |

| # | case | 期望 | 现有覆盖 |
| --- | --- | --- | --- |
| F1 | unknown + fallback=true | `rule-based(fallback)` + `done` | ✅ |
| F2 | unknown + fallback=false | `error` 500701 | ✅ |
| F3 | enabled throws + fallback=true | `rule-based(fallback)` | ✅ |
| F4 | enabled throws + fallback=false | `error` 500702 | ✅ |
| F5 | sink.error + fallback=true | 回退 rule-based | ✅ |
| F6 | 未启用引擎 + fallback=true | rule-based 流 | ☐ 待 Router stream 补 |
| F7 | 未启用引擎 + fallback=false | 500701 | ☐ 待补 |

---

## 5. Rule-based 收尾路径

无厂商外呼；保证最小可用 SSE 序列。

| # | case | 期望 | 现有覆盖 |
| --- | --- | --- | --- |
| R1 | `req.engine=rule-based` | `meta.engine=rule-based`，`done` 含规则摘要 | ☐ 显式单测待补 |
| R2 | 默认引擎 rule-based | 同 R1 | ✅ Router 间接 |
| R3 | fallback 到 rule-based | 可选短 `delta`，`done.engine=rule-based(fallback)` | ✅ |
| R4 | 空 content | 不 NPE，`done` 仍返回 | ☐ 待补 |
| R5 | 超长 content | 规则截断/摘要仍稳定 | ☐ 待补 |

---

## 6. 网关不加密 / 不 buffer（SSE 透传）

`GatewayFilter.secretOut` 对普通 JSON 会 `fluxBody.buffer()` 后加密，**会破坏 SSE**。

跳过条件（任一命中）：

1. 路径匹配 `/**/ai/chat/stream`
2. 响应 `Content-Type` 含 `text/event-stream`

| # | case | 期望 | 现有覆盖 |
| --- | --- | --- | --- |
| G1 | `POST .../ai/chat/stream` 经网关 | 不进入 buffer 加密分支 | ☐ 网关单测 / 手工 |
| G2 | 响应头 `text/event-stream` | `shouldSkipResponseEncryption=true` | 代码审查 ✅ |
| G3 | 中途可见 delta（非整包等到结束） | 浏览器/ curl 逐帧收到 | ☐ 预发手工 |
| G4 | 普通 `/ai/chat` 仍加密 | 行为不变 | ☐ 回归手工 |
| G5 | 网关鉴权失败 | 普通 HTTP 403，非 SSE | ☐ 手工 |

---

## 7. 配置与超时

| 配置项 | 默认 | 说明 |
| --- | --- | --- |
| `ai.stream.read-timeout-ms` | 120000 | 流式读超时，大于普通 `timeout-ms` |
| `ai.deepseek.timeout-ms` | 15000 | 厂商 connect/response 基线 |
| `ai.sensenova.timeout-ms` | 15000 | 同上 |

Client 使用 `Math.max(props.timeoutMs, readTimeoutMs)` 作为流式 responseTimeout。

| # | case | 期望 | 现有覆盖 |
| --- | --- | --- | --- |
| T1 | `readTimeoutMs=120000` 正常流 | 多 chunk 完整接收 | ✅ Client stream test |
| T2 | HTTP 500 厂商 | 抛异常，不走 onComplete | ✅ Client stream test |
| T3 | SSE 行解析（delta / DONE / ping） | 正确提取 content | ✅ `OpenAiStreamChunkParserTest` |

---

## 8. 测试用例规划

### 8.1 后端单元测试（JUnit5 + Mockito + MockWebServer）

| 类 | 职责 | 状态 |
| --- | --- | --- |
| `OpenAiStreamChunkParserTest` | SSE 行解析、DONE、空行 | ✅ |
| `OpenAiCompatibleClientStreamTest` | stream=true、多 delta、500 | ✅ |
| `AiEngineRouterStreamTest` | stream fallback 五场景 | ✅ |
| `AiEngineRouterTest` | 整包 fallback（batch 回归） | ✅ |
| `OpenAiCompatibleClientTest` | 整包 chat（batch 回归） | ✅ |
| `AbstractOpenAiCompatibleAiEngineStreamTest`（待补） | 全文拼 + JSON → done | ☐ |
| `AiSseEventWriterTest`（待补） | meta/delta/done/error 序列化 | ☐ |

### 8.2 轻集成（MockMvc + SseEmitter，25%）

- `POST /chat/stream` MockMvc 断言 `Content-Type` 与事件顺序
- 禁止 CI 打真实 DeepSeek / SenseNova 外网

### 8.3 网关（可选 5%）

- `GatewayFilterTest.shouldSkipEncryption_forSseStreamPath`
- 预发：curl `-N` 验证逐帧 delta

### 8.4 前端（PC `ai-chat`，后续专项）

- `sseParse.ts` 半包/粘包解析
- `responseMode: batch | stream` UI 状态
- `AbortController` 取消

---

## 9. 不测理由（必填）

| 项 | 不测理由 | 由谁兜底 |
| --- | --- | --- |
| 真实 DeepSeek / SenseNova 外网 SSE 联调 | 费用、不稳定、无 CI 密钥 | 预发手工 + Nacos 密文 |
| WebSocket 通道 | Non-Goals | — |
| Feign 流式调用 | 设计锁定仅整包 Feign | `AiAnalyzeApi` 不变 |
| 移动端 ai-chat 对齐 | 本轮 Non-Goals | 后续移动专项 |
| 伪流式（整包后切片） | 非主路径；真流式已落地 | — |
| Tomcat / Reactor Netty 线程模型内部 | 第三方 / 框架 | Spring |
| `SseEmitter` 框架实现细节 | Spring MVC 框架 | 框架 |
| Nacos 动态刷新 stream 超时 | 基础设施 | 运维冒烟 |
| 网关全量加密矩阵 | 网关模块独立演进 | gateway 模块 / 手工 G4 |
| 前端 Midscene E2E（ai-chat 打字机） | 本轮后端治理为主 | FE 专项 + checklist §8.4 |
| 中途断连下游 cancel 订阅 | 实现复杂、收益有限 | 后续 + AbortController |

---

## 10. 覆盖率目标

| 范围 | Line | Branch | 备注 |
| --- | --- | --- | --- |
| `stream/*` + `AiSseEventWriter` | ≥ 80% | ≥ 70% | SSE 写入 |
| `client/openai/OpenAiStreamChunkParser` | ≥ 90% | ≥ 85% | 小纯函数 |
| `client/openai/OpenAiCompatibleClient`（stream 分支） | ≥ 80% | ≥ 70% | MockWebServer |
| `engine/AiEngineRouter`（analyzeStream） | ≥ 85% | ≥ 75% | fallback 全分支 |
| `engine/impl/*` stream 路径 | ≥ 75% | ≥ 65% | JSON → done 待补 |

---

## 11. 进度跟踪

| 阶段 | 状态 | 完成日期 |
| --- | --- | --- |
| 设计文档锁定 | ✅ | 2026-08-28 |
| Client/Router/Parser 单测 | ✅ | 2026-08-28 |
| Controller + SseEmitter | ✅ | 2026-08-28 |
| 网关 SSE 跳过加密 | ✅ | 2026-08-28 |
| checklist（本文件） | ✅ | 2026-08-28 |
| yaml 注释（stream 超时 + 路径） | ✅ | 2026-08-28 |
| Engine stream JSON → done 单测 | ☐ | |
| MockMvc SSE IT | ☐ | |
| PC ai-chat stream E2E | ☐ | |

---

## 12. 修订记录

| 版本 | 日期 | 修改人 | 内容 |
| --- | --- | --- | --- |
| v1.0 | 2026-08-28 | alex | Task 7：SSE 流式 checklist 首版 |
