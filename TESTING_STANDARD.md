# AI 测试体系建设方案

> **适用项目**：`alex_miaosha`（后端 Spring Boot 多模块 + PC 端 Vue3 + 移动端 Vue3）
> **样本模块**：`gift`（礼尚往来）
> **文档版本**：v1.0
> **最后更新**：2026-05-28
> **维护者**：后端 / 测试组

---

## 目录

- [第一部分 · 现状诊断](#第一部分--现状诊断)
- [第二部分 · 测试方法论](#第二部分--测试方法论)
- [第三部分 · 标准化体系（三层架构）](#第三部分--标准化体系三层架构)
- [第四部分 · 30 天落地路线](#第四部分--30-天落地路线)
- [第五部分 · Anti-pattern 警示](#第五部分--anti-pattern-警示)
- [第六部分 · 核心心法](#第六部分--核心心法)
- [附录 A · gift 模块必测边界清单](#附录-a--gift-模块必测边界清单)
- [附录 B · 部署结构建议](#附录-b--部署结构建议)
- [附录 C · 流程总图](#附录-c--流程总图)

---

## 第一部分 · 现状诊断

### 1.1 已有 AI 测试基建（保留并发扬）

| 项 | 评价 |
|---|---|
| **三层金字塔**：smoke / button-matrix / flow CRUD 已成形 | ⭐⭐⭐⭐⭐ 教科书级 |
| **AI 锚点 + DOM 双轨**：`aiWaitFor` 兜底语义 + `getByRole` 精确定位 | ⭐⭐⭐⭐⭐ |
| **副作用探针**：`installHapticProbe` 拦截 `navigator.vibrate` 计数 | ⭐⭐⭐⭐⭐ 罕见亮点 |
| **网络层等待**：`waitForExactApiResponse` 替代 sleep | ⭐⭐⭐⭐ |
| **失败可追溯**：截图 + JSON 报告 + 追加日志 | ⭐⭐⭐⭐ |

### 1.2 缺口清单（按优先级）

| # | 缺口 | 风险 | 优先级 |
|---|---|---|---|
| 1 | `alex_miaosha_finance` 后端 0 个 Java 测试 | 核心业务逻辑无回归保障 | P0 |
| 2 | AI 测试用文案做精确匹配（`+ 快速记礼` 等） | 改文案即全红，token 浪费 | P1 |
| 3 | 无 fixture 池/脏数据自动清理 | `codex-pc-record-*` 残留污染 DB | P1 |
| 4 | 金额精度/边界值未覆盖 | `Long.MAX_SAFE_INTEGER` 精度坑无回归 | P1 |
| 5 | 回礼链路（`relatedRecordId` + RETURN）无 case | 核心业务无 E2E 保障 | P1 |
| 6 | 统计/报表只断言"卡片可见"，不校验数值正确性 | 数据错了测试无法感知 | P1 |
| 7 | 无 Midscene 缓存 | token 成本不可控 | P2 |
| 8 | 移动端 case 仅 3 个，与 PC 25+ 不对等 | 移动端回归保障弱 | P2 |
| 9 | 无 CI 工作流 | 仅本地手跑，无强制门槛 | P2 |
| 10 | 无统一测试标准文档 | 新人/AI 无规范可依 | P0 |

---

## 第二部分 · 测试方法论

### 2.1 测试金字塔分工

```
          ╱╲
         ╱AI╲       ←  5%   E2E/Midscene：用户体感、跨页面流程、视觉
        ╱────╲              成本高、慢、不稳定 → 只测人写不动的
       ╱ 集成 ╲     ←  25%  MockMvc/SpringBootTest：接口契约
      ╱────────╲            慢但准，验证 Controller→Service→DAO 链路
     ╱   单元   ╲   ←  70%  JUnit/Mockito：业务规则
    ╱────────────╲          快、便宜、稳定，跑 1000 个用例几秒钟
```

**铁律**：能用更低成本的层级测出来，**绝对不要用上层测**。

- `amount = 0.01` 精度边界 → 单测（成本 0）
- `direction` 状态机 → 单测
- 跨页面 CRUD 闭环 → AI 测试
- 视觉合理性、模糊指令 → AI 测试

### 2.2 测试范围边界（测什么 / 不测什么）

写测试前先画**范围矩阵**：

| 测什么 | 用哪一层 | 例子 |
|---|---|---|
| 业务规则、状态机、精度 | 单元测试 | `markAsReturned()` mock mapper |
| 参数校验、序列化、异常处理 | 集成测试 | `mockMvc.perform(post(...))` |
| 跨页面流程、按钮显隐、视觉 | AI 测试 | `GIFT-PC-RECORD-CRUD-001` |
| 验证码、第三方支付回调 | **不测**（mock 掉） | `@MockBean` |

**边界类型全景**（gift 模块举例）：

| 边界类型 | gift 例子 | 在哪测 |
|---|---|---|
| 输入字段边界 | amount 范围、payTime 日期 | 单元 |
| 业务状态边界 | direction 三态、returnedFlag 0↔1 | 单元 |
| 数据规模边界 | 1000 条 record 分页 | 性能 |
| 权限边界 | 超管 / org admin / user 三视角 | 集成 |
| 时间边界 | 跨月统计、跨年回礼、时区 | 单元+集成 |
| 并发边界 | 同时标记已回礼 | 集成 |
| 视觉边界 | 移动端 320↔768px、文本截断 | AI |

### 2.3 测试值边界 · 七点法

对任何数值/字符串/集合字段，至少测这 7 个点：

```
        非法               有效                非法
   ─────────────|═══════════════════|─────────────
                ↑  ↑       ↑       ↑  ↑
              min-1 min   typical  max max+1
```

**应用到 gift `amount`**：

| 点 | 值 | 期望 |
|---|---|---|
| far-low | `-99999` | 拒绝 |
| min-1 | `-0.01` | 拒绝 |
| min | `0` | 业务决定 |
| min+1 | `0.01` | 通过且精度无丢失 |
| typical | `200/888/1314` | 通过 |
| max | `999999999.99` | 通过 |
| max+1 | `1000000000` | 拒绝（DECIMAL(12,2)） |

**参数化测试一次性覆盖**：

```java
@ParameterizedTest(name = "amount={0} → {1}")
@CsvSource({
    "-0.01,        REJECT",
    "0,            ACCEPT",
    "0.01,         ACCEPT",
    "999999999.99, ACCEPT",
    "1000000000,   REJECT"
})
void testAmountBoundary(BigDecimal amount, String expected) {
    GiftRecordVo vo = baseVo().withAmount(amount);
    if ("ACCEPT".equals(expected)) {
        assertDoesNotThrow(() -> service.create(vo));
    } else {
        assertThrows(ValidationException.class, () -> service.create(vo));
    }
}
```

### 2.4 全字段边界对照表（直接抄）

| 字段类型 | 必测边界 |
|---|---|
| **整数** | `Integer.MIN_VALUE`、`-1`、`0`、`1`、`Integer.MAX_VALUE` |
| **金额 (BigDecimal)** | `0.00`、`0.01`、最小精度截断、超过 DB 精度 |
| **字符串** | `null`、`""`、`" "`、单字符、最大长度、最大长度+1、emoji、SQL 注入 |
| **日期** | `null`、未来、`1970-01-01`、`9999-12-31`、夏令时、闰年 `2024-02-29` |
| **枚举** | 每个值 + `null` + 非枚举值 |
| **集合** | `null`、`[]`、`[单元素]`、最大长度、最大长度+1 |
| **ID** | `null`、`0`、`-1`、不存在、超过 `Long.MAX_SAFE_INTEGER` 时必须字符串化 |
| **时间区间** | 起点>终点、起点=终点、跨月、跨年、跨时区 |

### 2.5 状态机边界（gift 核心）

```
                  标记已回礼
   GIVE  ──────────────────────→  RETURN（自动创建关联 record）
    │
    │ 不允许从 GIVE 直接 → RECEIVE
    ↓ ✗

  RECEIVE  ─────────────────────→ ✗（收礼不能"回"，要新建 GIVE）
                  非法转换
```

**所有合法路径 + 所有非法路径都要测**：

```java
@Test void giveCanBeReturned()            // GIVE → RETURN ✓
@Test void receiveCannotBeReturned()      // RECEIVE → RETURN ✗
@Test void returnCannotBeReturnedAgain()  // RETURN → RETURN ✗
@Test void returnHasRelatedRecordId()     // RETURN 必须挂在 GIVE 上
```

### 2.6 确认边界的 5 步实操法

1. **列字段**：打开 VO，把每个字段标"是否有边界"
2. **画状态图**：对 direction 等状态字段画 Mermaid 转换图
3. **查约束**：`@Valid` 注解 + DB schema + 产品需求**三处必须一致**
4. **套七点法**：每个字段列至少 5 个边界值
5. **问 5 个万能问题**：
   - 如果**为 null** 呢？
   - 如果**并发**呢？
   - 如果**重试**呢？（幂等性）
   - 如果**用户改时区/语言**呢？
   - 如果**数据被外部脏改**呢？

### 2.7 覆盖率工具栈

#### Jacoco · 行/分支覆盖率

**作用**：跑完测试告诉你哪行被测、哪行没被测。
**接入**：父 pom 加 `jacoco-maven-plugin`，跑 `mvn verify` 自动出报告。
**核心指标**：

| 指标 | 含义 | 目标 |
|---|---|---|
| Instructions | 字节码指令 | 一般看这个 |
| **Branches** | if/else 双路 | **最反映边界**，≥60% |
| Lines | 行数 | 沟通用，≥70% |
| Methods | 死方法 | ≥80% |
| Complexity | 圈复杂度 | 揪长方法 |

**Maven 配置示例**：

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <id>prepare-agent</id>
            <goals><goal>prepare-agent</goal></goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>verify</phase>
            <goals><goal>report</goal></goals>
        </execution>
        <execution>
            <id>check-coverage</id>
            <phase>verify</phase>
            <goals><goal>check</goal></goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.70</minimum>
                            </limit>
                            <limit>
                                <counter>BRANCH</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.60</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
                <excludes>
                    <exclude>**/vo/**</exclude>
                    <exclude>**/dto/**</exclude>
                    <exclude>**/entity/**</exclude>
                    <exclude>**/mapper/**</exclude>
                    <exclude>**/config/**</exclude>
                    <exclude>**/*Application.class</exclude>
                </excludes>
            </configuration>
        </execution>
    </executions>
</plugin>
```

**AI 测试覆盖率合并**：Spring Boot 启动时挂 Jacoco Agent（`destfile=jacoco-it.exec,append=true`），AI 测试跑完用 `jacococli dump` 拉取，再与单测的 `jacoco-ut.exec` 合并。能量化"AI 测试贡献了多少覆盖率"。

#### PIT · 突变测试（验证"测试真假"）

Jacoco 只能告诉你"这行被执行"，**不能告诉你"断言是否有效"**。

```java
@Test void testAmount() {
    service.create(vo.withAmount(100));
}
```

Jacoco 报 100%，但完全没意义。PIT 会自动突变代码（`>` 改 `>=`、`+` 改 `-`），如果测试**还通过 = 边界没测到**。这是检验"边界确认到位"的终极手段。

```xml
<plugin>
    <groupId>org.pitest</groupId>
    <artifactId>pitest-maven</artifactId>
    <version>1.15.0</version>
    <configuration>
        <targetClasses>
            <param>com.alex.finance.giftRecord.service.*</param>
        </targetClasses>
        <targetTests>
            <param>com.alex.finance.giftRecord.*Test</param>
        </targetTests>
    </configuration>
</plugin>
```

**目标**：Mutation Score ≥ 70%。

---

## 第三部分 · 标准化体系（三层架构）

让标准真正生效必须三层一起做。只做文档 = 没人看。

```
┌─────────────────────────────────┐
│  1. TESTING_STANDARD.md          │ ← 文档层
│     人能读 + AI 能读              │
└────────────┬────────────────────┘
             │ 约束
┌────────────▼────────────────────┐
│  2. feature-test-checklist.md   │ ← 模板层
│     每个新功能必填                │
└────────────┬────────────────────┘
             │ 强制
┌────────────▼────────────────────┐
│  3. CI + Cursor Rule             │ ← 执行层
│     机器卡点                      │
└─────────────────────────────────┘
```

### 3.1 第 1 层 · `TESTING_STANDARD.md`（本文档）

放三个仓库根目录，核心 6 节：

```markdown
# 测试标准 v1.0

## 1. 金字塔分工
- 单元 70% / 集成 25% / AI 5%

## 2. 必测边界（七点法 + 全字段对照表）

## 3. 必测维度
- ✅ 正常流 / 边界值 / 异常流
- ✅ 权限边界（超管/org admin/user/跨机构）
- ✅ 并发 / 幂等

## 4. 覆盖率门槛
- 新代码：Line≥70%, Branch≥60%
- 历史代码：每月递增 2% 不退
- 关键模块：≥80%
- Mutation Score：≥70%

## 5. 命名约定
- 单测：{Class}Test.java，方法 should_{期望}_when_{条件}
- 集成：{Business}IT.java
- AI caseId：{模块}-{端}-{动作}-{编号}

## 6. 禁止事项
- ❌ Thread.sleep（用 waitForResponse / aiWaitFor）
- ❌ 测试无 assert（必被 PIT 杀死）
- ❌ 测试间共享可变状态
- ❌ 生产 DB 跑测试
- ❌ AI 测试做文案精确匹配（用 data-testid）
- ❌ 提交脏数据（必须 try/finally）
```

### 3.2 第 2 层 · 功能级 checklist 模板

每开一个新功能在 `tests/checklists/{feature}.md` 填一份：

```markdown
# {功能} 测试 Checklist

## 元信息
- 模块 / 负责人 / 关联需求 / graphify 节点

## 输入字段边界分析
| 字段 | 类型 | 合法范围 | 必测边界值 | 依据 |

## 状态机分析（如有）
- 合法转换 / 非法转换 / 终止态

## 权限矩阵
| 操作 | 超管 | org admin | user(self) | user(other) |

## 测试用例规划
- [ ] 单元：amount 七点法
- [ ] 单元：direction 状态机
- [ ] 集成：权限矩阵 12 个 case
- [ ] 集成：并发
- [ ] AI：PC CRUD
- [ ] AI：移动端
- [ ] AI：按钮权限矩阵

## 不测理由（必填，防漏测）

## 覆盖率目标
- Line ≥ 75%, Branch ≥ 60%
- 关键服务方法 100%
```

### 3.3 第 2 层 · 测试代码骨架模板

`tests/templates/ServiceTest.template.java`：

```java
@ExtendWith(MockitoExtension.class)
class ${ServiceName}Test {

    @InjectMocks private ${ServiceName} service;
    @Mock private ${MapperName} mapper;
    @Mock private RedisTemplate<String, Object> redis;

    @Nested @DisplayName("正常流")
    class HappyPath {
        @Test void should_create_when_valid_input() { /* TODO */ }
    }

    @Nested @DisplayName("边界值 - 七点法")
    class BoundaryValue {
        @ParameterizedTest(name = "amount={0} → {1}")
        @CsvSource({
            "-0.01,        REJECT",
            "0.00,         ACCEPT",
            "0.01,         ACCEPT",
            "999999999.99, ACCEPT",
            "1000000000,   REJECT"
        })
        void should_validate_amount_boundary(BigDecimal amount, String expected) {
            /* TODO */
        }
    }

    @Nested @DisplayName("异常流")
    class ExceptionPath {
        @Test void should_throw_when_db_unavailable() { /* TODO */ }
        @Test void should_throw_when_redis_timeout() { /* TODO */ }
    }

    @Nested @DisplayName("权限边界")
    class PermissionBoundary {
        @Test void super_admin_can_access_all() { /* TODO */ }
        @Test void org_admin_can_access_own_org() { /* TODO */ }
        @Test void user_can_only_access_self() { /* TODO */ }
        @Test void cross_org_access_rejected() { /* TODO */ }
    }

    @Nested @DisplayName("并发与幂等")
    class ConcurrencyAndIdempotency {
        @Test void should_handle_concurrent_mark_returned() { /* TODO */ }
        @Test void should_be_idempotent_when_retry() { /* TODO */ }
    }
}
```

**5 个 `@Nested` 块逼着团队思考每个维度**。

### 3.4 第 3 层 · CI 强制 + Cursor Rule

#### CI 工作流（伪代码）

```yaml
name: Test Standard Check
on: [pull_request]
jobs:
  unit:
    steps:
      - run: mvn verify -Pcoverage           # Jacoco 阈值未达直接红
      - run: mvn pitest:mutationCoverage     # 突变分数 <70% 警告
  ai-smoke:
    steps:
      - run: pnpm test:midscene:smoke
      - uses: actions/upload-artifact@v3
        with: { path: reports/midscene/**, name: midscene-report }
  checklist-check:
    steps:
      - name: Check feature-test-checklist exists
        run: |
          for f in $(git diff --name-only origin/main | grep -E "src/.*(controller|service)"); do
            module=$(echo $f | cut -d/ -f4)
            test -f tests/checklists/${module}.md || (echo "Missing checklist for $module" && exit 1)
          done
```

#### `.cursor/rules/testing-standard.mdc`

```markdown
---
description: 项目测试标准
globs: ["**/src/**/*Test.java", "**/tests/midscene/**/*.mjs"]
alwaysApply: false
---

# 写测试时必须遵守

参考 `TESTING_STANDARD.md`，写测试代码时强制：

1. 七点法：每个数值字段至少 5 个边界值 @ParameterizedTest
2. 三段命名：should_xxx_when_yyy
3. 每个 @Test 至少 1 个 assert
4. AI 测试禁止精确文案匹配，用 data-testid
5. 测试数据必须 try/finally 清理
6. 每个 Controller/Service 同步更新 tests/checklists/{module}.md

写完必须跑：
- mvn verify（看 Jacoco 报告）
- mvn pitest:mutationCoverage（看变异分数）
- pnpm test:midscene:smoke（AI 测试）
```

这样 Cursor 帮你写测试时**自动跟标准走**。

---

## 第四部分 · 30 天落地路线

### Week 1 · 止血与降本

| 任务 | 投入 | 产出 |
|---|---|---|
| gift 关键按钮加 `data-testid`（约 20 个） | 0.5 天 | 稳定性 ↑, token ↓60% |
| `MIDSCENE_CACHE=true` + 替换文案匹配为 `getByTestId` | 0.5 天 | 单次 smoke 成本 $0.6 → $0.2 |
| fixture 池 + try/finally 清理 | 1 天 | 测试库无脏数据 |
| 父 pom 加 Jacoco（不卡阈值，先看基线） | 0.5 天 | 量化起点 |

### Week 2 · 后端契约层

| 任务 | 投入 | 产出 |
|---|---|---|
| AI 批量生成 `gift-record/person/event` 的 MockMvc 测试骨架 | 1 天 | CRUD + 校验失败路径覆盖 |
| 手写 3 个"业务大脑"用例：回礼状态机 / 数据权限 / 金额精度 | 2 天 | 核心逻辑回归保障 |
| Jacoco 出基线，目标 finance ≥ 70% | 0.5 天 | 量化指标 |

### Week 3 · AI 测试矩阵补全

| 任务 | 投入 | 产出 |
|---|---|---|
| 新增 5 个业务关键 AI case | 2 天 | 真正发现回归 |
| 移动端 case 从 3 个扩到 10 个 | 1.5 天 | PC/移动覆盖对等 |

**5 个新 AI case**：

| caseId | 业务价值 |
|---|---|
| `GIFT-FLOW-RETURN-001` | GIVE → 标记已回 → 自动创建 RETURN → analysis 待回礼 -1 |
| `GIFT-FLOW-PRECISION-001` | 输入 `999999999.99` 无精度丢失 |
| `GIFT-FLOW-PERMISSION-001` | manager 创建后 readonly 看不到详情但能看行 |
| `GIFT-MOBILE-OFFLINE-001` | 移动端断网时降级提示 |
| `GIFT-MOBILE-LARGE-LIST-001` | 1000 条 mock 数据验证虚拟滚动 |

### Week 4 · CI 自动化 + 可见性

| 任务 | 投入 | 产出 |
|---|---|---|
| GitHub Actions：PR 标签触发 + nightly 全量 | 1 天 | 每次提交自动跑 |
| 报告 Artifact + PR 失败截图自动评论 | 0.5 天 | 失败 5 分钟内被看到 |
| `tests/midscene/gift/README.md` 记录"哪个 case 测什么" | 0.5 天 | 新人 1 小时上手 |
| 周度覆盖率审计脚本（AI 比对 button 配置和 case 清单） | 1 天 | 新按钮没测会告警 |

---

## 第五部分 · Anti-pattern 警示

### AI 测试 5 大反模式

1. ❌ **AI 写"尽量全面"的断言**：`aiAssert('页面看起来正常')` 永远不挂 = 没测。每个 `aiAssert` 必须是**单一可证伪命题**。
2. ❌ **把 AI 当 selector 用**：`agent.aiAct('点第三个礼金记录')` 易抖动。AI 做语义指令，定位用 `getByTestId`。
3. ❌ **失败立刻重试**：AI 测试不稳定多数是真 bug。要做就做 failure clustering。
4. ❌ **追求 100% AI 化**：CRUD 闭环用纯 DOM 跑比 AI 稳 10 倍且免费。AI 只做你写不动的。
5. ❌ **不"测 AI 测试"**：每个 case 加负向用例（人工破坏 UI 看是否能感知失败），确保不是永远 pass 的安慰剂。

### 单元测试 3 大反模式

6. ❌ **测试无 assert**：被 PIT 一杀即知。
7. ❌ **测试间共享可变状态**：单测必须独立。
8. ❌ **覆盖率追求 100%**：到 80% 就够，剩下 20% 多是边角，强追性价比极低。

---

## 第六部分 · 核心心法

### AI 测试的本质

> **AI 测试不是用来"找 bug"，而是用来"防回归"。**
> 稳定性 > 智能性。能用 `getByTestId` 就别用 `aiAct`；能用 `waitForResponse` 就别用 `aiWaitFor`。AI 留给"视觉合理性、文案语义、模糊指令转动作"三类人写不动的场景。

### 边界确认的本质

> **把代码里所有 `if` / `switch` / `>` / `<` / `null check` 翻译成至少一对测试用例。**
>
> 能回答这三个问题边界就稳：
> 1. 字段合法范围在哪？→ 七点法
> 2. 方法有几个分支？→ 每分支至少 1 case
> 3. 突变测试能不能杀死所有变异？→ 不能就补 assert

### 测试标准的本质

> **把高级工程师的测试直觉，翻译成 AI 和新人都能执行的步骤。**
>
> 标准好不好看三点：
> 1. ✅ 新人 30 分钟能上手
> 2. ✅ AI 能直接执行
> 3. ✅ CI 能机器卡死

---

## 附录 A · gift 模块必测边界清单

```
[ amount 字段 ]
  □ 0.00, 0.01, 999999999.99, 1000000000 (拒绝)
  □ 负数, null, 小数 3 位精度截断
  □ 超过 Long.MAX_SAFE_INTEGER 前端是否字符串化

[ direction 状态机 ]
  □ GIVE → RETURN（合法，自动关联）
  □ RECEIVE → RETURN（非法）
  □ RETURN → RETURN（非法）
  □ 已 returnedFlag=1 不能再次标记

[ relatedRecordId 自关联 ]
  □ 关联自己（抛异常）
  □ 关联不存在的 ID
  □ 关联别人的 record（权限拒绝）
  □ 循环关联 A→B→A

[ payTime 日期 ]
  □ null, 未来时间
  □ 跨年（12/31 → 1/1）
  □ 跨时区（UTC vs Asia/Shanghai）
  □ 闰年 2/29

[ 数据权限 ]
  □ 超管查所有
  □ org admin 查本机构
  □ 普通用户只查自己
  □ 跨机构访问被拦截

[ 分页 ]
  □ pageNum = 0 / -1 / 超总页数
  □ pageSize = 0 / 1 / 10000

[ 并发 ]
  □ 两请求同时标记同笔已回礼（乐观锁？）
  □ 创建 + 立刻删除竞态
```

---

## 附录 B · 部署结构建议

```
backend/alex_miaosha/
├── TESTING_STANDARD.md           ← 后端标准（Java/Maven/Jacoco/PIT）
├── tests/
│   ├── checklists/
│   │   ├── gift.md
│   │   ├── personal-gift.md
│   │   ├── user.md
│   │   └── ...
│   └── templates/
│       └── ServiceTest.template.java
└── .cursor/rules/
    └── testing-standard.mdc

frontend/alex_miaosha_front/
├── TESTING_STANDARD.md           ← PC 端标准（Midscene/Playwright）
├── tests/
│   ├── checklists/
│   ├── templates/
│   └── midscene/...
└── .cursor/rules/
    └── testing-standard.mdc

frontend/alex_miaosha_mobile/
├── TESTING_STANDARD.md           ← 移动端标准
└── ...
```

并在每个 `AGENTS.md` 末尾追加：

> 写测试前必须先读 `TESTING_STANDARD.md` 并填写 `tests/checklists/{feature}.md`。

---

## 附录 C · 流程总图

```
                ┌──────────────────────────────────┐
                │   每个新功能开发开始              │
                └──────────────┬───────────────────┘
                               ▼
        ┌──────────────────────────────────────────┐
        │ 1. 读 TESTING_STANDARD.md                 │
        │ 2. 填 tests/checklists/{feature}.md      │
        │    - 字段边界（七点法）                    │
        │    - 状态机                                │
        │    - 权限矩阵                              │
        │    - 用例规划 + 不测理由                   │
        └──────────────┬───────────────────────────┘
                       ▼
        ┌──────────────────────────────────────────┐
        │ 3. 按金字塔分层写测试                      │
        │    单元 70% → 集成 25% → AI 5%            │
        │    Copy ServiceTest.template.java         │
        └──────────────┬───────────────────────────┘
                       ▼
        ┌──────────────────────────────────────────┐
        │ 4. CI 自动检查                             │
        │    - Jacoco Line ≥ 70% Branch ≥ 60%       │
        │    - PIT Mutation ≥ 70%                   │
        │    - Midscene smoke 全过                  │
        │    - checklist 已更新                     │
        └──────────────┬───────────────────────────┘
                       ▼
                    ✅ 合并
```

---

## 修订记录

| 版本 | 日期 | 修改人 | 内容 |
|---|---|---|---|
| v1.0 | 2026-05-28 | alex | 首版，覆盖 gift 模块完整方法论 |
