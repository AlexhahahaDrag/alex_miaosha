# Checklist: Gateway Global Unified JSON Exception, CORS Exposed Headers & Auth Short-Circuit (第三十二阶段)

**Module:** `alex_miaosha_gateway`  
**Date:** 2026-10-20  
**Standard:** `TESTING_STANDARD.md` (Level 2 Integration Tests)

---

## 1. 网关全局统一 JSON 异常处理契约矩阵

| # | 测试项 | 覆盖类 / 方法 | 验证断言 | 状态 |
|---|---|---|---|:---:|
| 1 | 404 路由与资源未找到标准包装 | `GatewayJsonErrorWebExceptionHandlerIT#testHandle_NotFoundException` | `NotFoundException` 捕获并统一输出 `{"code":"404","message":"网关未找到对应服务路由或资源不存在！","data":null}`，杜绝 Spring Boot 默认 Whitelabel 页面 | ✅ PASS |
| 2 | 503 微服务离线或连接拒绝 | `GatewayJsonErrorWebExceptionHandlerIT#testHandle_ConnectException` | `ConnectException` 捕获并映射为 HTTP 503 与统一提示 `"目标微服务不可用或网络不可达，请稍后重试！"` | ✅ PASS |
| 3 | 504 网关调用下游超时 | `GatewayJsonErrorWebExceptionHandlerIT#testHandle_TimeoutException` | `TimeoutException` 捕获并映射为 HTTP 504 与统一提示 `"下游微服务响应超时，请稍后重试！"` | ✅ PASS |
| 4 | 未知 Runtime 异常安全兜底 | `GatewayJsonErrorWebExceptionHandlerIT#testHandle_UnknownRuntimeException` | 未捕获内部异常统一返回标准 500 结构，保持前端响应契约稳定一致 | ✅ PASS |
| 5 | 加密版本回写与 Content-Type | `GatewayJsonErrorWebExceptionHandlerIT#testHandle_CryptoVersionHeaderEcho` | 响应头设置 `Content-Type: application/json;charset=UTF-8`，并在客户端带 `X-Crypto-Version: 2.0` 时回显头信息 | ✅ PASS |

---

## 2. 过滤器认证短路优化与超时降级矩阵

| # | 测试项 | 覆盖类 / 方法 | 验证断言 | 状态 |
|---|---|---|---|:---:|
| 6 | 缺少 Token 头前置短路 | `GatewayFilterAuthOptimizationIT#missingToken_shouldShortCircuitWithoutRpc` | 请求未携带 Token 时网关直接返回 403，断言 `verifyNoInteractions(mockUserApi)`（0 远程 RPC 消耗） | ✅ PASS |
| 7 | 纯空白字符串 Token 前置短路 | `GatewayFilterAuthOptimizationIT#blankToken_shouldShortCircuitWithoutRpc` | `Authorization: "   "` 时网关直接短路返回 403，断言 `verifyNoInteractions(mockUserApi)` | ✅ PASS |
| 8 | 有效 Token 认证成功放行 | `GatewayFilterAuthOptimizationIT#validToken_authenticationSuccess_shouldPass` | 远程认证返回 `true` 时正常执行后续 FilterChain 放行 | ✅ PASS |
| 9 | 有效 Token 认证失败阻断 | `GatewayFilterAuthOptimizationIT#validToken_authenticationFailed_shouldBlock` | 远程认证返回 `false` 或 `403` 时拦截请求，不执行下游链路 | ✅ PASS |
| 10 | 下游认证服务异常优雅降级 | `GatewayFilterAuthOptimizationIT#userApiThrowsException_shouldDegradeGracefully` | 远程调用异常或超时（>3s）时安全降级返回 403，网关 Netty 工作线程不挂死 | ✅ PASS |

---

## 3. CORS 跨域暴露头与 Swagger 聚合文档矩阵

| # | 测试项 | 覆盖类 / 方法 | 验证断言 | 状态 |
|---|---|---|---|:---:|
| 11 | CORS 关键响应头暴露 | `GatewayCorsAndSwaggerContractIT#corsConfiguration_mustExposeRequiredHeaders` | 暴露头必须包含 `X-Crypto-Version`、`Content-Disposition`、`token`、`Authorization`，允许携带 Cookies | ✅ PASS |
| 12 | CORS 预检请求长期缓存 | `GatewayCorsAndSwaggerContractIT#corsConfiguration_mustExposeRequiredHeaders` | `corsConfig.getMaxAge()` 必须严格为 18000 秒，减少浏览器重复 OPTIONS 开销 | ✅ PASS |
| 13 | Swagger 动态路由文档聚合 | `GatewayCorsAndSwaggerContractIT#swaggerResourceConfig_generatesAggregatedApiDocs` | 同步收集微服务路由定义生成对应 `v3/api-docs?group={routeId}` 文档端点 | ✅ PASS |
| 14 | Swagger 路由提取异常平滑降级 | `GatewayCorsAndSwaggerContractIT#swaggerResourceConfig_degradesGracefullyOnRouteLocatorException` | `RouteLocator` 异常时自动降级使用 `GatewayProperties` 预设静态路由定义生成文档 | ✅ PASS |

---

## 4. 不测理由

1. **生产真实 Nacos 物理配置推送**：多环境路由配置通过本地 `application-dev.yaml` / `application-test.yaml` / `application-prod.yaml` 结合集成测试进行校验，中间件长连接推送由平台运维监控保障；
2. **秒杀模块排除**：按照用户明确硬指令“秒杀的东西都先不做”，本次测试与网关加固严格排除秒杀相关接口与业务。
