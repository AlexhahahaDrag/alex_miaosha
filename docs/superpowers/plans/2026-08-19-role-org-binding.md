# 角色↔机构多对多绑定 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 落地 `docs/superpowers/specs/2026-08-19-role-org-binding-design.md`：角色通过 `t_role_org_info` 多机构绑定；非超管按机构范围看见角色；修复 manager 空表/无新增的产品错位。

**Architecture:** 新建关系表与 `assign-orgs`（全量替换）；`RoleInfoMapper` 数据权限从 `USER_IDS(operator)` 改为「绑定机构 ∩ 登录机构范围」；`assertRoleAccessible` 继续依赖 scoped `queryRoleInfo`；`assign-users` 增加用户有效机构与角色绑定机构交集校验；PC 补机构多选、空态与按钮权限种子。超管可见未绑定角色以便冷启动。

**Tech Stack:** JDK 17 · MyBatis-Plus · JSqlParser `@DataPermission` · JUnit/Mockito · Vue3 + Ant Design Vue · vitest（可选）

## Global Constraints

- **规格为准：** `docs/superpowers/specs/2026-08-19-role-org-binding-design.md`（已锁定）。
- **禁止** `t_role_info.org_id`；**禁止**应用层全量查询再内存过滤分页。
- **ID：** 前端一律 `string`；响应 `const { code, data, message } = await api()`。
- **自动导入：** 禁止重复手写已由 unplugin 提供的 Vue API / AntDV。
- **JAVA_HOME：** `C:\Program Files\Java\jdk-17`。
- **Commit：** 用户未明确要求前不提交；无 `Co-authored-by: Cursor`；Windows 避免中文乱码可用英文 commit message。
- **分支：** `develop-1.0-feature-org-manage`。
- **非目标：** 移动端对齐、授权 Diff、Batch4 axe 壳层。

### Spec → Task 映射

| Spec 条款                              | Task     |
| -------------------------------------- | -------- |
| §3 表结构                              | Task 1   |
| §5.1 assign-orgs + 创建绑机构          | Task 2–3 |
| §4 可见性 / DataPermission             | Task 4   |
| §5.3 守卫 / assign-users 交集 / 删级联 | Task 5   |
| §6 迁移种子 + 按钮权限                 | Task 6   |
| §7 PC                                  | Task 7   |
| §8–10 验收                             | Task 8   |

### File map（将创建 / 修改）

| 路径                                                             | 职责                                        |
| ---------------------------------------------------------------- | ------------------------------------------- | ------ | ------ | -------------------------- |
| `docs/sql/2026-08-19-t_role_org_info.sql`（或项目惯用 sql 目录） | DDL                                         |
| `.../roleOrgInfo/entity/RoleOrgInfo.java`                        | 实体                                        |
| `.../roleOrgInfo/mapper/*`                                       | Mapper + XML                                |
| `.../roleOrgInfo/service/*`                                      | `assignOrgs`                                |
| `.../roleInfo/controller/RoleInfoController.java`                | `POST .../assign-orgs`；创建入参可带 orgIds |
| `.../roleInfo/service/impl/RoleInfoServiceImp.java`              | 创建绑机构、删级联、守卫、assignUsers 交集  |
| `user_api/.../DataPermissionScope.java`                          | 新增 `ROLE_ORG_BOUND`                       |
| `user_api/.../DataPermissionHandlerImpl.java`                    | 实现 EXISTS/IN 过滤                         |
| `.../roleInfo/mapper/RoleInfoMapper.java`                        | 注解改 scope                                |
| `user_api/.../roleInfo/api/RoleInfoApi.java`                     | Feign `assign-orgs`                         |
| PC `roleInfo/api                                                 | config                                      | detail | index` | assignOrgs、机构多选、空态 |
| `tests/.../rbac/RoleOrg*.java`                                   | 单测                                        |

---

### Task 1: DDL + RoleOrgInfo 实体/Mapper 骨架

**Files:**

- Create: `docs/sql/2026-08-19-t_role_org_info.sql`
- Create: `alex_miaosha_user/user_boot/src/main/java/com/alex/user/roleOrgInfo/entity/RoleOrgInfo.java`
- Create: `.../roleOrgInfo/mapper/RoleOrgInfoMapper.java`
- Create: `.../roleOrgInfo/mapper/RoleOrgInfoMapper.xml`
- Create: `alex_miaosha_user/user_api/.../roleOrgInfo/vo/RoleOrgInfoVo.java`（若项目 VO 放 api 模块，对齐 roleUser）

**Steps:**

- [ ] **Step 1:** 写 DDL（MySQL），字段对齐 `t_role_user_info` + `role_id`/`org_id`/`summary`/`status`；有效唯一索引建议：

  ```sql
  -- 逻辑唯一靠应用层；物理索引辅助查询：
  KEY idx_role_org_active (role_id, org_id, status, is_delete)
  ```

- [ ] **Step 2:** 实体继承 `BaseEntity`，`@TableName("t_role_org_info")`，`roleId`/`orgId` 为 `String`。

- [ ] **Step 3:** Mapper 提供 `listValidByRoleId`、`listValidByOrgIds`（后续 Task 用）；本 Task 可不挂 `@DataPermission`。

- [ ] **Step 4:** 本地执行 DDL（或文档注明运维执行）；编译 `user_boot` 通过。

- [ ] **Step 5:**（可选）Commit：`feat(rbac): add t_role_org_info skeleton`

---

### Task 2: RoleOrgInfoService.assignOrgs（全量替换）

**Files:**

- Create: `.../roleOrgInfo/service/RoleOrgInfoService.java`
- Create: `.../roleOrgInfo/service/impl/RoleOrgInfoServiceImp.java`
- Create: `user_boot/src/test/java/com/alex/user/rbac/RoleOrgAssignmentServiceTest.java`

**Interfaces:**

- Produces: `Boolean assignOrgs(Long roleId, List<Long> orgIds)`
  - `orgIds` 空列表 → 清空全部有效绑定（仅超管治理场景；非超管由上层禁止）
  - 旧有效 `status→0`，再 `saveBatch` 新有效行
  - **本层不做机构范围校验**（由 RoleInfoService 调用前校验）

**Steps:**

- [ ] **Step 1 — 红灯：** 单测 mock mapper：先有 (role=1,org=10) 有效；`assignOrgs(1,[20,30])` 后旧行失效、新行两条；校验 `status`。

- [ ] **Step 2 — 实现：** 复制 `RolePermissionInfoServiceImp.assignPermissions` / `RoleUserInfoServiceImp.assignUsersToRole` 的失效+写入模式；`roleId`/`orgId` 存 String。

- [ ] **Step 3 — 绿灯。**

- [ ] **Step 4:**（可选）Commit：`feat(rbac): assignOrgs full replace for role-org`

---

### Task 3: API assign-orgs + 创建角色强制绑机构

**Files:**

- Modify: `RoleInfoController.java`
- Modify: `RoleInfoService.java` / `RoleInfoServiceImp.java`
- Modify: `user_api/.../roleInfo/api/RoleInfoApi.java`（补 `assign-orgs`；顺手补 `assign-users` 若仍缺）
- Create: `.../rbac/RoleOrgAssignApiGuardTest.java`（或扩现有 Ownership 测试）

**API（锁定）：**

```
POST ${api.version}/role-info/assign-orgs
Body: { "roleId": "<string>", "orgIds": ["<string>", ...] }
```

**创建规则（锁定）：**

- `addRoleInfo`：`@Transactional`；insert 后若入参含 `orgIds` 则用之，否则默认 `[loginUser.有效机构Id]`；`orgIds` 必须非空且全部 ∈ 调用方机构范围 `S`（超管任意）；再调 `assignOrgs`。
- 非超管调用 `assign-orgs`：每个 org ∈ `S`；否则 `SystemException` 文案：`无权绑定范围外机构`。

**机构范围 `S` 解析：** 复用 `OrgSubtreeLookup` / Batch3 admin 子树逻辑（与 `DataPermissionHandlerImpl.buildAdminOrgScopeIds` 同口径）；抽到可单测的 helper（若尚无 public 组件则本 Task 抽 `OrgScopeIdsResolver`）。

**Steps:**

- [ ] **Step 1 — 红灯：** 非超管绑范围外 org → 抛错；创建无机构且无默认机构 → 抛错。

- [ ] **Step 2 — 实现** Controller + Service 校验 + Feign。

- [ ] **Step 3 — 绿灯。**

- [ ] **Step 4:**（可选）Commit：`feat(rbac): expose assign-orgs and require orgs on role create`

---

### Task 4: ROLE_ORG_BOUND 数据权限（列表/详情）

**Files:**

- Modify: `DataPermissionScope.java` — 新增 `ROLE_ORG_BOUND`
- Modify: `DataPermissionHandlerImpl.java` — 实现该 scope
- Modify: `RoleInfoMapper.java` — `getPage`/`queryRoleInfo`：
  ```java
  @DataPermission(table = "t_role_info", scope = DataPermissionScope.ROLE_ORG_BOUND)
  ```
  （`field` 可忽略或文档注明；过滤走绑定表而非 operator）
- Modify: `DataPermissionScopeHandlerTest.java` / 新建 `RoleOrgBoundScopeTest.java`
- Modify: `DataPermissionCoverageTest` 若有盘点断言

**过滤 SQL 语义（锁定）：**

- 超管：不加条件
- 非超管：
  ```sql
  EXISTS (
    SELECT 1 FROM alex_user.t_role_org_info roi
    WHERE roi.is_delete = 0 AND roi.status = '1'
      AND roi.role_id = CAST(t_role_info.id AS CHAR)
      AND roi.org_id IN (...S as string/long consistent...)
  )
  ```
  `S` = admin 子树或 user 本机构（与 §4.1 一致）。

**Steps:**

- [ ] **Step 1 — 红灯：** handler 单测：admin 生成 SQL 含 `t_role_org_info` 与 `IN`；超管无 EXISTS。

- [ ] **Step 2 — 改注解 + handler。**

- [ ] **Step 3 — 绿灯；跑 `user_boot` rbac 相关测试，修复因旧 `operator` 假设失败的用例。**

- [ ] **Step 4:**（可选）Commit：`feat(rbac): filter roles by role-org bindings`

---

### Task 5: 守卫、assign-users 交集、删除级联 org

**Files:**

- Modify: `RoleInfoServiceImp.java` — `deleteRoleInfo` 失效 `t_role_org_info`；`assignUsers` 交集校验
- Modify: `RoleUserInfoServiceImp.assignUsersToRole` **或**仅在 `RoleInfoServiceImp.assignUsers` 前置校验（推荐前置，保持 relation service 纯替换）
- Modify: `RoleOwnershipGuardTest` / `RoleDeleteCascadeTest` / 新建 `RoleAssignUsersOrgIntersectionTest.java`

**交集规则（锁定）：**
对每个 `userId`：取其**当前有效** `t_org_user_info.org_id`；与角色有效绑定 `org_id` 集合求交；空则：
`无权分配：用户机构与角色绑定机构无交集`。

**Steps:**

- [ ] **Step 1 — 红灯：** 角色绑 org=10；用户有效机构=20 → assignUsers 失败；用户机构=10 → 成功。

- [ ] **Step 2 — 实现。**

- [ ] **Step 3 — 删除级联测试：删角色后 org 绑定 status=0。**

- [ ] **Step 4 — 绿灯全量 `*rbac*`。**

- [ ] **Step 5:**（可选）Commit：`feat(rbac): role access by binding + user-org intersection`

---

### Task 6: 数据迁移 + 按钮权限种子

**Files:**

- Create: `docs/sql/2026-08-19-t_role_org_info_migrate.sql`
- Create or Modify: 权限/菜单种子 SQL（项目现有 seed 位置；若无统一目录则写在 `docs/sql/` 并注明手工执行）
- Config（可选）: `application-*.yml`  
  `rbac.role-org.fallback-org-id: <根机构id>`

**迁移逻辑（锁定）：**

1. 对每个 `t_role_info` 有效行：若尚无有效 org 绑定，则
   - 优先：`operator`/`creator` 在 `t_org_user_info` 的有效 `org_id`
   - 否则：`rbac.role-org.fallback-org-id`
2. 确保 `role_code in ('super_super','admin','user')` 至少绑到 fallback/根机构。
3. 为「机构管理员」对应角色（`role_code=admin` 或你们种子里的 manager 角色）绑定按钮权限：`role:add`,`role:edit`,`role:auth`,`role:delete`（写入 `t_role_permission_info` 或现网权限种子脚本）。

**Steps:**

- [ ] **Step 1:** 写可重复执行的 migrate SQL（`NOT EXISTS` 防重复）。

- [ ] **Step 2:** 在本地/dev 执行；用 SQL 抽查三角色与 manager 按钮权限。

- [ ] **Step 3:** 文档：在 `docs/ROLE_MANAGEMENT.md` 增补「角色-机构绑定」一小节（短）。

- [ ] **Step 4:**（可选）Commit：`chore(rbac): migrate role-org bindings and role button grants`

---

### Task 7: PC 角色管理 UI

**Files:**

- Modify: `alex_miaosha_front/src/views/user/roleInfo/api/index.ts` — `assignRoleOrgs`
- Modify: `roleInfo/config/index.ts` — `orgIds?: string[]`
- Modify: `roleInfo/roleInfoDetail/index.vue` — 机构多选（`a-select mode="multiple"`，选项来自 `/org-info/tree` 或已有 org 列表 API）；校验至少一项；保存：`add/edit` 后 `assignRoleOrgs`（若后端创建已写绑定，则编辑场景必调）
- Modify: `roleInfo/index.vue` — 空态文案（spec §7）；确认 `useDataScopeHint` 对 **admin 角色码** 生效（若 `czp` 无 `admin` 码，配合 Task 6 种子）
- Modify: `authorizationDetail` / `userAssignmentDetail` — 无需大改；确认打开前角色可见
- Test（可选）: `tests/unit` 扩 `dataScopeHint` 已有单测即可

**空态文案（锁定）：**  
`当前机构范围内暂无角色。可新建并绑定机构，或请超管将角色绑定到本机构。`

**Steps:**

- [ ] **Step 1:** API + 类型。

- [ ] **Step 2:** Detail 机构多选 + rules。

- [ ] **Step 3:** 空态；`npm run lint` 触及文件无新增 error。

- [ ] **Step 4:**（可选）Commit：`feat(front): role-org multi select and empty state`

---

### Task 8: 验收与回归

**Steps:**

- [x] **Step 1:** focused role-org Maven tests green (17) after `user_api` install（stale jar 曾导致 `ROLE_ORG_BOUND` EnumConstantNotPresent）

- [ ] **Step 2:** 手工或 Playwright：`rbac_user_manager` — **Blocked** 直至 DDL/migrate/button grants 落地并重登

- [x] **Step 3:** 更新 `docs/ROLE_MANAGEMENT.md`（assign-orgs / create 绑机构 / 详情 orgIds / §10 表）

- [x] **Step 4:** 对照 spec §10 五条：2–5 Done（单测/代码）；1 live Blocked（见 progress 备注）
---

## Spec coverage self-check

| Spec                             | 覆盖               |
| -------------------------------- | ------------------ |
| §3 表                            | Task 1             |
| §4 可见性/Banner                 | Task 4, 7          |
| §5 assign-orgs/创建/守卫/交集/删 | Task 2–5           |
| §6 迁移/种子                     | Task 6             |
| §7 PC                            | Task 7             |
| §8 测试                          | Task 2–5, 8        |
| §9 非目标                        | Global Constraints |
| 超管见未绑定                     | Task 4             |
| 内置角色也绑定                   | Task 6             |

**Placeholder scan:** 无 TBD；fallback org 用配置项写明。

---

## Execution handoff

Plan complete and saved to `docs/superpowers/plans/2026-08-19-role-org-binding.md`.

**两种执行方式：**

1. **Subagent-Driven（推荐）** — 每 Task 新开子代理，任务间人工/主会话复核
2. **Inline Execution** — 本会话按 `executing-plans` 连续做，设检查点

选 **1** 或 **2**（或再说「Proceed」默认走 1）。  
**在你选定执行方式前，不改业务代码。**
