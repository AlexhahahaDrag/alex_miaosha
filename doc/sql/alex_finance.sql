/*
 Navicat Premium Data Transfer

 Source Server         : huawei_mysql_3336
 Source Server Type    : MySQL
 Source Server Version : 80031 (8.0.31)
 Source Host           : 123.249.83.33:3336
 Source Schema         : alex_finance

 Target Server Type    : MySQL
 Target Server Version : 80031 (8.0.31)
 File Encoding         : 65001

 Date: 10/01/2023 11:03:02
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '字典表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of dict_info
-- ----------------------------
INSERT INTO `dict_info` VALUES (23, 'jd', '京东', 'pay_way', '支付方式', 45, '1', '0', NULL, NULL, NULL, '2022-10-18 21:47:46', NULL, NULL, NULL, '2022-10-18 21:47:49');
INSERT INTO `dict_info` VALUES (51, 'income', '收入', 'income_expense_type', '收支类型', 10, '1', '0', NULL, NULL, NULL, '2022-10-19 22:37:09', NULL, NULL, NULL, '2022-10-19 22:37:12');
INSERT INTO `dict_info` VALUES (52, 'expense', '支出', 'income_expense_type', '收支类型', 10, '1', '0', NULL, NULL, NULL, '2022-10-19 22:37:09', NULL, NULL, NULL, '2022-10-19 22:37:12');
INSERT INTO `dict_info` VALUES (53, 'dfw', '电费w', 'pay_way', '支付方式', 49, '1', '0', NULL, NULL, NULL, '2022-10-11 21:55:41', NULL, NULL, NULL, '2022-10-11 21:55:41');
INSERT INTO `dict_info` VALUES (54, 'sf', '水费', 'pay_way', '支付方式', 52, '1', '0', NULL, NULL, NULL, '2022-10-11 21:55:41', NULL, NULL, NULL, '2022-10-11 21:55:41');
INSERT INTO `dict_info` VALUES (55, 'rqf', '燃气费', 'pay_way', '支付方式', 55, '1', '0', NULL, NULL, NULL, '2022-10-11 21:55:41', NULL, NULL, NULL, '2022-10-11 21:55:41');
INSERT INTO `dict_info` VALUES (56, 'bt', '白条', 'pay_way', '支付方式', 48, '1', '0', NULL, NULL, NULL, '2022-10-11 21:55:41', NULL, NULL, NULL, '2022-10-11 21:55:41');
INSERT INTO `dict_info` VALUES (100, 'hfw', '话费w', 'pay_way', '支付方式', 200, '1', '0', NULL, NULL, NULL, '2022-10-11 21:55:41', NULL, NULL, NULL, '2022-10-11 21:55:41');
INSERT INTO `dict_info` VALUES (101, 'hfm', '话费m', 'pay_way', '支付方式', 210, '1', '0', NULL, NULL, NULL, '2022-10-11 21:55:41', NULL, NULL, NULL, '2022-10-11 21:55:41');
INSERT INTO `dict_info` VALUES (102, 'hfb', '话费b', 'pay_way', '支付方式', 220, '1', '0', NULL, NULL, NULL, '2022-10-11 21:55:41', NULL, NULL, NULL, '2022-10-11 21:55:41');
INSERT INTO `dict_info` VALUES (103, 'hfmj', '话费mj', 'pay_way', '支付方式', 230, '1', '0', NULL, NULL, NULL, '2022-10-11 21:55:41', NULL, NULL, NULL, '2022-10-11 21:55:41');
INSERT INTO `dict_info` VALUES (104, 'dfm', '电费m', 'pay_way', '支付方式', 51, '1', '0', NULL, NULL, NULL, '2022-10-11 21:55:41', NULL, NULL, NULL, '2022-10-11 21:55:41');
INSERT INTO `dict_info` VALUES (105, 'hb', '花呗', 'pay_way', '支付方式', 42, '1', '0', NULL, NULL, NULL, '2022-10-11 21:55:41', NULL, NULL, NULL, '2022-10-11 21:55:41');
INSERT INTO `dict_info` VALUES (157983311610776371, 'mt', '美团', 'pay_way', '支付方式', 50, '1', '1', NULL, NULL, NULL, '2022-10-11 21:55:41', NULL, NULL, NULL, '2022-10-11 21:55:41');
INSERT INTO `dict_info` VALUES (1579830582328774657, 'wx', '微信', 'pay_way', '支付方式', 40, '1', '0', NULL, NULL, NULL, '2022-10-11 21:45:37', NULL, NULL, NULL, '2022-10-11 21:45:37');
INSERT INTO `dict_info` VALUES (1579832088037806082, 'zfb', '支付宝', 'pay_way', '支付方式', 30, '1', '0', NULL, NULL, NULL, '2022-10-11 21:51:36', NULL, NULL, NULL, '2022-10-11 21:51:36');
INSERT INTO `dict_info` VALUES (1579832177447784449, 'xj', '现金', 'pay_way', '支付方式', 10, '1', '0', NULL, NULL, NULL, '2022-10-11 21:51:58', NULL, NULL, NULL, '2022-10-11 21:51:58');
INSERT INTO `dict_info` VALUES (1579833116107763714, 'yhk', '银行卡', 'pay_way', '支付方式', 20, '1', '0', NULL, NULL, NULL, '2022-10-11 21:55:41', NULL, NULL, NULL, '2022-10-11 21:55:41');

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
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '财务表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of finance_base_info
-- ----------------------------
INSERT INTO `finance_base_info` VALUES (1, 100, 'zfb', '1', '0', NULL, NULL, NULL, '2022-10-31 07:28:25', NULL, NULL, NULL, '2022-10-31 07:28:25', 2, '2022-10-31 07:28:25');
INSERT INTO `finance_base_info` VALUES (2, 100, 'wx', '1', '0', NULL, NULL, NULL, '2022-10-31 07:28:25', NULL, NULL, NULL, '2022-10-31 07:28:25', 2, '2022-10-31 07:28:25');
INSERT INTO `finance_base_info` VALUES (3, 100, 'yhk', '1', '0', NULL, NULL, NULL, '2022-10-31 07:28:25', NULL, NULL, NULL, '2022-10-31 07:28:25', 2, '2022-10-31 07:28:25');
INSERT INTO `finance_base_info` VALUES (4, 100, 'jd', '1', '0', NULL, NULL, NULL, '2022-10-31 07:28:25', NULL, NULL, NULL, '2022-10-31 07:28:25', 2, '2022-10-31 07:28:25');
INSERT INTO `finance_base_info` VALUES (5, 100, 'sf', '1', '0', NULL, NULL, NULL, '2022-10-31 07:28:25', NULL, NULL, NULL, '2022-10-31 07:28:25', 2, '2022-10-31 07:28:25');
INSERT INTO `finance_base_info` VALUES (6, 100, 'rqf', '1', '0', NULL, NULL, NULL, '2022-10-31 07:28:25', NULL, NULL, NULL, '2022-10-31 07:28:25', 2, '2022-10-31 07:28:25');
INSERT INTO `finance_base_info` VALUES (7, 100, 'dfw', '1', '0', NULL, NULL, NULL, '2022-10-31 07:28:25', NULL, NULL, NULL, '2022-10-31 07:28:25', 2, '2022-10-31 07:28:25');
INSERT INTO `finance_base_info` VALUES (8, 100, 'dfm', '1', '0', NULL, NULL, NULL, '2022-10-31 07:28:25', NULL, NULL, NULL, '2022-10-31 07:28:25', 2, '2022-10-31 07:28:25');
INSERT INTO `finance_base_info` VALUES (9, 100, 'bt', '1', '0', NULL, NULL, NULL, '2022-10-31 07:28:25', NULL, NULL, NULL, '2022-10-31 07:28:25', 2, '2022-10-31 07:28:25');
INSERT INTO `finance_base_info` VALUES (10, 100, 'hfw', '1', '0', NULL, NULL, NULL, '2022-10-31 07:28:25', NULL, NULL, NULL, '2022-10-31 07:28:25', 2, '2022-10-31 07:28:25');
INSERT INTO `finance_base_info` VALUES (11, 100, 'hfm', '1', '0', NULL, NULL, NULL, '2022-10-31 07:28:25', NULL, NULL, NULL, '2022-10-31 07:28:25', 2, '2022-10-31 07:28:25');
INSERT INTO `finance_base_info` VALUES (12, 100, 'hfb', '1', '0', NULL, NULL, NULL, '2022-10-31 07:28:25', NULL, NULL, NULL, '2022-10-31 07:28:25', 2, '2022-10-31 07:28:25');
INSERT INTO `finance_base_info` VALUES (13, 100, 'hfmj', '1', '0', NULL, NULL, NULL, '2022-10-31 07:28:25', NULL, NULL, NULL, '2022-10-31 07:28:25', 2, '2022-10-31 07:28:25');
INSERT INTO `finance_base_info` VALUES (14, 100, 'xj', '1', '0', NULL, NULL, NULL, '2022-10-31 07:28:25', NULL, NULL, NULL, '2022-10-31 07:28:25', 2, '2022-10-31 07:28:25');
INSERT INTO `finance_base_info` VALUES (20, 100, 'zfb', '1', '0', NULL, NULL, NULL, '2022-10-31 07:28:25', NULL, NULL, NULL, '2022-10-31 07:28:25', 1, '2022-10-31 07:28:25');
INSERT INTO `finance_base_info` VALUES (21, 100, 'wx', '1', '0', NULL, NULL, NULL, '2022-10-31 07:28:25', NULL, NULL, NULL, '2022-10-31 07:28:25', 1, '2022-10-31 07:28:25');
INSERT INTO `finance_base_info` VALUES (22, 100, 'yhk', '1', '0', NULL, NULL, NULL, '2022-10-31 07:28:25', NULL, NULL, NULL, '2022-10-31 07:28:25', 1, '2022-10-31 07:28:25');
INSERT INTO `finance_base_info` VALUES (23, 0.00, 'hb', '1', '0', NULL, NULL, NULL, '2022-10-31 07:28:25', NULL, NULL, NULL, '2022-10-31 07:28:25', 1, '2022-10-31 07:28:25');
INSERT INTO `finance_base_info` VALUES (24, 0.00, 'hb', '1', '0', NULL, NULL, NULL, '2022-10-31 07:28:25', NULL, NULL, NULL, '2022-10-31 07:28:25', 2, '2022-10-31 07:28:25');

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
 PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '财务表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of finance_info
-- ----------------------------
SET FOREIGN_KEY_CHECKS = 1;
