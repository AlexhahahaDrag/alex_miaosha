# Design: alex_miaosha_ai 引擎全量治理

**Date:** 2026-08-26  
**Status:** Approved  
**Module:** `alex_miaosha_ai` (`ai_api` + `ai_boot`)  
**Approach:** 方案 2 — 同目标、分阶段交付  

## 0. Decisions (locked)

| 项 | 选择 |
|----|------|
| 范围 | 全量治理（枚举 + 公共 OpenAI Client + Properties 基类 + 测试 + 配置约定） |
| 配置真相源 | **Nacos**；仓库 profile yaml 不写完整 `ai:` |
| 回退 | `ai.fallback.enabled`，**默认 true** |
| HTTP | 本轮改为 **WebClient**（替换 `aiRestTemplate`） |
| 交付方式 | 分阶段合并，终态一次设计 |

## 1. Problem

当前 AI 微服务可用，但存在结构债与行为坑：

1. `DeepSeekClient` ≈ `SenseNovaClient`，`DeepSeekAiEngine` ≈ `SenseNovaAiEngine`（协议同为 OpenAI Chat Completions）。
2. 引擎 key / 展示名散落在 `key()`、Router `Map`、Swagger 注释中。
3. `AiHttpConfig` 超时只读 `deepseek.timeoutMs`，SenseNova 的 `timeoutMs` 无效。
4. 占位 Key（如 `sk-xxx`）会被 `isEnabled` 判为可用，真调失败后再 fallback。
5. profile yaml 中原有 `ai:` 段已删除；若仅依赖本地 yaml 且无 Nacos，会落到 `rule-based` 默认。
6. LLM JSON 解析失败被静默吞掉；模块几乎无单测。

## 2. Goals

1. 厂商差异收敛为「配置 + `AiEngineType`」；HTTP/协议只保留一份 OpenAI 兼容实现（WebClient）。
2. 引擎标识与失败文案统一来自枚举，消除魔法字符串双源。
3. 回退可配置：默认兼容现网；`fallback=false` 时明确失败（`AiException`）。
4. 占位/空 Key 视为未启用。
5. 超时按**当前选用厂商**的 `timeout-ms` 生效。
6. Nacos 配置约定文档化；仓库不提交真实 Key。
7. 按测试金字塔补 checklist + 单元/轻集成测试（本轮不做 Midscene）。

## 3. Non-Goals

- ASR / 流式 SSE / 多轮 Agent / 向量检索
- 前端改动
- 真实商汤/DeepSeek 外网联调作为 CI 门禁
- 插件化「无限 Provider 注册中心」（YAGNI；方案 3 否决）

## 4. Architecture (终态)

```
AiAnalyzeController / Feign
        │
        ▼
AiAnalyzeServiceImpl
        │
        ▼
AiEngineRouter
  · engine：req > Nacos(ai.engine) > RULE_BASED
  · AiEngineType：key / displayName
  · isEnabled（含占位 Key）
  · ai.fallback.enabled
        │
        ├── RuleBasedAiEngine
        ├── DeepSeekAiEngine  ─┐
        └── SenseNovaAiEngine ─┴─► AbstractOpenAiCompatibleAiEngine
                                      │
                                      ▼
                              OpenAiCompatibleClient (WebClient)
```

**原则**

- Router 只做选择与回退，不碰 HTTP。
- 策略边界仍是 `AiEngine`。
- 配置真相在 Nacos。

## 5. Components & File Map

### 5.1 新增

| 组件 | 建议路径 | 职责 |
|------|----------|------|
| `AiEngineType` | `ai_boot/.../engine/AiEngineType.java` | `DEEPSEEK` / `SENSENOVA` / `RULE_BASED`：`key`、`displayName`、`fromKey` |
| `OpenAiCompatibleProperties` | `ai_boot/.../config/OpenAiCompatibleProperties.java` | baseUrl、path、apiKey、model、temperature、maxTokens、timeoutMs |
| `OpenAiChatCompletionRequest/Response` | `ai_boot/.../client/openai/dto/` | 厂商无关 DTO（由 DeepSeek DTO 迁移更名） |
| `OpenAiCompatibleClient` | `ai_boot/.../client/openai/OpenAiCompatibleClient.java` | WebClient POST chat/completions，解析 `message.content` |
| `AiPromptBuilder` | `ai_boot/.../client/openai/AiPromptBuilder.java` | system/user prompt 唯一实现 |
| `AbstractOpenAiCompatibleAiEngine` | `ai_boot/.../engine/impl/AbstractOpenAiCompatibleAiEngine.java` | model 选择、调 Client、JSON→Resp、空响应文案 |
| `AiException` | `alex_miaosha_common/.../exception/AiException.java` | 对齐 `FinanceException`（code + msg） |
| Handler | `GlobalExceptionHandler` 增加 `@ExceptionHandler(AiException.class)` | 返回 `Result.error(code, msg)` |
| Checklist | `tests/checklists/ai-analyze.md` | 字段/开关矩阵与不测理由 |

### 5.2 改造

| 组件 | 变更 |
|------|------|
| `DeepSeekProperties` / `SenseNovaProperties` | 继承公共 Properties，仅保留默认值差异 |
| `AiProperties` | 增加 `fallbackEnabled`（`ai.fallback.enabled`，默认 `true`） |
| `DeepSeekAiEngine` / `SenseNovaAiEngine` | 变薄：`key()`→枚举；占位 Key；委托抽象基类 |
| `AiHttpConfig` | 删除 `aiRestTemplate`；提供 WebClient 构建能力（按厂商 timeout） |
| `AiEngineRouter` | 枚举展示名；`fallback` 分支；可选启动期 `Map<String, AiEngine>` |
| `AiAnalyzeReq` | Swagger 补充 `sensenova` |
| `application-{dev,test,prod}.yaml` | 不恢复完整 `ai:`；注释指向 Nacos 配置项 |
| `ai_boot/pom.xml` | 引入 WebClient 所需依赖（优先 `spring-webflux` + reactor-netty 作**客户端**；避免把应用改成 WebFlux 服务端。实现计划中写明具体坐标与排除项） |

### 5.3 删除

- `DeepSeekClient`、`SenseNovaClient`
- `client/deepseek/dto/*`（迁移完成后）
- Router 内硬编码 `ENGINE_DISPLAY_NAME` Map

## 6. Configuration (Nacos)

建议片段（DataId 以现网 AI 服务配置为准，实现时对照 Nacos 实际命名写入 checklist/注释）：

```yaml
ai:
  engine: sensenova
  fallback:
    enabled: true
  deepseek:
    base-url: https://api.deepseek.com
    chat-completions-path: /v1/chat/completions
    api-key: ${AI_DEEPSEEK_API_KEY:}
    model: deepseek-chat
    temperature: 0.2
    max-tokens: 1024
    timeout-ms: 15000
  sensenova:
    base-url: https://token.sensenova.cn
    chat-completions-path: /v1/chat/completions
    api-key: ${AI_SENSENOVA_API_KEY:}
    model: sensenova-6.8-flash-lite
    temperature: 0.2
    max-tokens: 1024
    timeout-ms: 15000
```

- Key 仅环境变量 / Nacos 密文；仓库禁止真实密钥。

## 7. Placeholder Key & Enablement

Trim 后满足任一则 `isEnabled == false`：

- `null` / 空串
- 忽略大小写等于：`sk-xxx`、`changeme`、`your-api-key`、`todo`
- 仅空白

判定逻辑集中在一处工具方法（如 `AiApiKeys.isConfigured(String)`），DeepSeek/SenseNova 共用。

## 8. Router Semantics & Errors

| 场景 | `fallback.enabled=true` | `=false` |
|------|-------------------------|----------|
| 引擎 key 未知 / Bean 不存在 | `rule-based` | `AiException`（如 `AI_ENGINE_UNAVAILABLE`） |
| 目标引擎未启用 | `rule-based` | `AiException`（`AI_ENGINE_UNAVAILABLE`） |
| 远端调用失败 | `rule-based` + `engine=rule-based(fallback)` +「{displayName} 调用失败，已回退规则引擎。」 | `AiException`（`AI_ENGINE_CALL_FAILED`，含引擎名与原因摘要） |
| 目标即 `rule-based` | 直接执行 | 直接执行 |

日志：

- 回退路径：保留 `error`
- JSON 结构化解析失败：至少 `warn`（禁止完全静默）

## 9. Phased Delivery

| Phase | 内容 | 可验证点 |
|-------|------|----------|
| **P1** | `AiEngineType`；Router + `fallback`；占位 Key；`AiException` + Handler；Swagger 文案 | 行为矩阵单测绿；Client 可暂留 RestTemplate |
| **P2** | Properties 基类；OpenAI DTO/Client（WebClient）；`AiPromptBuilder`；Engine 抽象基类；删除双 Client | 无重复 Client；超时按厂商 |
| **P3** | yaml 注释 + Nacos 约定写入模块说明/checklist | 仓库无完整强制 `ai:`、无真实 Key |
| **P4** | `tests/checklists/ai-analyze.md` + 单元/MockWebServer 轻集成 | CI 可跑绿 |

## 10. Testing

### 10.1 Checklist

`tests/checklists/ai-analyze.md`：引擎选择、占位 Key、fallback 矩阵、JSON 降级、超时来源、不测理由（真外网/流式/ASR）。

### 10.2 Unit (主)

- `AiEngineType.fromKey`
- `AiEngineRouter` + fake engines × fallback
- `AiApiKeys.isConfigured`
- 响应解析：合法 JSON / 非法 JSON 降级
- `AiPromptBuilder`：字段拼装；context 序列化失败不阻断

### 10.3 Light integration (次)

- `OpenAiCompatibleClient` + MockWebServer：2xx content、非 2xx、空 choices
- 禁止 CI 打真实外网

### 10.4 Success Criteria

1. 外呼只经 `OpenAiCompatibleClient`（WebClient）
2. key/展示名只来自 `AiEngineType`
3. Nacos 约定可查；仓库无真实 Key
4. `fallback` 默认 true 兼容现网；false → `AiException`
5. 超时按厂商配置
6. 约定测试 CI 绿

## 11. Risks & Mitigations

| 风险 | 缓解 |
|------|------|
| 引入 webflux 与 MVC 混用踩坑 | 仅作 HTTP 客户端依赖；不改 `@SpringBootApplication` 为 WebFlux；P2 单独验证启动 |
| Nacos 缺配置导致全员 rule-based | P3 文档 + 启动时 debug 日志打印 `ai.engine` / 各引擎 enabled |
| 行为变更影响 Feign 调用方 | 默认 `fallback=true`；false 为显式收紧 |
| 大 diff 难审 | 严格按 P1→P4 分 PR/提交 |

## 12. Open Implementation Notes (non-blocking)

- WebClient 依赖坐标在实现计划中钉死（与当前 Spring Boot 版本对齐）。
- 若 `ResultEnum` 适合扩展 AI 码则优先枚举；否则 `AiException(String code, String msg)` 与 Finance 一致即可。
- `fromKey` 未知 key 的返回约定（`Optional` vs `null`）在实现计划 Task 中写死一种。
