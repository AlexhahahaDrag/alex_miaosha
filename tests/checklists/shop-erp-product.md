# 🛍️ 商品中心、消费券、个人财务与店铺进销存全模块测试 Checklist

> **关联项目**：`alex_miaosha_finance` + `alex_miaosha_product`（后端） + `alex_miaosha_front`（PC） + `alex_miaosha_mobile`（移动端）
> **关联文档**：根目录 `TESTING_STANDARD.md`
> **参考样板**：`tests/checklists/gift.md`、`tests/checklists/message.md`
> **最后更新**：2026-10-19
> **维护责任**：前端架构组 / 后端核心研发组

---

## 0. 元信息

| 项 | 内容 |
| :--- | :--- |
| **涵盖模块** | 1. **商品中心**：`pmsShopProduct`（商城商品与秒杀池）、`pmsAttr`（商品属性与规格）<br>2. **消费券中心**：`cpn-coupon-info`（消费券主数据）、`cpn-user-coupon-info`（领券明细）、`cpn-redemption-record-info`（核销流水）<br>3. **个人财务**：`financeManager`（日常记账）、`accountRecordInfo`（账户记录）、`prepaidCardInfoT`（预付卡）、`prepaidConsumeRecordT`（刷卡消费流水）<br>4. **店铺进销存与电商财务**：`shopFinance`（店铺账单）、`shopFinanceAnalysis`（经营大盘）、`shopOrder`（销售订单）、`shopCart`（购物车）、`shopStock`（库存与毛利）、`shopStockBatch`（进货批次成本）、`shopStockAttrs`（库存规格属性） |
| **后端代码路径** | `alex_miaosha_finance/finance_boot/src/main/java/com/alex/finance/` (`shop/`, `cpn/`, `wallet/`, `bill/`)<br>`alex_miaosha_product/product_boot/src/main/java/com/alex/product/` (`pms/`) |
| **后端 VO 路径** | `alex_miaosha_finance/finance_api/src/main/java/com/alex/api/finance/` (`shop/vo/`, `cpn/vo/`, `wallet/vo/`)<br>`alex_miaosha_product/product_api/src/main/java/com/alex/api/product/vo/` |
| **PC 代码路径** | `src/views/product/` (`pmsShopProduct/`, `pmsAttr/`)<br>`src/views/cpn-coupon/` (`cpn-coupon-info/`, `cpn-user-coupon-info/`, `cpn-redemption-record-info/`)<br>`src/views/selfFinance/` (`financeManager/`, `accountRecordInfo/`, `prepaidCardInfoT/`, `prepaidConsumeRecordT/`)<br>`src/views/finance/` (`shopFinance/`, `shopFinanceAnalysis/`, `shopOrder/`, `shopCart/`, `shopStock/`, `shopStockBatch/`, `shopStockAttrs/`) |
| **核心数据库表** | `pms_shop_product_t`, `pms_attr_t`, `cpn_coupon_info_t`, `cpn_user_coupon_t`, `cpn_redemption_record_t`, `finance_record_t`, `account_record_info`, `prepaid_card_info_t`, `prepaid_consume_record_t`, `shop_finance_info_t`, `shop_order_info_t`, `shop_cart_info_t`, `shop_stock_info_t`, `shop_stock_batch_info_t`, `shop_stock_attrs_t` |
| **关键基础设施** | `UserUtils.getLoginUser()` + `@DataPermission(ORG_SHARED/TENANT)` + `Long2StringSerializer`（雪花 ID 序列化） + `DateUtils.now()`（时区安全时间） |

---

## 1. 核心业务规则与高价值测试目标（真实漏洞清单防线）

| # | 领域 | 漏洞/规则描述 | 必测 Case | 期望结果 |
| :--- | :--- | :--- | :--- | :--- |
| **P0-1** | 全域 ID 安全 | 后端雪花主键（`Long` 64 位）若以数字形式传递到前端 JS 会发生精度截断（低 2 位归 0）。必须由后端 `@JsonSerialize(using = Long2StringSerializer.class)` 序列化，且前端在 API 层与工具层递归转换为 `string` 防御。 | `normalize_should_convert_all_ids_to_string_safely` | 所有以 `id` / `*Id` 结尾字段均稳定为 `string`，大数雪花 ID（如 `1847192837192837192`）零失真。 |
| **P0-2** | 店铺财务表单 | `shopFinanceDetail` 历史上曾将支付方式 `payWay` 错误双向绑定到 `formState.isValid`，导致选择微信/支付宝时强行将有效状态改为 1/2，触发保存死锁与状态错乱。 | `test_shop_finance_payway_binding_independent_of_isvalid` | 改变 `payWay` 仅更新支付途径，不污染 `isValid` 字段。 |
| **P1-1** | 商品属性字典 | `pmsAttr` 中分类 `attrType`（0:销售属性, 1:基本属性, 2:两者兼有）、检索类型 `searchType`、展示描述 `showDesc` 与状态 `enable` 具有确定枚举映射，不可被越界数值污染。 | `test_pms_attr_enum_mapping_and_options_integrity` | 选项完整，与后端数据字典 100% 对齐。 |
| **P1-2** | 进货批次成本 | `shopStockBatch` 进货批次综合总成本公式为：`综合总成本 = 货品总采购成本(productCost) + 采购差旅交通杂费(travelExpense)`。若差旅费为空或 0，应按 0 兜底计入。 | `calc_batch_total_cost_should_sum_product_cost_and_travel_expense` | 精确计算且能抵御 `null`、负数和浮点精度偏移。 |
| **P1-3** | 单件预计毛利 | `shopStock` 商品单件预计毛利公式为：`预计毛利 = 建议销售单价(salePrice) - 进货核算单价(costPrice)`；毛利率为 `(salePrice - costPrice) / salePrice * 100%`。 | `calc_stock_gross_profit_and_margin_rate` | 正毛利、零毛利、负毛利（亏损预警）均正确推导，分母为 0 时返回 0%。 |
| **P1-4** | 消费券核销余量 | `cpn-coupon-info` 发行后，`剩余可用数量 = 发行总量(totalQuantity) - 已核销数量(redeemedQuantity)`；核销进度比为 `redeemedQuantity / totalQuantity`。 | `calc_coupon_remaining_and_progress_ratio` | 余量准确；当剩余为 0 时标记已售罄；已过期或余量为 0 时不可再核销。 |
| **P1-5** | 预付卡充值消费 | 储值卡余额流水约束：充值后余额增加，消费后余额扣减；若单笔刷卡金额大于当前可用余额，必须前置抛出余额不足拦截，防止透支穿透。 | `test_prepaid_card_consume_reject_when_balance_insufficient` | 余额不足拒绝扣款，余额充足扣减并生成对等消费明细。 |
| **P2-1** | 多租户/数据隔离 | 店铺进销存 (`shop*`) 属于商户机构私域资产，普通用户仅能查看自己的订单与购物车；商户管理员仅能管理本店铺库存与账单，禁止跨店越权修改。 | `shop_erp_should_enforce_org_and_owner_isolation` | 跨机构访问拦截并抛出无权限错误。 |

---

## 2. 字段边界分析（七点法）

### 2.1 商品与进销存核心字段边界

| 字段 | 类型 | 业务规则 | 七点法边界值 | 期望行为 |
| :--- | :--- | :--- | :--- | :--- |
| `pmsShopProduct.price` | `BigDecimal (10, 2)` | 商品售价，正数 | `null`, `-0.01`, `0.00`, `0.01` (min), `99.00` (typical), `9999999.99` (max), `10000000.00` (max+1) | $\le 0$ 校验拦截，超过 7 位整数拦截，合法金额通过 |
| `shopStock.num` | `Integer` | 当前库存数量，非负整数 | `null`, `-1`, `0` (售罄预警), `1` (min), `500` (typical), `999999` (max), `1000000` (max+1) | 负数拒绝，0 触发缺货/补货徽章，$\ge 1$ 正常可售 |
| `shopStockBatch.travelExpense` | `BigDecimal (10, 2)` | 采购差旅杂费，非负 | `null`, `-10.00`, `0.00` (min), `150.50` (typical), `50000.00` (max), `50000.01` (max+1) | `null` 默认为 0，负数拦截，合法数值计入总成本 |
| `cpnCoupon.discount` | `BigDecimal (10, 2)` | 抵扣/面额，非负 | `null`, `-1.00`, `0.00`, `0.01` (min), `50.00` (typical), `9999.99` (max), `10000.00` (max+1) | 必须大于 0，且不可超过设定门槛 `minSpend` |
| `shopOrder.orderStatus` | `Integer` 枚举 | 订单履行状态：0:待付款, 1:已付款, 2:已发货, 3:已完成, 4:已取消 | `null`, `-1`, `0`, `1`, `2`, `3`, `4`, `5` (非法) | 仅 0~4 有效映射为对应 Tag 与操作按钮，其他归兜底 |

---

## 3. 状态机流转矩阵

### 3.1 销售订单 (`shopOrder`) 生命周期流转

```
[买家下单] ──► 0: 待付款 (Pending Payment)
                     │
                     ├──► 用户取消 / 超时未支付 ──► 4: 已取消 (Cancelled)
                     │
                     ▼ 支付成功回调
               1: 已付款 (Paid / To Ship)
                     │
                     ▼ 商家出库发货 (关联 shopStock 出库扣减)
               2: 已发货 (Shipped)
                     │
                     ▼ 买家确认收货 / 自动签收
               3: 已完成 (Completed)
```

### 3.2 消费券 (`cpn-coupon`) 核销生命周期

```
[创建发券] ──► 1: 进行中 / 可领用 (Active)
                     │
                     ├──► 到期时间已过 ──► 2: 已过期 (Expired)
                     │
                     ▼ 用户领券并出示核销码
               [待核销]
                     │
                     ├──► 商家扫码 / 手动输入核销数量
                     │     └── 校验：redeemedQuantity + quantity <= totalQuantity
                     │
                     ▼ 核销成功
               [记录流水 cpn_redemption_record] ──► 余量为 0 时状态转为 3: 已售罄/核销完毕
```

---

## 4. 权限控制矩阵

| 业务实体 / 操作 | 超级管理员 (`super_super`) | 机构管理员 (`rbac_user_manager`) | 普通员工/消费者 (`rbac_readonly`) |
| :--- | :---: | :---: | :---: |
| **商品管理** (`pmsShopProduct` 增删改查) | 全局所有商品 | 仅限本机构/店铺商品 | 仅查已上架商品 (只读) |
| **店铺进销存** (`shopStock*` 盘点调拨) | 全局所有商户库存 | 仅限本店库存、进货批次 | 无权访问 (403 拦截) |
| **店铺账单** (`shopFinance` 收支台账) | 全局商户财务大盘 | 仅限本店收支记录与分析 | 无权访问 (403 拦截) |
| **消费券核销** (`cpn-coupon` 核销录入) | 全平台券种 | 仅核销本店铺可用券 | 仅查看个人已领券 (我的券包) |
| **个人财务** (`financeManager` 个人记账) | 仅个人名下账单 | 仅个人名下账单 | 仅个人名下账单 (绝对私有隔离) |

---

## 5. 不测理由清单 (What We Do NOT Test)

1. **Ant Design Vue 组件库底层动画**：如 `a-drawer` 的展开过渡动效、`a-modal` 遮罩淡入淡出（由 AntD 官方单测保障，避免测试脆弱性）；
2. **纯静态展示性文案**：如表格列头纯展示文字、无业务逻辑的静态副标题；
3. **Vite 原生 HMR 与模块热重载引擎**：构建工具自带标准机制，无需项目端重复验证；
4. **原生 CSS 响应式媒体查询像素表现**：样式计算由浏览器渲染引擎实现，重点通过 L3 视口烟测覆盖，不编写脆弱的 CSS 单元断言。

---

## 6. L3 端到端 AI 自动化烟测 (Midscene / Playwright) 覆盖情况

> 运行命令：`npm run test:ai:smoke`（执行脚本：`scripts/playwright/run-domain-ai-smoke.mjs`）  
> 测试用例集：`tests/midscene/rbac/cases/domain-ai-smoke.json`（**16/16 100% 全部通过**）

| 用例 ID | 场景标题 | 验证要点与断言目标 | 执行结果 |
| :--- | :--- | :--- | :---: |
| **`PRODUCT-E2E-PC-DRAWER`** | 商品中心价格监测抽屉读写分离与走势图渲染 | 点击 `[data-testid="btn-view-product"]` 唤起 680px `BaseRbacDrawer`，断言 `product-hero-card` 档案、`product-price-kpi-grid` 4 宫格价格指标卡与价格走势图正常渲染 | ✅ **PASS** |
| **`COUPON-E2E-PC-REDEEM`** | 消费券核销抽屉联动与动态余量计算 | 点击 `[data-testid="btn-redeem-coupon"]` 唤起 480px 核销抽屉，点击 `btn-redeem-plus` 步进器，断言 `coupon-redeem-linked-remain` 余量动态扣减计算 | ✅ **PASS** |
| **`SHOP-ERP-E2E-PC-FINANCE-DETAIL`** | 店铺财务凭证抽屉与支付方式防死锁断言 | 点击 `btn-view-shop-finance` 断言 `shop-finance-voucher` 只读电子凭证卡渲染，点击 `btn-edit-shop-finance` 校验 `payWay` 独立于 `isValid` 零死锁 | ✅ **PASS** |
| **`SHOP-ERP-E2E-PC-ANALYSIS`** | 店铺经营大盘 4 宫格 KPI 卡片与图表网格渲染 | 访问 `#/finance/shopFinanceAnalysis`，断言 `shop-analysis-kpi-grid` 4 宫格指标（营收/销量/爆款/渠道）与 `shop-analysis-charts-grid` 2×2 图表正常挂载 | ✅ **PASS** |
| **`AI-PRODUCT-001`** | 商品秒杀营销文案 AI 生成与一键应用 | 唤起 `ProductAiCopyModal`，输入商品名触发 AI 生成，断言 3 条爆款核心卖点与折扣文案成功回填并应用 | ✅ **PASS** |
| **`AI-COUPON-001`** | 优惠券新增弹窗 AI 营销策划与自动回填 | 唤起新增消费券，点击 `btn-generate-ai-plan`，断言 `ai-plan-result` 策略考量与 8 折优惠方案渲染 | ✅ **PASS** |

---

## 7. 后端微服务数据权限与业务集成测试 (IT) 覆盖情况

> 执行命令：`mvn test -pl alex_miaosha_finance/finance_boot -am "-Dtest=ShopErpDataPermissionIT,CpnCouponDataPermissionIT,ShopErpBusinessRuleIT,CpnCouponBusinessRuleIT,PrepaidCardDataPermissionIT,PrepaidCardBusinessRuleIT,FinanceManagerOwnershipIT" -DfailIfNoTests=false`  
> 运行环境：JDK 17 + Maven Reactor  
> 执行结果：**57/57 项用例 100% 全部通过 (Total=57, Passed=57, Failed=0, Errors=0)**

| 套件文件 | 覆盖领域 | 用例数 | 关键断言与防御目标 | 执行结果 |
| :--- | :--- | :---: | :--- | :---: |
| **`ShopErpDataPermissionIT`** | 店铺进销存多租户隔离 | 11 | 验证超管绕过、机构管理员（`org_admin`）在 `t_shop_finance`、`t_shop_stock`、`t_shop_stock_batch`、`t_shop_order`、`t_shop_cart` 的 `t_org_user_info` 子查询拼装，普通用户属主限制，未配置注解方法不污染，未登录安全兜底 | ✅ **PASS** |
| **`CpnCouponDataPermissionIT`** | 消费券多租户隔离 | 7 | 验证超管绕过、机构管理员在 `cpn_coupon_info_t`、`cpn_user_coupon_info_t`、`cpn_redemption_record_info_t` 的机构子查询隔离，普通用户属主限制，无登录安全兜底 | ✅ **PASS** |
| **`ShopErpBusinessRuleIT`** | 进销存核心业务与雪花 ID 序列化 | 12 | 验证批次综合总成本（`productCost + travelExpense` 七点法边界防 null）、库存单件预计毛利与防除零毛利率核算；**反射契约锁**验证 `ShopFinanceVo`、`ShopStockVo`、`ShopStockBatchVo`、`ShopOrderVo`、`ShopCartVo` 的 Long 类型 ID 均标注 `@JsonSerialize(using = Long2StringSerializer.class)` | ✅ **PASS** |
| **`CpnCouponBusinessRuleIT`** | 消费券余量流转与雪花 ID 序列化 | 7 | 验证消费券核销余量计算（`totalQuantity - consumedQuantity`）、超额核销前置拦截校验、售罄状态机自动流转；**反射契约锁**验证 `CpnCouponInfoVo`、`CpnUserCouponInfoVo`、`CpnRedemptionRecordInfoVo` 的 Long 类型 ID 均标注 `@JsonSerialize(using = Long2StringSerializer.class)` | ✅ **PASS** |
| **`PrepaidCardDataPermissionIT`** | 预付卡与消费流水多租户隔离 | 7 | 验证超管直通绕过、机构管理员在 `prepaid_card_info_t` 与 `prepaid_consume_record_t` 的机构子查询过滤，普通用户操作人限制，未登录安全兜底 | ✅ **PASS** |
| **`PrepaidCardBusinessRuleIT`** | 预付卡充值消费规则与雪花 ID 序列化 | 7 | 验证充值增加余额、消费扣减余额、**余额不足透支前置阻断拦截**（杜绝负余额穿透）、非正向交易金额拦截；**反射契约锁**验证 `PrepaidCardInfoTVo`、`PrepaidConsumeRecordTVo` 的 Long 类型 ID 标注 `@JsonSerialize(using = Long2StringSerializer.class)` | ✅ **PASS** |
| **`FinanceManagerOwnershipIT`** | 个人财务流水行级所有权越权防御 | 6 | 验证普通用户试图修改/删除他人财务流水强制阻断抛出 `403 Forbidden`、属主本人更新删除放行、超管全局管理放行、新增流水自动注入当前登录属主；**反射契约锁**验证 `FinanceInfoVo` 的 `id`、`belongTo`、`shopOrderId` 序列化配置 | ✅ **PASS** |



