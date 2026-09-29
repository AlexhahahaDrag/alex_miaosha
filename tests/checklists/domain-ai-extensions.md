# 领域 AI 拓展服务（营销券 / 秒杀文案 / 角色权限 / 缓存配额）测试 Checklist

> **关联微服务**：`alex_miaosha_ai` (30010) + `alex_miaosha_finance` (30008) + `alex_miaosha_product` (30007) + `alex_miaosha_user` (30006)  
> **关联前端**：`alex_miaosha_front` (PC) + `alex_miaosha_mobile` (移动端)  
> **关联标准**：根目录 `TESTING_STANDARD.md`  
> **样板属性**：七点法边界 / 降级兜底 / 权限矩阵 / 性能缓存 / ID 安全契约  
> **最后更新**：2026-09-29

---

## 0. 元信息

| 项 | 内容 |
| --- | --- |
| 覆盖模块 | Coupon AI Plan、Product Seckill AI Copy、Role Permission AI、AI Cache & Quota |
| 责任人 | @alex |
| 网关与 RPC | OpenFeign `AiAnalyzeApi` + Redis 指纹缓存 (`AiCacheKey:analyze:`) |
| 降级机制 | 外部 LLM 超时 (15s) / 断网 / 无 Key / 配额耗尽时，必须 100% 平滑降级至领域 Heuristic Rules / 本地规则引擎 |
| ID 安全红线 | 前端交互、VO 返回的主键/外键（`recommendedMenuIds` 等）严格保持为 `string` 类型 |

---

## 1. 核心测试场景与覆盖矩阵

### 🔴 P0 · 降级熔断与高可用保活

| # | 场景 | 必测 case | 期望结果 | 覆盖层 |
| --- | --- | --- | --- | --- |
| D1 | AI 微服务离线或无 API-Key | `CouponAiServiceTest#testFallbackRuleEngine` | 自动切换启发式规则，输出合理发券量与折扣门槛，返回 HTTP 200 | 单元测试 (`CouponAiServiceTest`) |
| D2 | 商品文案大模型调用超时 | `ProductAiServiceTest#testGenerateMarketingCopyFallback` | 自动降级本地兜底，输出标题、Slogan、卖点与种草详情 | 单元测试 (`ProductAiServiceTest`) |
| D3 | 角色权限推荐无外部 LLM | `RoleAiServiceTest#testFallbackForFinanceRole` | 按岗位名称/职责关键词推导权限，且自动补全父节点 ID | 单元测试 (`RoleAiServiceTest`) |
| D4 | 用户调用频次超限流/配额 | `AiAnalyzeCacheTest#testRateLimitDegradesToRuleBased` | 自动切换为 `rule-based(quota-degraded)`，附加友好提示，业务不中断 | 单元测试 (`AiAnalyzeCacheTest`) |

---

### 🟠 P1 · 性能指纹缓存与契约安全

| # | 场景 | 必测 case | 期望结果 | 覆盖层 |
| --- | --- | --- | --- | --- |
| C1 | 相同参数重复请求 AI | `AiAnalyzeCacheTest#testCacheHitReturnsCachedResp` | Redis 命中缓存，直接返回且 `engine="redis-cache"`，跳过 LLM 远程调用 | 单元测试 (`AiAnalyzeCacheTest`) |
| C2 | Context 字段不同顺序哈希 | `AiAnalyzeCacheTest#testFingerprintDeterministicWithDifferentKeyOrder` | 无论 Context 键序如何变化，生成一致的 MD5 语义指纹 | 单元测试 (`AiAnalyzeCacheTest`) |
| C3 | 权限树推荐 ID 安全 | `rbacAiRecommend.test.mts` | `recommendedMenuIds` 元素全部为 String，无 JS 超过安全整数截断为 00 的隐患 | 前端单元测试 (`npm run test:unit`) |
| C4 | 接口响应对象解构 | `RoleAiRecommendVo 契约测试` | 统一解构 `const { code, data, message } = await api()` | 前端代码审查 + 单测 |

---

### 🟡 P2 · 端侧交互与表单联动

| # | 场景 | 必测 case | 期望结果 | 覆盖层 |
| --- | --- | --- | --- | --- |
| U1 | PC 秒杀文案一键应用 | `btn-apply-ai-copy` 点击 | 自动将爆款标题同步至当前勾选的商品行，并将全套营销方案复制至剪贴板 | PC 端交互 |
| U2 | PC 角色权限 AI 推荐高亮 | `btn-ai-recommend-permissions` 点击 | 自动勾选并展开树节点，且节点标题高亮追加 `[✨ AI推荐]` 标识 | PC 端交互 |
| U3 | 移动端语音记账启动与识别 | `gift-record-ai-speech-btn` 点击 | 触发 Web Speech 录音与震动；不支持环境弹出友好提示，不白屏抛错 | 移动端交互 |
