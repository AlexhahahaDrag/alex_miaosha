# 角色管理（用户管理 → Role Management）

> 由 [jezweb/claude-skills@project-docs](https://skills.sh/jezweb/claude-skills/project-docs) 工作流产出：基于三端**实际代码**扫描，非规划文档。  
> 扫描日期：2026-08-19 · 分支语境：`develop-1.0-feature-org-manage`（含 Batch0–3 RBAC 改造）

---

## Overview

角色管理是 RBAC 的「角色中心」：维护 `t_role_info` 主数据，并通过**全量替换**语义绑定用户（`t_role_user_info`）与权限点（`t_role_permission_info`）。菜单可见性**不**直接绑角色，而是由登录态 `UserPermissionContextService.buildContext` 用角色权限码过滤全量菜单树。

| 端 | 产品模型 | 成熟度判断 |
|----|----------|------------|
| **Backend** | 角色中心 API + 写归属守卫 + 级联删除 + 缓存失效 | 主路径完整；关系表裸 CRUD 仍是旁路风险 |
| **PC** | 列表 + 授权 Drawer（权限树）+ 用户分配 Drawer + 独立关系页 | 主链路已通；Detail 仍用旧 `menu-tree`；授权无 Diff |
| **Mobile** | 三套关联表 CRUD 骨架 | 未对齐 PC「权限树 + assign 全量替换」模型 |

---

## Stack（角色相关）

| Layer | Technology | Location |
|-------|-----------|----------|
| Runtime | Spring Boot 微服务 | `alex_miaosha_user`（Nacos: `alex-user-dev` :30006） |
| API 前缀 | `/api/v1`（`${api.version}`） | Gateway 常转发为 `/api/am-user` |
| ORM | MyBatis-Plus + `@DataPermission` | Mapper 注解 + JSqlParser |
| Cache | Redis | `LoginKey` + `permission_context:{userId}` |
| PC | Vue 3 + Ant Design Vue + Pinia | `alex_miaosha_front/src/views/user/roleInfo` |
| Mobile | Vue 3 + Vant | `alex_miaosha_mobile/src/views/user/role{Info,UserInfo,PermissionInfo}` |

---

## Directory Structure（角色触点）

```
alex_miaosha_user/
├── user_boot/.../roleInfo/          # 角色主 Controller / Service / Mapper
├── user_boot/.../roleUserInfo/      # 角色-用户关系（含 assignRoles / assignUsersToRole）
├── user_boot/.../rolePermissionInfo/
├── user_boot/.../roleOrgInfo/       # 角色-机构多对多（assignOrgs 全量替换）
├── user_boot/.../rbac/              # RbacRoleCodes 消费、PermissionContextCache、DataPermission
└── user_api/.../roleInfo/api/       # Feign（缺 assign-users）

alex_miaosha_front/src/views/user/
├── roleInfo/                        # 列表 / Detail / 授权 / 用户分配
├── roleUserInfo/                    # 独立双栏关系页 /user/role-user-info
└── .../components/rbac/            # RbacPermissionTreePanel / DualList / DiffPreview

alex_miaosha_mobile/src/views/user/
├── roleInfo/                        # 角色 CRUD
├── roleUserInfo/                    # 单条用户关联
└── rolePermissionInfo/             # 单条权限关联（批量 UI 多为占位）
```

---

## Architecture：Key Flows

### Flow 1 — 创建角色 → 绑权限 → 分用户（推荐主路径）

```
PC/MB Client
  │
  ├─ POST /role-info                    → RoleInfoServiceImp.addRoleInfo
  │     assertRoleCodeUnique → insert → 同事务 assignOrgs（默认登录人有效机构）
  │     返回 id(String)
  │
  ├─ POST /role-info/assign-orgs        → assertRoleAccessible + 机构范围 S
  │     RoleOrgInfoServiceImp.assignOrgs（全量替换有效绑定）
  │
  ├─ POST /role-info/assign-permissions   → assertRoleAccessible
  │     RolePermissionInfoServiceImp.assignPermissions（旧 status=0，再 saveBatch）
  │     → PermissionContextCacheService.invalidateAll(绑定用户)
  │
  └─ POST /role-info/assign-users       → assertRoleGrantable（禁非超管授 super_super）
        RoleUserInfoServiceImp.assignUsersToRole（全量替换）
        → invalidate(旧∪新 userIds)
```

> PC `roleInfoDetail` / `authorizationDetail` 已按此契约调用；**不要**指望 `POST/PUT /role-info` 持久化 `permissionList`（服务层忽略该字段）。

### Flow 2 — 删除角色

```
DELETE /role-info?ids=
  → assertRoleAccessible
  → 若存在 status=有效 的用户绑定 → 拒绝
  → 同事务：失效 role-permission（及残留 role-user）→ invalidateAll → 逻辑删角色
```

### Flow 3 — 登录角色上下文（消费侧）

```
login → UserPermissionContextService.buildContext(userId)
  → getRoleInfoList(userId, true) + 全量有效菜单
  → 汇总 permissionCodes
  → roleCode==super_super → 全菜单；否则按 permissionCode 过滤
  → Redis 缓存 1h → 写入登录 VO
```

### Flow 4 — 用户维度绑角色（旁路）

用户增改：`TUserServiceImpl.syncUserRbacAssignments` → `assertRoleIdsGrantable` + `assignRoles` → `invalidate(userId)`。

---

## API Endpoints

### Base URL

- 服务内：`/api/v1/role-info` …
- 前端代理常见：`/api/am-user/role-info` …

### Authentication

- JWT 登录态（非白名单）
- **无** Controller 级 `@PreAuthorize`；细粒度靠前端 `v-permission` + 服务端 `@DataPermission` / 写守卫

### 角色主数据 — `/role-info`

| Method | Path | Auth / Guard | 说明 |
|--------|-------|--------------|------|
| POST | `/role-info/page` | 登录 + `@DataPermission(scope=ROLE_ORG_BOUND)` | 分页（非超管按机构绑定过滤） |
| GET | `/role-info?id=` | 登录 + scoped 查询 | 详情附带 `permissionList`、已绑权限、有效用户、`orgIds` |
| POST | `/role-info` | 登录；`assertRoleCodeUnique` + 机构 ∈ S（≥1） | 同事务 `assignOrgs`；返回 id `String` |
| PUT | `/role-info` | `assertRoleAccessible` + code 唯一 | 只更新主表 |
| DELETE | `/role-info?ids=` | 归属 + 有绑定用户则拒 | 级联失效关系 + 清缓存 |
| POST | `/role-info/assign-users` | `assertRoleGrantable` | Body: `{ roleId, userIds[] }` 全量替换 |
| POST | `/role-info/assign-permissions` | `assertRoleAccessible` | Body: `{ roleId, permissionIds[] }` 全量替换 |
| POST | `/role-info/assign-orgs` | `assertRoleAccessible` + 非超管 org ∈ S | Body: `{ roleId, orgIds[] }` 全量替换 |

### 关系表裸 CRUD（旁路风险）

| Group | Paths | 风险 |
|-------|-------|------|
| `/role-user-info` | page/GET/POST/PUT/DELETE | **无**归属校验、**无**缓存失效 |
| `/role-permission-info` | 同上 | 同上 |

PC/Mobile 关系页若改走裸 CRUD，会绕过 `assign*` 安全语义。

### Feign 缺口

`RoleInfoApi` 含 `assign-permissions`，**缺少** `assign-users`。

### Error Format

统一 `ResponseBody{ code, data, message }`（前端解构约定）。

---

## Database Schema（角色相关）

### Engine

MySQL（业务库 `alex_user` 语境）。

### `t_role_info`

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT | PK |
| role_code | VARCHAR | 业务唯一（服务层校验） |
| role_name | VARCHAR | |
| summary | VARCHAR | |
| status | VARCHAR/CHAR | 有效字典 `is_valid` |
| + BaseEntity | | creator/operator/is_delete … |

### `t_role_user_info`

| Column | Notes |
|--------|-------|
| role_id / user_id | **String** 存储（与 Long 主键存在 cast） |
| status | 有效/失效；`assignRoles` 会 prune 失效历史最多 5 条 |
| | `assignUsersToRole` **不做** prune |

### `t_role_permission_info`

| Column | Notes |
|--------|-------|
| role_id / permission_id | String |
| status | 全量替换时旧行置失效 |

### `t_role_org_info`（2026-08-19）

| Column | Notes |
|--------|-------|
| role_id / org_id | String；同一 `(role_id, org_id)` 有效行唯一 |
| status | `assignOrgs` 全量替换时旧有效行置 `0` |

DDL / 迁移：`docs/sql/2026-08-19-t_role_org_info.sql`、`docs/sql/2026-08-19-t_role_org_info_migrate.sql`（运维设置 `@fallback_org_id`）。

### 角色编码常量 — `RbacRoleCodes`

| Code | 含义 |
|------|------|
| `super_super` | 超管（全菜单；授予受限） |
| `admin` | 机构管理员 |
| `user` | 普通用户 |

**无** `t_role_menu`：菜单靠权限码间接关联。

### Relationships

```
t_role_info 1 ──* t_role_user_info → t_user
t_role_info 1 ──* t_role_permission_info → t_permission_info
t_role_info 1 ──* t_role_org_info → t_org_info
              ↘ (via permissionCode) MenuInfo 过滤
```

---

## Role-Org binding（角色-机构）

角色与机构为**多对多**；`t_role_info` **无** `org_id`。列表/详情可见性由 `DataPermissionScope.ROLE_ORG_BOUND` 控制：

| 身份 | 可见角色 |
|------|----------|
| `super_super` | 全部（含未绑定机构的角色，便于冷启动） |
| `admin` / `user` | 至少一条有效 `t_role_org_info` 且 `org_id ∈ S`（本机构或 admin 子孙） |

- **写路径**：`POST /role-info/assign-orgs`（全量替换）；创建角色同事务写入初始绑定。
- **守卫**：非超管不可绑范围外机构；`assign-users` 要求用户有效机构与角色绑定机构有交集。
- **迁移**：历史角色按 operator/creator 有效机构回填，否则 `@fallback_org_id`（配置项 `rbac.role-org.fallback-org-id`）。
- **按钮种子**：`docs/sql/2026-08-19-role-button-grants.md` 为 `admin` 角色授予 `role:add/edit/auth/delete`。

---

## Frontend：PC

### 路由

| Path | 用途 |
|------|------|
| `/#/user/roleInfo` | 动态菜单：角色列表 |
| `/#/user/role-user-info` | 静态隐藏：双栏用户分配 |
| （无独立授权路由） | `authorizationDetail` / `userAssignmentDetail` 为 Drawer |

### 关键页面

| 文件 | 职责 |
|------|------|
| `roleInfo/index.vue` | 搜索、分页、增删、打开 Drawer、跳转关系页 |
| `roleInfo/roleInfoDetail` | Modal 编辑 + **旧** `menu-tree` + 两阶段保存 |
| `roleInfo/authorizationDetail` | Drawer + **`RbacPermissionTreePanel`** + `assignRolePermissions` |
| `roleInfo/userAssignmentDetail` | Drawer + Transfer + `assignRoleUsers` |
| `roleUserInfo/index.vue` | DualList + DiffPreview + `assignRoleUsers` |

### 按钮权限码（UI）

`role:add` / `role:edit` / `role:delete` / `role:auth`（与菜单码 `user:roleInfo` 命名风格不一致，需与种子对齐）。

### PC Gaps（相对产品期望 / Midscene）

1. 列表无绑定用户数 / 权限数 / 更新时间列  
2. 授权 Drawer 无 `RbacDiffPreview`；`halfCheckedKeys` 恒空  
3. `roleInfoDetail` 仍用 `compoments/menu-tree`，与授权抽屉双轨  
4. `roleUserInfo/api` 闲置（页面正确走 `assignRoleUsers`）  
5. Detail / 用户分配 Drawer / rbac 公共组件 data-testid 不全  
6. 列表 `rowSelection` 仍有 `console.log` 残留  

---

## Frontend：Mobile

### 路由（动态菜单）

`/user/roleInfo`、`/user/roleUserInfo`、`/user/rolePermissionInfo`（及各自 Detail）。

### 模型差异

| 能力 | PC | Mobile |
|------|----|--------|
| 权限树 + assign-permissions | ✅ | ❌（手填 permissionId） |
| 全量用户 assign-users | ✅（Drawer + 独立页） | ❌（单条 role-user CRUD） |
| 按钮级 `v-permission` | ✅ | ❌ |
| 列表搜索 | ✅ | 搜索 UI 被注释 |
| 批量授权 UI | — | Toast「待接入」占位 |

---

## Security & Scope Checklist

| 项 | 状态 |
|----|------|
| 分页 `@DataPermission` | ✅ Role 页 `ROLE_ORG_BOUND`；RoleUser 页 |
| 详情 scoped 查询 | ✅ `queryRoleInfo` |
| 写归属 `assertRoleAccessible` | ✅ update/delete/assign* |
| 禁非超管授 `super_super` | ✅ `assertRoleGrantable` |
| roleCode 唯一 | ✅ |
| 删角色级联 + 绑定用户前置保护 | ✅ |
| assign 后清 `permission_context` | ✅ 主路径 |
| 关系表裸 CRUD 守卫 + 缓存 | ❌ 旁路 |
| Controller 方法级权限码强制 | ❌ |
| `addRoleInfo` 数据范围守卫 | ✅ 创建须绑 ≥1 机构且 ∈ S |
| 详情返回全量 `permissionList` | ⚠️ 面过大 |

---

## Tests（后端角色相关）

目录：`user_boot/src/test/java/com/alex/user/rbac/`

- `RoleOwnershipGuardTest`
- `RoleCodeUniquenessTest`
- `RoleDeleteCascadeTest`
- `RoleUserAssignmentServiceTest`
- `RolePermissionAssignmentServiceTest`
- `RoleOrgAssignmentServiceTest` / `RoleOrgBoundScopeTest` / `RoleOrgAssignApiGuardTest`
- `RoleUserHistoryPruneTest`
- `PermissionContextInvalidationTest`
- （旁路）`UserOwnershipGuardTest`、`UserPermissionContextServiceTest`、DataPermission 覆盖盘点

---

## Scorecard 对照（角色条目）

历史缺陷中与角色强相关、且 Batch1–3 已主路径修复的：

| ID | 主题 | 主路径现状 |
|-----|------|------------|
| RBAC-BE-ROLE-001 | 详情/写归属 | ✅ 守卫 + scoped |
| RBAC-BE-ROLE-002 | 删级联权限关系 | ✅ |
| RBAC-BE-ROLE-003 | roleCode 唯一 | ✅ |
| RBAC-PC-ROLE-001 | 授权复用 rbac 组件 | ✅ 授权 Drawer；Detail 仍旧树 |
| RBAC-PC-ROLE-003 | data-testid | 部分 ✅ |
| RBAC-MB-ROLE-* | 视觉/钩子 | Batch3/4 残留 |

仍打开的结构性风险：关系表裸 CRUD 双轨、Mobile 未接 assign API、Feign 缺 `assign-users`。

---

## Role-org binding 验收（spec §10，2026-08-19）

| # | 标准 | 状态 | 备注 |
|---|------|------|------|
| 1 | manager 列表非空 **或** 空态+新增可见 | ⚠️ Blocked（live） | 代码/单测 ✅；**须先**跑 DDL + migrate + button grants 并重登 |
| 2 | 同一角色可绑多机构 | ✅ 单测 | `RoleOrgAssignmentServiceTest` 覆盖全量替换 |
| 3 | 未绑定角色：非超管不可见 / 超管可见 | ✅ 单测 | `RoleOrgBoundScopeTest` |
| 4 | Banner 与过滤口径一致 | ✅ 代码 | PC banner「本机构及下级」+ `ROLE_ORG_BOUND` |
| 5 | assign-users 跨机构错配被拒 | ✅ 单测 | `RoleAssignUsersOrgIntersectionTest`（4） |

后端聚焦回归（`user_api` install 后）：`RoleOrg*` + `RoleAssignUsersOrgIntersection*` → **17 tests, 0 fail**。

运维顺序：`docs/sql/2026-08-19-t_role_org_info.sql` → `SET @fallback_org_id` → migrate → `docs/sql/2026-08-19-role-button-grants.md` → 重登。

---

## Recommended Next Steps（按优先级）

1. **封旁路**：关系表 Controller 写接口改为内部/弃用，或强制走 `assign*` + 缓存失效  
2. **PC 统一权限 UI**：`roleInfoDetail` 切到 `RbacPermissionTreePanel`；授权加 Diff  
3. **Mobile 对齐**：封装 `assign-users` / `assign-permissions` + 权限树 / 双栏选择器  
4. **补 Feign `assign-users`**  
5. **Batch4**：乱码、空状态、列表统计列、清理 `console.log`

---

## Related Docs

- `docs/testing/rbac-maturity-scorecard.md`
- `docs/superpowers/specs/2026-08-03-role-assign-permissions-design.md`
- `docs/superpowers/specs/2026-08-11-rbac-batch3-product-design.md`
- `docs/superpowers/plans/2026-08-11-rbac-batch3-s3.md`
