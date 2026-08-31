# Checklist: AI Gateway SSE + Apifox

**Spec:** docs/superpowers/specs/2026-08-31-ai-gateway-sse-apifox-design.md  
**Date:** 2026-08-31

## Config

| # | Item | Pass? | Notes |
|---|------|-------|-------|
| 1 | `application-dev/test/prod.yaml` 含 `id: alex-ai`, `Path=/am-ai/**`, `StripPrefix=1` | ✅ | 静态核对 `alex_miaosha_gateway/src/main/resources/application-{dev,test,prod}.yaml` 三文件均已包含 |
| 2 | `uri` 分别为 `lb://alex-ai-dev|test|prod` | ✅ | dev→`alex-ai-dev`，test→`alex-ai-test`，prod→`alex-ai-prod` |
| 3 | `spring.cloud.gateway.httpclient.response-timeout: 180s` | ✅ | 三环境 yaml 均为 `180s` |
| 4 | 若 Nacos 覆盖 Gateway 路由：线上 DataId 已同步 `alex-ai` 路由与超时 | ⏸ deferred | Nacos 不可达（`curl http://10.10.20.238:8848/...` exit 7）；DataId 候选 `alex-gateway-dev.yaml` / group `alex-miaosha` / tenant `033377eb-973b-4dac-a0e9-e99c87325009` — **verify on next Nacos up** |

## Functional (dev)

| # | Item | Pass? | Notes |
|---|------|-------|-------|
| 5 | `POST /am-ai/api/v1/ai/chat`（经 Gateway :30001，带 Token）返回业务成功 | ⏸ deferred | User 未起（`:30006` conn refused；Gateway `/am-user/.../login` → 503），无 Token；Gateway 无 Token → **401**。直连 `:30010/api/v1/ai/chat` rule-based → **200**（仅后端 sanity） |
| 6 | `POST /am-ai/api/v1/ai/chat/stream` 响应 `text/event-stream`，可见 `meta`/`delta`/`done` | ⏸ deferred | 同上无 Token，Gateway stream → **401**。直连 `:30010/.../stream` 可见 `event:meta`/`delta`/`done` |
| 7 | stream 响应体**不是** AES 整包密文（可人眼看到 `event:` 行） | ⏸ deferred | Gateway 路径未测（无 Token）。直连 AI stream 为明文 SSE（非 AES 整包）— Gateway 路径待 User 起后复测 |
| 8 | `GET /am-ai/v3/api-docs?group=alex-ai` 含 `/ai/chat` 与 stream | ❌ | Gateway → **401**（`GatewayWebSecurityConfig` 仅 permit `/v3/api-docs`，不含 `/am-ai/v3/api-docs`）。直连 `:30010/v3/api-docs?group=alex-ai` → **200**，paths 含 `/am-ai/api/v1/ai/chat` 与 `/stream` |
| 9 | Apifox 导入上述 OpenAPI URL 成功 | ⏸ deferred | 未手工打开 Apifox；Gateway OpenAPI URL 401。可暂用 `http://127.0.0.1:30010/v3/api-docs?group=alex-ai` 导入 — **verify when Gateway doc path fixed + Apifox available** |

## Automated

| # | Item | Pass? | Notes |
|---|------|-------|-------|
| 10 | `GatewayAiRouteConfigTest` green | | `mvn -pl alex_miaosha_gateway test -Dtest=GatewayAiRouteConfigTest` |
| 11 | `GatewaySsePathMatcherTest` green | | `mvn -pl alex_miaosha_gateway test -Dtest=GatewaySsePathMatcherTest` |

## 不测理由

- 真外网 LLM CI
- 移动端
- Feign stream
- Apifox CLI 自动同步
