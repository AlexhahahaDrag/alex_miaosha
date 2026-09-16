# Design: AI 经 Gateway 路由 + SSE 透传 + Apifox 文档聚合

**Date:** 2026-08-31  
**Status:** Approved  
**Module:** `alex_miaosha_gateway` + `alex_miaosha_ai`（Swagger 已齐）+ PC `ai-chat`（路径已对齐）  
**Approach:** 方案 2 — 补齐 `am-ai` 路由 + 显式 Gateway HTTP 超时 + 沿用现有 SSE 跳过加密 + 经网关 OpenAPI 供 Apifox

## 0. Decisions (locked)

| 项 | 选择 |
|----|------|
| 范围 | C：batch + stream 走网关 + Swagger 经网关聚合（Apifox） |
| 实现路径 | 方案 2（非仅审计、非单独 SSE 重过滤器） |
| 路由前缀 | `/am-ai/**`，与前端 `baseService.ai = '/api/am-ai'` + Vite 去 `/api` 一致 |
| route id / Swagger group | `alex-ai`（与 `SwaggerConfig.groupName`、`pathMapping=/am-ai` 对齐） |
| SSE 加密跳过 | 保持现有 path `/**/ai/chat/stream` + `Content-Type: text/event-stream` |
| Gateway response-timeout | 显式配置，建议 **180s**（≥ AI `ai.stream.read-timeout-ms` 默认 120s） |
| 全局关加密 / 独立 SSE 路由 | **不做** |

## 1. Problem

PC `ai-chat` 已按网关约定请求：

- batch：`POST /api/am-ai/api/v1/ai/chat`
- stream：`POST /api/am-ai/api/v1/ai/chat/stream`（`Accept: text/event-stream`）

Vite 将 `/api` rewrite 掉后，Gateway 实际看到的是 `/am-ai/api/v1/ai/...`。

当前 Gateway（dev/test/prod）显式路由含 `am-user` / `am-finance` / `am-mission` / `am-oss` / `am-product`，**没有 `am-ai`**。  
`discovery.locator` 只会生成 `/alex-ai-{profile}/**` 类路径，与前端 `/am-ai/**` **不匹配**。

流式侧：`GatewayFilter` + `GatewaySsePathMatcher` 已能对 `/am-ai/.../ai/chat/stream` 跳过 `buffer()` 加密（单测覆盖）。  
但缺路由则流量到不了 AI；且 Gateway **未配置** `httpclient.response-timeout`，长 SSE 有被默认超时截断的风险。

Apifox：AI 已提供 `group=alex-ai` 的 OpenAPI；网关 `SwaggerResourceConfig` 按 **route id** 拼 `v3/api-docs?group=`，无 `alex-ai` 路由则聚合不到。

## 2. Goals

1. 经 Gateway 访问 AI batch / stream，路径与其它微服务一致（`/am-ai` + `StripPrefix=1`）。
2. SSE 经网关**不整包加密、不 buffer 破坏帧**；长流在约定超时内不被 Gateway 提前掐断。
3. 经网关可拉取 `group=alex-ai` 的 OpenAPI，供 Knife4j 聚合与 **Apifox 导入**。
4. 三环境（dev/test/prod）配置一致；若 Nacos 承载 Gateway 路由，与本地 yaml **同内容对齐**（实现时核对）。

## 3. Non-Goals

- Feign 流式、移动端 AI
- 关闭全局响应加密或为 AI 单独拆 GlobalFilter
- Apifox 账号/CLI 自动同步流水线
- 修改 AI Controller 契约或 SSE event 协议（已在 2026-08-28 stream 设计中锁定）

## 4. As-is / To-be

### 4.1 As-is

```
前端 /api/am-ai/... 
  → Vite rewrite → /am-ai/...
  → Gateway：无 Path=/am-ai/**  → 404 / 错路由
SSE 跳过加密：代码已具备，但流量到不了
Swagger 聚合：无 id=alex-ai 路由 → 无 AI 文档资源
```

### 4.2 To-be

```
前端 /api/am-ai/api/v1/ai/chat[/stream]
  → Vite → /am-ai/api/v1/ai/chat[/stream]
  → Gateway route id=alex-ai, StripPrefix=1
  → lb://alex-ai-{profile}  /api/v1/ai/chat[/stream]
  → AI 服务

stream：secretOut 检测到 path 或 text/event-stream → super.writeWith(body) 透传
docs：Gateway → /am-ai/v3/api-docs?group=alex-ai → AI OpenAPI
```

## 5. Gateway Routing

三环境各增加一条（仅 `uri` 服务名后缀不同）：

| Profile | uri |
|---------|-----|
| dev | `lb://alex-ai-dev` |
| test | `lb://alex-ai-test` |
| prod | `lb://alex-ai-prod` |

```yaml
- id: alex-ai
  uri: lb://alex-ai-dev   # 按环境替换
  predicates:
    - Path=/am-ai/**
  filters:
    - StripPrefix=1
```

落地文件（实现计划钉死是否还有 Nacos 覆盖）：

- `alex_miaosha_gateway/src/main/resources/application-dev.yaml`
- `application-test.yaml`
- `application-prod.yaml`

## 6. SSE / Encryption / Timeout

### 6.1 Encryption skip（已有，保持）

- Path：`/**/ai/chat/stream`（覆盖 `/am-ai/api/v1/ai/chat/stream`）
- 或响应 `Content-Type` 含 `text/event-stream`
- 行为：`shouldSkipResponseEncryption` → 不 `fluxBody.buffer()`、不 AES

本方案**不改** matcher 默认模式，除非冒烟发现 path 形态与预期不符。

### 6.2 Response timeout（新增）

在 Gateway 配置显式 HTTP client 超时，建议：

```yaml
spring:
  cloud:
    gateway:
      httpclient:
        connect-timeout: 5000
        response-timeout: 180s
```

约束：

- `response-timeout` ≥ AI 侧 `ai.stream.read-timeout-ms`（默认 120000）
- 取值 **180s** 为默认推荐；若 Nacos 已有更严超时，以实现时「取更长、可配置」为准并在 checklist 记录实际值
- 仅调大超时，不引入按路由差异化超时（YAGNI）；若后续其它服务受影响再拆 per-route metadata

### 6.3 Auth

与其它业务相同：非白名单走 Token → `userApi.authToken` → `secretOut`。  
SSE 鉴权失败返回普通 JSON 403（可加密），不伪装成 SSE。

`docWhiteList` 已含 `/**/v3/api-docs/**`、`/doc.html`、`/swagger-resources/**`，文档拉取可跳过登录（与现网一致）。

## 7. Swagger / Apifox

| 层级 | 约定 |
|------|------|
| AI `SwaggerConfig` | `groupName=alex-ai`，`pathMapping=/am-ai`（已实现） |
| Gateway 聚合 | route `id: alex-ai` → location 形如 `/am-ai/v3/api-docs?group=alex-ai` |
| 前端经 Vite | `/api/am-ai/v3/api-docs?group=alex-ai` |
| 直连 AI（备选） | `http://{host}:30010/v3/api-docs?group=alex-ai` |

Apifox：导入 OpenAPI 3.0，优先用**经网关** URL，以便 path 带 `/am-ai` 前缀与线上一致。

## 8. Testing

### Checklist

新增或并入：`tests/checklists/ai-gateway-sse-apifox.md`

- [ ] Gateway 三环境存在 `id: alex-ai` + `Path=/am-ai/**`
- [ ] 经网关 batch `POST .../ai/chat` 成功
- [ ] 经网关 stream：响应为 SSE；中途可见 delta；**非**整包密文 AES 包
- [ ] `GET .../am-ai/v3/api-docs?group=alex-ai` 含 `/ai/chat` 与 `/ai/chat/stream`
- [ ] Apifox 可导入上述文档（手工一步即可）
- [ ] `GatewaySsePathMatcher` 单测回归
- [ ] 不测：真外网厂商 CI、移动端、Feign stream

### Automated

- 保持/补充 `GatewaySsePathMatcherTest`（含 `/am-ai/.../stream`）
- 可选：对 Gateway yaml 的路由存在性做轻量解析断言（实现计划选型）

### Success Criteria

1. `/am-ai/**` 路由到 `alex-ai-{profile}`  
2. stream 经网关透传且超时足够  
3. 经网关 OpenAPI `group=alex-ai` 可导入 Apifox  
4. 不破坏现有 user/finance 等路由与加密行为  

## 9. Risks

| 风险 | 缓解 |
|------|------|
| Nacos 覆盖本地 Gateway 路由导致仍无 am-ai | 实现计划要求核对 Nacos DataId；两边同补 |
| 全局 180s 影响其它慢接口体感 | 先全局；有问题再 per-route |
| Content-Type 尚未写出时仅靠 path | path 模式已覆盖；双条件兜底 |
| route id 与 group 不一致 | 锁定均为 `alex-ai` |

## 10. Open Notes (non-blocking)

- 若生产 Gateway 路由**仅**在 Nacos：本地 yaml 仍补齐作为源码真相，并同步运维改 Nacos。
- 是否为 AI 单独加大超时而不动全局：本版不做；记入后续优化。
