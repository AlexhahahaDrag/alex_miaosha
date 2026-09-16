# 角色↔机构多对多绑定设计（锁定）

> 日期：2026-08-19  
> 状态：**已锁定**（用户确认路径甲；实现计划见 `docs/superpowers/plans/2026-08-19-role-org-binding.md`）  
> 前置：ux-audit r2 `C-EMPTY`；产品选型 A（角色为机构侧可管资产）且拒绝角色表单字段 `org_id`  
> 相关：`docs/ROLE_MANAGEMENT.md`、`docs/superpowers/specs/2026-08-11-rbac-batch3-product-design.md`

## 1. 问题陈述

当前角色列表 `@DataPermission(field=operator, scope=USER_IDS)`，前端 Banner 却写「仅本人所属机构」。  
机构管理员（`rbac_user_manager`）打开角色管理常见：**表空 + 无 `role:add`**，授权/分用户无法开始。

根因是产品假设「管机构侧角色」与「按 operator 个人归属」不一致。  
约束：**同一角色必须能挂到多个机构**，因此禁止在 `t_role_info` 上加单一 `org_id`。

## 2. 锁定决策

| #   | 问题                                      | 锁定                                                           |
| --- | ----------------------------------------- | -------------------------------------------------------------- |
| 1   | 产品定位                                  | 机构侧可管「本机构范围内可见的角色」                           |
| 2   | 角色表 `org_id`                           | **不做**                                                       |
| 3   | 机构挂接                                  | **角色 ↔ 机构多对多**（`t_role_org_info`）                     |
| 4   | 内置角色 `super_super` / `admin` / `user` | **同样必须显式绑机构**才对非超管可见                           |
| 5   | 落地路径                                  | **甲**：关系表 + 列表 SQL 按机构范围过滤（禁止应用层全量再滤） |

## 3. 数据模型

### 3.1 新表 `t_role_org_info`

对齐 `t_role_user_info` 风格：

| 列         | 说明                                     |
| ---------- | ---------------------------------------- |
| id         | PK                                       |
| role_id    | 角色 id（String 存储，与现网关系表一致） |
| org_id     | 机构 id（String）                        |
| summary    | 可选描述                                 |
| status     | `is_valid`：1 有效 / 0 失效              |
| BaseEntity | creator/operator/is_delete/…             |

唯一性（有效行）：同一 `(role_id, org_id)` 最多一条 `status=1 AND is_delete=0`。

### 3.2 不变

- `t_role_info` 结构不变（无 `org_id`）
- `t_role_user_info` / `t_role_permission_info` 语义不变（仍全量替换 assign）

## 4. 可见性与数据权限

### 4.1 机构范围（与 Batch3 对齐）

| 身份               | 机构范围 `S`                            |
| ------------------ | --------------------------------------- |
| 超管 `super_super` | 不过滤（见 4.2）                        |
| 机构管理员 `admin` | 本机构 ∪ 全部子孙（`OrgSubtreeLookup`） |
| 普通 `user`        | 仅本机构（若开放只读角色页）            |

### 4.2 角色列表/详情可见条件

- **超管**：全部角色（**含未绑定任何机构的角色**，保证冷启动与治理）
- **非超管**：角色至少存在一条有效绑定，且 `org_id ∈ S`

实现偏好：

- 将 `RoleInfoMapper.getPage` / `queryRoleInfo` 从 `USER_IDS(operator)` 改为基于绑定表的过滤（新 scope 或 EXISTS 子查询），保证分页总数正确。
- **禁止**「先无权限查全表再内存过滤」。

### 4.3 Banner 文案（PC）

与真实过滤一致（修正误导）：

| 身份      | 文案                       |
| --------- | -------------------------- |
| 超管      | 全部                       |
| admin     | 本机构及下级               |
| 普通 user | 仅本人所属机构（若可进页） |

## 5. API 与写路径

### 5.1 新增

- `POST /role-info/assign-orgs`  
  Body：`{ roleId: string, orgIds: string[] }`  
  语义：**全量替换**有效绑定（旧有效 → status=0，再写入新有效行），与 `assign-users` / `assign-permissions` 一致。

- Feign：`RoleInfoApi` 同步补 `assign-orgs`（及既有缺口 `assign-users` 可同批补齐，非本设计阻断项）。

### 5.2 创建角色

1. `assertRoleCodeUnique`
2. insert `t_role_info`
3. **必须**至少绑定 1 个机构；每个 `org_id` 必须 ∈ 调用方可管范围 `S`
   - 默认：当前登录用户有效机构
   - PC：创建表单增加机构多选（至少一项）
4. 同一事务内完成角色行 + 初始绑定

### 5.3 更新 / 删除 / 授权 / 分用户

- `assertRoleAccessible`：**非超管**改为「角色在 4.2 意义下可见」，不再仅依赖 `operator` 归属。
- `assertRoleGrantable`：保留「非超管禁授 `super_super`」；并要求角色对调用方可见。
- `assign-users` 新增约束：每个 `userId` 的**当前有效机构**必须与该角色的有效绑定机构集合有交集；否则拒绝并返回明确文案。
- 删除角色：现有「有有效用户绑定则拒」+ 级联失效权限/用户关系；**同事务失效 `t_role_org_info`**。

### 5.4 旁路

- `/role-org-info` 若提供裸 CRUD：与角色权限关系表相同策略——**写路径应收敛到 `assign-orgs`**，或仅内部使用并挂守卫+缓存策略（本设计要求：对外写以 `assign-orgs` 为准）。

## 6. 种子与迁移

1. 建表 DDL + 实体/Mapper/Service（模式复用 role-user）。
2. 数据迁移：
   - 为现有角色补绑定：优先绑到「创建人/operator 当时有效机构」；若无法解析，绑到约定**根机构**或运维指定机构（实现计划里写死常量/配置项）。
   - 内置三角色 `super_super` / `admin` / `user`：**同样写入绑定**（用户锁定选项 2）；至少绑根机构，避免非超管环境「系统角色全灭」。
3. 权限种子：给机构管理员角色授予 `role:add` / `role:edit` / `role:auth` / `role:delete`（按现网按钮码），否则仅改可见性仍会出现「有数据无按钮」。

## 7. 前端（PC 优先）

| 面           | 变更                                                                       |
| ------------ | -------------------------------------------------------------------------- |
| 列表 Banner  | 按 §4.3                                                                    |
| 新建/编辑    | 机构多选；保存后调 `assign-orgs`（或创建接口服务端一次性写入）             |
| 空态         | 「当前机构范围内暂无角色。可新建并绑定机构，或请超管将角色绑定到本机构。」 |
| 关系页选角色 | 下拉数据源走同一可见性（已绑本范围的角色）                                 |
| 授权 Diff    | 非本设计阻断；可留后续                                                     |

移动端：本设计 **非目标**（二期对齐 assign-orgs + 机构选择器）。

## 8. 测试要点

| 层   | 用例                                                                                             |
| ---- | ------------------------------------------------------------------------------------------------ |
| 单测 | `assign-orgs` 全量替换；非超管不可绑范围外机构；`assign-users` 机构交集校验；删角色失效 org 绑定 |
| 集成 | admin 只看见绑到本机构/子孙的角色；超管看见未绑定角色；内置角色未绑时非超管不可见                |
| PC   | manager persona：有绑定后列表非空；可新建（有 `role:add`）；Banner 文案正确                      |
| 回归 | 既有 `RoleOwnershipGuard*` / 缓存失效用例适配新 `assertRoleAccessible`                           |

## 9. 非目标

- 角色表增加 `org_id`
- 应用层假分页过滤
- 本批次移动端完整对齐
- 废除 `assign-*` 改回关系表裸 CRUD 为主路径
- Batch4 纯视觉/axe 壳层（可另开）

## 10. 验收标准（Done）

1. `rbac_user_manager` 在角色已正确绑到其机构后，角色列表**非空**（或空态文案正确且有新增入口）。
2. 同一角色可同时绑定机构 A、B；A 的 admin 与 B 的 admin 均可见。
3. 未绑定任何机构的自定义角色：非超管不可见；超管可见。
4. Banner 与过滤口径一致（不再出现「仅本人」却按机构资产运营的错位）。
5. `assign-users` 跨机构错配被拒。

## 11. 实现顺序建议（供后续 plan）

1. DDL + 实体 + `assign-orgs` + 迁移/种子
2. 替换角色 Mapper 数据权限 + 守卫改造 + 测试
3. PC 表单/Banner/空态 + 按钮权限种子
4. ux-audit 切片重跑（manager → 角色管理）

---

**下一步：** 实现计划已就绪 → `docs/superpowers/plans/2026-08-19-role-org-binding.md`。选定执行方式（Subagent-Driven / Inline）后再改代码。
