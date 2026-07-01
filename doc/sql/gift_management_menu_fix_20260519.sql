USE alex_user;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- Move gift pages from the finance menu into a standalone top-level menu.
SET @gift_menu_id := 1900000000000001000;
SET @gift_permission_id := 1900000000000002000;
SET @gift_admin_role_id := '1900000000000004001';
SET @gift_user_role_id := '1900000000000004002';

INSERT INTO `t_permission_info`
(`id`, `permission_code`, `permission_name`, `summary`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleter`, `delete_time`, `is_delete`, `operator`, `operate_time`, `options`, `parent_id`)
SELECT @gift_permission_id, 'gift', '礼尚往来管理', '礼尚往来管理一级菜单权限', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, '/finance/gift', 5
WHERE NOT EXISTS (
  SELECT 1 FROM `t_permission_info` WHERE `id` = @gift_permission_id
);

UPDATE `t_permission_info`
SET `parent_id` = @gift_permission_id
WHERE `permission_code` IN ('gift:dashboard', 'gift:person', 'gift:event', 'gift:record', 'gift:analysis');

INSERT INTO `t_menu_info`
(`id`, `name`, `path`, `title`, `component`, `redirect`, `icon`, `hide_in_menu`, `parent_id`, `summary`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleter`, `delete_time`, `is_delete`, `operator`, `operate_time`, `order_by`, `show_in_home`, `permission_code`)
SELECT @gift_menu_id, 'gift', '/finance/gift', '礼尚往来管理', 'Layout', '/finance/gift/dashboard', 'finance', '0', NULL, '礼尚往来管理', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, 35, '0', 'gift'
WHERE NOT EXISTS (
  SELECT 1 FROM `t_menu_info` WHERE `id` = @gift_menu_id
);

UPDATE `t_menu_info`
SET `parent_id` = @gift_menu_id
WHERE `name` IN ('giftDashboard', 'giftPerson', 'giftEvent', 'giftRecord', 'giftAnalysis');

UPDATE `t_menu_info`
SET `component` = '/src/views/finance/gift/gift-dashboard/index.vue'
WHERE `name` = 'giftDashboard';

UPDATE `t_menu_info`
SET `order_by` = CASE `name`
  WHEN 'giftDashboard' THEN 10
  WHEN 'giftPerson' THEN 20
  WHEN 'giftEvent' THEN 30
  WHEN 'giftRecord' THEN 40
  WHEN 'giftAnalysis' THEN 50
  ELSE `order_by`
END
WHERE `name` IN ('giftDashboard', 'giftPerson', 'giftEvent', 'giftRecord', 'giftAnalysis');

INSERT INTO `t_role_permission_info`
(`id`, `role_id`, `permission_id`, `summary`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleter`, `delete_time`, `is_delete`, `operator`, `operate_time`)
SELECT 1900000000000005000, @gift_admin_role_id, CAST(@gift_permission_id AS CHAR), NULL, '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL
WHERE NOT EXISTS (
  SELECT 1 FROM `t_role_permission_info`
  WHERE `role_id` = @gift_admin_role_id
    AND `permission_id` = CAST(@gift_permission_id AS CHAR)
);

INSERT INTO `t_role_permission_info`
(`id`, `role_id`, `permission_id`, `summary`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleter`, `delete_time`, `is_delete`, `operator`, `operate_time`)
SELECT 1900000000000006000, @gift_user_role_id, CAST(@gift_permission_id AS CHAR), NULL, '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL
WHERE NOT EXISTS (
  SELECT 1 FROM `t_role_permission_info`
  WHERE `role_id` = @gift_user_role_id
    AND `permission_id` = CAST(@gift_permission_id AS CHAR)
);

SET FOREIGN_KEY_CHECKS = 1;
