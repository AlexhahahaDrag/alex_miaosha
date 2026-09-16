# User Boot MyBatis `@Lazy` OrgSubtreeLookup Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 `user_boot` 的 `MybatisPlusConfig` 对 `OrgSubtreeLookup` 使用 `@Lazy`，消除 `sqlSessionFactory` ↔ `OrgSubtreeLookupImpl` ↔ `OrgInfoMapper` 循环依赖，使 `UserApplication` 可启动并保留真实子树权限。

**Architecture:** Interceptor Bean 方法参数注入 `@Lazy OrgSubtreeLookup`；启动期注入懒代理，运行期首次查子树再创建 `OrgSubtreeLookupImpl`。

**Tech Stack:** Spring Boot 2.7、MyBatis-Plus、JUnit 5

**Spec:** `docs/superpowers/specs/2026-08-19-user-mybatis-lazy-orgsubtree-design.md`

## Global Constraints

- 仅修改 `user_boot` 的 `MybatisPlusConfig`（除非测试需要）
- 不启用 `spring.main.allow-circular-references`
- 不让 user_boot 使用 NOOP 替代真实 `OrgSubtreeLookupImpl`
- 不修改 Gateway / `DataPermissionHandlerImpl` 单构造契约 / finance·product·oss configs
- Windows Maven：`JAVA_HOME=C:\Program Files\Java\jdk-17`
- 未要求时不 git commit；提交信息勿带 Cursor co-author

## File Map

| File | Responsibility |
|------|----------------|
| `alex_miaosha_user/user_boot/src/main/java/com/alex/user/config/MybatisPlusConfig.java` | `@Lazy` 打断循环依赖 |
| （可选）`alex_miaosha_user/user_boot/src/test/java/com/alex/user/rbac/MybatisPlusConfigLazyWiringTest.java` | 锁住「Bean 方法参数带 Lazy」的结构契约，或启动冒烟说明 |

---

### Task 1: 红灯 — 用失败启动证据 / 结构测试锁定问题

**Files:**
- Create (optional but preferred): `alex_miaosha_user/user_boot/src/test/java/com/alex/user/rbac/MybatisPlusConfigOrgSubtreeLazyContractTest.java`

**Interfaces:**
- Consumes: `MybatisPlusConfig` 源码或字节码注解
- Produces: 失败断言「`mybatisPlusInterceptor` 的 `OrgSubtreeLookup` 参数必须带 `@Lazy`」在修复前失败

- [ ] **Step 1: Write failing contract test**

```java
package com.alex.user.rbac;

import com.alex.api.user.handler.OrgSubtreeLookup;
import com.alex.user.config.MybatisPlusConfig;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Lazy;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 防止 MybatisPlusConfig 再次对 OrgSubtreeLookup 构造期强依赖，触发 sqlSessionFactory 环。
 */
public class MybatisPlusConfigOrgSubtreeLazyContractTest {

    @Test
    void mybatisPlusInterceptor_orgSubtreeLookupParam_mustBeLazy() throws Exception {
        Method method = null;
        for (Method m : MybatisPlusConfig.class.getDeclaredMethods()) {
            if ("mybatisPlusInterceptor".equals(m.getName())) {
                method = m;
                break;
            }
        }
        assertTrue(method != null, "mybatisPlusInterceptor bean method must exist");

        boolean foundLazyOrgSubtree = false;
        for (Parameter p : method.getParameters()) {
            if (OrgSubtreeLookup.class.equals(p.getType())
                    && p.getAnnotation(Lazy.class) != null) {
                foundLazyOrgSubtree = true;
            }
        }
        assertTrue(
                foundLazyOrgSubtree,
                "OrgSubtreeLookup param of mybatisPlusInterceptor must be annotated @Lazy");
    }
}
```

- [ ] **Step 2: Run test — expect FAIL (当前为字段注入，方法无 `@Lazy` 参数)**

```bat
set JAVA_HOME=C:\Program Files\Java\jdk-17
mvn -pl alex_miaosha_user/user_boot -am clean test -Dtest=MybatisPlusConfigOrgSubtreeLazyContractTest -DfailIfNoTests=false
```

Expected: FAIL（找不到带 `@Lazy` 的 `OrgSubtreeLookup` 方法参数）

- [ ] **Step 3: Do NOT commit unless user asks**

---

### Task 2: 绿灯 — 改造 `MybatisPlusConfig`

**Files:**
- Modify: `alex_miaosha_user/user_boot/src/main/java/com/alex/user/config/MybatisPlusConfig.java`

**Interfaces:**
- Consumes: `UserUtils`, `@Lazy OrgSubtreeLookup`
- Produces: `MybatisPlusInterceptor` Bean（内含 `DataPermissionHandlerImpl`）

- [ ] **Step 1: Rewrite config per spec**

完整替换类内容为（保留 package/import 按需整理）：

```java
package com.alex.user.config;

import com.alex.api.user.handler.DataPermissionHandlerImpl;
import com.alex.api.user.handler.OrgSubtreeLookup;
import com.alex.api.user.user.UserUtils;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.aop.interceptor.PerformanceMonitorInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@MapperScan("com.alex.user.*.mapper")
public class MybatisPlusConfig {

    /**
     * OrgSubtreeLookup 必须 {@code @Lazy}：真实实现依赖 OrgInfoMapper，
     * 若在创建 sqlSessionFactory / interceptor 时强初始化，会形成循环依赖。
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(
            UserUtils userUtils,
            @Lazy OrgSubtreeLookup orgSubtreeLookup) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(
                new DataPermissionInterceptor(
                        new DataPermissionHandlerImpl(userUtils, orgSubtreeLookup)));
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        return interceptor;
    }

    @Bean
    @Profile({"dev", "test"})
    public PerformanceMonitorInterceptor performanceInterceptor() {
        return new PerformanceMonitorInterceptor();
    }
}
```

- [ ] **Step 2: Re-run contract test — expect PASS**

```bat
set JAVA_HOME=C:\Program Files\Java\jdk-17
mvn -pl alex_miaosha_user/user_boot -am clean test -Dtest=MybatisPlusConfigOrgSubtreeLazyContractTest -DfailIfNoTests=false
```

- [ ] **Step 3: Run related rbac tests**

```bat
set JAVA_HOME=C:\Program Files\Java\jdk-17
mvn -pl alex_miaosha_user/user_boot -am test -Dtest=DataPermissionHandlerCtorContractTest,DataPermissionScopeHandlerTest,OrgSubtreeLookupImplTest,MybatisPlusConfigOrgSubtreeLazyContractTest -DfailIfNoTests=false
```

Expected: all PASS

- [ ] **Step 4: Do NOT commit unless user asks**

---

### Task 3: `UserApplication` 启动验收

**Files:** Verify only

- [ ] **Step 1: Compile user_boot**

```bat
set JAVA_HOME=C:\Program Files\Java\jdk-17
mvn -pl alex_miaosha_user/user_boot -am clean compile -DskipTests
```

Expected: BUILD SUCCESS

- [ ] **Step 2: Start `UserApplication`（IDEA 或 `spring-boot:run`）**

Expected log **不得**再出现：

- `BeanCurrentlyInCreationException: Error creating bean with name 'sqlSessionFactory'`
- `UnsatisfiedDependencyException` … `orgSubtreeLookupImpl` … `orgInfoMapper` … circular reference

Expected: 应用完成启动（或至少越过 MyBatis / `sqlSessionFactory` 创建；若后续有无关 Nacos/Redis 问题，单独记录，不归本计划）

- [ ] **Step 3: 写验收备注到** `.superpowers/sdd/task-user-lazy-orgsubtree-verify.md`（命令、关键日志摘录）

---

## Spec Coverage

| Spec 项 | Task |
|---------|------|
| `@Lazy` 方法参数注入 | Task 2 |
| 仅改 user_boot MybatisPlusConfig | Task 2 |
| 契约防回归 | Task 1 |
| 启动无 sqlSessionFactory 环 | Task 3 |
| 保留真实 OrgSubtreeLookupImpl | Task 2–3 |

## Placeholder Scan

无 TBD。

## Type Consistency

- Bean 方法：`mybatisPlusInterceptor(UserUtils, @Lazy OrgSubtreeLookup)`
- Handler：`new DataPermissionHandlerImpl(userUtils, orgSubtreeLookup)`（代理实例）
