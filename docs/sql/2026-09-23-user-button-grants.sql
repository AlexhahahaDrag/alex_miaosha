-- ==============================================================================
-- 迁移脚本：用户管理操作按钮权限补齐与家庭管理员 (family_admin) / 组织用户管理员 (org_user_admin) / admin 角色赋权
-- 作用：
-- 1. 向 t_permission_info 插入 user:add, user:edit, user:delete 按钮级权限点（父节点: user:userManager）
-- 2. 向 t_role_permission_info 为 family_admin, org_user_admin, admin 角色赋予上述操作按钮权限
-- 特性：幂等安全执行，多次运行不会产生重复记录
-- ==============================================================================

USE alex_user;

-- 1. 确保按钮级权限元数据存在（父节点关联 user:userManager id=10）
INSERT INTO t_permission_info 
  (id, permission_code, permission_name, summary, status, is_delete, parent_id, create_time, operate_time)
SELECT 
  2026092300000000001, 'user:add', '新增用户', '用户管理页面新增用户按钮', '1', 0, 10, NOW(), NOW()
FROM DUAL 
WHERE NOT EXISTS (
  SELECT 1 FROM t_permission_info WHERE permission_code = 'user:add' AND is_delete = 0
);

INSERT INTO t_permission_info 
  (id, permission_code, permission_name, summary, status, is_delete, parent_id, create_time, operate_time)
SELECT 
  2026092300000000002, 'user:edit', '编辑用户', '用户管理页面编辑用户按钮', '1', 0, 10, NOW(), NOW()
FROM DUAL 
WHERE NOT EXISTS (
  SELECT 1 FROM t_permission_info WHERE permission_code = 'user:edit' AND is_delete = 0
);

INSERT INTO t_permission_info 
  (id, permission_code, permission_name, summary, status, is_delete, parent_id, create_time, operate_time)
SELECT 
  2026092300000000003, 'user:delete', '删除用户', '用户管理页面删除用户按钮', '1', 0, 10, NOW(), NOW()
FROM DUAL 
WHERE NOT EXISTS (
  SELECT 1 FROM t_permission_info WHERE permission_code = 'user:delete' AND is_delete = 0
);

-- 2. 幂等为 family_admin (家庭管理员), org_user_admin (组织用户管理员), admin 授予 user:add, user:edit, user:delete
INSERT INTO t_role_permission_info
  (id, role_id, permission_id, summary, status, is_delete, create_time, operate_time)
SELECT
  (2026092350000000000 + ri.id + pi.id) AS id,
  CAST(ri.id AS CHAR) AS role_id,
  CAST(pi.id AS CHAR) AS permission_id,
  CONCAT('migrate: grant ', pi.permission_code, ' to ', ri.role_code) AS summary,
  '1' AS status,
  0 AS is_delete,
  NOW(),
  NOW()
FROM t_permission_info pi
CROSS JOIN t_role_info ri
WHERE pi.permission_code IN ('user:add', 'user:edit', 'user:delete')
  AND pi.is_delete = 0
  AND pi.status = '1'
  AND ri.role_code IN ('family_admin', 'org_user_admin', 'admin')
  AND ri.is_delete = 0
  AND ri.status = '1'
  AND NOT EXISTS (
    SELECT 1
    FROM t_role_permission_info existing
    WHERE existing.role_id = CAST(ri.id AS CHAR)
      AND existing.permission_id = CAST(pi.id AS CHAR)
      AND existing.status = '1'
      AND existing.is_delete = 0
  );
