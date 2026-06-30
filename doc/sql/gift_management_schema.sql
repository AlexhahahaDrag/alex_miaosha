CREATE DATABASE IF NOT EXISTS alex_finance;
USE alex_finance;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for gift_person_info_t
-- ----------------------------
DROP TABLE IF EXISTS `gift_person_info_t`;
CREATE TABLE `gift_person_info_t` (
  `id` bigint NOT NULL COMMENT '主键',
  `org_id` bigint NOT NULL COMMENT '组织ID',
  `user_id` bigint NOT NULL COMMENT '归属用户ID',
  `bind_user_id` bigint NULL DEFAULT NULL COMMENT '绑定系统用户ID',
  `person_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '人员姓名',
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '手机号',
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
DROP TABLE IF EXISTS `gift_relation_info_t`;
CREATE TABLE `gift_relation_info_t` (
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
DROP TABLE IF EXISTS `gift_event_info_t`;
CREATE TABLE `gift_event_info_t` (
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
DROP TABLE IF EXISTS `gift_record_info_t`;
CREATE TABLE `gift_record_info_t` (
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
DROP TABLE IF EXISTS `gift_person_relation_option_t`;
CREATE TABLE `gift_person_relation_option_t` (
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

INSERT INTO `gift_person_relation_option_t` (
  `id`, `org_id`, `user_id`, `option_type`, `relation_code`, `relation_label`, `sort_order`, `last_used_time`, `is_delete`, `create_time`
) VALUES
(9000000000000000001, 0, 0, 'SYSTEM', 'RELATIVE', '亲属', 1, NOW(), 0, NOW()),
(9000000000000000002, 0, 0, 'SYSTEM', 'FRIEND', '朋友', 2, NOW(), 0, NOW()),
(9000000000000000003, 0, 0, 'SYSTEM', 'COLLEAGUE', '同事', 3, NOW(), 0, NOW()),
(9000000000000000004, 0, 0, 'SYSTEM', 'NEIGHBOR', '邻里', 4, NOW(), 0, NOW()),
(9000000000000000005, 0, 0, 'SYSTEM', 'OTHER', '其他', 5, NOW(), 0, NOW());

SET FOREIGN_KEY_CHECKS = 1;
