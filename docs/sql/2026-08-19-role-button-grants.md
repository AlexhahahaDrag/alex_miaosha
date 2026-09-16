# Role management button grants (admin role)

> Task 6 · Spec §6.3 · Manual dev/prod execution — **do not auto-apply**

Grants `role:add`, `role:edit`, `role:auth`, `role:delete` to the org-admin role (`t_role_info.role_code = 'admin'`) so `rbac_user_manager` persona sees action buttons after role-org bindings exist.

## Prerequisites

1. Button permission rows exist in `t_permission_info` with `permission_code` in:
   `role:add`, `role:edit`, `role:auth`, `role:delete`
2. Target role row exists: `role_code = 'admin'` (adjust `@admin_role_id` if your seed differs).

## Verify permission rows

```sql
SELECT id, permission_code, permission_name, status
FROM t_permission_info
WHERE permission_code IN ('role:add', 'role:edit', 'role:auth', 'role:delete')
  AND is_delete = 0;
```

If empty, create button permissions first (menu parent `user:roleInfo` / id `1746531627376271361` in dev seed), then run grants below.

## Grant SQL (idempotent)

```sql
-- Resolve admin role id (override if needed)
SET @admin_role_id = (
  SELECT CAST(id AS CHAR)
  FROM t_role_info
  WHERE role_code = 'admin'
    AND is_delete = 0
    AND status = '1'
  LIMIT 1
);

INSERT INTO t_role_permission_info
(id, role_id, permission_id, summary, status, creator, create_time, updater, update_time, deleter, delete_time, is_delete, operator, operate_time)
SELECT
  (2026081930000000000 + pi.id) AS id,
  @admin_role_id AS role_id,
  CAST(pi.id AS CHAR) AS permission_id,
  'migrate: role management buttons' AS summary,
  '1' AS status,
  NULL, NOW(), NULL, NULL, NULL, NULL,
  0 AS is_delete,
  NULL, NOW()
FROM t_permission_info pi
WHERE pi.permission_code IN ('role:add', 'role:edit', 'role:auth', 'role:delete')
  AND pi.is_delete = 0
  AND pi.status = '1'
  AND @admin_role_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM t_role_permission_info existing
    WHERE existing.role_id = @admin_role_id
      AND existing.permission_id = CAST(pi.id AS CHAR)
      AND existing.status = '1'
      AND existing.is_delete = 0
  );
```

## Post-check

```sql
SELECT pi.permission_code
FROM t_role_permission_info rpi
JOIN t_permission_info pi ON CAST(pi.id AS CHAR) = rpi.permission_id
JOIN t_role_info ri ON ri.id = CAST(rpi.role_id AS UNSIGNED)
WHERE ri.role_code = 'admin'
  AND pi.permission_code LIKE 'role:%'
  AND rpi.status = '1'
  AND rpi.is_delete = 0;
```

Expected: four rows (`role:add`, `role:edit`, `role:auth`, `role:delete`).  
After grants, invalidate affected users' `permission_context:{userId}` Redis keys or re-login.
