-- =============================================================================
-- alex_finance_gift_management.sql
-- 礼尚往来：表结构 + 权限/角色/菜单（含移动端亲友详情隐藏路由）
-- 合并自：
--   gift_management_schema.sql
--   gift_management_permission.sql
--   gift_management_permission_fix_20260518.sql
--   gift_management_menu_fix_20260519.sql
--   gift_person_detail_menu_20260720.sql
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Schema (alex_finance)
-- -----------------------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS alex_finance;
USE alex_finance;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for gift_person_info_t
-- ----------------------------
DROP TABLE IF EXISTS `alex_finance`.`gift_person_info_t`;
CREATE TABLE `alex_finance`.`gift_person_info_t` (
  `id` bigint NOT NULL COMMENT '主键',
  `org_id` bigint NOT NULL COMMENT '组织ID',
  `user_id` bigint NOT NULL COMMENT '归属用户ID',
  `bind_user_id` bigint NULL DEFAULT NULL COMMENT '绑定系统用户ID',
  `person_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '人员姓名',
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '手机号',
  `avatar` bigint NULL DEFAULT NULL COMMENT '头像 OSS 文件ID',
  `relation_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '关系类型',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updater` bigint NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `operator` bigint NULL DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  `deleter` bigint NULL DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_gift_person_org_user_name`(`org_id` ASC, `user_id` ASC, `person_name` ASC) USING BTREE,
  INDEX `idx_gift_person_org_phone`(`org_id` ASC, `phone` ASC) USING BTREE,
  INDEX `idx_gift_person_bind_user`(`org_id` ASC, `bind_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '礼尚往来人员表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for gift_relation_info_t
-- ----------------------------
DROP TABLE IF EXISTS `alex_finance`.`gift_relation_info_t`;
CREATE TABLE `alex_finance`.`gift_relation_info_t` (
  `id` bigint NOT NULL COMMENT '主键',
  `org_id` bigint NOT NULL COMMENT '组织ID',
  `user_id` bigint NOT NULL COMMENT '归属用户ID',
  `person_id` bigint NOT NULL COMMENT '人员ID',
  `relation_person_id` bigint NOT NULL COMMENT '关联人员ID',
  `relation_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '关系类型',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updater` bigint NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `operator` bigint NULL DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  `deleter` bigint NULL DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_gift_relation_org_user_person`(`org_id` ASC, `user_id` ASC, `person_id` ASC) USING BTREE,
  INDEX `idx_gift_relation_org_relation_person`(`org_id` ASC, `relation_person_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '礼尚往来关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for gift_event_info_t
-- ----------------------------
DROP TABLE IF EXISTS `alex_finance`.`gift_event_info_t`;
CREATE TABLE `alex_finance`.`gift_event_info_t` (
  `id` bigint NOT NULL COMMENT '主键',
  `org_id` bigint NOT NULL COMMENT '组织ID',
  `user_id` bigint NOT NULL COMMENT '归属用户ID',
  `event_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '事件名称',
  `event_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '事件类型',
  `event_time` datetime NULL DEFAULT NULL COMMENT '事件时间',
  `host_person_id` bigint NULL DEFAULT NULL COMMENT '主办人员ID',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updater` bigint NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `operator` bigint NULL DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  `deleter` bigint NULL DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_gift_event_org_user_time`(`org_id` ASC, `user_id` ASC, `event_time` ASC) USING BTREE,
  INDEX `idx_gift_event_org_type_time`(`org_id` ASC, `event_type` ASC, `event_time` ASC) USING BTREE,
  INDEX `idx_gift_event_host_person`(`org_id` ASC, `host_person_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '礼尚往来事件表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for gift_record_info_t
-- ----------------------------
DROP TABLE IF EXISTS `alex_finance`.`gift_record_info_t`;
CREATE TABLE `alex_finance`.`gift_record_info_t` (
  `id` bigint NOT NULL COMMENT '主键',
  `org_id` bigint NOT NULL COMMENT '组织ID',
  `user_id` bigint NOT NULL COMMENT '归属用户ID',
  `event_id` bigint NULL DEFAULT NULL COMMENT '事件ID',
  `giver_person_id` bigint NOT NULL COMMENT '送礼人员ID',
  `receiver_person_id` bigint NOT NULL COMMENT '收礼人员ID',
  `related_record_id` bigint NULL DEFAULT NULL COMMENT '关联原始收礼记录ID',
  `direction` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '礼金方向：GIVE/RECEIVE/RETURN',
  `amount` decimal(12, 2) NOT NULL COMMENT '金额',
  `pay_time` datetime NOT NULL COMMENT '礼金时间',
  `returned_flag` tinyint NULL DEFAULT 0 COMMENT '是否已回礼',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updater` bigint NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `operator` bigint NULL DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  `deleter` bigint NULL DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_gift_record_org_pay_time`(`org_id` ASC, `pay_time` ASC, `id` ASC) USING BTREE,
  INDEX `idx_gift_record_org_user_pay_time`(`org_id` ASC, `user_id` ASC, `pay_time` ASC, `id` ASC) USING BTREE,
  INDEX `idx_gift_record_event_direction_time`(`org_id` ASC, `event_id` ASC, `direction` ASC, `pay_time` ASC) USING BTREE,
  INDEX `idx_gift_record_giver_time`(`org_id` ASC, `giver_person_id` ASC, `pay_time` ASC) USING BTREE,
  INDEX `idx_gift_record_receiver_time`(`org_id` ASC, `receiver_person_id` ASC, `pay_time` ASC) USING BTREE,
  INDEX `idx_gift_record_related`(`org_id` ASC, `related_record_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '礼尚往来礼金记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for gift_person_relation_option_t
-- ----------------------------
DROP TABLE IF EXISTS `alex_finance`.`gift_person_relation_option_t`;
CREATE TABLE `alex_finance`.`gift_person_relation_option_t` (
  `id` bigint NOT NULL COMMENT '主键',
  `org_id` bigint NOT NULL COMMENT '组织ID',
  `user_id` bigint NOT NULL COMMENT '归属用户ID，系统预设为0',
  `option_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT 'CUSTOM' COMMENT 'SYSTEM|CUSTOM',
  `relation_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '预设code',
  `relation_label` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '关系展示文案',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `last_used_time` datetime NULL DEFAULT NULL COMMENT '最近使用时间',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updater` bigint NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `operator` bigint NULL DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  `deleter` bigint NULL DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_gift_person_relation_option_user_label`(`user_id` ASC, `relation_label` ASC) USING BTREE,
  UNIQUE INDEX `uk_gift_person_relation_option_system_code`(`user_id` ASC, `relation_code` ASC) USING BTREE,
  INDEX `idx_gift_person_relation_option_user_time`(`user_id` ASC, `last_used_time` DESC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '礼尚往来联系人关系词典' ROW_FORMAT = DYNAMIC;

INSERT INTO `alex_finance`.`gift_person_relation_option_t` (
  `id`, `org_id`, `user_id`, `option_type`, `relation_code`, `relation_label`, `sort_order`, `last_used_time`, `is_delete`, `create_time`
) VALUES
(9000000000000000001, 0, 0, 'SYSTEM', 'RELATIVE', '亲属', 1, NOW(), 0, NOW()),
(9000000000000000002, 0, 0, 'SYSTEM', 'FRIEND', '朋友', 2, NOW(), 0, NOW()),
(9000000000000000003, 0, 0, 'SYSTEM', 'COLLEAGUE', '同事', 3, NOW(), 0, NOW()),
(9000000000000000004, 0, 0, 'SYSTEM', 'NEIGHBOR', '邻里', 4, NOW(), 0, NOW()),
(9000000000000000005, 0, 0, 'SYSTEM', 'OTHER', '其他', 5, NOW(), 0, NOW());

-- ----------------------------
-- Table structure for gift_event_type_option_t
-- ----------------------------
DROP TABLE IF EXISTS `alex_finance`.`gift_event_type_option_t`;
CREATE TABLE `alex_finance`.`gift_event_type_option_t` (
  `id` bigint NOT NULL COMMENT '主键',
  `org_id` bigint NOT NULL COMMENT '组织ID，系统预设为0',
  `user_id` bigint NOT NULL COMMENT '创建用户ID，系统预设为0',
  `option_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT 'CUSTOM' COMMENT 'SYSTEM|CUSTOM',
  `event_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '预设code',
  `event_label` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '类型展示文案',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `last_used_time` datetime NULL DEFAULT NULL COMMENT '最近使用时间',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updater` bigint NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `operator` bigint NULL DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  `deleter` bigint NULL DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_gift_event_type_option_org_label`(`org_id` ASC, `event_label` ASC) USING BTREE,
  UNIQUE INDEX `uk_gift_event_type_option_system_code`(`user_id` ASC, `event_code` ASC) USING BTREE,
  INDEX `idx_gift_event_type_option_org_time`(`org_id` ASC, `last_used_time` DESC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '礼尚往来事由类型词典' ROW_FORMAT = DYNAMIC;

INSERT INTO `alex_finance`.`gift_event_type_option_t` (
  `id`, `org_id`, `user_id`, `option_type`, `event_code`, `event_label`, `sort_order`, `last_used_time`, `is_delete`, `create_time`
) VALUES
(9100000000000000001, 0, 0, 'SYSTEM', 'WEDDING', '婚礼', 1, NOW(), 0, NOW()),
(9100000000000000002, 0, 0, 'SYSTEM', 'BIRTH', '满月', 2, NOW(), 0, NOW()),
(9100000000000000003, 0, 0, 'SYSTEM', 'HOUSEWARMING', '乔迁', 3, NOW(), 0, NOW()),
(9100000000000000004, 0, 0, 'SYSTEM', 'EDUCATION', '升学', 4, NOW(), 0, NOW()),
(9100000000000000005, 0, 0, 'SYSTEM', 'BIRTHDAY', '寿宴', 5, NOW(), 0, NOW()),
(9100000000000000006, 0, 0, 'SYSTEM', 'OTHER', '其他', 6, NOW(), 0, NOW());

SET FOREIGN_KEY_CHECKS = 1;

-- -----------------------------------------------------------------------------
-- 2. Permissions / Menus / Roles (alex_user)
-- -----------------------------------------------------------------------------
USE alex_user;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

SET @gift_menu_id := 1900000000000001000;
SET @gift_permission_id := 1900000000000002000;
SET @gift_admin_role_id := '1900000000000004001';
SET @gift_user_role_id := '1900000000000004002';

-- ----------------------------
-- Gift page permissions
-- ----------------------------
INSERT IGNORE INTO `t_permission_info`
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
INSERT IGNORE INTO `t_permission_info`
(`id`, `permission_code`, `permission_name`, `summary`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleter`, `delete_time`, `is_delete`, `operator`, `operate_time`, `options`, `parent_id`)
VALUES
(1900000000000002011, 'gift:view', '礼尚往来-查看', '礼尚往来列表和详情查看权限', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL),
(1900000000000002012, 'gift:add', '礼尚往来-新增', '礼尚往来新增按钮权限', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL),
(1900000000000002013, 'gift:edit', '礼尚往来-编辑', '礼尚往来编辑按钮权限', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL),
(1900000000000002014, 'gift:delete', '礼尚往来-删除', '礼尚往来删除按钮权限', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL),
(1900000000000002015, 'gift:export', '礼尚往来-导出', '礼尚往来导出按钮权限', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Gift page menus (initially under finance parent_id=5, then regrouped below)
-- ----------------------------
INSERT IGNORE INTO `t_menu_info`
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
INSERT IGNORE INTO `t_role_info`
(`id`, `role_code`, `role_name`, `summary`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleter`, `delete_time`, `is_delete`, `operator`, `operate_time`)
VALUES
(1900000000000004001, 'gift_admin', '人情管理', '礼尚往来管理角色，组织级数据权限', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL),
(1900000000000004002, 'gift_user', '人情用户', '礼尚往来普通用户角色，用户级数据权限', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL);

-- ----------------------------
-- gift_admin: all gift page and action permissions
-- ----------------------------
INSERT IGNORE INTO `t_role_permission_info`
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
-- gift_user: core pages + view/add/edit/delete（无 export/analysis）
-- ----------------------------
INSERT IGNORE INTO `t_role_permission_info`
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

-- -----------------------------------------------------------------------------
-- 3. Top-level menu regroup（礼尚往来独立一级菜单）
-- -----------------------------------------------------------------------------
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

-- -----------------------------------------------------------------------------
-- 4. Role permission scope fix
-- gift_user: 去掉 export/analysis；确保 gift:delete
-- gift_admin: 确保 analysis/export
-- -----------------------------------------------------------------------------
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
    SELECT 1 FROM `t_role_permission_info` existing
    WHERE existing.`role_id` = @gift_admin_role_id
      AND existing.`permission_id` = CAST(pi.`id` AS CHAR)
  );

INSERT INTO `t_role_permission_info`
(`id`, `role_id`, `permission_id`, `summary`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleter`, `delete_time`, `is_delete`, `operator`, `operate_time`)
SELECT 1900000000000005015, @gift_admin_role_id, CAST(pi.`id` AS CHAR), NULL, '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL
FROM `t_permission_info` pi
WHERE pi.`permission_code` = 'gift:export'
  AND NOT EXISTS (
    SELECT 1 FROM `t_role_permission_info` existing
    WHERE existing.`role_id` = @gift_admin_role_id
      AND existing.`permission_id` = CAST(pi.`id` AS CHAR)
  );

INSERT INTO `t_role_permission_info`
(`id`, `role_id`, `permission_id`, `summary`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleter`, `delete_time`, `is_delete`, `operator`, `operate_time`)
SELECT 1900000000000006014, @gift_user_role_id, CAST(pi.`id` AS CHAR), NULL, '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL
FROM `t_permission_info` pi
WHERE pi.`permission_code` = 'gift:delete'
  AND NOT EXISTS (
    SELECT 1 FROM `t_role_permission_info` existing
    WHERE existing.`role_id` = @gift_user_role_id
      AND existing.`permission_id` = CAST(pi.`id` AS CHAR)
  );

-- -----------------------------------------------------------------------------
-- 5. Mobile 亲友详情隐藏菜单（修复新增 404）
-- -----------------------------------------------------------------------------
INSERT INTO `t_menu_info`
(`id`, `name`, `path`, `title`, `component`, `redirect`, `icon`, `hide_in_menu`, `parent_id`, `summary`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleter`, `delete_time`, `is_delete`, `operator`, `operate_time`, `order_by`, `show_in_home`, `permission_code`)
SELECT
  1900000000000001012,
  'giftPersonDetail',
  '/finance/gift/person/giftPersonDetail',
  '亲友管理详情',
  '/src/views/finance/gift/person/giftPersonDetail/index.vue',
  NULL,
  'giftPersonDetail',
  '1',
  @gift_menu_id,
  '礼尚往来亲友管理详情',
  '1',
  NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL,
  21,
  '0',
  'gift:person'
WHERE NOT EXISTS (
  SELECT 1 FROM `t_menu_info` WHERE `name` = 'giftPersonDetail'
);

SET FOREIGN_KEY_CHECKS = 1;
