# 🌐 微服务全局链路追踪 (TraceId) 全量贯通与全栈排错 Checklist

> **关联项目**：`alex_miaosha_gateway`（网关） + `alex_miaosha_common`（`common_api`, `common_core`） + 下游四大微服务（User, Finance, OSS, AI）  
> **关联文档**：`TESTING_STANDARD.md`、`01-Standards/后端微服务开发规则与运维规约.md`  
> **最后更新**：2026-10-20  
> **维护责任**：基础架构组 / 安全组 / 测试组

---

## 0. 元信息

| 项 | 内容 |
| :--- | :--- |
| **涵盖模块** | 1. **网关层**：`TraceIdGlobalFilter`（HIGHEST_PRECEDENCE 注入与回写）<br>2. **核心契约**：`common_api` 下 `Result`、`FeignConfig`（RPC 请求头保真透传）<br>3. **微服务核心**：`common_core` 下 `TraceIdContext`、`TraceIdFilter`、`GlobalExceptionHandler` |
| **测试分层配比** | 单元测试 (70%) + 集成测试 (25%) + 跨服务端到端 (5%) |
| **核心算法与业务** | 1. 32 位 Hex UUID TraceId 生成与保真透传；<br>2. SLF4J MDC 自动绑定与双向回写；<br>3. 跨微服务 Feign 远程调用请求头复制；<br>4. 统一异常处理器（`GlobalExceptionHandler`）响应体与响应头 TraceId 回写与线程安全回收。 |
| **质量门禁指标** | 单元与集成测试通过率 100%，无跨请求线程污染，微服务日志 100% 具备 `traceId` 追踪锚点。 |

---

## 1. 核心高价值测试目标与防线

| # | 领域 | 规则与必测目标 | 关联测试用例 | 期望结果 |
| :--- | :--- | :--- | :--- | :--- |
| **TRACE-01** | TraceId 生命周期与线程隔离 | `TraceIdContext.setTraceId` 必须同步写入 ThreadLocal 与 MDC，并在 `clear()` 后彻底抹平。 | `TraceIdPropagationAndContextTest#testTraceIdContextLifecycle` | 读写准确，清理后 ThreadLocal 与 MDC 无任何残留，杜绝线程池污染。 |
| **TRACE-02** | Servlet 过滤器透传与回写 | 请求携带有效 `X-Trace-Id` 时，Filter 保真透传并回写至 Response Header。 | `TraceIdPropagationAndContextTest#testFilterWithIncomingTraceId` | 响应头 `X-Trace-Id` 恒等于输入 ID，请求完成后上下文自动清空。 |
| **TRACE-03** | 缺省场景自动生成闭环 | 请求未携带 TraceId 时，Filter 自动生成 32 位无短横线 Hex TraceId 并回写。 | `TraceIdPropagationAndContextTest#testFilterWithoutIncomingTraceId` | 自动生成合法 32 位 Hex 字符串，请求链条具备完整追踪凭据。 |
| **TRACE-04** | 异常响应体 TraceId 回显 | 业务抛出异常经 `GlobalExceptionHandler` 捕获时，返回的 `Result` 对象必须携带当前 `traceId`。 | `TraceIdPropagationAndContextTest#testGlobalExceptionHandlerAttachesTraceId` | `Result.getTraceId()` 准确还原当前活跃 TraceId，便于前端与客户端直接呈现给用户。 |
| **TRACE-05** | Feign 跨微服务 RPC 穿透 | 微服务间发起 Feign 调用时，`FeignConfig` 自动提取当前 Web 上下文或 MDC 的 TraceId 复制到模板中。 | `FeignTraceIdAndHeaderPropagationIT` | 下游请求头包含 `X-Trace-Id`、`Authorization`、`X-Gray-Tag` 等核心标签。 |

---

## 2. 字段边界（七点法）与异常处理

| 字段 / 场景 | 极小值/空值 | 典型正常值 | 极大值/超长 | 格式边界/非法字符 | 预期处理 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **请求头 TraceId (`X-Trace-Id`)** | `null` / `""` | `4a3b2c1d9e8f...` (32位) | 128位超长追踪串 | 特殊符号 `\r\n` 注入 | 空值自动补齐生成；合法值透传；换行符由容器安全过滤。 |
| **MDC 键名** | `traceId` | `traceId` / `X-Trace-Id` | - | - | 统一双向注入 `traceId` 与 `X-Trace-Id`，兼容各类 Logback pattern。 |
| **异步任务/线程池** | 无 HTTP 上下文 | 手动或装饰器传递 | - | 线程复用残留 | 依赖 MDC 与 RequestContextListener，任务结束必须执行 `TraceIdContext.clear()`。 |

---

## 3. 不测项说明 (Exclusions & Rationale)

1. **底层 Undertow / Tomcat 原生 Header 解析器细节**：容器底层实现已由官方经过百万级测试，本模块仅断言标准 Servlet API 契约与 Filter 行为。
2. **商户进销存与秒杀商品中心**：属于隔离业务线，本功能不涉及、严禁触碰。
