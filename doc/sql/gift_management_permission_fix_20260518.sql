USE alex_user;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- Align gift_user with the confirmed permission scope:
-- allowed: gift:view, gift:add, gift:edit, gift:delete
-- denied: gift:export, gift:analysis
SET @gift_admin_role_id := '1900000000000004001';
SET @gift_user_role_id := '1900000000000004002';

DELETE rpi
FROM `t_role_permission_info` rpi
JOIN `t_permission_info` pi ON CAST(pi.`id` AS CHAR) = rpi.`permission_id`
WHERE rpi.`role_id` = @gift_user_role_id
  AND pi.`permission_code` IN ('gift:export', 'gift:analysis');

INSERT INTO `t_role_permission_info`
(`id`, `role_id`, `permission_id`, `summary`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleter`, `delete_time`, `is_delete`, `operator`, `operate_time`)
SELECT 1900000000000005005, @gift_admin_role_id, CAST(pi.`id` AS CHAR), NULL, '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL
FROM `t_permission_info` pi
WHERE pi.`permission_code` = 'gift:analysis'
  AND NOT EXISTS (
    SELECT 1
    FROM `t_role_permission_info` existing
    WHERE existing.`role_id` = @gift_admin_role_id
      AND existing.`permission_id` = CAST(pi.`id` AS CHAR)
  );

INSERT INTO `t_role_permission_info`
(`id`, `role_id`, `permission_id`, `summary`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleter`, `delete_time`, `is_delete`, `operator`, `operate_time`)
SELECT 1900000000000005015, @gift_admin_role_id, CAST(pi.`id` AS CHAR), NULL, '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL
FROM `t_permission_info` pi
WHERE pi.`permission_code` = 'gift:export'
  AND NOT EXISTS (
    SELECT 1
    FROM `t_role_permission_info` existing
    WHERE existing.`role_id` = @gift_admin_role_id
      AND existing.`permission_id` = CAST(pi.`id` AS CHAR)
  );

INSERT INTO `t_role_permission_info`
(`id`, `role_id`, `permission_id`, `summary`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleter`, `delete_time`, `is_delete`, `operator`, `operate_time`)
SELECT 1900000000000006014, @gift_user_role_id, CAST(pi.`id` AS CHAR), NULL, '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL
FROM `t_permission_info` pi
WHERE pi.`permission_code` = 'gift:delete'
  AND NOT EXISTS (
    SELECT 1
    FROM `t_role_permission_info` existing
    WHERE existing.`role_id` = @gift_user_role_id
      AND existing.`permission_id` = CAST(pi.`id` AS CHAR)
  );

SET FOREIGN_KEY_CHECKS = 1;
