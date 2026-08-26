# DataPermission Single-Ctor Unification Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 `DataPermissionHandlerImpl` 恢复为单一全参构造 + `@RequiredArgsConstructor`，并通过 `@ConditionalOnMissingBean` NOOP 与各 boot `MybatisPlusConfig` 注入，修复 Gateway 启动失败且保留机构子树权限。

**Architecture:** `user_api` 提供 `OrgSubtreeLookup` 接口与默认 NOOP Bean；`user_boot` 用 `OrgSubtreeLookupImpl` 覆盖；Handler 仅保留 `(UserUtils, OrgSubtreeLookup)` 一构造；Gateway 不改扫描。

**Tech Stack:** Spring Boot 2.7、Lombok、MyBatis-Plus DataPermission、JUnit 5

**Spec:** `docs/superpowers/specs/2026-08-18-datapermission-single-ctor-design.md`

## Global Constraints

- 不删除 `DataPermissionHandlerImpl` 的 `@Component`
- 不修改 `GatewayApplication` 的 `excludeFilters` / 缩小 `com.alex.api.user` 扫描（除非规格变更）
- 不把机构 Mapper SQL 引入 `user_api`
- Windows 下 Maven 使用 `JAVA_HOME=C:\Program Files\Java\jdk-17`
- 提交信息勿带 `Co-authored-by: Cursor`；未要求时不 git commit

## File Map

| File                                                                           | Responsibility         |
| ------------------------------------------------------------------------------ | ---------------------- |
| `alex_miaosha_user/user_api/.../DataPermissionHandlerImpl.java`                | 单构造 Handler Bean    |
| `alex_miaosha_user/user_api/.../OrgSubtreeLookupConfiguration.java`            | MissingBean → NOOP     |
| `alex_miaosha_user/user_boot/.../MybatisPlusConfig.java`                       | 双参 new（已基本到位） |
| `alex_miaosha_finance/finance_boot/.../MybatisPlusConfig.java`                 | 注入 lookup + 双参 new |
| `alex_miaosha_product/product_boot/.../MybatisPlusConfig.java`                 | 同上                   |
| `alex_miaosha_oss/oss_boot/.../config/mybatisplus/MybatisPlusConfig.java`      | 同上                   |
| `alex_miaosha_user/user_boot/src/test/.../DataPermissionScopeHandlerTest.java` | 去掉单参 helper        |
| 其它 `new DataPermissionHandlerImpl(` 测试                                     | 统一双参               |

---

### Task 1: 红灯 — 锁定「禁止单参构造」契约

**Files:**

- Create: `alex_miaosha_user/user_boot/src/test/java/com/alex/user/rbac/DataPermissionHandlerCtorContractTest.java`
- Test: 同上

**Interfaces:**

- Consumes: `DataPermissionHandlerImpl` 当前仍含单参构造（红灯阶段）
- Produces: 失败断言「公共构造器有且仅有 1 个，且参数为 `(UserUtils, OrgSubtreeLookup)`」

- [ ] **Step 1: Write the failing test**

```java
package com.alex.user.rbac;

import com.alex.api.user.handler.DataPermissionHandlerImpl;
import com.alex.api.user.handler.OrgSubtreeLookup;
import com.alex.api.user.user.UserUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 防止再次引入双构造导致 Gateway 无参回退启动失败。
 */
public class DataPermissionHandlerCtorContractTest {

    @Test
    void mustHaveSinglePublicConstructor_userUtilsAndOrgSubtreeLookup() {
        Constructor<?>[] ctors = DataPermissionHandlerImpl.class.getConstructors();
        assertEquals(1, ctors.length, "must keep a single public ctor for Spring");
        Class<?>[] params = ctors[0].getParameterTypes();
        assertEquals(2, params.length);
        assertEquals(UserUtils.class, params[0]);
        assertEquals(OrgSubtreeLookup.class, params[1]);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bat
set JAVA_HOME=C:\Program Files\Java\jdk-17
mvn -pl alex_miaosha_user/user_boot -am test -Dtest=DataPermissionHandlerCtorContractTest -DfailIfNoTests=false
```

Expected: FAIL — `expected: <1> but was: <2>`（当前双公共构造）

- [ ] **Step 3: Commit（仅当用户明确要求提交时）**

```bat
git add alex_miaosha_user/user_boot/src/test/java/com/alex/user/rbac/DataPermissionHandlerCtorContractTest.java
git commit -m "test(rbac): lock DataPermissionHandlerImpl to a single public constructor"
```

---

### Task 2: 绿灯 — Handler 改回 `@RequiredArgsConstructor` 单构造

**Files:**

- Modify: `alex_miaosha_user/user_api/src/main/java/com/alex/api/user/handler/DataPermissionHandlerImpl.java`
- Test: `DataPermissionHandlerCtorContractTest`

**Interfaces:**

- Consumes: `UserUtils`, `OrgSubtreeLookup`
- Produces: 唯一公共构造 `DataPermissionHandlerImpl(UserUtils, OrgSubtreeLookup)`

- [ ] **Step 1: Replace dual ctors with Lombok single ctor**

将类头改为（保留现有业务方法体不动）：

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class DataPermissionHandlerImpl implements DataPermissionHandler {

    private final UserUtils userUtils;

    /**
     * RBAC-BE-SCOPE-002: 机构子孙节点查询。
     * 无真实实现的服务注入 {@link OrgSubtreeLookup#NOOP}（见 OrgSubtreeLookupConfiguration）。
     */
    private final OrgSubtreeLookup orgSubtreeLookup;

    // 删除 public DataPermissionHandlerImpl(UserUtils) 与显式双参构造
```

确保 `import lombok.RequiredArgsConstructor;` 存在。

- [ ] **Step 2: Re-run ctor contract test**

Run: 同 Task 1 Step 2  
Expected: PASS

- [ ] **Step 3: Commit（仅用户要求时）**

```bat
git add alex_miaosha_user/user_api/src/main/java/com/alex/api/user/handler/DataPermissionHandlerImpl.java
git commit -m "refactor(rbac): restore single RequiredArgsConstructor on DataPermissionHandlerImpl"
```

---

### Task 3: NOOP Bean + 各 boot MybatisPlusConfig

**Files:**

- Create: `alex_miaosha_user/user_api/src/main/java/com/alex/api/user/handler/OrgSubtreeLookupConfiguration.java`
- Modify: `alex_miaosha_finance/finance_boot/src/main/java/com/alex/finance/config/MybatisPlusConfig.java`
- Modify: `alex_miaosha_product/product_boot/src/main/java/com/alex/product/config/MybatisPlusConfig.java`
- Modify: `alex_miaosha_oss/oss_boot/src/main/java/com/alex/oss/config/mybatisplus/MybatisPlusConfig.java`
- Modify: `alex_miaosha_user/user_boot/src/main/java/com/alex/user/config/MybatisPlusConfig.java`（核对双参）

**Interfaces:**

- Consumes: `OrgSubtreeLookup` Bean（NOOP 或 Impl）
- Produces: 拦截器内 `new DataPermissionHandlerImpl(userUtils, orgSubtreeLookup)`

- [ ] **Step 1: Add MissingBean NOOP configuration**

```java
package com.alex.api.user.handler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrgSubtreeLookupConfiguration {

    @Bean
    @ConditionalOnMissingBean(OrgSubtreeLookup.class)
    public OrgSubtreeLookup orgSubtreeLookup() {
        return OrgSubtreeLookup.NOOP;
    }
}
```

- [ ] **Step 2: Update finance MybatisPlusConfig**

在现有 `@RequiredArgsConstructor` 类中增加字段并改 new：

```java
private final UserUtils userUtils;
private final OrgSubtreeLookup orgSubtreeLookup;

@Bean
public MybatisPlusInterceptor mybatisPlusInterceptor() {
    MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
    interceptor.addInnerInterceptor(
            new DataPermissionInterceptor(new DataPermissionHandlerImpl(userUtils, orgSubtreeLookup)));
    // ...其余 interceptor 保持不变
    return interceptor;
}
```

`import com.alex.api.user.handler.OrgSubtreeLookup;`

- [ ] **Step 3: Update product / oss MybatisPlusConfig 同样模式**

与 Step 2 相同：注入 `OrgSubtreeLookup`，`new DataPermissionHandlerImpl(userUtils, orgSubtreeLookup)`。

- [ ] **Step 4: Confirm user_boot MybatisPlusConfig**

已有：

```java
new DataPermissionHandlerImpl(userUtils, orgSubtreeLookup)
```

无需逻辑变更；确认编译即可。

- [ ] **Step 5: Compile affected modules**

```bat
set JAVA_HOME=C:\Program Files\Java\jdk-17
mvn -pl alex_miaosha_user/user_api,alex_miaosha_user/user_boot,alex_miaosha_finance/finance_boot,alex_miaosha_product/product_boot,alex_miaosha_oss/oss_boot,alex_miaosha_gateway -am compile -DskipTests
```

Expected: BUILD SUCCESS

- [ ] **Step 6: Commit（仅用户要求时）**

```bat
git add alex_miaosha_user/user_api/src/main/java/com/alex/api/user/handler/OrgSubtreeLookupConfiguration.java alex_miaosha_finance/finance_boot/src/main/java/com/alex/finance/config/MybatisPlusConfig.java alex_miaosha_product/product_boot/src/main/java/com/alex/product/config/MybatisPlusConfig.java alex_miaosha_oss/oss_boot/src/main/java/com/alex/oss/config/mybatisplus/MybatisPlusConfig.java
git commit -m "fix(rbac): provide OrgSubtreeLookup NOOP bean and wire MybatisPlusConfigs"
```

---

### Task 4: 适配测试中的单参 `new`

**Files:**

- Modify: `alex_miaosha_user/user_boot/src/test/java/com/alex/user/rbac/DataPermissionScopeHandlerTest.java`
- Modify: 全仓其它 `new DataPermissionHandlerImpl(`（finance IT 等，以 grep 为准）

**Interfaces:**

- Produces: 所有手工构造均为 `(UserUtils, OrgSubtreeLookup)`

- [ ] **Step 1: Grep 残留单参调用**

```bat
rg "new DataPermissionHandlerImpl\(" -g "*.java"
```

- [ ] **Step 2: Fix DataPermissionScopeHandlerTest helpers**

```java
private static DataPermissionHandlerImpl handler(TUserVo loginUser) {
    return new DataPermissionHandlerImpl(new FakeUserUtils(loginUser), OrgSubtreeLookup.NOOP);
}

private static DataPermissionHandlerImpl handler(TUserVo loginUser, OrgSubtreeLookup orgSubtreeLookup) {
    return new DataPermissionHandlerImpl(new FakeUserUtils(loginUser), orgSubtreeLookup);
}
```

- [ ] **Step 3: Fix finance IT / 其它测试同样改为双参 + NOOP**

示例：

```java
handler = new DataPermissionHandlerImpl(userUtils, OrgSubtreeLookup.NOOP);
```

- [ ] **Step 4: Run rbac handler tests**

```bat
set JAVA_HOME=C:\Program Files\Java\jdk-17
mvn -pl alex_miaosha_user/user_boot -am test -Dtest=DataPermissionHandlerCtorContractTest,DataPermissionScopeHandlerTest,OrgSubtreeLookupImplTest
```

Expected: all PASS

- [ ] **Step 5: Commit（仅用户要求时）**

```bat
git add alex_miaosha_user/user_boot/src/test alex_miaosha_finance/finance_boot/src/test
git commit -m "test(rbac): adapt DataPermissionHandlerImpl constructions to two-arg ctor"
```

---

### Task 5: Gateway 启动验收

**Files:**

- Verify only（不改 Gateway 源码，除非启动仍失败再开新规格）

- [ ] **Step 1: Compile gateway against updated user_api**

```bat
set JAVA_HOME=C:\Program Files\Java\jdk-17
mvn -pl alex_miaosha_gateway -am compile -DskipTests
```

- [ ] **Step 2: 在 IDEA 重新 Run `GatewayApplication`**

Expected:

- 不再出现 `NoSuchMethodException: DataPermissionHandlerImpl.<init>()`
- 上下文刷新成功（后续若有其它无关错误，单独开缺陷，不塞进本计划）

- [ ] **Step 3: 可选 — 断言 Spring 能实例化 Handler（不启全量网关）**

若需要自动化，可在 gateway 模块加极薄的 context smoke（仅当现有测试基建允许）；默认人工 IDEA 启动验收即可。

---

## Spec Coverage Check

| Spec 项                             | Task               |
| ----------------------------------- | ------------------ |
| 单构造 + `@RequiredArgsConstructor` | Task 2             |
| MissingBean NOOP                    | Task 3             |
| finance/product/oss 注入            | Task 3             |
| Gateway 不改扫描                    | Task 5             |
| 测试适配 + 契约锁                   | Task 1, 4          |
| 保留 `@Component`                   | Task 2（显式保留） |

## Placeholder Scan

无 TBD / TODO 占位。

## Type Consistency

- 唯一构造：`(UserUtils, OrgSubtreeLookup)`
- Bean 名默认方法名 `orgSubtreeLookup`
- Impl：`OrgSubtreeLookupImpl implements OrgSubtreeLookup`
