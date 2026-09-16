-- Idempotent backfill: t_role_org_info for existing roles
-- Prerequisite: docs/sql/2026-08-19-t_role_org_info.sql (DDL)
-- Spec: docs/superpowers/specs/2026-08-19-role-org-binding-design.md §6
--
-- Ops MUST set fallback root org before running (matches rbac.role-org.fallback-org-id):
--   SET @fallback_org_id = '1732681655123001345';
SET @fallback_org_id = NULL;

-- -----------------------------------------------------------------------------
-- 1) Roles without any valid org binding → operator/creator valid org, else fallback
-- -----------------------------------------------------------------------------
INSERT INTO `t_role_org_info`
(`id`, `role_id`, `org_id`, `summary`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleter`, `delete_time`, `is_delete`, `operator`, `operate_time`)
SELECT
  (2026081910000000000 + r.`id`) AS `id`,
  CAST(r.`id` AS CHAR) AS `role_id`,
  resolved.`org_id`,
  'migrate: operator/creator org or fallback' AS `summary`,
  '1' AS `status`,
  r.`creator`,
  NOW() AS `create_time`,
  NULL, NULL, NULL, NULL,
  0 AS `is_delete`,
  r.`operator`,
  NOW() AS `operate_time`
FROM `t_role_info` r
JOIN (
  SELECT
    r2.`id` AS `role_pk`,
    COALESCE(
      (SELECT ou.`org_id`
       FROM `t_org_user_info` ou
       WHERE ou.`user_id` = CAST(r2.`operator` AS CHAR)
         AND ou.`status` = '1'
         AND ou.`is_delete` = 0
       ORDER BY ou.`id`
       LIMIT 1),
      (SELECT ou.`org_id`
       FROM `t_org_user_info` ou
       WHERE ou.`user_id` = CAST(r2.`creator` AS CHAR)
         AND ou.`status` = '1'
         AND ou.`is_delete` = 0
       ORDER BY ou.`id`
       LIMIT 1),
      @fallback_org_id
    ) AS `org_id`
  FROM `t_role_info` r2
  WHERE r2.`is_delete` = 0
    AND r2.`status` = '1'
) resolved ON resolved.`role_pk` = r.`id`
WHERE r.`is_delete` = 0
  AND r.`status` = '1'
  AND resolved.`org_id` IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `t_role_org_info` roi
    WHERE roi.`role_id` = CAST(r.`id` AS CHAR)
      AND roi.`status` = '1'
      AND roi.`is_delete` = 0
  );

-- -----------------------------------------------------------------------------
-- 2) Built-in roles still unbound → force @fallback_org_id (requires step 0 SET)
-- -----------------------------------------------------------------------------
INSERT INTO `t_role_org_info`
(`id`, `role_id`, `org_id`, `summary`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleter`, `delete_time`, `is_delete`, `operator`, `operate_time`)
SELECT
  (2026081920000000000 + r.`id`) AS `id`,
  CAST(r.`id` AS CHAR) AS `role_id`,
  @fallback_org_id AS `org_id`,
  'migrate: built-in role fallback org' AS `summary`,
  '1' AS `status`,
  r.`creator`,
  NOW() AS `create_time`,
  NULL, NULL, NULL, NULL,
  0 AS `is_delete`,
  r.`operator`,
  NOW() AS `operate_time`
FROM `t_role_info` r
WHERE r.`is_delete` = 0
  AND r.`status` = '1'
  AND r.`role_code` IN ('super_super', 'admin', 'user')
  AND @fallback_org_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `t_role_org_info` roi
    WHERE roi.`role_id` = CAST(r.`id` AS CHAR)
      AND roi.`status` = '1'
      AND roi.`is_delete` = 0
  );

-- Optional yml (application-dev.yml):
-- rbac:
--   role-org:
--     fallback-org-id: '1732681655123001345'
