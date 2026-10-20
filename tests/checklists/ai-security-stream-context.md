# AI 智能助手多会话流式响应背压控制、Prompt 防注入与上下文记忆裁剪质量清单 (`ai-security-stream-context.md`)

## 1. 业务场景与覆盖范围

涵盖 `alex_miaosha_ai` 微服务的会话流式交互、安全注入防护与对话记忆管理：
- **SSE 流式背压与取消响应**：`AiStreamBackpressureManager` 用户级多流会话并发限制（默认上限 2）、并发超额返回 429 错误码、超时/断开主动标记 `isCancelled` 并中断大模型 HTTP 流连接；
- **Prompt 注入防御与数据清洗**：`AiPromptSanitizer` 剥离大模型特殊标记（`<|im_start|>`, `[INST]`, `<<SYS>>` 等）、越狱特征指令中性化、XML 结构化标签包裹与超长截断（8000 字符）；
- **上下文对话历史记忆裁剪**：`AiContextPruner` 滑动窗口修剪（保留最近 N 轮）、总字符预算控制（默认 3000 字符），在保留关键财务与业务上下文的同时杜绝 Token 溢出与算力浪费。

---

## 2. 字段边界与输入防御矩阵（七点法）

| 字段 | 极小值/空值 | 边界下值 | 正常边界值 | 越权/非法值 | 超长值 | 注入/特殊字符 | 防御策略 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `userId` (背压) | null / 未登录 | "anon" | 合法用户 ID 字符串 | 模拟高并发伪造 ID | 任意长度字符串 | 特殊分隔符 | 每个用户限 2 个并发流，Map 计数归零时主动清理防内存泄漏 |
| `userInput` (Prompt) | null / "" | 长度 1 | 标准提问 10~500 字符 | 越狱指令（"ignore all previous"） | 长度 > 8000 字符 | `<|im_start|>`, `[INST]`, `<<SYS>>` | 正则清洗所有模板特殊 Token，越狱词脱敏中性化，硬截断至 8000 字符 |
| `history` (上下文) | null / 空列表 | 1 轮对话 | 2~6 轮对话 | 伪造篡改的历史角色 | > 20 轮或字符 > 3000 | 大文本 Prompt 轰炸 | 滑动窗口截取最近 6 条，逆序遍历预算裁剪（<= 3000 字符），旧历史平滑舍弃 |

---

## 3. 流式交互背压与安全处理流程图

```mermaid
flowchart TD
    ClientReq[客户端请求 /stream SSE] --> BackpressureCheck{BackpressureManager: 检查该用户并发流}
    BackpressureCheck -->|并发 >= 2| Reject429[返回 429001 并发超限错误并关闭]
    BackpressureCheck -->|并发 < 2| AcquireSlot[并发数+1, 绑定 AsyncContext]
    AcquireSlot --> SanitizeInput[AiPromptSanitizer: 清洗输入与截断]
    SanitizeInput --> PruneContext[AiContextPruner: 裁剪历史会话记忆]
    PruneContext --> BuildPrompt[AiPromptBuilder: 组装结构化 Prompt]
    BuildPrompt --> CallEngine[AiEngine: 发起模型流式推理]
    CallEngine --> StreamDelta{流式 Delta 输出}
    StreamDelta -->|客户端主动断开/超时| MarkCancel[sink.markCancelled 并中断连接]
    StreamDelta -->|正常推送完成| SendDone[推送 [DONE] 标记]
    MarkCancel --> ReleaseSlot[finally: 释放许可, 归零清理 Map]
    SendDone --> ReleaseSlot
```

---

## 4. 自动化测试套件与覆盖情况

1. **`AiPromptInjectionSanitizationIT`**（5 项用例全部 PASS）：
   - `sanitize_whenSpecialControlTokensPresent_stripsTokens`：成功剥离 `<|im_start|>`, `[INST]`, `<<SYS>>` 等大模型特殊控制 Token；
   - `sanitize_whenJailbreakPhrasesPresent_neutralizesContent`：中性化/脱敏典型的越狱与指令重置提示词；
   - `sanitize_whenLengthExceedsThreshold_truncatesSafely`：输入超过 8000 字符时安全截断；
   - `fenceContent_wrapsInXmlTags`：XML 标签包裹用户输入，杜绝指令逃逸；
   - `promptBuilder_integratesSanitization`：Prompt 组装器整体链路防注入协同集成。
2. **`AiStreamBackpressureAndCancellationIT`**（4 项用例全部 PASS）：
   - `backpressure_whenExceedsConcurrencyLimit_rejectsNewStream`：单用户并发流超限时拒绝并释放；
   - `backpressure_whenStreamCompleted_cleansUpResource`：流完成时并发数减 1，归零时 Map 记录自动回收防泄漏；
   - `cancellation_whenSinkMarkedCancelled_engineStopsConsumingStream`：流被客户端中断后，底层流消费立即停止；
   - `engineRouter_propagatesCancellationState`：路由层正确透传 `isCancelled()` 状态。
3. **`AiContextMemoryPruningIT`**（4 项用例全部 PASS）：
   - `prune_whenHistoryExceedsMaxRounds_keepsRecentRounds`：超出最大轮数时截取最新对话；
   - `prune_whenTotalCharactersExceedBudget_prunesOldestMessages`：超出字符总预算时逆序优先保留最近上下文；
   - `prune_whenHistoryWithinBudget_retainsAll`：合规长度内完整保留上下文；
   - `prune_whenHistoryEmptyOrNull_handlesSafely`：空上下文防御与向后兼容。

---

## 5. 不测理由说明 (Exclusions)

- **外部大模型 API 网络抖动与不可达（DeepSeek/OpenAI 生产端）**：AI 模块已具备本地规则降级引擎（`RuleBasedAiEngine`），在线三方端点网络由离线 Mock 和降级机制保证，不直连外网真机计费接口跑自动化测试；
- **前端 DOM 级别 SSE 渲染**：由前端 Web 端 Midscene/Playwright 专门覆盖，后端微服务聚焦 SSE 规范报文事件与流式生命周期。
