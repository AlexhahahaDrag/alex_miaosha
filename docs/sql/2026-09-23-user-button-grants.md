# 用户管理操作按钮授权（家庭管理员 family_admin 与 组织用户管理员 org_user_admin）

> 迁移记录与执行说明 — **基于 RBAC 原生权限点**

为 `family_admin`（家庭管理员）、`org_user_admin`（组织用户管理员）及 `admin` 角色授予 `user:add`、`user:edit`、`user:delete` 按钮权限，使非超管管理员在用户管理界面正常显示新增、编辑、删除等操作按钮。

## 一、前置条件与检查

### 1. 验证用户管理页面父菜单节点是否存在

```sql
SELECT id, permission_code, permission_name, status
FROM t_permission_info
WHERE permission_code = 'user:userManager'
  AND is_delete = 0;
```
返回 id 为 `10`。

### 2. 检查按钮权限是否已存在

```sql
SELECT id, permission_code, permission_name, status
FROM t_permission_info
WHERE permission_code IN ('user:add', 'user:edit', 'user:delete')
  AND is_delete = 0;
```

---

## 二、执行迁移 SQL

执行同目录下的脚本文件：`docs/sql/2026-09-23-user-button-grants.sql`。

---

## 三、后置验证与缓存刷新

### 1. 验证角色权限绑定

```sql
SELECT ri.role_code, ri.role_name, pi.permission_code, pi.permission_name
FROM t_role_permission_info rpi
JOIN t_permission_info pi ON CAST(pi.id AS CHAR) = rpi.permission_id
JOIN t_role_info ri ON ri.id = CAST(rpi.role_id AS UNSIGNED)
WHERE ri.role_code IN ('family_admin', 'org_user_admin', 'admin')
  AND pi.permission_code IN ('user:add', 'user:edit', 'user:delete')
  AND rpi.status = '1'
  AND rpi.is_delete = 0;
```

预期：查询到 6 条记录（`family_admin` 与 `org_user_admin` 各拥有 `user:add`, `user:edit`, `user:delete`）。

### 2. 刷新权限缓存

执行完 SQL 授权后，受影响的管理员用户重新登录即可加载最新权限点。
