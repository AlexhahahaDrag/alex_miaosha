# AI Analyze Stream (Batch + SSE) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在保留 `POST /ai/analyze` 整包契约的前提下，新增真流式 `POST /ai/analyze/stream`（SSE），并让 PC `ai-chat` 可选 batch/stream；网关跳过 SSE 加密缓冲。

**Architecture:** Controller 用 `SseEmitter` 推 `meta/delta/done/error`；Router 复用引擎选择与 fallback；LLM 引擎经 `OpenAiCompatibleClient.chatStream`（厂商 `stream:true`）转发 delta，结束后复用现有 JSON 解析产出 `done`；Feign 仅整包。

**Tech Stack:** Spring Boot 2.7 · SseEmitter · WebClient · MockWebServer · Vue3 · fetch ReadableStream · Gateway WebFlux filter

**Spec:** `docs/superpowers/specs/2026-08-28-ai-analyze-stream-design.md`

## Global Constraints

- Spec 为准；Non-Goals：WebSocket、Feign 流式、移动端、伪流式主路径、真外网 CI。
- JAVA_HOME：`C:\Program Files\Java\jdk-17`。
- Commit：用户未明确要求前不提交；无 `Co-authored-by: Cursor`。
- `/analyze` 行为与 Feign **零破坏**。
- 前端 stream 必须用 **fetch + Authorization**（不用裸 EventSource）。
- 网关 SSE 路径必须跳过 `secretOut` 的 `buffer()` 加密。

### Spec → Task 映射

| Spec | Task |
|------|------|
| §6 事件契约 / 解析工具 | Task 1 |
| §7 Client.chatStream | Task 2 |
| §7 Engine + Router stream + fallback | Task 3 |
| §5.2 Controller | Task 4 |
| §8 Gateway | Task 5 |
| §9 前端 ai-chat | Task 6 |
| §11 Checklist + stream timeout 配置 | Task 7 |

### File map

| 路径 | 职责 |
|------|------|
| `ai_boot/.../stream/AiStreamSink.java` | 回调：meta/delta/done/error |
| `ai_boot/.../stream/AiSseEventWriter.java` | 写 SseEmitter 帧 |
| `ai_boot/.../client/openai/OpenAiStreamChunkParser.java` | 解析厂商 SSE 行 → delta text |
| `ai_boot/.../client/openai/OpenAiCompatibleClient.java` | `chatStream` |
| `ai_boot/.../engine/AiEngine.java` | default `analyzeStream` |
| `.../AbstractOpenAiCompatibleAiEngine.java` | 真流式 + done 解析 |
| `.../RuleBasedAiEngine.java` | meta + 可选 1 条 delta + done |
| `.../AiEngineRouter.java` | `analyzeStream` + fallback |
| `.../AiAnalyzeController.java` | `/analyze/stream` |
| `.../config/AiProperties.java` | `stream.readTimeoutMs` 默认 120000 |
| `gateway/.../GatewayFilter.java` | 跳过 SSE buffer/加密 |
| `front/.../ai-chat/api/index.ts` | `analyzeAiStream` + SSE parse |
| `front/.../ai-chat/index.vue` | responseMode 开关 |
| `tests/checklists/ai-analyze-stream.md` | 清单 |

### Locked decisions (from §13)

- Controller：**`SseEmitter`**（超时取 `ai.stream.read-timeout-ms`）。
- rule-based：发 `meta` → **一条** `delta`（全文 summary）→ `done`（完整 Resp）。
- 不在 `AiAnalyzeReq` 强制加 `responseMode`（URL 区分）。

---

### Task 1: Stream 契约类型 + 厂商 SSE 行解析

**Files:**

- Create: `alex_miaosha_ai/ai_boot/src/main/java/com/alex/ai/stream/AiStreamSink.java`
- Create: `alex_miaosha_ai/ai_boot/src/main/java/com/alex/ai/client/openai/OpenAiStreamChunkParser.java`
- Create: `alex_miaosha_ai/ai_boot/src/test/java/com/alex/ai/client/openai/OpenAiStreamChunkParserTest.java`

**Interfaces:**

```java
public interface AiStreamSink {
    void meta(String requestId, String engine);
    void delta(String text);
    void done(AiAnalyzeResp resp);
    void error(String code, String message);
}
```

```java
public final class OpenAiStreamChunkParser {
    /** 输入一行（可含 data: 前缀或裸 JSON）；返回 delta content，无则 empty */
    public static Optional<String> parseDeltaText(String line);
    /** 是否结束标记：[DONE] */
    public static boolean isDoneLine(String line);
}
```

解析规则（OpenAI 兼容）：

- 忽略空行、以 `:` 开头的注释行
- `data: [DONE]` → done
- `data: {json}` → 读 `choices[0].delta.content`（缺省/空 → empty）
- 非法 JSON → empty（不抛；由上层决定）

- [ ] **Step 1: 红灯测试** — 合法 delta、`[DONE]`、空 content、半残 JSON

- [ ] **Step 2: 实现 Parser + Sink 接口 → 测试 PASS**

```bat
cmd.exe /c "set JAVA_HOME=C:\Program Files\Java\jdk-17&& cd /d F:\workplace\project\myself\backend\alex_miaosha && mvn -pl alex_miaosha_ai/ai_boot -am test -Dtest=OpenAiStreamChunkParserTest -DfailIfNoTests=false"
```

- [ ] **Step 3:** 不 commit（除非用户要求）

---

### Task 2: OpenAiCompatibleClient.chatStream

**Files:**

- Modify: `OpenAiCompatibleClient.java`
- Modify: `AiProperties.java` — 增加：

```java
private Stream stream = new Stream();

@Data
public static class Stream {
    /** 流式读超时（毫秒），默认 120000 */
    private int readTimeoutMs = 120000;
}
```

- Create: `.../OpenAiCompatibleClientStreamTest.java`（MockWebServer）

**Interfaces:**

```java
public void chatStream(AiAnalyzeReq req,
                       OpenAiCompatibleProperties props,
                       String providerLabel,
                       Consumer<String> onDelta,
                       Runnable onComplete);
```

行为：

- `buildRequestBody` 增加重载或参数 `boolean stream`；stream 路径设 `request.setStream(true)`
- WebClient：`Accept: text/event-stream`，`bodyToFlux(String.class)` 或按行拆分；`responseTimeout` 用 `Math.max(props.timeoutMs, aiProperties.stream.readTimeoutMs)` — Client 需能读到 stream 超时：构造注入 `AiProperties` 或方法入参 `int readTimeoutMs`
- **钉死：** `chatStream(..., int readTimeoutMs, ...)` 由上层传入 `ai.getStream().getReadTimeoutMs()`
- 每行交给 `OpenAiStreamChunkParser`；有 text 则 `onDelta.accept(text)`；遇 DONE 或 flux complete → `onComplete.run()`
- HTTP 非 2xx → 抛 `IllegalStateException`（含 providerLabel），**不**调 onComplete
- **不改变** 现有 `chat()` 行为

MockWebServer 用例：

1. 两帧 delta + DONE → onDelta 两次 + onComplete  
2. HTTP 500 → 抛异常且 onComplete 不调  

- [ ] **Step 1–4: TDD → GREEN**

- [ ] **Step 5:** 不 commit

---

### Task 3: Engine + Router analyzeStream

**Files:**

- Modify: `AiEngine.java` — default：

```java
default void analyzeStream(AiAnalyzeReq req, String requestId, long start, AiStreamSink sink) {
    try {
        sink.meta(requestId, key());
        AiAnalyzeResp resp = analyze(req, requestId, start);
        if (resp.getSummary() != null && !resp.getSummary().isEmpty()) {
            sink.delta(resp.getSummary());
        }
        sink.done(resp);
    } catch (Exception e) {
        sink.error("500702", e.getMessage() == null ? "AI 引擎调用失败" : e.getMessage());
    }
}
```

- Modify: `AbstractOpenAiCompatibleAiEngine` — override：`meta` → `chatStream` 累积 StringBuilder → deltas → `toAnalyzeRespFromLlm`（抽现有私有方法为 `protected`）→ `done`；异常 `sink.error`
- Modify: `RuleBasedAiEngine` — 可依赖 default，或显式同逻辑（推荐依赖 default）
- Modify: `AiEngineRouter` — 新增 `analyzeStream(...)`：
  - 选择引擎逻辑对齐 `analyze`
  - 不可用 + fallback true → `ruleBasedAiEngine.analyzeStream`（meta.engine 可用 `rule-based(fallback)`：在 rule 路径前 `sink.meta(requestId, "rule-based(fallback)")` 或 done.engine 字段体现；**钉死：** `done`/`meta` 的 engine 与整包一致用 `rule-based(fallback)` 仅当从 LLM 失败回退时——实现：回退前先 `sink.meta(id, "rule-based(fallback)")` 再调 rule 的 analyze 拼 done，避免 default 再发一次 meta；更简：**回退时直接调 rule.analyze 得 Resp，手动 meta+delta+done**，Resp.engine 设为 `rule-based(fallback)`
  - 不可用 + fallback false → `sink.error("500701", ...)`
  - LLM `analyzeStream` 内部失败：fallback true → 同上回退；false → error `500702`
- Create: `AiEngineRouterStreamTest.java` — 至少：未知引擎 fallback on/off；mock 引擎 stream 失败 fallback on

**钉死回退写法（Router）：**

```java
private void emitRuleFallback(AiAnalyzeReq req, String requestId, long start, AiStreamSink sink) {
    sink.meta(requestId, "rule-based(fallback)");
    AiAnalyzeResp resp = ruleBasedAiEngine.analyze(req, requestId, start);
    resp.setEngine("rule-based(fallback)");
    if (resp.getSummary() != null && !resp.getSummary().isEmpty()) {
        sink.delta(resp.getSummary());
    }
    sink.done(resp);
}
```

- [ ] **Step 1–4: TDD Router stream → 实现 Engine override → GREEN**

```bat
mvn -pl alex_miaosha_ai/ai_boot -am test -Dtest=AiEngineRouterStreamTest,AiEngineRouterTest -DfailIfNoTests=false
```

- [ ] **Step 5:** 不 commit

---

### Task 4: Controller `/analyze/stream`

**Files:**

- Modify: `AiAnalyzeController.java`
- Create: `ai_boot/.../stream/AiSseEventWriter.java` — 将 Sink 接到 `SseEmitter`

**Interfaces:**

```java
@PostMapping(value = "/analyze/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter analyzeStream(@RequestBody AiAnalyzeReq req) { ... }
```

行为：

- `long timeout = aiProperties.getStream().getReadTimeoutMs()`（≤0 则 120000）
- `SseEmitter emitter = new SseEmitter(timeout)`
- 在**工作线程**执行（`CompletableFuture.runAsync` 或注入的 `TaskExecutor`）：生成 requestId/start → `router.analyzeStream` → writer
- `AiSseEventWriter`：

```java
// event name + JSON data
emitter.send(SseEmitter.event().name("meta").data(json));
emitter.send(SseEmitter.event().name("delta").data(json));
emitter.send(SseEmitter.event().name("done").data(resp)); // Jackson 序列化 AiAnalyzeResp
emitter.send(SseEmitter.event().name("error").data(json));
// done/error 后 emitter.complete()
```

- `emitter.onTimeout/onError` → completeWithError / complete
- 保持 `/analyze` 不变

- [ ] **Step 1:** 实现 Writer + Controller；编译通过；可选 MockMvc 轻测 produces SSE（若成本高可依赖 Task 3 单测 + 手工）

- [ ] **Step 2:** 不 commit

---

### Task 5: Gateway 跳过 SSE 加密缓冲

**Files:**

- Modify: `alex_miaosha_gateway/.../GatewayFilter.java`

**Changes:**

1. 增加路径模式，例如 `SSE_STREAM_PATHS = { "/**/ai/analyze/stream" }`（与 FILE 模式同用 `AntPathMatcher`）
2. `isFileResponse` 重命名或新增 `shouldSkipResponseEncryption(...)`：
   - 原文件判断 **或**
   - path 匹配 SSE 模式 **或**
   - contentType 含 `text/event-stream`
3. `secretOut` 里跳过条件改为 `shouldSkipResponseEncryption`

- [ ] **Step 1:** 实现；若有 Gateway 单测则补 path 匹配断言；否则文档注明手工验证：stream 中途可见 delta

- [ ] **Step 2:** 不 commit

---

### Task 6: 前端 API + ai-chat UI

**Files:**

- Modify: `alex_miaosha_front/src/views/tools/ai-chat/api/index.ts`
- Modify: `alex_miaosha_front/src/views/tools/ai-chat/index.vue`
- Create（可选）: `alex_miaosha_front/src/views/tools/ai-chat/api/sseParse.ts` + 单测（若项目有前端单测习惯；否则解析函数同文件并手工测）

**API:**

```ts
export type AiResponseMode = 'batch' | 'stream';

export interface AiStreamHandlers {
  onMeta?: (meta: { requestId?: string; engine?: string }) => void;
  onDelta?: (text: string) => void;
  onDone?: (resp: AiAnalyzeResp) => void;
  onError?: (err: { code?: string; message?: string }) => void;
}

export function analyzeAiStream(
  req: AiAnalyzeReq,
  handlers: AiStreamHandlers,
  signal?: AbortSignal,
): Promise<void>;
```

实现要点：

- URL：`baseService.ai + '/ai/analyze/stream'`
- headers：与现有请求一致带 Token（从现有 request 工具取 token 的方式对齐项目；若 `postData` 封装难用于流，直接用 `fetch` + 项目 getToken 工具）
- 解析 buffer：按 `\n\n` 拆事件；读 `event:` / `data:` 行
- `engine` 类型注释补 `sensenova`

**UI：**

- `responseMode` ref，默认 `'batch'`；设置区 Radio/Switch
- `onSend`：batch 走原逻辑；stream：`assistantMsg.content` 初始 `''`，`onDelta` 追加，`onDone` 可用 `buildAssistantText(data)` 覆盖或保留流式文本并附 keyPoints，`onError` 设 error
- `AbortController`：组件卸载或再次发送时 abort 上一次

- [ ] **Step 1:** 实现 API + UI
- [ ] **Step 2:** `npm run lint`（front 目录，按项目脚本）
- [ ] **Step 3:** 不 commit

---

### Task 7: Checklist + Nacos/yaml 注释

**Files:**

- Create: `tests/checklists/ai-analyze-stream.md`
- Modify: `application-*.yaml` AI 注释区追加：

```yaml
# ai.stream.read-timeout-ms: 120000
# POST /api/v1/ai/analyze/stream → SSE (meta/delta/done/error)
```

- 更新 `tests/checklists/ai-analyze.md` 增加指向 stream checklist 的一行（若存在）

Checklist 必含：batch 回归、事件顺序、done 完整、fallback、rule-based、网关不加密、不测理由。

- [ ] **Step 1:** 写文档
- [ ] **Step 2:** 跑 ai_boot 相关测试全绿

```bat
cmd.exe /c "set JAVA_HOME=C:\Program Files\Java\jdk-17&& cd /d F:\workplace\project\myself\backend\alex_miaosha && mvn -pl alex_miaosha_ai/ai_boot -am test -DfailIfNoTests=false"
```

- [ ] **Step 3:** 不 commit

---

## Self-Review (plan vs spec)

| Spec 要求 | Task |
|-----------|------|
| 双接口 | 4 |
| SSE 事件契约 | 1, 4 |
| 真流式 chatStream | 2, 3 |
| done 完整 Resp | 3, 4 |
| fallback 对齐 | 3 |
| 网关跳过 buffer | 5 |
| 前端 batch/stream | 6 |
| Feign 不改 | （无 Task 改 Feign）|
| Checklist / 超时配置 | 2, 7 |

无 TBD；SseEmitter / rule-based 单 delta / URL 区分模式已钉死。

---

## Execution Handoff

Plan complete and saved to `docs/superpowers/plans/2026-08-28-ai-analyze-stream.md`.

**Two execution options:**

1. **Subagent-Driven（推荐）** — 每 Task 子代理 + 复核  
2. **Inline Execution** — 本会话连续执行  

回复 **1** / **2** 或 **Proceed**（默认 Inline 从 Task 1）。未授权前不改业务代码。
