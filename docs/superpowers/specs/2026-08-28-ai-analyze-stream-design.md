# Design: AI Analyze 双模式返回（整包 + SSE 流式）

**Date:** 2026-08-28  
**Status:** Approved  
**Module:** `alex_miaosha_ai` + Gateway + PC `ai-chat`  
**Approach:** 方案 1 — 双接口；下游真流式（`stream:true`）；`done` 带完整 `AiAnalyzeResp`

## 0. Decisions (locked)

| 项 | 选择 |
|----|------|
| 流式内容 | A：原文 token 打字机；结束后再结构化 |
| 通道 | C：保留 `/analyze` 整包 + 新增 `/analyze/stream` SSE |
| 收尾 | A：最后一帧 `event:done` 带完整 `AiAnalyzeResp` |
| 实现路径 | 方案 1：真流式转发厂商 SSE，非伪切片 |
| Feign | 仅整包 `/analyze`，不加 stream |

## 1. Problem

当前 AI 全链路同步整包：

- 前端 `postData` → `POST /api/v1/ai/analyze` → `Result<AiAnalyzeResp>`
- `OpenAiCompatibleClient`：`stream=false` + `block()` 等全文
- `ai-chat` 需等 Promise 结束后才展示，无打字机体验

希望前端可按类型选择：**流式边出边显**，或 **等完一次性返回**。

## 2. Goals

1. 保留现有整包接口与 Feign 契约，零破坏。
2. 新增 SSE 流式接口；下游对 OpenAI 兼容厂商使用 `stream:true`。
3. 流式过程推送原文 delta；结束推送完整 `AiAnalyzeResp`（含 `summary`/`keyPoints`）。
4. Router `fallback` 语义与整包对齐。
5. 网关对 SSE **透传**，禁止整包加密缓冲。
6. PC `ai-chat` 支持 `batch | stream` 切换。

## 3. Non-Goals

- WebSocket
- Feign 流式
- 移动端对齐（本轮）
- 伪流式作为主路径
- 真外网厂商作为 CI 门禁

## 4. Current State (as-is)

```
前端 analyzeAi (JSON)
  → Gateway
  → AiAnalyzeController.analyze
  → Result.success(AiAnalyzeResp)
  → OpenAiCompatibleClient.chat (stream=false, block)
```

## 5. Target Architecture (to-be)

### 5.1 Batch（不变）

`POST /api/v1/ai/analyze`  
`Content-Type: application/json`  
`Result<AiAnalyzeResp>`

### 5.2 Stream（新增）

`POST /api/v1/ai/analyze/stream`  
请求体：与整包相同的 `AiAnalyzeReq`（用 URL 区分模式，不强制新必填字段）  
响应：`Content-Type: text/event-stream; charset=UTF-8`

```
前端 fetch POST + Authorization
  → Gateway（跳过 buffer/加密）
  → Controller.analyzeStream (SseEmitter 等)
  → Router.analyzeStream
  → Engine.analyzeStream
  → Client.chatStream (厂商 stream:true)
       ├─ delta.content → event:delta
       └─ 结束拼全文 → 现有 JSON 解析 → event:done(AiAnalyzeResp)
```

## 6. SSE Event Contract

帧格式：`event: <name>\ndata: <json>\n\n`

| event | data | 时机 |
|-------|------|------|
| `meta` | `{ "requestId", "engine" }` | 开流后立刻 |
| `delta` | `{ "text": "..." }` | 增量原文，可多条 |
| `done` | 完整 `AiAnalyzeResp` | 收尾后关闭连接 |
| `error` | `{ "code", "message" }` | 失败后关闭连接 |

说明：

- 不依赖前端解析厂商原始 `[DONE]`；服务端吸收后发 `done`/`error`。
- `done.summary` 应与流式拼出的全文一致，或为其规范化解析结果；`keyPoints` 来自现有 JSON 解析/降级逻辑。
- `rule-based`：无厂商流时保证 `meta` + `done`（可选短 `delta`）；实现计划钉死是否切片。

## 7. Backend Components

| 组件 | 变更 |
|------|------|
| `AiAnalyzeController` | 新增 `analyzeStream` |
| `AiEngine` | 可选 `analyzeStream`；默认策略在实现计划钉死 |
| `AbstractOpenAiCompatibleAiEngine` | 真流式 + 结束复用解析 |
| `OpenAiCompatibleClient` | `chatStream(...)`；`stream=true`；解析 `choices[].delta.content` |
| `RuleBasedAiEngine` | 无厂商流的收尾路径 |
| `AiEngineRouter` | `analyzeStream` + fallback |
| `AiAnalyzeApi` | 不改 |

### Constraints

- 流式读超时：建议独立配置（如 `ai.stream.read-timeout-ms`，默认大于普通 `timeoutMs`，例 120000）。
- 线程模型：避免堵死 Tomcat 工作线程（实现计划钉死）。
- 鉴权：与 `/analyze` 相同；前端用 fetch + Authorization（不用裸 EventSource）。

## 8. Gateway

`GatewayFilter.secretOut` 当前对非文件响应 `fluxBody.buffer()` 后加密，会破坏 SSE。

必须：

- path 匹配 `**/ai/analyze/stream` 和/或 `Content-Type: text/event-stream` 时 **跳过 buffer 与加密**，直接 `super.writeWith(body)`。
- 校验路由/响应超时对长流足够。

## 9. Frontend (`ai-chat`)

| 项 | 设计 |
|----|------|
| UI 开关 | `responseMode: 'batch' \| 'stream'`（可本地持久化） |
| batch | 现有 `analyzeAi` |
| stream | `analyzeAiStream(req, handlers)`：fetch + ReadableStream 解析 SSE |
| UI | `delta` 追加助手消息；`done` 用完整 Resp 收尾（含 keyPoints）；`error` → error 态 |
| 取消 | `AbortController`（可后续加「停止生成」） |
| 类型 | `engine` 补 `sensenova`；模式用 URL 区分 |

## 10. Error & Fallback

| 场景 | stream |
|------|--------|
| `fallback=true`，引擎不可用/调用失败 | 切 rule-based 流或直接 `done`；`meta.engine` 可标 `rule-based(fallback)` |
| `fallback=false` | `event:error`（`500701`/`500702`），关流 |
| 网关/鉴权失败 | 普通 HTTP 错误（非 SSE） |
| 中途断连 | 前端 error；服务端尽量 cancel 下游订阅 |

## 11. Testing

### Checklist

`tests/checklists/ai-analyze-stream.md`（或并入 `ai-analyze.md`）：

- batch 回归
- `meta → delta* → done` 顺序
- `done` 完整性
- fallback 矩阵
- rule-based 收尾
- 网关不加密 buffer
- 不测：真外网 CI、WebSocket、移动端

### Automated

- 单元：SSE 行解析、全文 JSON → done
- 单元：Router stream fallback
- MockWebServer：厂商多 chunk + DONE
- 前端：SSE 解析半包/粘包；stream UI 状态（可选）

### Success Criteria

1. `/analyze` 兼容现网与 Feign  
2. `/analyze/stream` 真流式，可打字机展示  
3. `done` = 完整 `AiAnalyzeResp`  
4. 网关 SSE 透传  
5. fallback 与整包一致  
6. Mock 测试可绿  

## 12. Risks

| 风险 | 缓解 |
|------|------|
| 网关漏跳过 | 手工/自动化确认中途可见 delta |
| 超时过短 | 独立 stream 读超时 |
| 加密与 SSE 混用 | path + content-type 双条件 |

## 13. Open Implementation Notes (non-blocking)

- Controller 用 `SseEmitter` vs 其它 MVC 流式 API：实现计划按 Spring Boot 2.7 选型钉死。
- rule-based 是否发短 delta：实现计划二选一。
- 是否在 `AiAnalyzeReq` 增加可选 `responseMode` 仅作文档/透传：非必须（URL 已区分）。
