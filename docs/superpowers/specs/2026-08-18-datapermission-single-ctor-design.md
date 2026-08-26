# Design: DataPermissionHandler 单构造器统一注入

**Date:** 2026-08-18  
**Branch:** `develop-1.0-feature-org-manage`  
**Status:** Approved (方案 C)  
**Related:** RBAC-BE-SCOPE-002；Gateway 启动失败 `NoSuchMethodException: DataPermissionHandlerImpl.<init>()`

## 1. Problem

`711379d5` 为机构子树权限引入 `OrgSubtreeLookup` 后，`DataPermissionHandlerImpl` 从「`@Component` + `@RequiredArgsConstructor` 单构造」变为「双显式构造、均无 `@Autowired`」。

Spring 在存在多个构造器且未指定注入构造器时会回退无参构造；该类无无参构造，导致扫描 `com.alex.api.user` 的 **Gateway** 启动失败。  
`product` / `oss` / `finance` 的 `MybatisPlusConfig` 仍以单参 `new DataPermissionHandlerImpl(userUtils)` 调用，与双构造兼容意图绑定在一起，但未解决 Spring Bean 创建路径。

## 2. Goals

1. Gateway（及所有扫描 `DataPermissionHandlerImpl` 的应用）可正常创建 Spring Bean。
2. 保留 RBAC-BE-SCOPE-002：`user_boot` 使用真实 `OrgSubtreeLookupImpl`；其它服务默认 NOOP（不扩展子孙）。
3. 构造模型统一为**单一全参构造**，避免再次出现「双构造 + `@Component`」陷阱。
4. **不**删除 `@Component`；**不**给 Gateway 加 `excludeFilters`（已否决方案）。

## 3. Non-Goals

- 不把机构树 SQL / Mapper 下沉进 `user_api`（handler 保持 DB 无关）。
- 不升级 Vite / 不处理前端登录 echarts 问题。
- 不重构其它 `@ComponentScan` 范围（除非编译/启动证明必须）。

## 4. Approach (C)

### 4.1 Handler

- 恢复 `@RequiredArgsConstructor`。
- 字段：`final UserUtils userUtils`、`final OrgSubtreeLookup orgSubtreeLookup`。
- 删除单参构造与双参显式构造重载。
- 保留 `@Component` + `@Slf4j`。
- null 语义：Spring / 测试 / `MybatisPlusConfig` 必须传入非 null；NOOP 由 Bean 或测试显式传入，不再在构造器内 `null → NOOP`（若需防御，可在业务方法入口对 lookup 做一次 `Optional`/`NOOP` 兜底，但不恢复双构造）。

### 4.2 NOOP Bean（user_api）

新增配置类（建议路径）：

`alex_miaosha_user/user_api/src/main/java/com/alex/api/user/handler/OrgSubtreeLookupConfiguration.java`

```java
@Configuration
public class OrgSubtreeLookupConfiguration {
    @Bean
    @ConditionalOnMissingBean(OrgSubtreeLookup.class)
    public OrgSubtreeLookup orgSubtreeLookup() {
        return OrgSubtreeLookup.NOOP;
    }
}
```

- Gateway / finance / product / oss：拿到 NOOP。
- `user_boot` 的 `OrgSubtreeLookupImpl`（`@Component`）优先注册，满足 `MissingBean` 条件，覆盖 NOOP。

### 4.3 MybatisPlusConfig

| 模块           | 变更                                                                                                                             |
| -------------- | -------------------------------------------------------------------------------------------------------------------------------- |
| `user_boot`    | 已注入 `OrgSubtreeLookup`；改为与统一签名一致的 `new DataPermissionHandlerImpl(userUtils, orgSubtreeLookup)`（若已是双参则保持） |
| `finance_boot` | 注入 `OrgSubtreeLookup`，`new DataPermissionHandlerImpl(userUtils, orgSubtreeLookup)`                                            |
| `product_boot` | 同上                                                                                                                             |
| `oss_boot`     | 同上                                                                                                                             |

### 4.4 Gateway

- **不修改** `GatewayApplication` 的 `@ComponentScan`。
- 依赖：单构造 + `UserUtils` Bean + NOOP `OrgSubtreeLookup` Bean 即可完成注入。

### 4.5 Tests

- `DataPermissionScopeHandlerTest`：`handler(user)` 改为传入 `OrgSubtreeLookup.NOOP`；保留 `handler(user, lookup)`。
- 其它 `new DataPermissionHandlerImpl(userUtils)` 调用点同步改为双参（NOOP 或 Fake）。
- 现有子树单测语义不变。

## 5. Success Criteria

1. `DataPermissionHandlerImpl` 仅有一个公共构造器（Lombok 生成）。
2. `GatewayApplication` 启动不再出现 `NoSuchMethodException: DataPermissionHandlerImpl.<init>()`。
3. `user_boot` rbac 相关单测（至少 `DataPermissionScopeHandlerTest`、`OrgSubtreeLookupImplTest`）通过。
4. finance / product / oss 模块编译通过。
5. admin + 有子孙 → IN；无子孙 / 失败 → EqualsTo 本机构（行为与现网一致）。

## 6. Risks

| Risk                                                           | Mitigation                                                          |
| -------------------------------------------------------------- | ------------------------------------------------------------------- |
| 某模块扫描了 api.user 但未加载 `OrgSubtreeLookupConfiguration` | 配置类放在 `com.alex.api.user.handler`，与现有 ComponentScan 包一致 |
| `OrgSubtreeLookupImpl` 与 NOOP 双 Bean                         | `@ConditionalOnMissingBean` + Impl 为 `@Component`                  |
| 测试遗漏单参 `new`                                             | grep `new DataPermissionHandlerImpl(` 全仓清零单参                  |

## 7. Decision Log

- 2026-08-18：否决「去 `@Component` + Gateway exclude」。
- 2026-08-18：采纳方案 C（单构造 + 全链路注入 `OrgSubtreeLookup` + MissingBean NOOP）。
