# Checklist: User Center RBAC Security, Active Org Assignment & Redis Cache Anti-Pollution (第二十九阶段)

**Module:** `alex_miaosha_user` & `alex_miaosha_common`  
**Date:** 2026-10-20  
**Standard:** `TESTING_STANDARD.md` (Level 2 Integration Tests)

---

## 1. VO 大数雪花 ID 序列化契约与修复矩阵

| # | 测试项 | 覆盖类 / 方法 | 验证断言 | 状态 |
|---|---|---|---|:---:|
| 1 | `OrgInfoVo.parentId` 契约修复 | `OrgInfoVo#parentId` | 补齐 `@JsonSerialize(using = Long2StringSerializer.class)`，消除组织机构树父级节点 64 位大数精度丢失 | ✅ PASS |
| 2 | 全量 VO 反射扫描契约锁 | `UserVoSerializationContractIT#allUserVos_longFields_mustHaveJsonSerializeAnnotation` | 递归扫描用户中心 11 大核心 VO 体系（`TUserVo`, `RoleInfoVo`, `MenuInfoVo`, `OrgInfoVo` 等）中所有非静态 `Long` 字段，断言 100% 绑定 `Long2StringSerializer.class` | ✅ PASS |
| 3 | 集合大数泛型元素契约锁 | `UserVoSerializationContractIT#collectionFields_mustHaveContentUsingAnnotation` | 扫描 `roleIds`, `auxiliaryOrgIds` 等 `List<Long>` 集合，断言显式声明 `contentUsing = Long2StringSerializer.class` | ✅ PASS |
| 4 | JSON 序列化高保真转换验证 | `UserVoSerializationContractIT#jsonSerialization_preservesFullPrecisionAsString` | 19 位雪花 ID（如 `1845123456789012345L`）在 Jackson 序列化输出中强制转为带引号字符串，无低位抹零 | ✅ PASS |

---

## 2. 菜单树 Redis 共享缓存隔离与防污染矩阵

| # | 测试项 | 覆盖类 / 方法 | 验证断言 | 状态 |
|---|---|---|---|:---:|
| 5 | 管理端树形接口防污染 | `MenuTreeCacheAntiPollutionIT#getTree_mustNotReadOrWriteMenuAllTreeCache` | `getTree` 必须走带 `@DataPermission` 的 `getList` 在内存动态拼接 `children`，绝不调用 `getListAll`，绝不读写 `menu_all_tree` | ✅ PASS |
| 6 | 全量查询共享缓存契约 | `MenuTreeCacheAntiPollutionIT#isFullQuery_inGetList_readsAndWritesCache` | 仅在 `status=1` 且无筛选关键字时触发全局共享缓存 `menu_all_tree` 读写（TTL 1小时） | ✅ PASS |
| 7 | 条件筛选隔离防串扰 | `MenuTreeCacheAntiPollutionIT#filteredQuery_inGetList_isolatedFromGlobalCache` | 带关键字过滤的 `getList` 严禁读写全局共享缓存，避免污染登录态完整树 | ✅ PASS |
| 8 | 内存不可变性与深克隆契约 | `MenuTreeCacheAntiPollutionIT#permissionFilter_mustNotMutateOriginalMenuNodes` | 权限过滤时深层克隆节点并组装独立 `children`，原始共享缓存节点的 `children` 长度与引用维持不变 | ✅ PASS |

---

## 3. 单有效机构事务与主动逐出失效矩阵

| # | 测试项 | 覆盖类 / 方法 | 验证断言 | 状态 |
|---|---|---|---|:---:|
| 9 | 调岗事务与缓存逐出 | `OrgUserSingleActiveAssignmentIT#testAssignSingleOrgInvalidatesOldAndTriggersCacheInvalidation` | `assignSingleOrg` 将原有效关系置为 `0`、新增关系置为 `1`，成功后主动调用 `permissionContextCacheService.invalidate(userId)` | ✅ PASS |
| 10 | 异常回滚缓存安全 | `OrgUserSingleActiveAssignmentIT#testAssignSingleOrgFailsDoesNotInvalidateCache` | 保存新关系失败回滚时，绝不提前或错误逐出缓存，保证状态一致性 | ✅ PASS |
| 11 | 显式失效状态新增拦截 | `OrgUserSingleActiveAssignmentIT#testAddOrgUserInfoRejectsInvalidStatusExplicitly` | `addOrgUserInfo` 显式传入 `status=0` 时抛出 `PARAM_ERROR` 业务异常，拒绝伪调岗侵入 | ✅ PASS |
| 12 | 激活历史失效孤儿清理 | `OrgUserSingleActiveAssignmentIT#testUpdateOrgUserInfoActivateViaAssignDeletesDuplicateOrphan` | 编辑激活历史失效记录时，自动物理删除孤儿失效行，确保 `(user, org)` 记录唯一 | ✅ PASS |

---

## 4. RBAC 动态权限上下文构建与瘦身降级矩阵

| # | 测试项 | 覆盖类 / 方法 | 验证断言 | 状态 |
|---|---|---|---|:---:|
| 13 | 多角色绑定与权限码去重 | `UserPermissionContextBuildIT#buildContext_mergesMultiRolesAndExtractsFirstOrg` | 合并多角色权限码去重保序，第一有效机构注入 `orgInfo`，自动写入 Redis 缓存 | ✅ PASS |
| 14 | 超级管理员菜单全量直通 | `UserPermissionContextBuildIT#buildContext_superAdminBypassesMenuFiltering` | `super_super` 角色全量放行菜单，跳过权限码过滤裁剪 | ✅ PASS |
| 15 | 缓存命中瘦身模式安全 | `UserPermissionContextBuildIT#buildContext_cachedSlimMode_stripsMenusSafely` | `includeMenus=false` 时返回剥离 `menuList` 的瘦身对象，原始 Redis 缓存对象内存完整不受污染 | ✅ PASS |
| 16 | Redis 读写异常平滑降级 | `UserPermissionContextBuildIT#buildContext_redisExceptionDegradesGracefully` | Redis 读写抛出异常时仅记录日志，自动降级为全量动态计算，业务主流程平稳运行 | ✅ PASS |

---

## 5. 不测理由

1. **真实物理 Redis 集群主从网络复制**：测试聚焦于 Redis 缓存读写键隔离、内存防污染、主动逐出语义与高可用容错；物理集群主从同步由中间件层与运维保障；
2. **秒杀模块排除**：按照用户明确指令，本阶段加固与测试严格排除秒杀模块。
