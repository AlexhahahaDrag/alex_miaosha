# AI Engine Governance Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 按已批准规格治理 `alex_miaosha_ai`：统一引擎枚举、可配置回退、占位 Key 判定、OpenAI 兼容 WebClient 单 Client，并补齐 checklist 与单测/轻集成。

**Architecture:** `AiEngineRouter` 负责选择与回退；`AiEngineType` 统一 key/展示名；DeepSeek/SenseNova 经 `AbstractOpenAiCompatibleAiEngine` + `OpenAiCompatibleClient(WebClient)` 共用协议层；配置真相在 Nacos。

**Tech Stack:** JDK 17 · Spring Boot 2.7.18 · WebClient (`spring-webflux` 作客户端) · JUnit 5 · Mockito · OkHttp MockWebServer · Lombok

**Spec:** `docs/superpowers/specs/2026-08-26-ai-engine-governance-design.md`

## Global Constraints

- 规格为准；Non-Goals：ASR、流式、前端、真外网 CI。
- JAVA_HOME：`C:\Program Files\Java\jdk-17`（Windows Maven）。
- Commit：用户未明确要求前不提交；无 `Co-authored-by: Cursor`；可用英文 commit message 避乱码。
- 仓库 profile yaml **不**恢复完整 `ai:`；Key 不入库。
- `ai.fallback.enabled` 默认 **true**（兼容现网）。
- WebClient **仅作 HTTP 客户端**，不把应用改成 WebFlux 服务端。
- 测试命令示例：`cmd.exe /c "set JAVA_HOME=C:\Program Files\Java\jdk-17&& mvn -pl alex_miaosha_ai/ai_boot -am test -Dtest=ClassName"`

### Spec → Task 映射

| Spec | Task |
|------|------|
| §5 `AiEngineType` / §10 fromKey | Task 1 |
| §7 占位 Key / `AiApiKeys` | Task 2 |
| §8 `AiException` + Handler + 错误码 | Task 3 |
| §4/§8 Router + `fallback` + Swagger engine 文案 | Task 4 |
| Engine `key()`/`isEnabled` 接枚举与 AiApiKeys | Task 5 |
| §5 Properties 基类 | Task 6 |
| §5 OpenAI DTO / Prompt / WebClient Client + 依赖 | Task 7 |
| §5 抽象 Engine、删双 Client、超时按厂商 | Task 8 |
| §6/§10 Nacos 注释 + checklist | Task 9 |

### File map

| 路径 | 职责 |
|------|------|
| `ai_boot/.../engine/AiEngineType.java` | 枚举 key + displayName |
| `ai_boot/.../util/AiApiKeys.java` | 占位/空 Key 判定 |
| `common/.../exception/AiException.java` | 业务异常 |
| `common/.../GlobalExceptionHandler.java` | 处理 AiException |
| `base/.../ResultEnum.java` | `AI_ENGINE_UNAVAILABLE` / `AI_ENGINE_CALL_FAILED` |
| `ai_boot/.../config/AiProperties.java` | `fallback.enabled` |
| `ai_boot/.../config/OpenAiCompatibleProperties.java` | 公共 LLM 配置字段 |
| `ai_boot/.../engine/AiEngineRouter.java` | 路由 + 回退 |
| `ai_boot/.../client/openai/**` | DTO、PromptBuilder、Client |
| `ai_boot/.../engine/impl/AbstractOpenAiCompatibleAiEngine.java` | 共用解析 |
| `ai_boot/.../config/AiHttpConfig.java` | WebClient 工厂 |
| 删除 `DeepSeekClient` / `SenseNovaClient` / deepseek dto |
| `tests/checklists/ai-analyze.md` | 测试清单 |
| `ai_boot/pom.xml` | webflux + test + mockwebserver |

---

### Task 1: AiEngineType

**Files:**

- Create: `alex_miaosha_ai/ai_boot/src/main/java/com/alex/ai/engine/AiEngineType.java`
- Create: `alex_miaosha_ai/ai_boot/src/test/java/com/alex/ai/engine/AiEngineTypeTest.java`
- Modify: `alex_miaosha_ai/ai_boot/pom.xml` — 若无 `spring-boot-starter-test`，加入（scope test）

**Interfaces:**

- Produces:
  - `enum AiEngineType { DEEPSEEK("deepseek","DeepSeek"), SENSENOVA("sensenova","SenseNova"), RULE_BASED("rule-based","规则引擎"); }`
  - `String getKey()` / `String getDisplayName()`
  - `static Optional<AiEngineType> fromKey(String key)` — null/blank → empty；忽略大小写匹配；未知 → empty

- [ ] **Step 1: 红灯测试**

```java
package com.alex.ai.engine;

import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class AiEngineTypeTest {
    @Test
    void fromKey_ignoreCase() {
        assertEquals(AiEngineType.DEEPSEEK, AiEngineType.fromKey("DeepSeek").orElseThrow());
        assertEquals(AiEngineType.SENSENOVA, AiEngineType.fromKey("sensenova").orElseThrow());
        assertEquals(AiEngineType.RULE_BASED, AiEngineType.fromKey("rule-based").orElseThrow());
    }

    @Test
    void fromKey_unknownOrBlank_empty() {
        assertTrue(AiEngineType.fromKey(null).isEmpty());
        assertTrue(AiEngineType.fromKey("  ").isEmpty());
        assertTrue(AiEngineType.fromKey("openai").isEmpty());
    }
}
```

- [ ] **Step 2: 运行确认失败**

```bat
cmd.exe /c "set JAVA_HOME=C:\Program Files\Java\jdk-17&& mvn -pl alex_miaosha_ai/ai_boot -am test -Dtest=AiEngineTypeTest"
```

Expected: 编译失败或测试失败（类不存在）。

- [ ] **Step 3: 实现枚举**

```java
package com.alex.ai.engine;

import java.util.Locale;
import java.util.Optional;

public enum AiEngineType {
    DEEPSEEK("deepseek", "DeepSeek"),
    SENSENOVA("sensenova", "SenseNova"),
    RULE_BASED("rule-based", "规则引擎");

    private final String key;
    private final String displayName;

    AiEngineType(String key, String displayName) {
        this.key = key;
        this.displayName = displayName;
    }

    public String getKey() { return key; }
    public String getDisplayName() { return displayName; }

    public static Optional<AiEngineType> fromKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            return Optional.empty();
        }
        String normalized = key.trim().toLowerCase(Locale.ROOT);
        for (AiEngineType type : values()) {
            if (type.key.equals(normalized)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }
}
```

- [ ] **Step 4: 再跑测试 Expected: PASS**

- [ ] **Step 5:**（仅用户要求时）Commit `test(ai): add AiEngineType`

---

### Task 2: AiApiKeys

**Files:**

- Create: `alex_miaosha_ai/ai_boot/src/main/java/com/alex/ai/util/AiApiKeys.java`
- Create: `alex_miaosha_ai/ai_boot/src/test/java/com/alex/ai/util/AiApiKeysTest.java`

**Interfaces:**

- Produces: `public static boolean isConfigured(String apiKey)`
  - false：null、trim 空、忽略大小写等于 `sk-xxx` / `changeme` / `your-api-key` / `todo`
  - true：其它非空

- [ ] **Step 1: 红灯测试**

```java
assertFalse(AiApiKeys.isConfigured(null));
assertFalse(AiApiKeys.isConfigured(""));
assertFalse(AiApiKeys.isConfigured("  "));
assertFalse(AiApiKeys.isConfigured("sk-xxx"));
assertFalse(AiApiKeys.isConfigured("SK-XXX"));
assertFalse(AiApiKeys.isConfigured("changeme"));
assertTrue(AiApiKeys.isConfigured("sk-real-key-value"));
```

- [ ] **Step 2: 跑测失败 → 实现 → 跑测通过**

```java
public final class AiApiKeys {
    private static final Set<String> PLACEHOLDERS = Set.of(
            "sk-xxx", "changeme", "your-api-key", "todo");
    private AiApiKeys() {}
    public static boolean isConfigured(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return false;
        }
        return !PLACEHOLDERS.contains(apiKey.trim().toLowerCase(Locale.ROOT));
    }
}
```

- [ ] **Step 3:**（可选）Commit `test(ai): add AiApiKeys placeholder guard`

---

### Task 3: AiException + ResultEnum + Handler

**Files:**

- Modify: `alex_miaosha_base/src/main/java/com/alex/base/enums/ResultEnum.java` — 在 `test` 枚举项之前增加：
  - `AI_ENGINE_UNAVAILABLE("500701", "AI 引擎不可用")`
  - `AI_ENGINE_CALL_FAILED("500702", "AI 引擎调用失败")`
- Create: `alex_miaosha_common/src/main/java/com/alex/common/exception/AiException.java`（对齐 `FinanceException`：`(ResultEnum)` 与 `(String code, String message)`）
- Modify: `alex_miaosha_common/src/main/java/com/alex/common/exception/handler/GlobalExceptionHandler.java` — 增加：

```java
@ExceptionHandler(AiException.class)
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public Result<String> handle(AiException e) {
    log.error("AI异常:{}", e.getMsg(), e);
    return Result.error(e.getCode(), e.getMsg());
}
```

**Interfaces:**

- Produces: `new AiException(ResultEnum.AI_ENGINE_UNAVAILABLE)`；`new AiException(ResultEnum.AI_ENGINE_CALL_FAILED.getCode(), detailMsg)`

- [ ] **Step 1:** 实现三处修改；编译 `alex_miaosha_common` + `alex_miaosha_base`。

- [ ] **Step 2:**（可选）Commit `feat(ai): add AiException and result codes`

---

### Task 4: AiProperties.fallback + Router 语义 + Swagger

**Files:**

- Modify: `alex_miaosha_ai/ai_boot/src/main/java/com/alex/ai/config/AiProperties.java`
- Modify: `alex_miaosha_ai/ai_boot/src/main/java/com/alex/ai/engine/AiEngineRouter.java`
- Modify: `alex_miaosha_ai/ai_api/src/main/java/com/alex/api/ai/vo/AiAnalyzeReq.java` — engine 说明改为 `sensenova / deepseek / rule-based`
- Create: `alex_miaosha_ai/ai_boot/src/test/java/com/alex/ai/engine/AiEngineRouterTest.java`

**Interfaces:**

- `AiProperties` 增加嵌套：

```java
private Fallback fallback = new Fallback();

@Data
public static class Fallback {
    /** 对应 ai.fallback.enabled，默认 true */
    private boolean enabled = true;
}
```

- Router 行为（规格 §8）：
  - 解析 desiredKey：`req.engine` > `ai.engine`；`fromKey` 仅用于展示名；引擎查找仍按 `AiEngine.key()` 忽略大小写。
  - 未知引擎 / `!isEnabled`：`fallback.enabled` → rule-based；否则 `throw new AiException(ResultEnum.AI_ENGINE_UNAVAILABLE)`（msg 可附带 key）。
  - 调用异常：fallback true → rule-based + `engine=rule-based(fallback)` + summary `{displayName} 调用失败，已回退规则引擎。`（displayName 来自 `AiEngineType.fromKey(engineKey).map(AiEngineType::getDisplayName).orElse(null)`，null 则用「AI 引擎」通用句）；fallback false → `AiException(AI_ENGINE_CALL_FAILED, ...)`。
  - 删除 `ENGINE_DISPLAY_NAME` Map。

- [ ] **Step 1: 红灯 Router 单测（Mockito fake engines）**

覆盖至少：

1. 未知 engine + fallback true → rule-based 成功  
2. 未知 engine + fallback false → 抛 `AiException` code `500701`  
3. enabled 引擎抛异常 + fallback true → summary 含展示名与「回退规则引擎」  
4. 同上 + fallback false → `AiException` code `500702`

Fake：`AiEngine` mock / 匿名实现：`key()`、`isEnabled`、`analyze`。

- [ ] **Step 2: 实现 Properties + Router + Swagger → 测试 PASS**

- [ ] **Step 3:**（可选）Commit `feat(ai): configurable fallback router`

---

### Task 5: Engines 使用枚举与 AiApiKeys

**Files:**

- Modify: `.../engine/impl/DeepSeekAiEngine.java`
- Modify: `.../engine/impl/SenseNovaAiEngine.java`
- Modify: `.../engine/impl/RuleBasedAiEngine.java`
- Create: `.../src/test/java/com/alex/ai/engine/impl/LlmEngineEnablementTest.java`

**Interfaces:**

- `key()` 分别返回 `AiEngineType.DEEPSEEK.getKey()` 等
- `isEnabled`：`AiApiKeys.isConfigured(props.getApiKey())`

- [ ] **Step 1: 测试** `DeepSeekAiEngine`/`SenseNovaAiEngine` 在 `apiKey=sk-xxx` 时 `isEnabled==false`；真实形态 key 时 true（可用手动 new + mock props，或 Spring 测试构造）。

- [ ] **Step 2: 改三处 Engine → PASS**

- [ ] **Step 3:**（可选）Commit `refactor(ai): engines use AiEngineType and AiApiKeys`

---

### Task 6: OpenAiCompatibleProperties

**Files:**

- Create: `alex_miaosha_ai/ai_boot/src/main/java/com/alex/ai/config/OpenAiCompatibleProperties.java`
- Modify: `DeepSeekProperties.java` / `SenseNovaProperties.java` — `extends OpenAiCompatibleProperties`，删除重复字段，保留类注释与默认值（可在子类无参构造或字段初始化器设默认 baseUrl/model）

**Interfaces:**

- 基类字段：`baseUrl`、`chatCompletionsPath`、`apiKey`、`model`、`temperature`、`maxTokens`、`timeoutMs`（名称与现 yaml 绑定一致：kebab `chat-completions-path` / `timeout-ms`）

- [ ] **Step 1:** 抽取后 `mvn -pl alex_miaosha_ai/ai_boot -am compile` 通过

- [ ] **Step 2:**（可选）Commit `refactor(ai): extract OpenAiCompatibleProperties`

---

### Task 7: OpenAI DTO + PromptBuilder + WebClient Client

**Files:**

- Modify: `alex_miaosha_ai/ai_boot/pom.xml`：

```xml
<dependency>
  <groupId>org.springframework</groupId>
  <artifactId>spring-webflux</artifactId>
</dependency>
<dependency>
  <groupId>io.projectreactor.netty</groupId>
  <artifactId>reactor-netty-http</artifactId>
</dependency>
<dependency>
  <groupId>com.squareup.okhttp3</groupId>
  <artifactId>mockwebserver</artifactId>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-test</artifactId>
  <scope>test</scope>
</dependency>
```

（版本由 Boot BOM 管理；若 reactor-netty 需显式，与 2.7.18 对齐。）

- Create: `.../client/openai/dto/OpenAiChatCompletionRequest.java`（内容由 DeepSeek request 迁移更名）
- Create: `.../client/openai/dto/OpenAiChatCompletionResponse.java`
- Create: `.../client/openai/AiPromptBuilder.java` — `buildSystemPrompt(AiAnalyzeReq)`、`buildUserPrompt(AiAnalyzeReq, ObjectMapper)`
- Create: `.../client/openai/OpenAiCompatibleClient.java`
- Create: `.../src/test/java/com/alex/ai/client/openai/OpenAiCompatibleClientTest.java`
- Create: `.../src/test/java/com/alex/ai/client/openai/AiPromptBuilderTest.java`
- Modify: `AiHttpConfig.java` — 提供构建带超时 WebClient 的方法/Bean（见下）

**Interfaces:**

```java
public String chat(AiAnalyzeReq req, OpenAiCompatibleProperties props, String providerLabel);
```

- `providerLabel` 用于异常消息（如 `"SenseNova"` / `"DeepSeek"`）
- URL：`baseUrl` + `chatCompletionsPath`（沿用现有 slash 拼接规则）
- Header：`Authorization: Bearer {apiKey}`，`Content-Type: application/json`
- 使用 `WebClient`；`HttpClient`/`ReactorClientHttpConnector` 设置 `responseTimeout(Duration.ofMillis(timeoutMs))`
- 非 2xx / 空 choices / 空 content → `IllegalStateException`（Router 捕获；文案含 providerLabel）
- **本 Task 暂不删旧 Client**；可先实现新 Client + 测试

`AiHttpConfig` 建议：

```java
public WebClient buildAiWebClient(int timeoutMs) { ... }
```

或 `@Bean` 工厂；避免单一全局 timeout 绑死 DeepSeek。

- [ ] **Step 1: PromptBuilder 单测** — 含 bizType/depth/content；context 非法对象不抛（吞并继续）。

- [ ] **Step 2: MockWebServer 测试 Client**

  - enqueue 合法 JSON choices[0].message.content → 返回该字符串  
  - enqueue 500 → 抛异常  
  - enqueue choices 空 → 抛异常  

- [ ] **Step 3: 实现 DTO/Builder/Client/Config → PASS**

- [ ] **Step 4:**（可选）Commit `feat(ai): OpenAiCompatibleClient on WebClient`

---

### Task 8: 抽象 Engine + 切换调用 + 删除旧 Client

**Files:**

- Create: `.../engine/impl/AbstractOpenAiCompatibleAiEngine.java`
- Modify: `DeepSeekAiEngine` / `SenseNovaAiEngine` — 继承抽象类；注入 `OpenAiCompatibleClient`；删除本地 `toAnalyzeRespFromLlm` / `AiStructuredResult` 重复
- Delete: `DeepSeekClient.java`、`SenseNovaClient.java`、`client/deepseek/dto/*`
- Modify: 任何仍引用旧 DTO/Client 的 import

**Interfaces（抽象类）：**

```java
protected abstract AiEngineType engineType();
protected abstract OpenAiCompatibleProperties resolveProps(AiProperties aiProperties);

// analyze: chat → parse JSON {summary,keyPoints}；失败 warn + 原文 summary
// 空 content：summary = engineType().getDisplayName() + " 返回为空。"
// resp.engine = engineType().getKey() + ":" + model
```

JSON 解析失败：`log.warn("LLM JSON parse failed, engine={}, err={}", ...)`

- [ ] **Step 1: 切换两 Engine 到新 Client；删除旧类；编译 + 既有 Router/Enablement 测试 PASS**

- [ ] **Step 2: 确认无 RestTemplate `aiRestTemplate` Bean 残留引用；`AiHttpConfig` 仅 WebClient**

- [ ] **Step 3:**（可选）Commit `refactor(ai): unify LLM engines on OpenAiCompatibleClient`

---

### Task 9: Nacos 约定注释 + checklist

**Files:**

- Modify: `application-dev.yaml` / `application-test.yaml` / `application-prod.yaml` — 文件末尾增加注释块（勿写真实 key），示例：

```yaml
# AI 配置真相源：Nacos（服务 alex-ai / 对应 DataId）
# 必填项见 docs/superpowers/specs/2026-08-26-ai-engine-governance-design.md §6
# ai.engine / ai.fallback.enabled / ai.deepseek.* / ai.sensenova.*
# Key 环境变量：AI_DEEPSEEK_API_KEY / AI_SENSENOVA_API_KEY
```

- Create: `tests/checklists/ai-analyze.md` — 含：引擎选择、占位 Key、fallback 矩阵、JSON 降级、超时按厂商、不测理由（真外网/流式/ASR/Midscene）
- 若模块有 `DEVELOPMENT.md` / README，补一句指向 spec §6；无则仅 checklist + yaml 注释即可

- [ ] **Step 1: 写 checklist 与注释**

- [ ] **Step 2: 全量跑 ai_boot 测试**

```bat
cmd.exe /c "set JAVA_HOME=C:\Program Files\Java\jdk-17&& mvn -pl alex_miaosha_ai/ai_boot -am test"
```

Expected: PASS

- [ ] **Step 3:**（可选）Commit `docs(ai): Nacos config notes and ai-analyze checklist`

---

## Self-Review (plan vs spec)

| Spec 要求 | 覆盖 |
|-----------|------|
| AiEngineType | Task 1 |
| AiApiKeys / 占位 | Task 2、5 |
| AiException / codes / Handler | Task 3 |
| fallback 可配置 + Router 表 | Task 4 |
| Properties 基类 | Task 6 |
| 单 Client WebClient + Prompt + DTO | Task 7–8 |
| 删双 Client | Task 8 |
| Nacos 为准 + yaml 不写满 ai | Task 9 |
| checklist + 单测/MockWebServer | Task 1–4、7、9 |
| 超时按厂商 | Task 7–8（Client 用 props.timeoutMs） |
| 默认 fallback true | Task 4 |
| 非目标未纳入 | 是 |

无 TBD 步骤；`fromKey` 钉死为 `Optional`；错误码钉死 `500701`/`500702`。

---

## Execution Handoff

Plan complete and saved to `docs/superpowers/plans/2026-08-26-ai-engine-governance.md`.

**Two execution options:**

1. **Subagent-Driven（推荐）** — 每 Task 新开子代理，Task 间人工复核  
2. **Inline Execution** — 本会话按 executing-plans 连续执行并设检查点  

回复 **1** / **2**，或说 **Proceed**（默认按 Inline 从 Task 1 开始）。未明确授权前不改业务代码。
