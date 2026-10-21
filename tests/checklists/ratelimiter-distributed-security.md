# 分布式接口防刷与多维度 RateLimiter 限流测试清单

## 一、 领域边界与测试范围
本清单覆盖 `common_api` 与 `common_core` 底座实现的分布式限流防刷组件（`@RateLimiter`、`RateLimiterAspect`、`RateLimitKeyResolver`、`RedisSlidingWindowRateLimiter`、`CaffeineFallbackRateLimiter`、`RateLimiterManager`）以及全局异常拦截契约。
核心治理目标：
1. 替代粗颗粒度的固定时间窗口递增，基于 Redis ZSet + Lua 脚本实现精准滑动时间窗口（Sliding Window），彻底根治 59s/61s 临界突发双倍流量漏洞；
2. 支持 `IP`、`USER`、`PARAM`（SpEL 表达式提取入参）、`GLOBAL`（集群接口总容量）多维度隔离；
3. Redis 发生宕机、超时或网络分区时，系统透明自动降级为本地 Caffeine 滑动窗口限流，确保限流组件故障绝不反向瘫痪业务；
4. 拦截超限请求时抛出结构化 `RateLimitException`，由全局异常处理器拦截回写 HTTP 429 状态码与 `Retry-After: {seconds}` 响应头。

---

## 二、 七点法与边界验证矩阵

| 用例编号 | 测试类与方法 | 验证场景与边界输入 | 预期结果与安全断言 | 状态 |
| :--- | :--- | :--- | :--- | :--- |
| **LIMIT-01** | `RateLimiterSlidingWindowIT#testSlidingWindowThresholdEnforcement` | 滑动窗口内阈值限制（time=1s, count=3） | 允许前 3 次正常执行，第 4 次触发拦截抛出 `RateLimitException`（code=429, retryAfter=1） | ✅ PASS |
| **LIMIT-02** | `RateLimiterSlidingWindowIT#testSlidingWindowExpiryAndRecovery` | 窗口滚动与历史过期淘汰（等待 1100ms） | 耗尽配额后拦截，跨越窗口周期后历史请求滑出窗口，新请求自动恢复放行 | ✅ PASS |
| **DIM-01** | `RateLimiterMultiDimensionIT#testIpDimensionIsolation` | `LimitType.IP` 维度配额隔离 | IP-A（192.168.1.101）超限被拦截，IP-B（192.168.1.102）配额独立正常放行 | ✅ PASS |
| **DIM-02** | `RateLimiterMultiDimensionIT#testUserDimensionIsolation` | `LimitType.USER` 维度配额隔离 | 用户 1001 超限被拦截，用户 1002 配额独立正常放行；未登录安全降级为 IP 维度 | ✅ PASS |
| **DIM-03** | `RateLimiterMultiDimensionIT#testParamSpelDimensionIsolation` | `LimitType.PARAM` + SpEL（`#phone`）入参隔离 | 手机号 13800000000 超限被拦截，手机号 13999999999 独立放行，支持业务对象级防刷 | ✅ PASS |
| **FALLBACK-01** | `RateLimiterFallbackAndSecurityIT#testRedisFailureGracefulFallbackToCaffeine` | Redis 故障模拟（连接失败/超时异常） | Manager 捕获异常后透明切换本地 Caffeine 滑动窗口，业务不报错且本地限流依旧精准生效 | ✅ PASS |
| **HTTP-429** | `RateLimiterFallbackAndSecurityIT#testRateLimitExceptionProducesHttp429WithRetryAfterHeader` | 全局异常处理 `GlobalExceptionHandler` 契约 | 精确映射 HTTP 429 状态码，响应头包含 `Retry-After: 45`，响应体携带 code="429" 与友好提示 | ✅ PASS |

---

## 三、 测试覆盖率与金字塔分层总结
- **Level 2 专项集成测试**：7 项专项用例 100% 绿色通过；
- **微服务全量回归**：
  - `common_core`：103/103 PASS（含多级缓存、Druid、链路追踪、XXL-Job与分布式限流）；
  - `user_boot`：权限与密码安全 5/5 PASS。
