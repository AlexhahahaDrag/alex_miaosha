# Checklist: AI Engine Routing, Markdown Cleaning & Quota Degradation (第三十阶段)

**Module:** `alex_miaosha_ai`  
**Date:** 2026-10-20  
**Standard:** `TESTING_STANDARD.md` (Level 2 Integration Tests)

---

## 1. 大模型 Markdown 代码块自适应清洗与结构化容错矩阵

| # | 测试项 | 覆盖类 / 方法 | 验证断言 | 状态 |
|---|---|---|---|:---:|
| 1 | Markdown 围栏多变体剥离 | `AiMarkdownFenceCleaningIT#testStripMarkdownFencesVariants` | 验证 `stripMarkdownFences` 正确清洗 ````json`、CRLF 空行、无语言标识 ```` 以及普通文本 | ✅ PASS |
| 2 | ````json 包裹结构化解析 | `AiMarkdownFenceCleaningIT#testAnalyzeMarkdownFencedJsonSuccess` | 大模型返回代码块包裹时，自适应清洗并成功提取 `summary` 和 `keyPoints` 集合 | ✅ PASS |
| 3 | 纯 JSON 响应正常反序列化 | `AiMarkdownFenceCleaningIT#testAnalyzePlainJsonSuccess` | 无围栏的合法 JSON 字符串 100% 结构化解析 | ✅ PASS |
| 4 | 非 JSON 自由文本平稳降级 | `AiMarkdownFenceCleaningIT#testAnalyzeFreeTextDegradesGracefully` | 大模型返回纯自然语言时降级为完整纯文本 summary，`keyPoints` 保留为空列表 | ✅ PASS |
| 5 | 畸变未闭合 JSON 容错保活 | `AiMarkdownFenceCleaningIT#testAnalyzeMalformedJsonHandledGracefully` | 语法错误的半截 JSON 捕获异常，降级输出清洗后文本，绝不抛出 500 崩溃 | ✅ PASS |

---

## 2. 多模型动态路由与调用方参数覆盖矩阵

| # | 测试项 | 覆盖类 / 方法 | 验证断言 | 状态 |
|---|---|---|---|:---:|
| 6 | 默认配置路由 | `AiEngineDynamicRoutingIT#testDefaultRouteToDeepSeek` | 入参未传 engine 时，按配置文件默认路由至 `deepseek` 引擎 | ✅ PASS |
| 7 | 入参动态覆盖路由 | `AiEngineDynamicRoutingIT#testExplicitRouteOverrideToSenseNova` | 入参 `req.setEngine("sensenova")` 动态覆盖配置，精准分发至商汤引擎 | ✅ PASS |
| 8 | 规则引擎直通 | `AiEngineDynamicRoutingIT#testDirectRouteToRuleBased` | 请求指定 `engine="rule-based"` 直通本地轻量规则引擎 | ✅ PASS |
| 9 | 上游异常自动回退规则引擎 | `AiEngineDynamicRoutingIT#testLlmFailureFallbacksToRuleBasedGracefully` | 大模型超时异常时，自动 fallback 到规则引擎，标记 `rule-based(fallback)` 并追加提示语 | ✅ PASS |
| 10 | 禁用降级时熔断阻断 | `AiEngineDynamicRoutingIT#testEngineUnavailableThrowsExceptionWhenFallbackDisabled` | `fallback=false` 时，未知或不可用引擎严格抛出 `AiException(AI_ENGINE_UNAVAILABLE)` | ✅ PASS |

---

## 3. Redis 语义指纹防穿透与多级限流配额降级矩阵

| # | 测试项 | 覆盖类 / 方法 | 验证断言 | 状态 |
|---|---|---|---|:---:|
| 11 | 语义指纹顺序无关确定性 | `AiServiceFingerprintAndQuotaIT#testCacheFingerprintDeterministicRegardlessOfMapOrder` | context Map 插入顺序不同但内容相同时，生成确定一致的 SHA-256 语义指纹缓存键 | ✅ PASS |
| 12 | 缓存命中免扣配额 | `AiServiceFingerprintAndQuotaIT#testCacheHitReturnsDirectlyWithoutRouting` | Redis 命中时返回结果并标注 `engine="redis-cache"`，0 引擎路由调用，不扣减限额 | ✅ PASS |
| 13 | 分钟超限（>15次）平滑降级 | `AiServiceFingerprintAndQuotaIT#testMinuteLimitExceededDegradesToRuleBasedWithBanner` | 超过分钟限额时降级至规则引擎（`rule-based(quota-degraded)`），追加友好使用横幅 | ✅ PASS |
| 14 | 单日配额（>150次）超限降级 | `AiServiceFingerprintAndQuotaIT#testDailyQuotaExceededDegradesToRuleBasedWithBanner` | 超过单日配额时平滑降级至轻量规则引擎，杜绝抛出 429 报错影响用户体验 | ✅ PASS |
| 15 | Redis 故障高可用容错 | `AiServiceFingerprintAndQuotaIT#testRedisExceptionDoesNotBlockMainService` | Redis 断连/超时抛异常时只记录告警日志，业务主调用链路平稳放行 | ✅ PASS |

---

## 4. SSE 流式传输生命周期与安全回收矩阵

| # | 测试项 | 覆盖类 / 方法 | 验证断言 | 状态 |
|---|---|---|---|:---:|
| 16 | 完整协议时序推送 | `AiSseStreamLifecycleIT#testNormalLifecycleSendsMetaDeltaDoneAndCompletes` | 严格按 `meta -> delta(多次) -> done` 推送并最终触发 `emitter.complete()` | ✅ PASS |
| 17 | 异常中断流式收尾 | `AiSseStreamLifecycleIT#testErrorLifecycleSendsErrorAndIgnoresSubsequentDeltas` | 异常时推送 `error` 事件并关闭流，后续迟到的 `delta` 数据被安全阻断 | ✅ PASS |
| 18 | 客户端提前断开容错 | `AiSseStreamLifecycleIT#testClientDisconnectIOExceptionHandledGracefully` | 抛出 `IOException`（如 Broken pipe）时通过 `completeWithErrorQuietly` 安全释放资源 | ✅ PASS |
| 19 | 重复关闭安全锁 | `AiSseStreamLifecycleIT#testDoneAlreadyCompletedDoesNotDuplicateEmitterCall` | `done` 关闭后后续异常收尾被静默忽略，杜绝重复调用抛出异常 | ✅ PASS |

---

## 5. 不测理由

1. **真实物理 OpenAI/DeepSeek 远程公网 API 计费网络调用**：测试聚焦于双路由分发、Markdown 清洗容错、Redis 缓存与多级限流规则；外部公网 API 网络受网络连通性与 API 计费限制，由 MockClient 确保行为确定性；
2. **秒杀模块排除**：按照用户明确指令，本阶段测试与代码加固严格排除秒杀模块。
