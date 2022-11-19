/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80020
 Source Host           : localhost:3306
 Source Schema         : alex_finance

 Target Server Type    : MySQL
 Target Server Version : 80020
 File Encoding         : 65001

 Date: 19/11/2022 13:08:01
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
INSERT INTO `dict_info` VALUES (23, 'other', '其他', 'pay_way', '支付方式', 60, '1', '0', NULL, NULL, NULL, '2022-10-18 21:47:46', NULL, NULL, NULL, '2022-10-18 21:47:49');
INSERT INTO `dict_info` VALUES (51, 'income', '收入', 'income_expense_type', '收支类型', 10, '1', '0', NULL, NULL, NULL, '2022-10-19 22:37:09', NULL, NULL, NULL, '2022-10-19 22:37:12');
INSERT INTO `dict_info` VALUES (52, 'expense', '支出', 'income_expense_type', '收支类型', 10, '1', '0', NULL, NULL, NULL, '2022-10-19 22:37:09', NULL, NULL, NULL, '2022-10-19 22:37:12');
INSERT INTO `dict_info` VALUES (53, 'df', '电费', 'pay_way', '支付方式', 51, '1', '0', NULL, NULL, NULL, '2022-10-11 21:55:41', NULL, NULL, NULL, '2022-10-11 21:55:41');
INSERT INTO `dict_info` VALUES (54, 'sf', '水费', 'pay_way', '支付方式', 52, '1', '0', NULL, NULL, NULL, '2022-10-11 21:55:41', NULL, NULL, NULL, '2022-10-11 21:55:41');
INSERT INTO `dict_info` VALUES (55, 'rqf', '燃气费', 'pay_way', '支付方式', 55, '1', '0', NULL, NULL, NULL, '2022-10-11 21:55:41', NULL, NULL, NULL, '2022-10-11 21:55:41');
INSERT INTO `dict_info` VALUES (56, 'bt', '白条', 'pay_way', '支付方式', 56, '1', '0', NULL, NULL, NULL, '2022-10-11 21:55:41', NULL, NULL, NULL, '2022-10-11 21:55:41');
INSERT INTO `dict_info` VALUES (157983311610776371, 'mt', '美团', 'pay_way', '支付方式', 50, '1', '0', NULL, NULL, NULL, '2022-10-11 21:55:41', NULL, NULL, NULL, '2022-10-11 21:55:41');
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
INSERT INTO `finance_base_info` VALUES (1, 12113.61, 'zfb', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-09 00:00:00');
INSERT INTO `finance_base_info` VALUES (2, 492.52, 'wx', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-09 00:00:00');
INSERT INTO `finance_base_info` VALUES (3, 59.35, 'yhk', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-09 00:00:00');
INSERT INTO `finance_base_info` VALUES (4, 0.00, 'jd', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-09 00:00:00');
INSERT INTO `finance_base_info` VALUES (5, -33.50, 'sf', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-10 00:00:00');
INSERT INTO `finance_base_info` VALUES (6, 2.00, 'rqf', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-11 00:00:00');
INSERT INTO `finance_base_info` VALUES (7, 0.00, 'dfw', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-11 00:00:00');
INSERT INTO `finance_base_info` VALUES (8, 0.00, 'dfm', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-11 00:00:00');
INSERT INTO `finance_base_info` VALUES (9, -899.39, 'bt', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-11 00:00:00');

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
INSERT INTO `finance_info` VALUES (1582876350685904898, '买东西', '妈妈驿站', 14.90, 'wx', 'expense', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-09 00:00:00');
INSERT INTO `finance_info` VALUES (1582876350723653633, '饭补', '工资', 10.00, 'wx', 'income', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-09 00:00:00');
INSERT INTO `finance_info` VALUES (1582876350723653634, '红包', '红包', 3.25, 'wx', 'income', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-09 00:00:00');
INSERT INTO `finance_info` VALUES (1582876350794956801, '红包', '红包', 0.07, 'wx', 'income', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-09 00:00:00');
INSERT INTO `finance_info` VALUES (1582876350794956802, '红包', '红包', 0.15, 'wx', 'income', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-10 00:00:00');
INSERT INTO `finance_info` VALUES (1582876350794956803, '红包', '红包', 2.31, 'wx', 'income', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-11 00:00:00');
INSERT INTO `finance_info` VALUES (1582876350794956804, '买东西', '妈妈驿站', 9.90, 'wx', 'expense', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-11 00:00:00');
INSERT INTO `finance_info` VALUES (1582876350853677057, '美团', '美团', 0.89, 'wx', 'expense', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-11 00:00:00');
INSERT INTO `finance_info` VALUES (1582876350853677058, '饿了吗', '饿了吗', 4.50, 'wx', 'expense', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-11 00:00:00');
INSERT INTO `finance_info` VALUES (1582876350853677059, '红包', '红包', 0.36, 'wx', 'income', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-11 00:00:00');
INSERT INTO `finance_info` VALUES (1582876350853677060, '淘菜菜', '淘菜菜', 7.23, 'wx', 'expense', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-12 00:00:00');
INSERT INTO `finance_info` VALUES (1582876350916591617, '水费', '转账', 33.50, 'wx', 'expense', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-12 00:00:00');
INSERT INTO `finance_info` VALUES (1582876350916591618, '红包', '红包', 0.04, 'wx', 'income', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-13 00:00:00');
INSERT INTO `finance_info` VALUES (1582876350916591619, '买东西', '妈妈驿站', 24.00, 'wx', 'expense', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-13 00:00:00');
INSERT INTO `finance_info` VALUES (1582876350916591620, '淘菜菜', '淘菜菜', 9.99, 'wx', 'expense', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-13 00:00:00');
INSERT INTO `finance_info` VALUES (1582876350916591621, '买菜', '实体店', 10.90, 'wx', 'expense', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-13 00:00:00');
INSERT INTO `finance_info` VALUES (1582876350979506178, '采暖费', '采暖费', 2172.26, 'wx', 'expense', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-14 00:00:00');
INSERT INTO `finance_info` VALUES (1582876350979506179, '红包', '红包', 0.05, 'wx', 'income', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-17 00:00:00');
INSERT INTO `finance_info` VALUES (1582876350979506180, '红包', '红包', 0.04, 'wx', 'income', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-18 00:00:00');
INSERT INTO `finance_info` VALUES (1582876350979506181, '红包', '红包', 2.24, 'wx', 'income', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-15 00:00:00');
INSERT INTO `finance_info` VALUES (1582876350979506182, '饭补', '工资', 10.00, 'wx', 'income', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-15 00:00:00');
INSERT INTO `finance_info` VALUES (1582876350979506183, '美团', '美团', 7.80, 'yhk', 'expense', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-08 00:00:00');
INSERT INTO `finance_info` VALUES (1582876350979506184, '美团', '美团', 9.69, 'yhk', 'expense', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-08 00:00:00');
INSERT INTO `finance_info` VALUES (1582876351046615042, '工资', '工资', 5632.20, 'yhk', 'income', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-09 00:00:00');
INSERT INTO `finance_info` VALUES (1582876351046615043, '买东西', '妈妈驿站', 40.00, 'yhk', 'expense', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-10 00:00:00');
INSERT INTO `finance_info` VALUES (1582876351046615044, '还京东欠款', '京东', 9.00, 'yhk', 'expense', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-10 00:00:00');
INSERT INTO `finance_info` VALUES (1582876351046615045, '美团', '美团', 0.68, 'yhk', 'expense', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-10 00:00:00');
INSERT INTO `finance_info` VALUES (1582876351046615046, '美团', '美团', 1.08, 'yhk', 'expense', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-10 00:00:00');
INSERT INTO `finance_info` VALUES (1582876351046615047, '美团', '美团', 1.08, 'yhk', 'expense', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-11 00:00:00');
INSERT INTO `finance_info` VALUES (1582876351046615048, '美团', '美团', 1.69, 'yhk', 'expense', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-12 00:00:00');
INSERT INTO `finance_info` VALUES (1582876351046615049, '美团', '美团', 1.28, 'yhk', 'expense', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-13 00:00:00');
INSERT INTO `finance_info` VALUES (1582876351046615050, '买白菜', '实体店', 2.50, 'yhk', 'expense', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-13 00:00:00');
INSERT INTO `finance_info` VALUES (1582876351109529601, '钢化膜', '淘宝', 1.82, 'yhk', 'expense', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-14 00:00:00');
INSERT INTO `finance_info` VALUES (1582876351109529602, '饿了吗', '饿了吗', 5.01, 'wx', 'expense', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-15 00:00:00');
INSERT INTO `finance_info` VALUES (1582876351109529603, '牛奶', '妈妈驿站', 46.00, 'yhk', 'expense', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-17 00:00:00');
INSERT INTO `finance_info` VALUES (1582876351109529604, '煎饼', '实体店', 11.97, 'yhk', 'expense', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-17 00:00:00');
INSERT INTO `finance_info` VALUES (1582876351109529605, '外卖', '美团', 16.89, 'yhk', 'expense', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-17 00:00:00');
INSERT INTO `finance_info` VALUES (1582876351109529606, '还京东欠款', '京东', 888.63, 'yhk', 'expense', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-18 00:00:00');
INSERT INTO `finance_info` VALUES (1582876351109529607, '买东西', '京东', 14.02, 'bt', 'expense', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-10 00:00:00');
INSERT INTO `finance_info` VALUES (1582876351109529608, '买东西', '京东', 0.01, 'bt', 'expense', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-10 00:00:00');
INSERT INTO `finance_info` VALUES (1582876351172444162, '红包', '红包', 0.31, 'zfb', 'income', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-11 00:00:00');
INSERT INTO `finance_info` VALUES (1582876351172444163, '红包', '红包', 0.01, 'zfb', 'income', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-12 00:00:00');
INSERT INTO `finance_info` VALUES (1582876351172444164, '棉棒', '淘宝', 0.01, 'zfb', 'expense', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-12 00:00:00');
INSERT INTO `finance_info` VALUES (1582876351172444165, '利息', '红包', 4.97, 'zfb', 'income', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-18 00:00:00');
INSERT INTO `finance_info` VALUES (1582876351172444166, '转账', '转账', 5590.00, 'yhk', 'expense', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-10 00:00:00');
INSERT INTO `finance_info` VALUES (1582876351172444167, '转账', '转账', 5590.00, 'zfb', 'income', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-10 00:00:00');
INSERT INTO `finance_info` VALUES (1582876351172444168, '转账', '转账', 2000.00, 'zfb', 'expense', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-14 00:00:00');
INSERT INTO `finance_info` VALUES (1582876351172444169, '转账', '转账', 2000.00, 'yhk', 'income', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-14 00:00:00');
INSERT INTO `finance_info` VALUES (1582876351235358721, '转账', '转账', 1779.37, 'yhk', 'expense', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-14 00:00:00');
INSERT INTO `finance_info` VALUES (1582876351235358722, '转账', '转账', 1779.37, 'wx', 'income', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-14 00:00:00');
INSERT INTO `finance_info` VALUES (1582876351235358723, '转账', '转账', 800.00, 'zfb', 'expense', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-18 00:00:00');
INSERT INTO `finance_info` VALUES (1582876351235358724, '转账', '转账', 800.00, 'yhk', 'income', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-18 00:00:00');
INSERT INTO `finance_info` VALUES (1582876351235358725, '白条收入', '京东', 10.00, 'bt', 'income', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-10 00:00:00');
INSERT INTO `finance_info` VALUES (1582876351235358726, '白条收入', '京东', 889.39, 'bt', 'income', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-10 00:00:00');
INSERT INTO `finance_info` VALUES (1582876351235358727, '水费', '转账', 33.50, 'sf', 'income', '1', '0', NULL, NULL, NULL, '2022-10-20 07:28:25', NULL, NULL, NULL, '2022-10-20 07:28:25', 2, '2022-10-10 00:00:00');

SET FOREIGN_KEY_CHECKS = 1;
