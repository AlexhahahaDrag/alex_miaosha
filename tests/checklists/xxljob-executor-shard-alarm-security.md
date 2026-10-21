# XXL-Job 分布式任务调度执行器、分片广播与链路告警测试清单

## 一、 领域边界与测试范围
本清单覆盖 `common_core` 中沉淀的 XXL-Job 分布式任务调度通用执行器底座，以及全微服务（如 `finance_boot` 等）定时任务的稳定性、链路追踪与容灾治理。
核心治理目标：
1. 执行器自动注册心跳、智能内网 IP 探测与防 `127.0.0.1` 误报；
2. 分片广播路由（Sharding Broadcast）取模与列表切片安全、非调度单机环境优雅兜底；
3. `@XxlJob` 任务执行线程自动注入 `TraceIdContext` 与 SLF4J MDC 染色，`finally` 严格清理防线程池污染；
4. 任务执行异常统一捕获、`XxlJobHelper.handleFail` 回写、执行耗时度量与 `JobAlarmNotifier` 失败告警通知。

---

## 二、 七点法与边界验证矩阵

| 用例编号 | 测试类与方法 | 验证场景与边界输入 | 预期结果与安全断言 | 状态 |
| :--- | :--- | :--- | :--- | :--- |
| **SHARD-01** | `JobShardHelperIT#testDefaultSingleNodeFallback` | 非调度环境/本地单机/单测调用 `getShardInfo()` | 安全返回 `(shardIndex=0, shardTotal=1)`，任意 ID 取模判定恒为 true，绝不抛出任何异常 | ✅ PASS |
| **SHARD-02** | `JobShardHelperIT#testIsMyShardWithModulo` | 分片总数=4，测试 ID=0..3 与大数值 10007L | 精确命中对应分片索引，其余分片索引返回 false，分片覆盖率 100% | ✅ PASS |
| **SHARD-03** | `JobShardHelperIT#testNegativeAndExtremeValues` | 负数 ID（-4L）与极值 `Long.MIN_VALUE` 防呆 | 自动取绝对值并做越界防呆，绝不出现负索引或除零越界异常 | ✅ PASS |
| **SHARD-04** | `JobShardHelperIT#testFilterByShard` | 内存数据集切片过滤、空集合与 null 边界 | 单机模式全量放行，非单机模式按分片切片过滤，null/空集合返回空列表 | ✅ PASS |
| **TRACE-01** | `XxlJobTraceAndMdcIT#testTraceIdInjectedDuringJobExecution` | `@XxlJob` 任务触发执行，方法内获取上下文 | 自动生成并注入 `job-{handler}-{uuid}` 标准 TraceId，MDC 同步写入成功 | ✅ PASS |
| **TRACE-02** | `XxlJobTraceAndMdcIT#testTraceIdContextClearedAfterJobCompletion` | 任务执行完毕离开方法 | `finally` 强制执行 `TraceIdContext.clear()`，ThreadLocal 与 MDC 彻底抹平，杜绝线程污染 | ✅ PASS |
| **ALARM-01** | `XxlJobExecutionAndAlarmIT#testNormalExecutionSuccess` | 正常定时任务执行成功 | 顺利返回业务结果，切面记录耗时，不触发告警通知，执行后清空上下文 | ✅ PASS |
| **ALARM-02** | `XxlJobExecutionAndAlarmIT#testFailingJobTriggersAlarm` | 任务抛出未捕获异常（如业务抛错） | 切面捕获并提取堆栈摘要，触发 `JobAlarmNotifier` 告警通知，携带 handler、traceId、costMillis、errorStack 等元数据 | ✅ PASS |
| **ALARM-03** | `XxlJobExecutionAndAlarmIT#testAlarmNotifierFailureRobustness` | 告警通知器内部自身抛出网络异常 | 异常不影响任务流程感知，`finally` 依然安全清除 TraceId 上下文 | ✅ PASS |

---

## 三、 测试覆盖率与金字塔分层总结
- **Level 2 专项集成测试**：9 项专项用例 100% 绿色通过；
- **微服务全量回归**：
  - `common_core`：96/96 PASS（含缓存、Druid、链路追踪与任务调度）；
  - `finance_boot`：定时任务与站内信巡检 14 项用例 100% PASS。
