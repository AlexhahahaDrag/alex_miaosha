# AI 智能记账多场景扩展功能测试清单 (Finance AI Ledger Checklist)

## 1. 业务场景覆盖矩阵
| 场景编号 | 场景名称 | 典型测试用例输入 | 预期解析场景 | 核心校验要素 | 降级容灾预期 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **TC-01** | 日常餐饮支出 | "昨天中午和同事在肯德基聚餐微信付了128.5元，备注汉堡套餐" | `DAILY_EXPENSE` | 方向=`expense`, 金额=`128.5`, 分类=`catering`, 渠道=`wx`, 时间=昨天 | AI超时或断网时走本地正则，100%抽取金额与分类 |
| **TC-02** | 工资收入入账 | "今天收到发工资到招商银行卡18500元" | `DAILY_EXPENSE` | 方向=`income`, 金额=`18500`, 分类=`salary`, 渠道=`yhk` | 正确识别收入方向与银行卡渠道 |
| **TC-03** | 超市购物小票 | 包含商品单价、数量、总计的收银单文本 | `SHOP_RECEIPT` | 方向=`expense`, 总金额识别, `receiptItems` 列表拆分品名与数量 | 结构化拆解条目小计 |
| **TC-04** | 预付储值卡刷卡 | "在咖啡厅刷星巴克星礼卡消费了38元" | `PREPAID_CARD` | 自动匹配登录用户的星礼卡, 绑定 `prepaidCardId`, 渠道=`prepaid_card` | 卡号通过 Long2String 保障精度安全 |
| **TC-05** | 空输入与异常输入 | "" 或纯空格文本 | `DAILY_EXPENSE` | 返回安全兜底初始结构，不产生空指针异常或前端白屏 | 统一提示默认初始化结构 |

## 2. 字段边界与七点法测试 (Amount & ID Safety)
- 零金额边界：`¥0.00` 允许解析，默认展示并提示；
- 超大额消费：`¥9999999.99` 精度完整保留，数值不溢出；
- 小数点格式：`128`, `128.5`, `128.50`, `¥128.00` 均正常兼容提取；
- 雪花算法 Long 型 ID：`prepaidCardId`、`belongTo` 后端以 Long 存储，序列化走 `Long2StringSerializer` 输出为字符串，前端一律保持 string 类型；
- 时区安全规约：日期时间解析与默认兜底严格调用 `DateUtils.now()` 与 `DateUtils.FORMATTER_YYYY_MM_DD_HH_MM_SS`，杜绝时区偏差。

## 3. 双端交互测试矩阵
- **PC 端**：
  - 列表顶部「智能记账」按钮（`data-testid="btn-finance-ai-parse"`）调出弹窗；
  - 填入文本后「开始智能解析」（`data-testid="btn-do-finance-ai-parse"`），展示结构化预览卡片与小票明细表格；
  - 点击「一键填入表单」（`data-testid="btn-apply-finance-ai-result"`）自动带入记账抽屉，支持二次修改保存。
- **移动端**：
  - 首页搜索栏右侧新增「AI记账」快捷胶囊按钮（`data-testid="mobile-btn-index-ai-ledger"`）；
  - 记账详情页顶部收支切换栏右侧常驻「AI 识别记账」胶囊（`data-testid="mobile-btn-open-ai-sheet"`）；
  - ActionSheet 动作面板支持快捷示例填入，解析后一键填入金额、分类、收支模式与时间。
