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

SET FOREIGN_KEY_CHECKS = 1;
