# AI Gateway SSE + Apifox Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make AI batch/stream traffic work through Gateway (`/am-ai/**`), keep SSE unbuffered/unencrypted, set response-timeout ≥ AI stream timeout, and expose `group=alex-ai` OpenAPI via Gateway for Apifox.

**Architecture:** Add explicit Gateway routes (same pattern as `alex-user`) for `id: alex-ai` → `lb://alex-ai-{profile}` with `StripPrefix=1`. Keep existing `GatewaySsePathMatcher` + Content-Type skip in `GatewayFilter`. Set `spring.cloud.gateway.httpclient.response-timeout: 180s`. No new GlobalFilter. AI SwaggerConfig already uses `groupName=alex-ai` / `pathMapping=/am-ai`.

**Tech Stack:** Spring Cloud Gateway, YAML config, JUnit 5, existing Knife4j/Springfox aggregation (`SwaggerResourceConfig`).

**Spec:** `docs/superpowers/specs/2026-08-31-ai-gateway-sse-apifox-design.md`

## Global Constraints

- Route prefix: `/am-ai/**` only (frontend Vite strips `/api`).
- Route id and Swagger group: both exactly `alex-ai`.
- SSE skip: keep `/**/ai/chat/stream` + `text/event-stream`; do not disable global encryption.
- `response-timeout: 180s` (≥ AI `ai.stream.read-timeout-ms` default 120000).
- Touch Gateway `application-dev.yaml` / `application-test.yaml` / `application-prod.yaml`; if live Nacos overrides Gateway routes, mirror the same `alex-ai` route there (verify during Task 4).
- Do not commit unless the user explicitly asks for git commit/push.
- Windows shell: prefer CMD / non-interactive PowerShell scripts; set `JAVA_HOME` to JDK 17 when running Maven.

## File map

| File | Responsibility |
|------|----------------|
| `alex_miaosha_gateway/.../application-dev.yaml` | dev route + httpclient timeout |
| `alex_miaosha_gateway/.../application-test.yaml` | test route + timeout |
| `alex_miaosha_gateway/.../application-prod.yaml` | prod route + timeout |
| `alex_miaosha_gateway/src/test/java/.../GatewayAiRouteConfigTest.java` | assert yaml contains `alex-ai` / `/am-ai/**` / timeout |
| `alex_miaosha_gateway/.../GatewaySsePathMatcherTest.java` | existing SSE path regression (no change unless fails) |
| `tests/checklists/ai-gateway-sse-apifox.md` | manual/冒烟 + Apifox + Nacos check |

No changes expected to AI Controllers, front `ai-chat`, or `GatewayFilter` Java (already SSE-aware).

---

### Task 1: Failing route/timeout config test

**Files:**
- Create: `alex_miaosha_gateway/src/test/java/com/alex/gateway/config/GatewayAiRouteConfigTest.java`
- Test: same

**Interfaces:**
- Consumes: classpath YAML under `application-dev.yaml` (and optionally test/prod)
- Produces: assertions that will pass only after Task 2 edits

- [ ] **Step 1: Write the failing test**

Create `GatewayAiRouteConfigTest.java`:

```java
package com.alex.gateway.config;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GatewayAiRouteConfigTest {

    @Test
    void applicationDev_hasAlexAiRouteAndHttpClientTimeout() {
        Map<String, Object> root = loadYaml("application-dev.yaml");
        Map<String, Object> gateway = nest(root, "spring", "cloud", "gateway");
        assertNotNull(gateway, "spring.cloud.gateway missing");

        Map<String, Object> httpclient = castMap(gateway.get("httpclient"));
        assertNotNull(httpclient, "httpclient missing");
        assertEquals("180s", String.valueOf(httpclient.get("response-timeout")));

        List<Map<String, Object>> routes = castList(gateway.get("routes"));
        assertNotNull(routes);
        Map<String, Object> ai = routes.stream()
                .filter(r -> "alex-ai".equals(r.get("id")))
                .findFirst()
                .orElse(null);
        assertNotNull(ai, "route id=alex-ai missing");
        assertEquals("lb://alex-ai-dev", ai.get("uri"));
        assertTrue(predicatesContainPath(ai, "/am-ai/**"));
        assertTrue(filtersContainStripPrefix1(ai));
    }

    @Test
    void applicationTest_hasAlexAiRoute() {
        assertProfileRoute("application-test.yaml", "lb://alex-ai-test");
    }

    @Test
    void applicationProd_hasAlexAiRoute() {
        assertProfileRoute("application-prod.yaml", "lb://alex-ai-prod");
    }

    private void assertProfileRoute(String resource, String expectedUri) {
        Map<String, Object> root = loadYaml(resource);
        Map<String, Object> gateway = nest(root, "spring", "cloud", "gateway");
        List<Map<String, Object>> routes = castList(gateway.get("routes"));
        Map<String, Object> ai = routes.stream()
                .filter(r -> "alex-ai".equals(r.get("id")))
                .findFirst()
                .orElse(null);
        assertNotNull(ai, resource + " missing alex-ai");
        assertEquals(expectedUri, ai.get("uri"));
        assertTrue(predicatesContainPath(ai, "/am-ai/**"));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadYaml(String name) {
        InputStream in = GatewayAiRouteConfigTest.class.getClassLoader().getResourceAsStream(name);
        assertNotNull(in, "classpath resource missing: " + name);
        Object loaded = new Yaml().load(in);
        assertTrue(loaded instanceof Map);
        return (Map<String, Object>) loaded;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> nest(Map<String, Object> root, String... keys) {
        Map<String, Object> cur = root;
        for (String k : keys) {
            Object next = cur.get(k);
            if (!(next instanceof Map)) {
                return null;
            }
            cur = (Map<String, Object>) next;
        }
        return cur;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object o) {
        return o instanceof Map ? (Map<String, Object>) o : null;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> castList(Object o) {
        return o instanceof List ? (List<Map<String, Object>>) o : null;
    }

    @SuppressWarnings("unchecked")
    private static boolean predicatesContainPath(Map<String, Object> route, String path) {
        Object preds = route.get("predicates");
        if (!(preds instanceof List)) {
            return false;
        }
        for (Object p : (List<?>) preds) {
            if (p instanceof String && ((String) p).contains("Path=" + path)) {
                return true;
            }
            if (p instanceof Map) {
                Map<String, Object> m = (Map<String, Object>) p;
                if ("Path".equals(String.valueOf(m.get("name")))
                        && String.valueOf(m.get("args")).contains(path)) {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static boolean filtersContainStripPrefix1(Map<String, Object> route) {
        Object filters = route.get("filters");
        if (!(filters instanceof List)) {
            return false;
        }
        for (Object f : (List<?>) filters) {
            if (f instanceof String && ((String) f).contains("StripPrefix=1")) {
                return true;
            }
            if (f instanceof Map) {
                Map<String, Object> m = (Map<String, Object>) f;
                if ("StripPrefix".equals(String.valueOf(m.get("name")))) {
                    return true;
                }
            }
        }
        return false;
    }
}
```

Notes for implementer:
- Spring YAML short form `Path=/am-ai/**` loads as **String** in the list; the `instanceof String` branch must succeed.
- If `snakeyaml` is not already on gateway test classpath (it usually is via Spring Boot), add test-scoped dependency only if compile fails.

- [ ] **Step 2: Run test to verify it fails**

```bat
set JAVA_HOME=C:\Program Files\Java\jdk-17
cd /d F:\workplace\project\myself\backend\alex_miaosha
mvn -pl alex_miaosha_gateway -am test -Dtest=GatewayAiRouteConfigTest -q
```

Expected: FAIL — `route id=alex-ai missing` and/or `httpclient missing`.

- [ ] **Step 3: Stop** — do not edit yaml yet (Task 2).

---

### Task 2: Add `alex-ai` routes + httpclient timeout (three profiles)

**Files:**
- Modify: `alex_miaosha_gateway/src/main/resources/application-dev.yaml`
- Modify: `alex_miaosha_gateway/src/main/resources/application-test.yaml`
- Modify: `alex_miaosha_gateway/src/main/resources/application-prod.yaml`
- Test: `GatewayAiRouteConfigTest`

**Interfaces:**
- Consumes: Task 1 assertions
- Produces: routable `/am-ai/**` → `lb://alex-ai-{profile}`

- [ ] **Step 1: Edit `application-dev.yaml`**

Under `spring.cloud.gateway`, add `httpclient` sibling to `discovery` / `routes`:

```yaml
      httpclient:
        connect-timeout: 5000
        response-timeout: 180s
```

After the `alex-product` route block, add:

```yaml
        - id: alex-ai
          uri: lb://alex-ai-dev
          predicates:
            - Path=/am-ai/**
          filters:
            - StripPrefix=1
```

Keep indentation consistent with existing `alex-product` entry (same list under `routes:`).

- [ ] **Step 2: Edit `application-test.yaml`**

Same `httpclient` block. Route:

```yaml
        - id: alex-ai
          uri: lb://alex-ai-test
          predicates:
            - Path=/am-ai/**
          filters:
            - StripPrefix=1
```

- [ ] **Step 3: Edit `application-prod.yaml`**

Same `httpclient` block. Route:

```yaml
        - id: alex-ai
          uri: lb://alex-ai-prod
          predicates:
            - Path=/am-ai/**
          filters:
            - StripPrefix=1
```

- [ ] **Step 4: Run Task 1 tests — expect PASS**

```bat
set JAVA_HOME=C:\Program Files\Java\jdk-17
cd /d F:\workplace\project\myself\backend\alex_miaosha
mvn -pl alex_miaosha_gateway -am test -Dtest=GatewayAiRouteConfigTest,GatewaySsePathMatcherTest -q
```

Expected: PASS for both classes.

- [ ] **Step 5: If SnakeYAML predicate parsing fails** (e.g. path not detected): adjust `predicatesContainPath` to also match raw string `"Path=/am-ai/**"` only — do not weaken to “any Path”. Re-run until green.

---

### Task 3: Checklist + Nacos verification notes

**Files:**
- Create: `tests/checklists/ai-gateway-sse-apifox.md`

**Interfaces:**
- Consumes: Spec §8 success criteria
- Produces: operator checklist for smoke + Apifox + Nacos

- [ ] **Step 1: Create checklist** (use gift/stream checklist style: fields, pass/fail, 不测理由)

```markdown
# Checklist: AI Gateway SSE + Apifox

**Spec:** docs/superpowers/specs/2026-08-31-ai-gateway-sse-apifox-design.md  
**Date:**

## Config

| # | Item | Pass? | Notes |
|---|------|-------|-------|
| 1 | `application-dev/test/prod.yaml` 含 `id: alex-ai`, `Path=/am-ai/**`, `StripPrefix=1` | | |
| 2 | `uri` 分别为 `lb://alex-ai-dev|test|prod` | | |
| 3 | `spring.cloud.gateway.httpclient.response-timeout: 180s` | | |
| 4 | 若 Nacos 覆盖 Gateway 路由：线上 DataId 已同步 `alex-ai` 路由与超时 | | 无 Nacos 覆盖则填 N/A |

## Functional (dev)

| # | Item | Pass? | Notes |
|---|------|-------|-------|
| 5 | `POST /am-ai/api/v1/ai/chat`（经 Gateway :30001，带 Token）返回业务成功 | | 前端等价 `/api/am-ai/...` |
| 6 | `POST /am-ai/api/v1/ai/chat/stream` 响应 `text/event-stream`，可见 `meta`/`delta`/`done` | | |
| 7 | stream 响应体**不是** AES 整包密文（可人眼看到 `event:` 行） | | |
| 8 | `GET /am-ai/v3/api-docs?group=alex-ai` 含 `/ai/chat` 与 stream | | docWhiteList 可免登录 |
| 9 | Apifox 导入上述 OpenAPI URL 成功 | | |

## Automated

| # | Item | Pass? | Notes |
|---|------|-------|-------|
| 10 | `GatewayAiRouteConfigTest` green | | |
| 11 | `GatewaySsePathMatcherTest` green | | |

## 不测理由

- 真外网 LLM CI
- 移动端
- Feign stream
- Apifox CLI 自动同步
```

- [ ] **Step 2: Probe Nacos for Gateway overrides** (when Nacos reachable)

```bat
curl.exe -s "http://10.10.20.238:8848/nacos/v1/cs/configs?dataId=alex-gateway-dev.yaml&group=alex-miaosha&tenant=033377eb-973b-4dac-a0e9-e99c87325009"
```

If the DataId name differs, search Nacos console for gateway route configs. If a remote file defines `spring.cloud.gateway.routes` **without** `alex-ai`, append the same route + ensure `httpclient.response-timeout: 180s` remotely (ops step). Record DataId name in checklist row 4.

If Nacos unreachable, mark row 4 as “deferred / verify on next Nacos up” and still ship local yaml.

---

### Task 4: Manual smoke (local stack)

**Files:** none (runtime)

**Interfaces:**
- Consumes: Gateway + AI + (optional) User for token
- Produces: checklist rows 5–9 filled

- [ ] **Step 1: Ensure services up** — Nacos, `alex-ai-dev` (port 30010), `alex-gateway-dev` (30001), user for auth if needed.

- [ ] **Step 2: Batch via Gateway**

```bat
curl.exe -s -X POST "http://127.0.0.1:30001/am-ai/api/v1/ai/chat" -H "Content-Type: application/json" -H "Authorization: <token>" -d "{\"content\":\"hello\",\"engine\":\"rule-based\"}"
```

Expected: HTTP 200 business success (possibly AES-encrypted body if encryption enabled — that is OK for **batch**).

- [ ] **Step 3: Stream via Gateway**

```bat
curl.exe -N -s -X POST "http://127.0.0.1:30001/am-ai/api/v1/ai/chat/stream" -H "Content-Type: application/json" -H "Accept: text/event-stream" -H "Authorization: <token>" -d "{\"content\":\"hello\",\"engine\":\"rule-based\"}"
```

Expected: plaintext SSE frames (`event:meta` / `delta` / `done`), not a single AES ciphertext blob.

- [ ] **Step 4: OpenAPI for Apifox**

```bat
curl.exe -s "http://127.0.0.1:30001/am-ai/v3/api-docs?group=alex-ai"
```

Expected: JSON with paths containing `/ai/chat`. Import this URL (or save JSON) into Apifox.

- [ ] **Step 5: Fill checklist** — mark pass/fail; stop if stream still encrypted (then debug `GatewayFilter.shouldSkipResponseEncryption` logs for path).

---

### Task 5: Final review

**Files:** all Task 1–3 outputs

- [ ] **Step 1: Spec coverage check**

| Spec requirement | Task |
|------------------|------|
| `/am-ai/**` route three envs | Task 2 |
| `response-timeout: 180s` | Task 2 |
| SSE skip unchanged | Task 2 (no Java change) + Task 1/2 regression tests |
| Apifox via gateway docs | Task 3–4 |
| Nacos mirror if needed | Task 3 |

- [ ] **Step 2: Run full gateway unit tests**

```bat
set JAVA_HOME=C:\Program Files\Java\jdk-17
cd /d F:\workplace\project\myself\backend\alex_miaosha
mvn -pl alex_miaosha_gateway -am test -q
```

Expected: PASS.

- [ ] **Step 3: Do not commit** unless user says「git提交代码」. If committing later, message suggestion:

```
feat(gateway): add am-ai route and SSE-friendly response timeout
```

---

## Self-review (plan vs spec)

1. **Spec coverage:** Routing, timeout, SSE keep, Apifox, Nacos note, checklist — all have tasks. Non-goals respected (no Feign stream, no global encrypt off).
2. **Placeholders:** None intentional; Nacos DataId may vary — Task 3 documents discovery.
3. **Consistency:** `alex-ai` / `/am-ai/**` / `180s` / `lb://alex-ai-{profile}` match spec §0 and §5–7.

## Execution handoff

Plan saved to `docs/superpowers/plans/2026-08-31-ai-gateway-sse-apifox.md`.

**Two execution options:**

1. **Subagent-Driven (recommended)** — fresh subagent per task, review between tasks  
2. **Inline Execution** — execute tasks in this session with checkpoints  

Which approach? (Or reply **Proceed** / **执行** to start inline by default.)
