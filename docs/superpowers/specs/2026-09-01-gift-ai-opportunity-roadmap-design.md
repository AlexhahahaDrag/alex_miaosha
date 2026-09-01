# Gift × AI 机会地图与分批路线图

- Date: 2026-09-01
- Branch context: `develop-1.0-feature-org-manage`
- Status: Design approved in brainstorming (approach: 用户旅程驱动)
- Scope: 机会地图 + 分批路线（PC + Mobile 同规划）；本文件不包含具体实现任务拆解

## 1. 背景与约束

### 1.1 已有 AI 能力

- 服务：`alex_miaosha_ai`（Nacos `alex-ai-{profile}`），网关前缀 `/am-ai/**`
- 接口：
  - `POST /am-ai/api/v1/ai/chat` → `Result<AiAnalyzeResp>`
  - `POST /am-ai/api/v1/ai/chat/stream` → SSE `meta` / `delta` / `done` / `error`
- 请求：`bizType?`、`content`（必填）、`context?`、`depth?`、可选 `engine` / `model`
- 响应：`requestId`、`summary`、`keyPoints[]`、`engine`、`costMs`
- 引擎：`deepseek` / `sensenova` / `rule-based`（失败可回落）
- 现状：仅 PC `tools/ai-chat` 消费；Mobile 无 AI 客户端；Finance 已 `@EnableFeignClients` 含 `com.alex.api.ai` 但 gift 未调用

### 1.2 Gift 现状

- 子域：person / event / record / analysis（ORG_SHARED）
- 金额推荐：规则接口 `GET .../gift-event-info-t/recommend-amount`（非 LLM）
- ID：后端 Long 序列化字符串；前端保持 string

### 1.3 已确认产品决策

| 决策点 | 选择 |
|--------|------|
| 交付物形态 | 完整机会地图 + 分批路线图（暂不单点落地某一功能） |
| 端覆盖 | PC + Mobile 同等纳入 |
| 敏感数据 | 允许姓名、金额进入外部大模型 |
| 组织方式 | 用户旅程驱动；每批 PC+Mobile 同能力 |
| 语音转文字 | **不做实施**；列入 Backlog（现有模型/接口不支持 ASR） |

## 2. 机会地图（按用户旅程）

统一复用：`bizType` + `content` + `context` → `summary` / `keyPoints`（batch 或 SSE）。  
金额**数值**继续走规则 `recommend-amount`；AI 只做解释/叙事，不替代规则。

| 旅程 | 机会 | 建议 bizType | PC | Mobile | 备注 |
|------|------|--------------|----|--------|------|
| 录入 | 备注润色 | `gift-remark` | 记录抽屉 / 快捷记账 | 快捷记账 | 结果进草稿 |
| 录入 | 白话抽字段填表 | `gift-parse-record` | 同上 | 同上 | 人工确认后写库 |
| 录入 | 事由分类 | `gift-event-classify` | 事件表单 | 事件表单 | `context` 带 type-options |
| 录入 | 规则金额 +「为何这档」 | `gift-amount-explain` | 快捷记账 | 快捷记账（补接 recommend） | 先规则后 AI |
| 人脉 | 疑似重复人解释 | `gift-person-dedupe` | 新建人 | 新建人 | 先确定性候选 |
| 人脉 | 档案/往来摘要 | `gift-person-profile` | 人详情 | giftPersonDetail | profile+summary 进 context |
| 回礼 | 待回礼优先级叙事 | `gift-return-coach` | 待回礼 / Dashboard | 概览 | 待回礼列表进 context |
| 复盘 | 图表 AI 解读 | `gift-analysis` | Dashboard / Analysis | 概览 / 分析 | 聚合 JSON |
| 复盘 | 自然语言 → 筛选 | `gift-nl-filter` | 业务列表 | 业务列表 | 映射现有 query，不直写库 |
| 横切 | GiftAiClient 双端封装 | — | gift 公共层 | 新建 AI 客户端 | P0 基建 |

### 2.1 明确不做（本路线图外）

- AI 自动改库 / 自动记账
- AI 覆盖规则推荐金额
- 跨机构数据进入同一 prompt
- 本期实施语音 ASR

## 3. 分批路线图

| 批次 | 主题 | 交付 | 基建要点 |
|------|------|------|----------|
| **P0** | 双端 AI 通道 | Gift 调用 chat/stream；统一 bizType、降级、loading | Mobile 新建 AI 客户端；鉴权走现网 JWT |
| **P1** | 复盘洞察 | Dashboard/分析「AI 解读」流式展示 | `gift-analysis`；context 仅聚合数据 |
| **P2** | 录入助手 | 备注润色、字段抽取、事由分类、金额解释 | 一律确认后写库 |
| **P3** | 回礼 + 人脉 | 待回礼教练、档案摘要、去重解释 | 去重混合策略 |
| **P4** | NL 筛选 | 自然语言 → 现有 list/page 过滤条件 | 固化 keyPoints→过滤器契约 |
| **Backlog** | 语音录入 | 语音 → ASR → 文字 → 接 P2 填表 | **需另接 ASR**；优先 Mobile |

## 4. 架构

### 4.1 数据流

```
PC / Mobile UI
  → GiftAiClient（Authorization + bizType/content/context）
  → Gateway /am-ai/**（JWT）
  → alex-ai /api/v1/ai/chat 或 /stream
  → Engine（deepseek | sensenova | rule-based fallback）
  → summary + keyPoints
  → UI 展示；写库仅用户确认后走现有 gift CRUD
```

Finance 侧若需「先查业务再解读」，可用已启用的 `AiAnalyzeApi` Feign；纯展示场景前端直调网关即可。

### 4.2 契约

| 项 | 约定 |
|----|------|
| bizType | 见 §2 表 |
| context | 当前页聚合/选项；ID 一律 string；禁止无界整表塞入 |
| 写操作 | AI 永不直接 CRUD |
| 金额 | 数值来自 recommend-amount；AI 只解释 |
| 降级 | LLM 失败 → toast；可隐藏 AI 区块或回落 rule-based |
| 权限 | 与 gift 菜单/按钮同级；无 `gift:view` 不调 AI |

## 5. 风险与测试

| 风险 | 应对 |
|------|------|
| 敏感数据出域（已接受） | 日志用 requestId；Key 在 Nacos；手机号默认不进 prompt |
| 幻觉改金额/ID | 金额以规则为准；ID 必须落在候选集 |
| Mobile 无 SSE | P0 打通；可先 batch |
| 成本/延迟 | 分析用 stream；录入短 context；按钮触发非自动刷 |

测试对齐 `TESTING_STANDARD.md`：

- 后端：bizType/解析单测；契约测可用 rule-based
- PC/Mobile：入口 `data-testid`；失败降级；确认后才写库
- 后续实现阶段补 `tests/checklists/gift-ai.md`

## 6. 下一步

1. 用户审阅本 spec  
2. 通过后进入 `writing-plans`：按批次（建议先 P0→P1）写实现计划  
3. 未获「Proceed」前不改业务代码  

## 7. Backlog 备忘

- [ ] 语音转文字（ASR）：另选供应商/浏览器 API；转写结果接入 P2 `gift-parse-record`；现有 DeepSeek/SenseNova chat 接口不支持语音
