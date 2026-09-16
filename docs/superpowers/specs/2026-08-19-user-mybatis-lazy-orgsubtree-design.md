# Design: User Boot MybatisPlusConfig `@Lazy` OrgSubtreeLookup

**Date:** 2026-08-19  
**Branch:** `develop-1.0-feature-org-manage`  
**Status:** Approved (方案 A)  
**Related:** RBAC-BE-SCOPE-002；`UserApplication` 启动 `BeanCurrentlyInCreationException: sqlSessionFactory`

## 1. Problem

日志（`logs/alex-user-dev/alex-user-dev-error.log`，约 2026-08-19 11:40）显示不可解析循环依赖：

```
sqlSessionFactory (creating)
  → MybatisPlusInterceptor (MybatisPlusConfig)
    → OrgSubtreeLookupImpl (constructor-injected into MybatisPlusConfig)
      → OrgInfoMapper
        → sqlSessionFactory (still creating)
```

根因：方案 C 将真实 `OrgSubtreeLookup` 作为 `MybatisPlusConfig` 的**构造期强依赖**；`OrgSubtreeLookupImpl` 又依赖 Mapper，而 Mapper 依赖尚未完成的 `sqlSessionFactory`。

finance/product/oss 注入的是 `@ConditionalOnMissingBean` NOOP，不碰 `OrgInfoMapper`，一般不触发该环。

## 2. Goals

1. `UserApplication` 可正常完成上下文刷新，不再出现 `sqlSessionFactory` 循环依赖。
2. 保留 `user_boot` 使用 `OrgSubtreeLookupImpl` 的真实子树查询能力。
3. 改动面最小，不破坏既有 DataPermission 单构造契约。

## 3. Non-Goals

- 不启用 `spring.main.allow-circular-references=true`
- 不让 `user_boot` 退回 `OrgSubtreeLookup.NOOP`
- 不修改 Gateway / Handler 双参构造契约 / finance·product·oss（除非验证需要风格统一）
- 不把机构 Mapper SQL 下沉进 `user_api`

## 4. Approach (A)

### 4.1 Change

**File:** `alex_miaosha_user/user_boot/src/main/java/com/alex/user/config/MybatisPlusConfig.java`

- 将 `OrgSubtreeLookup`（及可选 `UserUtils`）从类字段构造注入，改为 `@Bean` 方法参数注入。
- 对 `OrgSubtreeLookup` 参数标注 `@Lazy`。

推荐形态：

```java
@Configuration
@EnableTransactionManagement
@MapperScan("com.alex.user.*.mapper")
public class MybatisPlusConfig {

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

    // performanceInterceptor 保持不变
}
```

- 去掉类上 `@RequiredArgsConstructor` 及对应 final 字段（若全部改为方法参数）。
- 保留对 `DataPermissionHandlerImpl(userUtils, orgSubtreeLookup)` 的双参调用（lookup 为懒代理，运行期首次调用再初始化 Impl）。

### 4.2 Why it works

`@Lazy` 注入 Spring 懒代理：创建 `MybatisPlusInterceptor` / `sqlSessionFactory` 时**不**实例化 `OrgSubtreeLookupImpl`，从而不提前拉取 `OrgInfoMapper`。首次数据权限需要子孙机构时再解析真实 Bean，此时 `sqlSessionFactory` 已就绪。

### 4.3 Out of scope siblings

| 模块 | 动作 |
|------|------|
| finance / product / oss `MybatisPlusConfig` | 不改（NOOP，无环） |
| `OrgSubtreeLookupImpl` | 不改 |
| `DataPermissionHandlerImpl` | 不改 |
| Gateway | 不改 |

## 5. Testing / Acceptance

1. 聚焦编译：`mvn -pl alex_miaosha_user/user_boot -am compile -DskipTests` 成功。
2. 启动 `UserApplication`：日志不再出现 `BeanCurrentlyInCreationException: sqlSessionFactory` / 上述循环依赖栈。
3. 回归：`DataPermissionHandlerCtorContractTest`、`DataPermissionScopeHandlerTest`、`OrgSubtreeLookupImplTest` 仍绿。
4. （可选手工）admin + 有子孙机构数据，ORG_ID scope 仍含子机构。

## 6. Risks

| Risk | Mitigation |
|------|------------|
| 懒代理首次调用失败难排查 | 保留 Impl 上原有异常降级（handler 已有 lookup failure → self-only） |
| 误改其它 boot | 本设计明确仅 user_boot |
| `@Lazy` 与构造注入混用不当 | 统一改为方法参数 + `@Lazy`，去掉字段强依赖 |

## 7. Decision Log

- 2026-08-19：否决 allow-circular-references / user_boot NOOP。
- 2026-08-19：采纳方案 A（`MybatisPlusConfig` 对 `OrgSubtreeLookup` `@Lazy`）。
