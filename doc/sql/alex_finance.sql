CREATE DATABASE IF NOT EXISTS alex_finance;
USE alex_finance;
/*
 Navicat Premium Data Transfer

 Source Server         : 10.10.20.38
 Source Server Type    : MySQL
 Source Server Version : 80200 (8.2.0)
 Source Host           : 10.10.20.38:3336
 Source Schema         : alex_finance

 Target Server Type    : MySQL
 Target Server Version : 80200 (8.2.0)
 File Encoding         : 65001

 Date: 25/03/2024 21:55:01
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for account_record_info
-- ----------------------------
DROP TABLE IF EXISTS `account_record_info`;
CREATE TABLE `account_record_info`  (
  `id` bigint NOT NULL COMMENT 'id',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `avli_date` timestamp NULL DEFAULT NULL COMMENT '有效期',
  `amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '金额',
  `account` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账号',
  `is_send` tinyint NULL DEFAULT NULL COMMENT '是否发送提醒',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for account_record_info_test
-- ----------------------------
DROP TABLE IF EXISTS `account_record_info_test`;
CREATE TABLE `account_record_info_test`  (
  `id` bigint NOT NULL COMMENT 'id',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `avli_date` timestamp NULL DEFAULT NULL COMMENT '有效期',
  `amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '金额',
  `account` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账号',
  `is_send` tinyint NULL DEFAULT NULL COMMENT '是否发送提醒',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for dict_info
-- ----------------------------
DROP TABLE IF EXISTS `dict_info`;
CREATE TABLE `dict_info`  (
  `id` bigint NOT NULL COMMENT 'id',
  `type_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '类别编码',
  `type_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '类别',
  `belong_to` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '分类编码',
  `belong_to_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '分类',
  `order_by` int NULL DEFAULT NULL COMMENT '排序',
  `is_valid` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '是否有效',
  `is_delete` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '是否删除',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updater` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleter` bigint NULL DEFAULT NULL COMMENT '删除人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `delete_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `operator` bigint NULL DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `dict_info_type_code_IDX`(`type_code` ASC, `belong_to` ASC, `is_valid` ASC, `is_delete` ASC, `order_by` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '字典表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for finance_base_info
-- ----------------------------
DROP TABLE IF EXISTS `finance_base_info`;
CREATE TABLE `finance_base_info`  (
  `id` bigint NOT NULL COMMENT 'id',
  `amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '钱数',
  `from_source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '来源',
  `is_valid` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '是否有效',
  `is_delete` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '是否删除',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updater` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleter` bigint NULL DEFAULT NULL COMMENT '删除人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `delete_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `operator` bigint NULL DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  `belong_to` bigint NULL DEFAULT NULL COMMENT '属于',
  `info_date` datetime NULL DEFAULT NULL COMMENT '业务日期',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `finance_base_info_from_source_IDX`(`from_source` ASC, `is_valid` ASC, `is_delete` ASC, `belong_to` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '财务表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for finance_info
-- ----------------------------
DROP TABLE IF EXISTS `finance_info`;
CREATE TABLE `finance_info`  (
  `id` bigint NOT NULL COMMENT 'id',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '名称',
  `type_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '类别',
  `amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '钱数',
  `from_source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '来源',
  `income_and_expenses` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '收入类型',
  `is_valid` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '是否有效',
  `is_delete` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '是否删除',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updater` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleter` bigint NULL DEFAULT NULL COMMENT '删除人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `delete_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `operator` bigint NULL DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  `belong_to` bigint NULL DEFAULT NULL COMMENT '属于',
  `info_date` datetime NULL DEFAULT NULL COMMENT '业务日期',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `finance_status_info_date_IDX`(`is_valid` ASC, `is_delete` ASC, `info_date` ASC) USING BTREE,
  INDEX `finance_info_from_source_IDX`(`from_source` ASC, `belong_to` ASC, `is_valid` ASC, `is_delete` ASC, `info_date` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '财务表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_shop_finance
-- ----------------------------
DROP TABLE IF EXISTS `t_shop_finance`;
CREATE TABLE `t_shop_finance`  (
  `id` bigint NOT NULL COMMENT 'id',
  `shop_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '商品名称',
  `shop_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '商品编码',
  `sale_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '售价',
  `is_valid` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '是否有效',
  `is_delete` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '是否删除',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updater` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleter` bigint NULL DEFAULT NULL COMMENT '删除人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `delete_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `operator` bigint NULL DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  `sale_date` datetime NULL DEFAULT NULL COMMENT '销售日期',
  `pay_way` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '支付方式',
  `income_and_expenses` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '收支类型',
  `sale_num` decimal(10, 0) NULL DEFAULT NULL COMMENT '个数',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '商店财务表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_shop_order
-- ----------------------------
DROP TABLE IF EXISTS `t_shop_order`;
CREATE TABLE `t_shop_order`  (
  `id` bigint NOT NULL COMMENT 'id',
  `sale_order_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '订单编码',
  `sale_order_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '订单名称',
  `sale_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '总销售金额',
  `is_valid` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '状态,字典(is_valid) 1：有效,0:失效)',
  `is_delete` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '是否删除',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updater` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleter` bigint NULL DEFAULT NULL COMMENT '删除人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `delete_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `operator` bigint NULL DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  `sale_date` datetime NULL DEFAULT NULL COMMENT '销售日期',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '描述',
  `pay_way` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '支付方式',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '商店订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_shop_order_detail
-- ----------------------------
DROP TABLE IF EXISTS `t_shop_order_detail`;
CREATE TABLE `t_shop_order_detail`  (
  `id` bigint NOT NULL COMMENT 'id',
  `order_id` bigint NOT NULL COMMENT '订单id',
  `shop_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '商品名称',
  `shop_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '商品编码',
  `sale_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '售价',
  `is_valid` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '是否有效',
  `is_delete` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '是否删除',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updater` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleter` bigint NULL DEFAULT NULL COMMENT '删除人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `delete_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `operator` bigint NULL DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  `sale_date` datetime NULL DEFAULT NULL COMMENT '销售日期',
  `pay_way` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '支付方式',
  `sale_num` decimal(10, 0) NULL DEFAULT NULL COMMENT '个数',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '商店订单明细表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_shop_stock
-- ----------------------------
DROP TABLE IF EXISTS `t_shop_stock`;
CREATE TABLE `t_shop_stock`  (
  `id` bigint NOT NULL COMMENT 'id',
  `shop_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '商品名称',
  `shop_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '商品编码',
  `cost_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '成本价',
  `sale_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '零售价',
  `is_valid` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '状态,字典(is_valid) 1：有效,0:失效)',
  `is_delete` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '是否删除',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updater` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleter` bigint NULL DEFAULT NULL COMMENT '删除人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `delete_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `operator` bigint NULL DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  `sale_date` datetime NULL DEFAULT NULL COMMENT '进货日期',
  `category` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '类别,字典(shop_category)',
  `purchase_place` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '进货地点,字典(stock_place) ',
  `sale_num` decimal(10, 0) NULL DEFAULT NULL COMMENT '数量',
  `old_shop_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '原商品编码',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '描述',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '商店库存表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for finance_budget_info
-- ----------------------------
DROP TABLE IF EXISTS `finance_budget_info`;
CREATE TABLE `finance_budget_info`  (
  `id` bigint NOT NULL COMMENT 'id',
  `org_id` bigint NOT NULL DEFAULT 20 COMMENT '家庭组/机构ID',
  `belong_to` bigint NULL DEFAULT NULL COMMENT '属于(用户ID，预留家庭组下特定个人)',
  `budget_month` varchar(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '预算月份(格式: YYYY-MM)',
  `income_and_expenses` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT 'expense' COMMENT '收支类型(expense:支出, income:收入)',
  `budget_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '月度零花钱预算金额',
  `category_codes` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '纳入统计的支出类别编码列表(逗号分隔，为空表示统计全部非转账支出)',
  `is_valid` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '1' COMMENT '是否有效(1:有效 0:无效)',
  `is_delete` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '0' COMMENT '是否删除(0:未删除 1:已删除)',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updater` bigint NULL DEFAULT NULL COMMENT '更新人',
  `deleter` bigint NULL DEFAULT NULL COMMENT '删除人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `delete_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `operator` bigint NULL DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `finance_budget_org_month_IDX`(`org_id` ASC, `budget_month` ASC, `is_delete` ASC) USING BTREE,
  INDEX `finance_budget_belong_month_IDX`(`belong_to` ASC, `budget_month` ASC, `is_delete` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '财务月度零花钱与分类预算表' ROW_FORMAT = DYNAMIC;

-- =============================================================================
-- 礼尚往来管理模块 (Gift) 领域模型与数据字典 (2026-09-28)
-- =============================================================================

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

-- ----------------------------
-- Table structure for gift_event_type_option_t
-- ----------------------------
DROP TABLE IF EXISTS `gift_event_type_option_t`;
CREATE TABLE `gift_event_type_option_t` (
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

INSERT INTO `gift_event_type_option_t` (
  `id`, `org_id`, `user_id`, `option_type`, `event_code`, `event_label`, `sort_order`, `last_used_time`, `is_delete`, `create_time`
) VALUES
(9100000000000000001, 0, 0, 'SYSTEM', 'WEDDING', '婚礼', 1, NOW(), 0, NOW()),
(9100000000000000002, 0, 0, 'SYSTEM', 'BIRTH', '满月', 2, NOW(), 0, NOW()),
(9100000000000000003, 0, 0, 'SYSTEM', 'HOUSEWARMING', '乔迁', 3, NOW(), 0, NOW()),
(9100000000000000004, 0, 0, 'SYSTEM', 'EDUCATION', '升学', 4, NOW(), 0, NOW()),
(9100000000000000005, 0, 0, 'SYSTEM', 'BIRTHDAY', '寿宴', 5, NOW(), 0, NOW()),
(9100000000000000006, 0, 0, 'SYSTEM', 'OTHER', '其他', 6, NOW(), 0, NOW());

-- ----------------------------
-- 下线关系表 gift_relation_info_t (关系语义由 gift_person_info_t.relation_type + gift_person_relation_option_t 承载)
-- ----------------------------
DROP TABLE IF EXISTS `gift_relation_info_t`;

-- ----------------------------
-- Table structure for gift_event_type_user_config_t
-- ----------------------------
CREATE TABLE IF NOT EXISTS `gift_event_type_user_config_t` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `option_id` bigint NOT NULL COMMENT '关联的事由分类选项ID',
  `org_id` bigint DEFAULT NULL COMMENT '机构/家庭组ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '启用状态(1:启用, 0:停用)',
  `custom_amount` decimal(10,2) DEFAULT NULL COMMENT '个性化推荐金额',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `operator` bigint DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '操作时间',
  `deleter` bigint DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  PRIMARY KEY (`id`),
  KEY `idx_org_option` (`org_id`, `option_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='事由分类个性化配置表';

-- ----------------------------
-- Table structure for sys_message_info_t
-- ----------------------------
CREATE TABLE IF NOT EXISTS `sys_message_info_t` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `org_id` bigint NULL DEFAULT NULL COMMENT '所属机构ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '接收用户ID',
  `msg_type` varchar(32) NOT NULL COMMENT '消息大类：SYSTEM / GIFT_EVENT / COUPON_EXPIRE / CARD_EXPIRE',
  `sub_type` varchar(32) NULL DEFAULT NULL COMMENT '子类型：WELCOME / ANNOUNCE / D_DAY / D_MINUS_1 / D_MINUS_3',
  `priority` varchar(16) NOT NULL DEFAULT 'NORMAL' COMMENT '优先级：HIGH / MEDIUM / NORMAL',
  `title` varchar(200) NOT NULL COMMENT '消息标题',
  `content` varchar(1000) NOT NULL COMMENT '消息正文',
  `biz_id` varchar(64) NULL DEFAULT NULL COMMENT '关联业务主键ID',
  `biz_type` varchar(32) NULL DEFAULT NULL COMMENT '关联业务类型：GIFT_EVENT / COUPON / ACCOUNT_RECORD / PREPAID_CARD / SYSTEM',
  `jump_url` varchar(255) NULL DEFAULT NULL COMMENT '跳转路由路径',
  `read_status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '阅读状态：0-未读，1-已读',
  `read_time` datetime NULL DEFAULT NULL COMMENT '阅读时间',
  `dedup_key` varchar(128) NULL DEFAULT NULL COMMENT '幂等防重键',
  `remind_date` date NULL DEFAULT NULL COMMENT '提醒日期',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `operator` bigint NULL DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `deleter` bigint NULL DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删，1-已删',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_message_dedup` (`dedup_key`),
  KEY `idx_sys_message_user_read` (`user_id`, `read_status`, `is_delete`, `create_time`),
  KEY `idx_sys_message_org_type` (`org_id`, `msg_type`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统消息与业务到期提醒表';

-- ----------------------------
-- Table structure for user_notice_config_t
-- ----------------------------
CREATE TABLE IF NOT EXISTS `user_notice_config_t` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `notice_code` varchar(32) NOT NULL COMMENT '业务类型编码 (对应枚举 NoticeBizType)',
  `internal_enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否接收站内信：1-是，0-否',
  `wechat_enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否接收微信消息：1-是，0-否',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_notice_code` (`user_id`, `notice_code`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户个性化消息渠道开关配置表';

SET FOREIGN_KEY_CHECKS = 1;
