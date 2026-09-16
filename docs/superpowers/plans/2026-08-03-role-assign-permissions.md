# Role Assign Permissions Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 打通角色权限批量分配：新增 `POST /role-info/assign-permissions`，PC 授权入口改调该接口，角色主数据保存不再携带 `permissionList`。

**Architecture:** 关系替换逻辑落在 `RolePermissionInfoService.assignPermissions`（对齐 `assignRoles`：旧有效行 status→0，再批量插新行）。`RoleInfoService` 编排校验角色、调用关系服务、清理绑定用户的 `permission_context` 缓存。Controller 暴露与 `assign-users` 对称的请求体。PC `authorizationDetail` 只调分配接口；`roleInfoDetail` 先存主数据再分配。

**Tech Stack:** Spring Boot 2.7 / MyBatis-Plus / 现有 RedisUtils + LoginKey；Vue3 + Ant Design Vue PC；单测风格对齐 `RoleUserAssignmentServiceTest`（无 JUnit 注解的可运行断言类，由现有测试入口或 `mvn test` 发现方式决定——若仓库用 surefire 扫描，则补 `@Test`/`main` 与现有 rbac 测试保持一致）。

## Global Constraints

- 前端 ID 一律 `string`，禁止 `Number(id)`。
- 接口响应解构：`const { code, data, message } = await api()`。
- 权限变更唯一写入口：`assign-permissions`；`add/update` 角色继续忽略 `permissionList`。
- 关系字段 `role_id` / `permission_id` 实体为 String；API 用 Long，服务内 `String.valueOf`。
- 不改移动端；不改 Org/Role `@DataPermission`。
- Spec：`docs/superpowers/specs/2026-08-03-role-assign-permissions-design.md`。

---

## File Structure

**Create:**

- `alex_miaosha_user/user_boot/src/test/java/com/alex/user/rbac/RolePermissionAssignmentServiceTest.java`

**Modify (backend):**

- `alex_miaosha_user/user_boot/.../rolePermissionInfo/service/RolePermissionInfoService.java`
- `alex_miaosha_user/user_boot/.../rolePermissionInfo/service/impl/RolePermissionInfoServiceImp.java`
- `alex_miaosha_user/user_boot/.../roleInfo/service/RoleInfoService.java`
- `alex_miaosha_user/user_boot/.../roleInfo/service/impl/RoleInfoServiceImp.java`
- `alex_miaosha_user/user_boot/.../roleInfo/controller/RoleInfoController.java`
- `alex_miaosha_user/user_api/.../roleInfo/api/RoleInfoApi.java`
- `alex_miaosha_user/user_api/.../roleInfo/fallback/RoleInfoFallbackFactory.java`

**Modify (frontend PC):**

- `alex_miaosha_front/src/views/user/roleInfo/api/index.ts`
- `alex_miaosha_front/src/views/user/permissionInfo/api/index.ts`（补 `/list` 供新增角色拉权限树）
- `alex_miaosha_front/src/views/user/roleInfo/authorizationDetail/index.vue`
- `alex_miaosha_front/src/views/user/roleInfo/roleInfoDetail/index.vue`

**Docs (optional sync):**

- 实现完成后可将 spec 状态改为「已实现」。

---

### Task 1: RolePermissionInfoService.assignPermissions（TDD）

**Files:**
- Create: `alex_miaosha_user/user_boot/src/test/java/com/alex/user/rbac/RolePermissionAssignmentServiceTest.java`
- Modify: `alex_miaosha_user/user_boot/src/main/java/com/alex/user/rolePermissionInfo/service/RolePermissionInfoService.java`
- Modify: `alex_miaosha_user/user_boot/src/main/java/com/alex/user/rolePermissionInfo/service/impl/RolePermissionInfoServiceImp.java`

**Interfaces:**
- Consumes: `SysConf.VALID_STATUS` / `INVALID_STATUS`；`RolePermissionInfo` 实体字段 `roleId`/`permissionId`/`status`（String）
- Produces: `Boolean assignPermissions(Long roleId, List<Long> permissionIds)`

- [ ] **Step 1: Write the failing test**

创建 `RolePermissionAssignmentServiceTest.java`，完整仿照 `RoleUserAssignmentServiceTest` 的断言辅助与 `Testable*` 子类覆盖 `list` / `updateById` / `saveBatch`。至少包含：

```java
public void testAssignPermissionsInvalidatesOldAndCreatesNew() {
    RolePermissionInfo old = assignment(1L, "10", "100", "1");
    TestableRolePermissionInfoService service = new TestableRolePermissionInfoService(old);

    Boolean result = service.assignPermissions(10L, Arrays.asList(200L, 300L));

    assertTrue(Boolean.TRUE.equals(result), "should return true");
    assertEquals("0", old.getStatus(), "old active should be invalidated");
    assertEquals(2, service.savedAssignments.size(), "two new rows");
    assertAssignment(service.savedAssignments.get(0), "10", "200", "1");
    assertAssignment(service.savedAssignments.get(1), "10", "300", "1");
}

public void testAssignPermissionsEmptyOnlyInvalidates() { /* empty list → no saveBatch */ }

public void testAssignPermissionsDeduplicatesAndFiltersNull() { /* 200,200,null → one row */ }

public void testAssignPermissionsNullRoleIdThrows() { /* expect RuntimeException */ }

public void testAssignPermissionsDeclaresTransactional() throws Exception {
    Transactional t = RolePermissionInfoServiceImp.class
        .getMethod("assignPermissions", Long.class, List.class)
        .getAnnotation(Transactional.class);
    assertNotNull(t, "must declare @Transactional");
}
```

`TestableRolePermissionInfoService` 构造：`super((RolePermissionInfoMapper) null)`，与角色用户测试相同模式。

- [ ] **Step 2: Run test to verify it fails**

```bash
cd f:/workplace/project/myself/backend/alex_miaosha
mvn -pl alex_miaosha_user/user_boot -Dtest=RolePermissionAssignmentServiceTest test
```

Expected: FAIL（方法不存在或编译失败）。

若 surefire 扫不到无 `@Test` 的类：给每个用例方法加 `@org.junit.jupiter.api.Test`，或增加 `public static void main` 依次调用并在失败时 `System.exit(1)`——与仓库现有 rbac 测试运行方式对齐（先看 `RoleUserAssignmentServiceTest` 是否被 surefire 执行；若不被执行则统一加 `@Test`）。

- [ ] **Step 3: Minimal implementation**

在 `RolePermissionInfoService` 增加：

```java
Boolean assignPermissions(Long roleId, List<Long> permissionIds);
```

在 `RolePermissionInfoServiceImp`：

```java
@Override
@Transactional(rollbackFor = Exception.class)
public Boolean assignPermissions(Long roleId, List<Long> permissionIds) {
    if (roleId == null) {
        throw new SystemException(ResultEnum.PARAM_ERROR, "角色权限分配参数错误:");
    }
    List<RolePermissionInfo> active = list(Wrappers.<RolePermissionInfo>lambdaQuery()
            .eq(RolePermissionInfo::getRoleId, String.valueOf(roleId))
            .eq(RolePermissionInfo::getStatus, SysConf.VALID_STATUS));
    for (RolePermissionInfo row : active) {
        row.setStatus(SysConf.INVALID_STATUS);
        if (!updateById(row)) {
            throw new SystemException(ResultEnum.SYSTEM_ERROR, "角色权限旧关系失效失败:");
        }
    }
    if (permissionIds == null || permissionIds.isEmpty()) {
        return true;
    }
    Set<Long> unique = new LinkedHashSet<>();
    for (Long pid : permissionIds) {
        if (pid != null) {
            unique.add(pid);
        }
    }
    if (unique.isEmpty()) {
        return true;
    }
    List<RolePermissionInfo> batch = new ArrayList<>();
    for (Long pid : unique) {
        RolePermissionInfo row = new RolePermissionInfo();
        row.setRoleId(String.valueOf(roleId));
        row.setPermissionId(String.valueOf(pid));
        row.setStatus(SysConf.VALID_STATUS);
        batch.add(row);
    }
    if (!saveBatch(batch)) {
        throw new SystemException(ResultEnum.SYSTEM_ERROR, "角色权限新关系保存失败:");
    }
    return true;
}
```

补全 import：`Wrappers`、`Transactional`、`SysConf`、`SystemException`、`ResultEnum`、`ArrayList`、`LinkedHashSet`、`Set`。

- [ ] **Step 4: Run test to verify it passes**

同 Step 2 命令。Expected: PASS。

- [ ] **Step 5: Commit**（仅当用户明确要求提交时执行）

```bash
git add alex_miaosha_user/user_boot/src/test/java/com/alex/user/rbac/RolePermissionAssignmentServiceTest.java \
  alex_miaosha_user/user_boot/src/main/java/com/alex/user/rolePermissionInfo/service/RolePermissionInfoService.java \
  alex_miaosha_user/user_boot/src/main/java/com/alex/user/rolePermissionInfo/service/impl/RolePermissionInfoServiceImp.java
git commit -m "$(cat <<'EOF'
feat(user): add role permission assignPermissions replace semantics

EOF
)"
```

---

### Task 2: RoleInfo 编排 + Controller + Feign

**Files:**
- Modify: `RoleInfoService.java`、`RoleInfoServiceImp.java`、`RoleInfoController.java`
- Modify: `user_api/.../RoleInfoApi.java`、`RoleInfoFallbackFactory.java`

**Interfaces:**
- Consumes: `RolePermissionInfoService.assignPermissions`；`RoleUserInfoService.list`；`RedisUtils` + `LoginKey.loginKey`
- Produces: `Boolean assignPermissions(Long roleId, List<Long> permissionIds)`；`POST /assign-permissions`

- [ ] **Step 1: 扩展 RoleInfoService 接口**

```java
Boolean assignPermissions(Long roleId, List<Long> permissionIds);
```

- [ ] **Step 2: 实现 RoleInfoServiceImp.assignPermissions**

注入 `RedisUtils`（与 `TUserServiceImpl` 相同）。实现：

```java
@Override
public Boolean assignPermissions(Long roleId, List<Long> permissionIds) {
    if (roleId == null) {
        throw new SystemException(ResultEnum.PARAM_ERROR, "角色权限分配参数错误:");
    }
    RoleInfo roleInfo = roleInfoMapper.selectById(roleId);
    if (roleInfo == null) {
        throw new SystemException(ResultEnum.PARAM_ERROR, "角色不存在:");
    }
    Boolean ok = rolePermissionInfoService.assignPermissions(roleId, permissionIds);
    List<RoleUserInfo> users = roleUserInfoService.list(Wrappers.<RoleUserInfo>lambdaQuery()
            .eq(RoleUserInfo::getRoleId, String.valueOf(roleId))
            .eq(RoleUserInfo::getStatus, SysConf.VALID_STATUS));
    for (RoleUserInfo ru : users) {
        if (ru.getUserId() == null) {
            continue;
        }
        try {
            redisUtils.delete(LoginKey.loginKey, "permission_context:" + ru.getUserId());
        } catch (Exception ignored) {
            // 缓存清理失败不阻断主流程，与用户同步侧 try/catch 一致
        }
    }
    return ok;
}
```

确认 `LoginKey`、`RedisUtils` 的 import 包路径与 `TUserServiceImpl` 一致。

- [ ] **Step 3: Controller 暴露接口**

在 `RoleInfoController` 的 `assignUsers` 旁新增：

```java
@LogRestRequest(apiName = "角色分配权限")
@ApiOperationSupport(order = 70, author = "alex")
@ApiOperation(value = "角色分配权限", notes = "全量替换角色权限关系", response = Result.class)
@PostMapping("/assign-permissions")
public Result<Boolean> assignPermissions(@RequestBody RolePermissionAssignRequest request) {
    return Result.success(roleInfoService.assignPermissions(request.getRoleId(), request.getPermissionIds()));
}

@Data
public static class RolePermissionAssignRequest {
    private Long roleId;
    private List<Long> permissionIds;
}
```

- [ ] **Step 4: 同步 Feign**

`RoleInfoApi` 增加：

```java
@PostMapping("/assign-permissions")
Result<Boolean> assignPermissions(@RequestBody RolePermissionAssignRequest request);
```

若 Feign 不能引用 Controller 内部类：在 `user_api` 新建简易 DTO  
`com.alex.api.user.roleInfo.vo.RolePermissionAssignRequest`（字段 `Long roleId`、`List<Long> permissionIds`），Controller 与 Api 共用该 Vo，删除内部静态类重复定义。

`RoleInfoFallbackFactory` 对新建方法返回失败 Result。

- [ ] **Step 5: 编译验证**

```bash
mvn -pl alex_miaosha_user/user_boot,alex_miaosha_user/user_api -am compile -DskipTests
mvn -pl alex_miaosha_user/user_boot -Dtest=RolePermissionAssignmentServiceTest test
```

Expected: BUILD SUCCESS。

- [ ] **Step 6: Commit**（仅当用户要求）

```bash
git commit -m "$(cat <<'EOF'
feat(user): expose POST /role-info/assign-permissions and clear permission cache

EOF
)"
```

---

### Task 3: PC API 封装

**Files:**
- Modify: `f:/workplace/project/myself/frontend/alex_miaosha_front/src/views/user/roleInfo/api/index.ts`
- Modify: `f:/workplace/project/myself/frontend/alex_miaosha_front/src/views/user/permissionInfo/api/index.ts`

**Interfaces:**
- Produces: `assignRolePermissions(roleId: string, permissionIds: string[])`；`getPermissionInfoList(params?)`

- [ ] **Step 1: roleInfo API**

```ts
export function assignRolePermissions(
	roleId: string,
	permissionIds: string[],
): Promise<ResponseBody<boolean>> {
	return postData(baseService.user + baseRoleInfo + '/assign-permissions', {
		roleId,
		permissionIds,
	});
}
```

- [ ] **Step 2: permissionInfo API 补 list**

在 `permissionInfo/api/index.ts` 增加（路径以该文件现有 `baseService.user + '/permission-info'` 为准）：

```ts
export function getPermissionInfoList(
	params?: Record<string, unknown>,
): Promise<ResponseBody<unknown[]>> {
	return postData(baseService.user + basePermissionInfo + '/list', params ?? {});
}
```

- [ ] **Step 3: Commit**（仅当用户要求）

---

### Task 4: authorizationDetail 改调 assign-permissions

**Files:**
- Modify: `alex_miaosha_front/src/views/user/roleInfo/authorizationDetail/index.vue`

**Interfaces:**
- Consumes: `assignRolePermissions`、`getRoleInfoDetail`
- Produces: 保存仅分配权限，不再 edit 角色

- [ ] **Step 1: 改 imports 与保存逻辑**

删除对 `addRoleInfo` / `editRoleInfo` 的依赖。`saveRoleInfoManager` 改为：

```ts
const saveRoleInfoManager = async () => {
	const roleId = formState.value?.id != null ? String(formState.value.id) : '';
	if (!roleId) {
		loading.value = false;
		message.error('角色 ID 缺失，无法保存权限');
		return;
	}
	const permissionIds = selectPermission.value.map((id) => String(id));
	const { code, message: messageInfo } = await assignRolePermissions(
		roleId,
		permissionIds,
	).finally(() => {
		loading.value = false;
	});
	if (code === '200') {
		message.success(messageInfo || '保存成功！');
		modelInfo.value.open = false;
		emit('success');
	} else {
		message.error(messageInfo || '保存失败！');
	}
};
```

加载逻辑保持 `getRoleInfoDetail`；已选权限 id 继续 `String(item.id)`，禁止 `Number`。

- [ ] **Step 2: lint**

```bash
cd f:/workplace/project/myself/frontend/alex_miaosha_front
npx eslint src/views/user/roleInfo/authorizationDetail/index.vue --max-warnings=0
```

Expected: 无错误。

- [ ] **Step 3: Commit**（仅当用户要求）

---

### Task 5: roleInfoDetail 职责拆分

**Files:**
- Modify: `alex_miaosha_front/src/views/user/roleInfo/roleInfoDetail/index.vue`

**Interfaces:**
- Consumes: `addRoleInfo` / `editRoleInfo` / `assignRolePermissions` / `getPermissionInfoList` / `getRoleInfoPage`
- Produces: 主数据与权限两阶段保存；新增角色用 roleCode 回查 id

- [ ] **Step 1: 新增模式权限树**

`init` 中 `else` 分支：删除 `getRoleInfoDetail('1')`。改为：

```ts
const { code, data, message: messageInfo } = await getPermissionInfoList();
if (code === '200') {
	permissionTree.value = (data as unknown[]) || [];
} else {
	message.error(messageInfo || '获取权限树失败！');
}
```

确认 `/permission-info/list` 返回树形结构与 `menu-tree` 所需字段一致（与角色详情里的 `permissionList` 同源：`PermissionInfoService.getList`）。若字段不一致，以角色详情 `permissionList` 结构为准做一次映射（仅 map `id`/`permissionName`/`children`）。

- [ ] **Step 2: 保存拆分**

```ts
const saveRoleInfoManager = async () => {
	const payload = { ...formState.value };
	delete (payload as { permissionList?: unknown }).permissionList;

	const isEdit = !!payload.id;
	const api = isEdit ? editRoleInfo : addRoleInfo;
	const { code, message: messageInfo } = await api(payload);
	if (code !== '200') {
		loading.value = false;
		message.error(messageInfo || '保存失败！');
		return;
	}

	let roleId = payload.id != null ? String(payload.id) : '';
	if (!roleId && payload.roleCode) {
		const pageRes = await getRoleInfoPage(
			{ roleCode: payload.roleCode } as RoleInfoData,
			1,
			1,
		);
		const record = pageRes.data?.records?.[0];
		roleId = record?.id != null ? String(record.id) : '';
	}

	if (!roleId) {
		loading.value = false;
		message.error('角色已保存，但无法获取角色 ID，请稍后在授权中配置权限');
		modelInfo.value.open = false;
		emit('success');
		return;
	}

	const permissionIds = selectPermission.value.map((id) => String(id));
	const assignRes = await assignRolePermissions(roleId, permissionIds).finally(
		() => {
			loading.value = false;
		},
	);
	if (assignRes.code === '200') {
		message.success(assignRes.message || '保存成功！');
		modelInfo.value.open = false;
		emit('success');
	} else {
		message.error(assignRes.message || '角色已保存，权限分配失败');
	}
};
```

注意：`CommonPageResult` 字段名以项目类型为准（`records` / `list`）；打开 `types/api` 核对后写死正确字段。

- [ ] **Step 3: lint**

```bash
npx eslint src/views/user/roleInfo/roleInfoDetail/index.vue src/views/user/roleInfo/api/index.ts src/views/user/permissionInfo/api/index.ts --max-warnings=0
```

- [ ] **Step 4: graphify 更新（前端规则）**

```bash
cd f:/workplace/project/myself/frontend/alex_miaosha_front
npm run graphify:update
```

若脚本不存在：`graphify update .`

- [ ] **Step 5: Commit**（仅当用户要求）

---

### Task 6: 手工验收清单

- [ ] **Step 1: 后端启动用户服务后**，用已有角色在授权抽屉勾选权限保存；查库：

```sql
SELECT role_id, permission_id, status
FROM t_role_permission_info
WHERE role_id = '{roleId}'
ORDER BY id DESC
LIMIT 20;
```

Expected: 新勾选为 `status=1`；取消的旧行 `status=0`。

- [ ] **Step 2: 浏览器 Network**
  - 授权抽屉保存：仅 `POST .../role-info/assign-permissions`
  - 角色编辑保存：`PUT .../role-info` body **无** `permissionList`，随后 `assign-permissions`

- [ ] **Step 3: 缓存**
  - 给角色绑定一用户后改权限；确认 Redis key `permission_context:{userId}` 被删，或该用户重新拉菜单/权限码已更新。

---

## Spec Coverage Self-Review

| Spec 项 | Task |
|---------|------|
| POST assign-permissions | Task 2 |
| 旧失效 + 新插 + 空列表清空 | Task 1 |
| 清 permission_context | Task 2 |
| authorizationDetail 只调新接口 | Task 4 |
| roleInfoDetail 去掉 permissionList + 另调分配 | Task 5 |
| 新增角色无 id → roleCode 回查 | Task 5 |
| ID 保持 string | Task 3–5 |
| 单测 | Task 1 |
| 不改移动端 / 数据权限 | 全局约束 |

**Placeholder scan:** 无 TBD。  
**类型一致:** `assignPermissions(Long, List<Long>)` / 前端 `assignRolePermissions(string, string[])` 全程统一。

---

## Execution Handoff

Plan complete and saved to `docs/superpowers/plans/2026-08-03-role-assign-permissions.md`.

**Two execution options:**

1. **Subagent-Driven（推荐）** — 每任务独立子代理 + 任务间 review  
2. **Inline Execution** — 本会话按 executing-plans 连续执行并设检查点  

Which approach?
