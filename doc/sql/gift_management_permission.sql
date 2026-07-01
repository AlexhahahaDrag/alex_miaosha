USE alex_user;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Gift page permissions
-- ----------------------------
INSERT INTO `t_permission_info`
(`id`, `permission_code`, `permission_name`, `summary`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleter`, `delete_time`, `is_delete`, `operator`, `operate_time`, `options`, `parent_id`)
VALUES
(1900000000000002001, 'gift:dashboard', '礼尚往来-数据概览', '礼尚往来数据概览页面权限', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, '/finance/gift/dashboard', 5),
(1900000000000002002, 'gift:person', '礼尚往来-亲友管理', '礼尚往来亲友管理页面权限', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, '/finance/gift/person', 5),
(1900000000000002003, 'gift:event', '礼尚往来-事由管理', '礼尚往来事由管理页面权限', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, '/finance/gift/event', 5),
(1900000000000002004, 'gift:record', '礼尚往来-礼金记录', '礼尚往来礼金记录页面权限', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, '/finance/gift/record', 5),
(1900000000000002005, 'gift:analysis', '礼尚往来-统计报表', '礼尚往来统计报表页面权限', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, '/finance/gift/analysis', 5);

-- ----------------------------
-- Gift action permissions
-- ----------------------------
INSERT INTO `t_permission_info`
(`id`, `permission_code`, `permission_name`, `summary`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleter`, `delete_time`, `is_delete`, `operator`, `operate_time`, `options`, `parent_id`)
VALUES
(1900000000000002011, 'gift:view', '礼尚往来-查看', '礼尚往来列表和详情查看权限', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL),
(1900000000000002012, 'gift:add', '礼尚往来-新增', '礼尚往来新增按钮权限', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL),
(1900000000000002013, 'gift:edit', '礼尚往来-编辑', '礼尚往来编辑按钮权限', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL),
(1900000000000002014, 'gift:delete', '礼尚往来-删除', '礼尚往来删除按钮权限', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL),
(1900000000000002015, 'gift:export', '礼尚往来-导出', '礼尚往来导出按钮权限', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Gift admin menu entries under finance menu id 5
-- ----------------------------
INSERT INTO `t_menu_info`
(`id`, `name`, `path`, `title`, `component`, `redirect`, `icon`, `hide_in_menu`, `parent_id`, `summary`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleter`, `delete_time`, `is_delete`, `operator`, `operate_time`, `order_by`, `show_in_home`, `permission_code`)
VALUES
(1900000000000001001, 'giftDashboard', '/finance/gift/dashboard', '数据概览', '/src/views/finance/gift/gift-dashboard/index.vue', NULL, 'financeAnalysis', '0', 5, '礼尚往来数据概览', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, 810, '1', 'gift:dashboard'),
(1900000000000001002, 'giftPerson', '/finance/gift/person', '亲友管理', '/src/views/finance/gift/person/index.vue', NULL, 'menuInfo', '0', 5, '礼尚往来亲友管理', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, 820, '1', 'gift:person'),
(1900000000000001003, 'giftEvent', '/finance/gift/event', '事由管理', '/src/views/finance/gift/event/index.vue', NULL, 'dict', '0', 5, '礼尚往来事由管理', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, 830, '1', 'gift:event'),
(1900000000000001004, 'giftRecord', '/finance/gift/record', '礼金记录', '/src/views/finance/gift/record/index.vue', NULL, 'finance', '0', 5, '礼尚往来礼金记录', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, 840, '1', 'gift:record'),
(1900000000000001005, 'giftAnalysis', '/finance/gift/analysis', '统计报表', '/src/views/finance/gift/analysis/index.vue', NULL, 'financeAnalysis', '0', 5, '礼尚往来统计报表', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, 850, '1', 'gift:analysis');

-- ----------------------------
-- Gift roles
-- ----------------------------
INSERT INTO `t_role_info`
(`id`, `role_code`, `role_name`, `summary`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleter`, `delete_time`, `is_delete`, `operator`, `operate_time`)
VALUES
(1900000000000004001, 'gift_admin', '人情管理', '礼尚往来管理角色，组织级数据权限', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL),
(1900000000000004002, 'gift_user', '人情用户', '礼尚往来普通用户角色，用户级数据权限', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL);

-- ----------------------------
-- gift_admin: all gift page and action permissions
-- ----------------------------
INSERT INTO `t_role_permission_info`
(`id`, `role_id`, `permission_id`, `summary`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleter`, `delete_time`, `is_delete`, `operator`, `operate_time`)
VALUES
(1900000000000005001, '1900000000000004001', '1900000000000002001', NULL, '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL),
(1900000000000005002, '1900000000000004001', '1900000000000002002', NULL, '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL),
(1900000000000005003, '1900000000000004001', '1900000000000002003', NULL, '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL),
(1900000000000005004, '1900000000000004001', '1900000000000002004', NULL, '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL),
(1900000000000005005, '1900000000000004001', '1900000000000002005', NULL, '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL),
(1900000000000005011, '1900000000000004001', '1900000000000002011', NULL, '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL),
(1900000000000005012, '1900000000000004001', '1900000000000002012', NULL, '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL),
(1900000000000005013, '1900000000000004001', '1900000000000002013', NULL, '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL),
(1900000000000005014, '1900000000000004001', '1900000000000002014', NULL, '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL),
(1900000000000005015, '1900000000000004001', '1900000000000002015', NULL, '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL);

-- ----------------------------
-- gift_user: core page permissions and data-maintenance actions, without export/analysis
-- ----------------------------
INSERT INTO `t_role_permission_info`
(`id`, `role_id`, `permission_id`, `summary`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleter`, `delete_time`, `is_delete`, `operator`, `operate_time`)
VALUES
(1900000000000006001, '1900000000000004002', '1900000000000002001', NULL, '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL),
(1900000000000006002, '1900000000000004002', '1900000000000002002', NULL, '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL),
(1900000000000006003, '1900000000000004002', '1900000000000002003', NULL, '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL),
(1900000000000006004, '1900000000000004002', '1900000000000002004', NULL, '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL),
(1900000000000006011, '1900000000000004002', '1900000000000002011', NULL, '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL),
(1900000000000006012, '1900000000000004002', '1900000000000002012', NULL, '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL),
(1900000000000006013, '1900000000000004002', '1900000000000002013', NULL, '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL),
(1900000000000006014, '1900000000000004002', '1900000000000002014', NULL, '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL);

-- ----------------------------
-- Gift menu regroup: move gift pages out of finance into a standalone top menu
-- ----------------------------
INSERT IGNORE INTO `t_permission_info`
(`id`, `permission_code`, `permission_name`, `summary`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleter`, `delete_time`, `is_delete`, `operator`, `operate_time`, `options`, `parent_id`)
VALUES
(1900000000000002000, 'gift', '礼尚往来管理', '礼尚往来管理一级菜单权限', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, '/finance/gift', 5);

UPDATE `t_permission_info`
SET `parent_id` = 1900000000000002000
WHERE `permission_code` IN ('gift:dashboard', 'gift:person', 'gift:event', 'gift:record', 'gift:analysis');

INSERT IGNORE INTO `t_menu_info`
(`id`, `name`, `path`, `title`, `component`, `redirect`, `icon`, `hide_in_menu`, `parent_id`, `summary`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleter`, `delete_time`, `is_delete`, `operator`, `operate_time`, `order_by`, `show_in_home`, `permission_code`)
VALUES
(1900000000000001000, 'gift', '/finance/gift', '礼尚往来管理', 'Layout', '/finance/gift/dashboard', 'finance', '0', NULL, '礼尚往来管理', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, 35, '0', 'gift');

UPDATE `t_menu_info`
SET `parent_id` = 1900000000000001000
WHERE `name` IN ('giftDashboard', 'giftPerson', 'giftEvent', 'giftRecord', 'giftAnalysis');

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

INSERT IGNORE INTO `t_role_permission_info`
(`id`, `role_id`, `permission_id`, `summary`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleter`, `delete_time`, `is_delete`, `operator`, `operate_time`)
VALUES
(1900000000000005000, '1900000000000004001', '1900000000000002000', NULL, '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL),
(1900000000000006000, '1900000000000004002', '1900000000000002000', NULL, '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;
