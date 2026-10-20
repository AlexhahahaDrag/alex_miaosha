# 📈 礼尚往来人情收支可视化与亲友下钻对账 Checklist

> **关联项目**：`alex_miaosha_front`（PC端） + `alex_miaosha_mobile`（移动端） + `alex_miaosha_finance`（财务微服务）  
> **关联文档**：`TESTING_STANDARD.md`、`01-Standards/PC端开发规则与组件范式.md`、`01-Standards/移动端开发规则与适配规约.md`、`02-Features/01-Gift-礼尚往来/`  
> **最后更新**：2026-10-20  
> **维护责任**：前端研发组 / 移动端研发组 / 测试组

---

## 0. 元信息

| 项 | 内容 |
| :--- | :--- |
| **涵盖模块** | 1. **PC 端**：`views/finance/gift/analysis/index.vue`、`views/finance/gift/analysis/components/GiftPersonDetailDrawer.vue`<br>2. **移动端**：`views/finance/gift/analysis/index.vue`、`components/chart/BaseChart.vue`<br>3. **测试套件**：PC 端 `tests/unit/giftAnalyticsVisualization.test.ts`、移动端 `tests/unit/giftMobileAnalyticsTrend.test.ts` |
| **测试分层配比** | 单元测试 (60%) + 组件/快照测试 (25%) + AI/E2E 流程测试 (15%) |
| **核心算法与业务** | 1. **收支顺逆差走势图**：`netGap = receiveAmount - giveAmount`，双轴（柱条代表收/送绝对值，折线代表差额正负）；<br>2. **圈层亲密度占比图**：亲属、同事、好友、同学四大圈层聚合与百分比折算，扇区点击联动表格过滤；<br>3. **亲友往来明细抽屉**：原位右侧滑出该亲友时间轴流水、双向轧差对账明细与 AI 回礼速算联动；<br>4. **移动端轻量走势与胶囊筛选**：小屏友好双轴图表、横向滑动圈层胶囊（`selection` 触觉反馈）。 |
| **质量门禁指标** | PC 端与移动端单测通过率 100%，无全局 ECharts 同步全量引入，路由切换妥善 dispose，无雪花 ID 精度截断。 |

---

## 1. 核心高价值测试目标与防线

| # | 领域 | 规则与必测目标 | 关联测试用例 | 期望结果 |
| :--- | :--- | :--- | :--- | :--- |
| **GIFT-VIS-01** | PC 顺逆差曲线算法 | 双轴图表中折线系列计算：`netGap = receiveAmount - giveAmount`，顺差正数，逆差负数。 | `test_pc_net_gap_computation` | 准确计算多月差额，正值在 0 轴上方，负值在 0 轴下方，支持平滑悬浮展示。 |
| **GIFT-VIS-02** | PC 圈层分布聚合 | 遍历往来亲友与记录，按 `relationType` 分组聚合总金额与总人数，折算各圈层占比。 | `test_pc_relation_distribution_aggregation` | 聚合比例和为 100%，空数据或未知关系归入其他或平滑兜底，点击扇区可驱动过滤。 |
| **GIFT-VIS-03** | 亲友往来明细抽屉下钻 | 在平衡矩阵中点击亲友「往来明细」，通过 `personId` 唤起侧拉抽屉，展现该亲友双向时间轴流水。 | `GiftPersonDetailDrawer` 单元/组件断言 | 抽屉正常开启，流水按时间倒序排列，支持联动触发回礼档位测算。 |
| **GIFT-VIS-04** | 移动端轻量双轴走势 | 移动端采用小屏紧凑配置（双轴柱条+折线），隐藏冗余纵向网格，Tooltip 支持触控 confine。 | `test_mobile_trend_chart_option_assembly` | 正确组装 options，空记录时返回空对象防空白塌陷。 |
| **GIFT-VIS-05** | 移动端圈层胶囊过滤 | 矩阵卡片上方滑动胶囊栏（全部/亲属/同事/好友/同学），点击调用 `triggerHaptic('selection')` 并过滤列表。 | `test_mobile_circle_capsule_filtering` | 圈层与关键词双重过滤交集准确，触觉振动探针触发 10ms。 |
| **GIFT-VIS-06** | 雪花 ID 精度安全护城河 | 所有抽屉下钻、参数传递及矩阵渲染，亲友 ID、事件 ID 必须纯 `string` 传输。 | `test_person_id_string_contract` | 绝不出现 number 转换与高位 00 截断。 |
| **GIFT-VIS-07** | 图表生命周期与内存防护 | 离开路由或组件销毁时，移除 `window.resize` 监听并调用 `chartInstance.dispose()`。 | PC/移动端生命周期销毁测试 | 杜绝 SPA 页面跳转导致的内存泄漏与 DOM 孤儿节点。 |
| **GIFT-VIS-08** | 年度人情日历热力图 | 全年 365 天网格按天呈现随礼/收礼密度，深浅渐变色阶映射金额，点击方块可联动查看当日收支。 | `GiftCalendarHeatmap` 单元断言 | 完整映射全年日期，吉日散点分布计算准确，支持按年份切换。 |
| **GIFT-VIS-09** | 亲友社交圈层力导向图谱 | 以本人为核心节点辐射 5 大圈层 Hub 与各亲友节点，节点大小映射礼金总额，连线粗细映射往来权重。 | `GiftRelationshipGraph` 单元断言 | 准确构建力导向网络，节点半径受 `[18, 40]` 范围约束，失衡亲友高亮红色虚线边框，点击亲友唤起明细抽屉。 |

---

## 2. 字段边界（七点法）与异常处理

| 字段 / 场景 | 极小值/空值 | 典型正常值 | 极大值/超长 | 格式边界/非法字符 | 预期处理 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **月度收礼金额 (`receiveAmount`)** | `0.00` / `null` | `1000.00` | `99999999.99` | 科学计数法 `"1e5"` | 转换为数值类型计算，空值或非法值兜底为 `0`。 |
| **月度随礼金额 (`giveAmount`)** | `0.00` / `null` | `500.00` | `99999999.99` | 负数 `-500` | 随礼必须为非负数，展示柱条高度非负。 |
| **顺逆差 (`netGap`)** | `-99999999.99` | `+500.00`、`-300.00` | `+99999999.99` | 浮点精度误差 `0.00000001` | 保留两位小数，正值标绿/蓝，负值标红/橙。 |
| **亲友关系圈层 (`relationType`)** | `null` / `""` | `RELATIVE`、`COLLEAGUE` | 未知类型 `OTHER_CUSTOM` | 特殊字符 `!@#$` | 未知类型归入"其他/未分类"，平滑渲染无异常。 |
| **明细流水记录列表 (`records`)** | `[]` 空数组 | 5 ~ 20 条流水 | 1000+ 条超长流水 | 存在 null 项 | 空数组展示友好缺省占位，大列表支持虚拟滚动或自然分页。 |

---

## 3. 权限与数据隔离矩阵

| 角色 / 场景 | 权限范围 | 可视化与下钻表现 | 数据隔离机制 |
| :--- | :--- | :--- | :--- |
| **普通用户 (Normal User)** | 仅限本用户自身的人情往来流水与亲友 | 仅渲染属于当前 `user_id` 的月度趋势、圈层饼图与亲友抽屉明细 | MyBatis-Plus `@DataPermission` 注入 `user_id = #{currentUserId}` 过滤 |
| **机构管理员 (Org Admin)** | 所属机构辖区内的人情往来聚合（若开启机构账本） | 查看机构维度的趋势对比与成员人情矩阵 | `@DataPermission` 注入 `org_id in (...)` |
| **系统管理员 (Super Admin)** | 全局人情往来统计看板 | 具备全局总览视角与任意亲友明细下钻分析能力 | 绕过 `@DataPermission` 拦截 |

---

## 4. 不测项说明 (Exclusions & Rationale)

1. **ECharts 底层 WebGL / Canvas 像素级逐帧渲染**：不纳入自动化断言范围，由 ECharts 6 官方内核保障，上层仅断言配置项（Options）与数据驱动映射。
2. **移动端真机物理马达振动波形**：依赖特定硬件驱动，自动化中通过 Mock `navigator.vibrate` 断言振动时长（如 10ms、30ms）与微交互参数。
3. **商户进销存与秒杀商品中心**：属于隔离业务线，本功能不涉及、严禁触碰，不纳入本模块测试矩阵。
