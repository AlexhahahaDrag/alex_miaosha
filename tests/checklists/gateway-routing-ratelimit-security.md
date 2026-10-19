# Checklist: Gateway Dynamic Routing, Rate Limiting & Security (第二十七阶段)

**Module:** `alex_miaosha_gateway`  
**Date:** 2026-10-19  
**Standard:** `TESTING_STANDARD.md` (Level 2 Integration Tests)

---

## 1. 动态路由与多环境配置覆盖矩阵

| # | 测试项 | 覆盖类 / 方法 | 验证断言 | 状态 |
|---|---|---|---|:---:|
| 1 | 开发环境 5 大微服务路由 | `GatewayFullRouteConfigIT#applicationDev_verifyAllFiveMicroserviceRoutes` | user/finance/oss/product/ai 5 大路由全部配置 `StripPrefix=1`，`Path=/am-*/**`，`uri=lb://alex-*-dev` | ✅ PASS |
| 2 | 测试环境 5 大微服务路由 | `GatewayFullRouteConfigIT#applicationTest_verifyAllFiveMicroserviceRoutes` | 路由映射对应 `lb://alex-*-test`，前缀剥离与路径模式一致 | ✅ PASS |
| 3 | 生产环境 5 大微服务路由 | `GatewayFullRouteConfigIT#applicationProd_verifyAllFiveMicroserviceRoutes` | 路由映射对应 `lb://alex-*-prod`，前缀剥离与路径模式一致 | ✅ PASS |
| 4 | 全局 HTTPClient 超时契约 | `GatewayFullRouteConfigIT#allProfiles_verifyHttpClientTimeouts` | 连接超时 5000ms，响应超时 180s（服务 AI 长文本流与导出） | ✅ PASS |
| 5 | 服务发现 Locator 契约 | `GatewayFullRouteConfigIT#allProfiles_verifyDiscoveryLocator` | `discovery.locator.enabled=true`，`lower-case-service-id=true` | ✅ PASS |

---

## 2. 多维度限流 KeyResolver 矩阵

| # | 测试项 | 覆盖类 / 方法 | 边界与验证逻辑 | 状态 |
|---|---|---|---|:---:|
| 6 | IP 限流：多级代理穿透 | `GatewayRateLimiterIT#ipKeyResolver_withXForwardedFor_shouldExtractFirstClientIp` | `X-Forwarded-For: 203.0.113.195, 70.41.3.18` 提取首个真实客户端 IP | ✅ PASS |
| 7 | IP 限流：X-Real-IP 提取 | `GatewayRateLimiterIT#ipKeyResolver_withXRealIp_shouldExtractIp` | 单级代理 `X-Real-IP: 198.51.100.42` 正确识别 | ✅ PASS |
| 8 | IP 限流：底层 RemoteAddress 兜底 | `GatewayRateLimiterIT#ipKeyResolver_withRemoteAddress_shouldExtractIp` | 无代理请求头时，提取底层 Socket `RemoteAddress.getHostAddress()` | ✅ PASS |
| 9 | IP 限流：极值边界 NPE 防御 | `GatewayRateLimiterIT#ipKeyResolver_withNullAddress_shouldFallbackToUnknown` | 无 Socket 连接或 null request 时优雅回退为 `"unknown"`，0 崩溃 | ✅ PASS |
| 10 | 用户限流：默认 Authorization | `GatewayRateLimiterIT#userKeyResolver_withDefaultAuthorization_shouldExtractToken` | 提取 `Authorization: Bearer test-jwt-token-abcdef` | ✅ PASS |
| 11 | 用户限流：自定义 tokenHeader | `GatewayRateLimiterIT#userKeyResolver_withCustomTokenHeader_shouldExtractToken` | 自定义 `X-Access-Token` 提取 | ✅ PASS |
| 12 | 用户限流：未登录匿名兜底 | `GatewayRateLimiterIT#userKeyResolver_withoutToken_shouldFallbackToAnonymous` | 无 Token 时降级返回 `"anonymous"` | ✅ PASS |
| 13 | API 限流：规范 URI 提取 | `GatewayRateLimiterIT#apiKeyResolver_shouldExtractRequestPath` | 提取规范 URI 路径 `/am-ai/api/v1/ai/chat/stream` | ✅ PASS |

---

## 3. 安全鉴权、协议协商与加密旁路矩阵

| # | 测试项 | 覆盖类 / 方法 | 验证断言 | 状态 |
|---|---|---|---|:---:|
| 14 | 文档白名单直通 | `GatewaySecurityFilterIT#docWhiteList_shouldPassDirectlyWithoutAuthentication` | `/doc.html` 与 `/swagger-resources/**` 直接调用 FilterChain 放行，不请求远程 UserApi | ✅ PASS |
| 15 | 未认证 403 阻断与密文报文 | `GatewaySecurityFilterIT#missingToken_shouldReturn403EncryptedJson` | 未携带 Token 时阻断，响应载荷解密验证含 `{"code":403,"data":"请先登录！"}` | ✅ PASS |
| 16 | 协议协商 V2.0 回显 | `GatewaySecurityFilterIT#cryptoVersionNegotiation_v2_shouldEchoV2Header` | 客户端传 `X-Crypto-Version: 2.0` 时，响应头正确回显 `X-Crypto-Version: 2.0` | ✅ PASS |
| 17 | 加密旁路：SSE 流与大文件豁免 | `GatewaySecurityFilterIT#sseStream_and_binaryFile_shouldSkipResponseEncryption` | `text/event-stream`、`/chat/stream`、`.xlsx` 下载豁免加密，杜绝长连接与二进制文件损坏 | ✅ PASS |

---

## 4. 熔断降级控制器契约矩阵

| # | 测试项 | 覆盖类 / 方法 | 验证断言 | 状态 |
|---|---|---|---|:---:|
| 18 | 默认降级返回值绑定 | `GatewayFallbackControllerIT#defaultFallback_shouldReturnSystemUpError` | 访问 `/defaultFallback` 返回规范 `Result.error(ResultEnum.SYSTEM_UP_ERROR)` | ✅ PASS |
| 19 | 降级语义错误码与提示语 | `GatewayFallbackControllerIT#defaultFallback_code_and_message_contract` | 错误码为 500，提示语包含"系统更新中，请稍后尝试！" | ✅ PASS |

---

## 5. 不测理由

1. **分布式 Redis 物理集群并发压测**：测试聚焦于 Reactor 响应式 `KeyResolver` 键解析契约与单机流控逻辑；物理 Redis Cluster Sentinel 在生产环境由基础设施统一运维，不在本单测/集成测试范围；
2. **生产 Nacos 动态配置推送**：涉及真实 Nacos 物理端口与外部网络环境，CI 构建中通过本地 yaml 解析保障多环境配置 100% 幂等一致。
