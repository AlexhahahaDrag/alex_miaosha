# Checklist: AI Gateway SSE + Apifox + 全局 Copilot 流式穿透

**Spec:** docs/superpowers/specs/2026-08-31-ai-gateway-sse-apifox-design.md  
**Date:** 2026-10-20 (集成强化验收)

## 1. Gateway 路由与长连接超时配置

| # | Item | Pass? | Notes |
|---|------|-------|-------|
| 1 | `application-dev/test/prod.yaml` 含 `id: alex-ai`, `Path=/am-ai/**`, `StripPrefix=1` | ✅ | 静态核对 `alex_miaosha_gateway/src/main/resources/application-{dev,test,prod}.yaml` 三环境均已包含 |
| 2 | `uri` 分别为 `lb://alex-ai-dev|test|prod` | ✅ | dev→`alex-ai-dev`，test→`alex-ai-test`，prod→`alex-ai-prod` |
| 3 | `spring.cloud.gateway.httpclient.response-timeout: 180s` | ✅ | 三环境 yaml 均为 `180s`（保障大模型深度思考与长文本流式不被网关截断） |
| 4 | 若 Nacos 覆盖 Gateway 路由：线上 DataId 已同步 `alex-ai` 路由与超时 | ⏸ deferred | Nacos 物理机环境运维统一维护；本地构建与多环境 yaml 保持 100% 幂等配置 |

## 2. Gateway SSE 加密旁路与端到端穿透测试 (Automated Integration)

| # | Item | Pass? | Notes |
|---|------|-------|-------|
| 5 | `POST /am-ai/api/v1/ai/chat/stream` 携带 Token 时经网关放行且豁免加密 | ✅ | 由 `GatewayAiStreamRoutingIT#authorizedAiStream_shouldForwardToChainWithoutEncryption` 自动化断言通过 |
| 6 | stream 响应体**保持原生明文 SSE 帧**（可明文观察到 `event: meta` / `delta` / `done`，非 AES 整包密文） | ✅ | `GatewayAiStreamRoutingIT` 逐字节断言验证通过，杜绝长连接被整包加密缓冲损坏 |
| 7 | 未携带 Token 请求流式端点时被网关 403 阻断并加密报错 | ✅ | 由 `GatewayAiStreamRoutingIT#unauthorizedAiStream_shouldBlockWith403` 自动化断言通过 |
| 8 | 泛型 SSE 路径识别（如 `finance-info/ai-parse-stream` 与 `custom/stream`） | ✅ | 由 `GatewayAiStreamRoutingIT#genericSsePaths_shouldSkipResponseEncryption` 自动化断言通过 |
| 9 | `GatewaySsePathMatcherTest` 8 项测试全绿 | ✅ | 覆盖 `/am-ai/.../chat/stream`、`finance-info/ai-parse-stream` 等泛型通配与拒绝普通 Rest 路径 |
| 10 | `AiAnalyzeControllerStreamTest` 3 项测试全绿 | ✅ | 覆盖 Controller 层 `/chat/stream` SseEmitter 异步推送与异常降级 |

## 3. PC 端全局 AI 智能助手组件 (Copilot)

| # | Item | Pass? | Notes |
|---|------|-------|-------|
| 11 | 通用 API 封装 `src/api/ai/index.ts` 支持 SSE 流式逐帧解析 | ✅ | 由 `tests/unit/globalAiAssistant.test.ts` 自动化单测通过，支持 `meta`、`delta`、`done`、`error` |
| 12 | 全局悬浮胶囊与侧边抽屉组件 `GlobalAiAssistantDrawer.vue` | ✅ | 挂载于 `src/App.vue`，支持全局悬浮展开、逐字打字机呼吸光标、快捷指令卡片推荐 |
| 13 | PC 端 `vue-tsc --noEmit` 0 报错 & unit tests 83 项全绿 | ✅ | 彻底斩断循环/深层依赖，类型安全坚如磐石 |

## 4. 不测理由

- 真外网大模型物理网络波动（单测基于 Mock/集成测试覆盖状态机流转与协议断言）
- Apifox CLI 自动同步（在 CI 环境通过 OpenAPI 文档白名单放行验证，手动导入不受阻断）
