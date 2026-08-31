# AI Analyze（引擎治理）模块测试 Checklist

> **关联项目**：`alex_miaosha_ai`（`ai_api` + `ai_boot`）  
> **关联文档**：根目录 `TESTING_STANDARD.md`；`docs/superpowers/specs/2026-08-26-ai-engine-governance-design.md`  
> **样板属性**：结构参考 `tests/checklists/gift.md`，聚焦 AI 引擎路由 / OpenAI 兼容客户端 / Nacos 配置约定  
> **流式专项**：SSE 双模式见 [`ai-analyze-stream.md`](./ai-analyze-stream.md)  
> **最后更新**：2026-08-28

---

## 0. 元信息

| 项 | 内容 |
| --- | --- |
| 模块名 | ai-analyze（AI 分析 / 引擎治理） |
| 业务负责人 | @alex |
| 测试负责人 | @alex |
| 关联需求 | `2026-08-26-ai-engine-governance-design.md` |
| 后端代码路径 | `alex_miaosha_ai/ai_boot/src/main/java/com/alex/ai/` |
| 契约 VO 路径 | `alex_miaosha_ai/ai_api/src/main/java/com/alex/api/ai/vo/` |
| 公共异常 | `alex_miaosha_common/.../exception/AiException.java` |
| 配置真相源 | **Nacos**（服务 `alex-ai-${profile}`；仓库 profile yaml 仅注释，不写完整 `ai:`） |
| 关键依赖 | `OpenAiCompatibleClient`（WebClient）、`AiEngineRouter`、`AiEngineType`、`AiApiKeys` |

---

## 1. 引擎选择（Engine Selection）

### 1.1 优先级

| 优先级 | 来源 | 说明 |
| --- | --- | --- |
| 1 | `AiAnalyzeReq.engine` | 请求级覆盖 |
| 2 | `ai.engine`（Nacos） | 服务默认 |
| 3 | 内置默认 | `AiProperties.engine` 默认 `rule-based` |

### 1.2 合法引擎 key（`AiEngineType`）

| key | displayName | Bean | 备注 |
| --- | --- | --- | --- |
| `deepseek` | DeepSeek | `DeepSeekAiEngine` | OpenAI 兼容 |
| `sensenova` | SenseNova | `SenseNovaAiEngine` | OpenAI 兼容 |
| `rule-based` | RuleBased | `RuleBasedAiEngine` | 本地规则，无外呼 |

- `fromKey` 忽略大小写；未知 / 空白 → `Optional.empty()`。
- 展示名与 key **只**来自 `AiEngineType`，禁止 Router 内硬编码 Map。

### 1.3 必测 case

| # | case | 期望 | 现有覆盖 |
| --- | --- | --- | --- |
| E1 | `req.engine=deepseek` + 已配置 Key | 路由 DeepSeek | Router 间接 |
| E2 | `req.engine` 空，Nacos `ai.engine=sensenova` | 路由 SenseNova | 待 IT |
| E3 | `req.engine=unknown` + fallback=true | `rule-based` | ✅ `AiEngineRouterTest` |
| E4 | `req.engine=unknown` + fallback=false | `AiException` 500701 | ✅ |
| E5 | 目标引擎未启用（占位 Key）+ fallback=true | `rule-based` | 逻辑在 Router |
| E6 | 目标引擎未启用 + fallback=false | `AiException` 500701 | 待补 |
| E7 | `req.engine=rule-based` | 直接规则引擎，不外呼 | 待补 |
| E8 | `AiEngineType.fromKey` 大小写 | 忽略大小写匹配 | ✅ `AiEngineTypeTest` |

---

## 2. 占位 Key 与启用判定（Placeholder Keys）

### 2.1 `AiApiKeys.isConfigured`

Trim 后满足任一则 **未配置**（`isEnabled == false`）：

| 输入 | 判定 |
| --- | --- |
| `null` / `""` / 仅空白 | 未配置 |
| `sk-xxx` / `SK-XXX` | 占位 |
| `changeme` / `your-api-key` / `todo` | 占位 |
| 其他非空串（如 `sk-real-key-value`） | 已配置 |

### 2.2 引擎 `isEnabled` 矩阵

| 引擎 | null props | 占位 Key | 真实 Key |
| --- | --- | --- | --- |
| DeepSeek | false | false | true |
| SenseNova | false | false | true |
| RuleBased | **true** | **true** | **true** |

### 2.3 必测 case

| # | case | 期望 | 现有覆盖 |
| --- | --- | --- | --- |
| K1 | 七种占位 / 空值 | 全部 `isConfigured=false` | ✅ `AiApiKeysTest` |
| K2 | DeepSeek `sk-xxx` | `isEnabled=false` | ✅ `LlmEngineEnablementTest` |
| K3 | SenseNova `sk-xxx` | `isEnabled=false` | ✅ |
| K4 | RuleBased 任意 props | `isEnabled=true` | ✅ |

---

## 3. Fallback 矩阵（`ai.fallback.enabled`）

默认 **true**（兼容现网）；Nacos 可设为 `false` 以强制显式失败。

| 场景 | `fallback.enabled=true` | `fallback.enabled=false` |
| --- | --- | --- |
| 引擎 key 未知 / Bean 不存在 | 回退 `rule-based` | `AiException` `AI_ENGINE_UNAVAILABLE`（500701） |
| 目标引擎未启用（Key 占位/空） | 回退 `rule-based` | `AiException` `AI_ENGINE_UNAVAILABLE`（500701） |
| 远端调用失败（HTTP/解析/空 choices） | 回退 `rule-based(fallback)` + 摘要含 displayName +「回退规则引擎」 | `AiException` `AI_ENGINE_CALL_FAILED`（500702） |
| 目标即 `rule-based` | 直接执行 | 直接执行 |

### 3.1 必测 case

| # | case | 期望 | 现有覆盖 |
| --- | --- | --- | --- |
| F1 | unknown engine + fallback=true | engine=`rule-based` | ✅ |
| F2 | unknown engine + fallback=false | 500701 | ✅ |
| F3 | enabled engine throws + fallback=true | engine=`rule-based(fallback)`，摘要含 DeepSeek | ✅ |
| F4 | enabled engine throws + fallback=false | 500702 | ✅ |
| F5 | 未启用引擎 + fallback=true | rule-based | 待 Router 单测补 |
| F6 | 未启用引擎 + fallback=false | 500701 | 待补 |

---

## 4. JSON 结构化降级（LLM Response Degrade）

LLM 应返回 JSON：`{ "summary": "...", "keyPoints": ["..."] }`（见 `AiPromptBuilder.buildSystemPrompt`）。

| 响应形态 | 行为 | 日志 |
| --- | --- | --- |
| 合法 JSON | 填充 `summary` + `keyPoints` | — |
| 非法 JSON / 非对象 | **降级**：`summary=原始 content`，`keyPoints=[]` | `warn`「结构化 JSON 解析失败」 |
| 空 / 空白 content | `summary="{displayName} 返回为空。"`，`keyPoints=[]` | — |
| HTTP 非 2xx / 空 choices | 抛异常 → 走 Router fallback 或 `AiException` | Router `error` |

### 4.1 必测 case

| # | case | 期望 | 现有覆盖 |
| --- | --- | --- | --- |
| J1 | 合法 JSON content | summary/keyPoints 正确 | 待 Engine 单测 |
| J2 | 纯文本非 JSON | 降级为 summary，keyPoints 空 | 待 Engine 单测 |
| J3 | 空 choices | Client 抛异常 | ✅ `OpenAiCompatibleClientTest` |
| J4 | context 序列化失败 | Prompt 仍生成，不含 context | ✅ `AiPromptBuilderTest` |

---

## 5. 超时按厂商配置（Timeout per Provider）

- `OpenAiCompatibleClient.chat` 每次按 **当前厂商** `OpenAiCompatibleProperties.timeoutMs` 构建 WebClient。
- `timeoutMs` 为 null 或 ≤0 时默认 **15000 ms**（connect + responseTimeout 同源）。
- **禁止**全局只读 `deepseek.timeoutMs` 而忽略 `sensenova.timeoutMs`（历史漏洞已修复）。

| 厂商 | 配置项 | 默认 |
| --- | --- | --- |
| DeepSeek | `ai.deepseek.timeout-ms` | 15000 |
| SenseNova | `ai.sensenova.timeout-ms` | 15000 |

### 5.1 必测 case

| # | case | 期望 | 现有覆盖 |
| --- | --- | --- | --- |
| T1 | `timeoutMs=5000` MockWebServer 正常响应 | 200 内返回 | ✅ Client 测试 setup |
| T2 | `timeoutMs=0` 或 null | 回落 15000 | 待 `AiHttpConfig` 单测 |
| T3 | DeepSeek vs SenseNova 各自 props | 互不影响 | 架构保证；待显式单测 |

---

## 6. Nacos 配置约定（§6 摘要）

仓库 **禁止** 提交完整 `ai:` 或真实 Key；profile yaml 仅保留注释指向：

```yaml
# ai.engine / ai.fallback.enabled / ai.deepseek.* / ai.sensenova.*
# Key 环境变量：AI_DEEPSEEK_API_KEY / AI_SENSENOVA_API_KEY
```

Nacos 建议片段见设计文档 §6（`base-url`、`chat-completions-path`、`api-key`、`model`、`temperature`、`max-tokens`、`timeout-ms`）。

---

## 7. 测试用例规划

### 7.1 后端单元测试（JUnit5 + Mockito，主路径 70%）

| 类 | 职责 | 状态 |
| --- | --- | --- |
| `AiEngineTypeTest` | `fromKey` 大小写 / 未知 | ✅ |
| `AiApiKeysTest` | 占位 Key 判定 | ✅ |
| `AiEngineRouterTest` | fallback 四象限 | ✅ |
| `LlmEngineEnablementTest` | 三引擎 key + isEnabled | ✅ |
| `AiPromptBuilderTest` | prompt 拼装 + context 失败不阻断 | ✅ |
| `OpenAiCompatibleClientTest` | MockWebServer：2xx / 500 / 空 choices | ✅ |
| `AbstractOpenAiCompatibleAiEngineTest`（待补） | JSON 合法 / 非法降级 | ☐ |

### 7.2 轻集成（MockWebServer，25%）

- 禁止 CI 打真实 DeepSeek / SenseNova 外网。
- MockWebServer 覆盖 HTTP 契约即可。

### 7.3 本轮不做（见 §8）

- Midscene / Playwright E2E
- 真实商汤/DeepSeek 联调门禁

---

## 8. 不测理由（必填）

| 项 | 不测理由 | 由谁兜底 |
| --- | --- | --- |
| 真实 DeepSeek / SenseNova 外网联调 | 费用、不稳定、无 CI 密钥 | 预发手工 + Nacos 密文 |
| SSE 流式响应（`stream=true`） | 非 Goals；Client 固定 `stream=false` | 未来专项 |
| ASR / 语音输入 | Non-Goals | — |
| 多轮 Agent / 工具调用 | Non-Goals | — |
| 向量检索 / RAG | Non-Goals | — |
| Midscene / 前端 AI 页面 E2E | 本轮仅后端治理；无前端改动 | 后续 FE 专项 |
| WebClient / Reactor Netty 内部实现 | 第三方库 | Spring |
| `@ConfigurationProperties` 绑定本身 | Spring Boot 框架 | 框架 |
| Nacos 注册与动态刷新 | 基础设施 | 运维 / 集成环境冒烟 |
| `GlobalExceptionHandler` 全量异常矩阵 | common 模块已有模式 | common 模块 |

---

## 9. 覆盖率目标

| 范围 | Line | Branch | 备注 |
| --- | --- | --- | --- |
| `engine/AiEngineRouter` | ≥ 85% | ≥ 75% | fallback 全分支 |
| `engine/impl/*AiEngine` | ≥ 80% | ≥ 70% | JSON 降级待补 |
| `client/openai/*` | ≥ 80% | ≥ 70% | MockWebServer |
| `util/AiApiKeys` | ≥ 90% | ≥ 85% | 小工具类 |
| `config/AiHttpConfig` | ≥ 70% | ≥ 60% | timeout 默认 |

---

## 10. 进度跟踪

| 阶段 | 状态 | 完成日期 |
| --- | --- | --- |
| P1 Router + 占位 Key + AiException | ✅ | 2026-08-26 |
| P2 OpenAI 兼容 Client / 抽象 Engine | ✅ | 2026-08-26 |
| P3 yaml 注释 + Nacos 约定 | ✅ | 2026-08-26 |
| P4 checklist + 单元/MockWebServer | ✅ | 2026-08-26 |
| JSON 降级 Engine 单测 | ☐ | |
| 未启用引擎 fallback 单测补全 | ☐ | |

---

## 11. 修订记录

| 版本 | 日期 | 修改人 | 内容 |
| --- | --- | --- | --- |
| v1.0 | 2026-08-26 | alex | Task 9：Nacos 注释 + checklist 首版 |
