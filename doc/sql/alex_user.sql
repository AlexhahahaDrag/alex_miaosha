CREATE DATABASE IF NOT EXISTS alex_user;
USE alex_user;
/*
 Navicat Premium Data Transfer

 Source Server         : 10.10.20.38
 Source Server Type    : MySQL
 Source Server Version : 80200 (8.2.0)
 Source Host           : 10.10.20.38:3336
 Source Schema         : alex_user

 Target Server Type    : MySQL
 Target Server Version : 80200 (8.2.0)
 File Encoding         : 65001

 Date: 25/03/2024 21:56:43
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_menu_info
-- ----------------------------
DROP TABLE IF EXISTS `t_menu_info`;
CREATE TABLE `t_menu_info`  (
  `id` bigint NOT NULL,
  `name` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '菜单名称',
  `path` varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '菜单路径',
  `title` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '菜单标题',
  `component` varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '组件',
  `redirect` varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '跳转',
  `icon` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '菜单图标',
  `hide_in_menu` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '是否隐藏菜单,字典(true_or_false) 1：有效,0:失效)',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父级机构id',
  `summary` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '备注',
  `status` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '状态,字典(is_valid) 1：有效,0:失效)',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updater` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `deleter` bigint NULL DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  `operator` bigint NULL DEFAULT NULL COMMENT '修改人',
  `operate_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `order_by` int NULL DEFAULT NULL COMMENT '排序',
  `show_in_home` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '是否在home中展示,字典(true_or_false) 1：展示,0:不展示)',
  `permission_code` varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '权限编码',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `t_menu_info_p_id_IDX`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '菜单管理表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_menu_info
-- ----------------------------
INSERT INTO `t_menu_info` VALUES (1, 'message', '/message', '消息管理', 'Layout', '/message/messageManager', 'messageManager', '0', NULL, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 10, '0', 'message');
INSERT INTO `t_menu_info` VALUES (2, 'messageManager', '/message/messageManager', '消息管理', '/src/views/message/index.vue', NULL, 'message', '0', 1, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, '1', 'message');
INSERT INTO `t_menu_info` VALUES (3, 'user', '/user', '用户管理', 'Layout', '/user/userManager', 'userManagerIcon', '0', NULL, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 20, '0', 'user');
INSERT INTO `t_menu_info` VALUES (5, 'finance', '/finance', '财务管理', 'Layout', '/finance/financeManager', 'financeManager', '0', NULL, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 30, '0', 'finance');
INSERT INTO `t_menu_info` VALUES (6, 'financeManager', '/finance/financeManager', '财务信息', '/src/views/finance/financeManager/index.vue', NULL, 'finance', '0', 5, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 10, '1', 'finance:financeManager');
INSERT INTO `t_menu_info` VALUES (7, 'financeAnalysis', '/finance/financeAnalysis', '财务分析', '/src/views/finance/financeAnalysis/index.vue', NULL, 'financeAnalysis', '0', 5, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 40, '1', 'finance:financeAnalysis');
INSERT INTO `t_menu_info` VALUES (8, 'accountRecordInfo', '/finance/accountRecordInfo', '猫超管理', '/src/views/finance/accountRecordInfo/index.vue', NULL, 'accountRecordInfo', '0', 5, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 30, '1', 'finance:accountRecordInfo');
INSERT INTO `t_menu_info` VALUES (9, 'dict', '/finance/dict', '字典信息', '/src/views/finance/dict/index.vue', NULL, 'dict', '0', 5, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 20, '1', 'finance:dict');
INSERT INTO `t_menu_info` VALUES (10, 'userManager', '/user/userManager', '用户信息', '/src/views/user/userManager/index.vue', NULL, 'user', '0', 3, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 10, '1', 'user:userManager');
INSERT INTO `t_menu_info` VALUES (11, 'orgManager', '/user/orgManager', '机构管理', '/src/views/user/orgInfo/index.vue', NULL, 'org', '0', 3, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 20, '1', 'user:orgManager');
INSERT INTO `t_menu_info` VALUES (12, 'menuInfo', '/user/menuInfo', '菜单管理', '/src/views/user/menuInfo/index.vue', NULL, 'menuInfo', '0', 3, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 30, '1', 'user:menuInfo');
INSERT INTO `t_menu_info` VALUES (17, 'incomeAnalysis', '/finance/financeAnalysis/incomeAnalysis', '收入分析', '/src/views/finance/financeAnalysis/detail/incomeAnalysis.vue', NULL, 'incomeAnalysis', '1', 5, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 50, '0', 'finance:financeAnalysis:incomeAnalysis');
INSERT INTO `t_menu_info` VALUES (18, 'overview', '/finance/financeAnalysis/overview', '总览', '/src/views/finance/financeAnalysis/detail/overview.vue', NULL, 'overview', '1', 5, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 60, '0', 'finance:financeAnalysis:overview');
INSERT INTO `t_menu_info` VALUES (19, 'incomeDetail', '/finance/financeAnalysis/incomeDetail', '收入明细', '/src/views/finance/financeAnalysis/detail/incomeDetail.vue', NULL, 'incomeDetail', '1', 5, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 70, '0', 'finance:financeAnalysis:incomeDetail');
INSERT INTO `t_menu_info` VALUES (22, 'menuInfoDetail', '/user/menuInfo/menuInfoDetail', '菜单管理详情', '/src/views/user/menuInfo/menuInfoDetail/index.vue', NULL, 'menuInfoDetail', '1', 3, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 10, '0', 'user:menuInfo:menuInfoDetail');
INSERT INTO `t_menu_info` VALUES (23, 'orgInfoDetail', '/user/orgManager/orgInfoDetail', '机构管理详情', '/src/views/user/orgInfo/orgInfoDetail/index.vue', NULL, 'orgInfoDetail', '1', 3, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 10, '0', 'user:orgManager:orgManagerDetail');
INSERT INTO `t_menu_info` VALUES (24, 'dictDetail', '/finance/dict/dictDetail', '字典信息详情', '/src/views/finance/dict/dictDetail/index.vue', NULL, 'dictDetail', '1', 5, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 10, '0', 'finance:dict:dictDetail');
INSERT INTO `t_menu_info` VALUES (25, 'financeManagerDetail', '/finance/financeManager/financeManagerDetail', '财务信息详情', '/src/views/finance/financeManager/financeManagerDetail/index.vue', NULL, 'financeDetail', '1', 5, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 10, '0', 'finance:financeManager:financeManagerDetail');
INSERT INTO `t_menu_info` VALUES (29, 'dictDetail', '/finance/dict/dictDetail', '字典信息详情', '/src/views/finance/dict/dictDetail/index.vue', NULL, 'dictDetail', '1', 5, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 20, '0', 'finance:dict:dictDetail');
INSERT INTO `t_menu_info` VALUES (30, 'accountRecordInfoDetail', '/finance/accountRecordInfo/accountRecordInfoDetail', '猫超管理详情', '/src/views/finance/accountRecordInfo/accountRecordInfoDetail/index.vue', NULL, 'accountRecordInfoDetail', '1', 5, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 30, '0', 'finance:accountRecordInfo:accountRecordInfoDetail');
INSERT INTO `t_menu_info` VALUES (31, 'pmsAttrDetail', '/product/pmsAttr/pmsAttrDetail', '商品属性详情', '/src/views/product/pmsAttr/pmsAttrDetail/index.vue', NULL, 'pmsAttrDetail', '1', 1739906000547254274, NULL, '1', NULL, '2023-12-28 08:33:55', NULL, '2023-12-28 19:50:55', NULL, NULL, 0, NULL, '2023-12-28 19:50:55', 10, '0', 'pmsAttrDetail');
INSERT INTO `t_menu_info` VALUES (33, 'shopFinanceAnalysis', '/finance/shopFinanceAnalysis', '店财务分析', '/src/views/finance/shopFinanceAnalysis/index.vue', NULL, 'shopFinanceAnalysis', '0', 5, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 150, '1', 'finance:shopFinanceAnalysis');
INSERT INTO `t_menu_info` VALUES (34, 'shopIncomeAnalysis', '/finance/shopFinanceAnalysis/shopIncomeAnalysis', '店收入分析', '/src/views/finance/shopFinanceAnalysis/detail/shopIncomeAnalysis.vue', NULL, 'shopIncomeAnalysis', '1', 5, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 50, '0', 'finance:shopFinanceAnalysis:shopIncomeAnalysis');
INSERT INTO `t_menu_info` VALUES (35, 'shopOverview', '/finance/shopFinanceAnalysis/shopOverview', '总览', '/src/views/finance/shopFinanceAnalysis/detail/shopOverview.vue', NULL, 'shopOverview', '1', 5, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 60, '0', 'finance:shopFinanceAnalysis:shopOverview');
INSERT INTO `t_menu_info` VALUES (36, 'shopIncomeDetail', '/finance/shopFinanceAnalysis/shopIncomeDetail', '店收入明细', '/src/views/finance/shopFinanceAnalysis/detail/shopIncomeDetail.vue', NULL, 'shopIncomeDetail', '1', 5, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 70, '0', 'finance:shopFinanceAnalysis:shopIncomeDetail');
INSERT INTO `t_menu_info` VALUES (176737064364695, 'shopProduct', '/finance/shopProduct', '店铺商品', '/src/views/finance/shopProduct/index.vue', NULL, 'shopProduct', '0', 5, NULL, '1', NULL, '2024-03-12 10:02:52', NULL, '2024-03-12 16:49:31', NULL, NULL, 0, NULL, '2024-03-12 16:49:31', 610, '1', 'finance:shopProduct');
INSERT INTO `t_menu_info` VALUES (176737064429707, 'shopStockDetail', '/finance/shopProduct/shopProductDetail', '店铺商品详情', '/src/views/finance/shopProduct/shopProductDetail/index.vue', NULL, 'shopProductDetail', '1', 5, NULL, '1', NULL, '2024-03-12 10:02:52', NULL, '2024-03-12 16:49:31', NULL, NULL, 0, NULL, '2024-03-12 16:49:31', 610, '0', 'finance:shopProduct:detail');
INSERT INTO `t_menu_info` VALUES (176737064364695123, 'saleTicket', '/finance/saleTicket', '店铺销售单', '/src/views/finance/saleTicket/index.vue', NULL, 'saleTicket', '0', 5, NULL, '1', NULL, '2024-03-12 10:02:52', NULL, '2024-03-12 16:49:31', NULL, NULL, 0, NULL, '2024-03-12 16:49:31', 710, '0', 'finance:saleTicket');
INSERT INTO `t_menu_info` VALUES (1739906000547254274, 'product', '/product', '商品管理', 'Layout', '/product/pmsAttr', 'product', '0', NULL, NULL, '1', NULL, '2023-12-27 15:08:11', NULL, NULL, NULL, NULL, 0, NULL, '2023-12-27 15:08:11', 40, '0', 'product');
INSERT INTO `t_menu_info` VALUES (1740169165491191809, 'pmsAttr', '/product/pmsAttr', '商品属性', '/src/views/product/pmsAttr/index.vue', NULL, 'pmsAttr', '0', 1739906000547254274, NULL, '1', NULL, '2023-12-28 08:33:54', NULL, '2023-12-28 19:50:54', NULL, NULL, 0, NULL, '2023-12-28 19:50:54', 10, '1', 'product:pmsAttr');
INSERT INTO `t_menu_info` VALUES (1740169166007091202, 'pmsAttrDetail', '/product/pmsAttr/pmsAttrDetail', '商品属性详情', '/src/views/product/pmsAttr/detail/index.vue', NULL, 'pmsAttrDetail', '1', 1739906000547254274, NULL, '1', NULL, '2023-12-28 08:33:55', NULL, '2023-12-28 19:50:55', NULL, NULL, 0, NULL, '2023-12-28 19:50:55', 10, '0', 'pmsAttrDetail');
INSERT INTO `t_menu_info` VALUES (1746531627376271361, 'roleInfo', '/user/roleInfo', '角色管理', '/src/views/user/roleInfo/index.vue', NULL, 'roleInfo', '0', 3, NULL, '1', NULL, '2024-01-14 21:56:04', NULL, '2024-01-14 21:56:19', NULL, NULL, 0, NULL, '2024-01-14 21:56:19', 40, '1', 'user:roleInfo');
INSERT INTO `t_menu_info` VALUES (1746531628089303041, 'roleInfoDetail', '/user/roleInfo/roleInfoDetail', '角色管理详情', '/src/views/user/roleInfo/roleInfoDetail/index.vue', NULL, 'roleInfoDetail', '1', 3, NULL, '1', NULL, '2024-01-14 21:56:04', NULL, '2024-01-14 21:56:19', NULL, NULL, 0, NULL, '2024-01-14 21:56:19', 10, '0', 'user:roleInfo:roleInfoDetail');
INSERT INTO `t_menu_info` VALUES (1747162769425887234, 'permissionInfo', '/user/permissionInfo', '权限信息', '/src/views/user/permissionInfo/index.vue', NULL, 'permissionInfo', '0', 3, NULL, '1', NULL, '2024-01-16 15:44:00', NULL, NULL, NULL, NULL, 0, NULL, '2024-01-16 15:44:00', 70, '1', 'user:permissionInfo');
INSERT INTO `t_menu_info` VALUES (1747162770101170177, 'permissionInfoDetail', '/user/permissionInfo/permissionInfoDetail', '权限信息详情', '/src/views/user/permissionInfo/permissionInfoDetail/index.vue', NULL, 'permissionInfoDetail', '1', 3, NULL, '1', NULL, '2024-01-16 15:44:00', NULL, NULL, NULL, NULL, 0, NULL, '2024-01-16 15:44:00', 10, '0', 'user:permissionInfo');
INSERT INTO `t_menu_info` VALUES (1761015127361941505, 'shopFinance', '/finance/shopFinance', '店财务管理', '/src/views/finance/shopFinance/index.vue', NULL, 'shopFinance', '0', 5, NULL, '1', NULL, '2024-02-23 21:08:19', NULL, '2024-02-23 21:19:51', NULL, NULL, 0, NULL, '2024-02-23 21:19:51', 110, '1', 'finance:shopFinance');
INSERT INTO `t_menu_info` VALUES (1761015128049807361, 'shopFinanceDetail', '/finance/shopFinance/shopFinanceDetail', '店财务管理详情', '/src/views/finance/shopFinance/shopFinanceDetail/index.vue', NULL, 'shopFinanceDetail', '1', 5, NULL, '1', NULL, '2024-02-23 21:08:19', NULL, '2024-02-23 21:19:51', NULL, NULL, 0, NULL, '2024-02-23 21:19:51', 110, '0', 'finance:shopFinance:shopFinanceDetail');
INSERT INTO `t_menu_info` VALUES (1767370643646955521, 'shopStock', '/finance/shopStock', '店铺库存', '/src/views/finance/shopStock/index.vue', NULL, 'shopStock', '0', 5, NULL, '1', NULL, '2024-03-12 10:02:52', NULL, '2024-03-12 16:49:31', NULL, NULL, 0, NULL, '2024-03-12 16:49:31', 610, '1', 'finance:shopStock');
INSERT INTO `t_menu_info` VALUES (1767370644297072641, 'shopStockDetail', '/finance/shopStock/shopStockDetail', '店铺库存详情', '/src/views/finance/shopStock/shopStockDetail/index.vue', NULL, 'shopStockDetail', '1', 5, NULL, '1', NULL, '2024-03-12 10:02:52', NULL, '2024-03-12 16:49:31', NULL, NULL, 0, NULL, '2024-03-12 16:49:31', 610, '0', 'finance:shopStock:detail');

-- ----------------------------
-- Table structure for t_menu_user_info
-- ----------------------------
DROP TABLE IF EXISTS `t_menu_user_info`;
CREATE TABLE `t_menu_user_info`  (
  `id` bigint NOT NULL,
  `name` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '菜单名称',
  `path` varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '菜单路径',
  `title` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '菜单标题',
  `icon` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '菜单图标',
  `component` varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '组件',
  `redirect` varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '跳转',
  `hide_in_menu` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '是否隐藏菜单',
  `p_id` bigint NULL DEFAULT NULL COMMENT '父级机构id',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户id',
  `role_id` bigint NULL DEFAULT NULL COMMENT '角色id',
  `summary` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '备注',
  `status` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '状态',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updater` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `deleter` bigint NULL DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  `operator` bigint NULL DEFAULT NULL COMMENT '修改人',
  `operate_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `show_in_pc` tinyint NULL DEFAULT NULL COMMENT '是否在pc端显示',
  `show_in_mobile` tinyint NULL DEFAULT NULL COMMENT '是否在移动端显示',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '菜单用户管理表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_menu_user_info
-- ----------------------------

-- ----------------------------
-- Table structure for t_org_info
-- ----------------------------
DROP TABLE IF EXISTS `t_org_info`;
CREATE TABLE `t_org_info`  (
  `id` bigint NOT NULL,
  `org_code` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '机构编码',
  `org_name` varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '机构名称',
  `org_short_name` varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '机构简称',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父级机构id',
  `summary` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL COMMENT '简介',
  `status` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '状态,字典(is_valid) 1：有效,0:失效)',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updater` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `deleter` bigint NULL DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  `operator` bigint NULL DEFAULT NULL COMMENT '修改人',
  `operate_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '机构表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_org_info
-- ----------------------------
INSERT INTO `t_org_info` VALUES (1732681655123001345, '13', '213', '123', 1735563603956748289, '213', '1', NULL, '2023-12-07 16:41:13', NULL, NULL, NULL, NULL, 0, NULL, '2023-12-07 16:41:13');
INSERT INTO `t_org_info` VALUES (1735563796248809473, 'test2', '测试机构2', 'test2', NULL, 'dsgsdg', '1', NULL, '2023-12-15 15:33:49', NULL, NULL, NULL, NULL, 0, NULL, '2023-12-15 15:33:49');

-- ----------------------------
-- Table structure for t_org_user_info
-- ----------------------------
DROP TABLE IF EXISTS `t_org_user_info`;
CREATE TABLE `t_org_user_info`  (
  `id` bigint NOT NULL,
  `org_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '公司角色id',
  `user_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '用户id',
  `summary` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '描述',
  `status` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '状态,字典(is_valid) 1：有效,0:失效)',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updater` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `deleter` bigint NULL DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  `operator` bigint NULL DEFAULT NULL COMMENT '修改人',
  `operate_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户公司信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_org_user_info
-- ----------------------------
INSERT INTO `t_org_user_info` VALUES (3, '1732681655123001345', '1763166043771621378', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `t_org_user_info` VALUES (4, '1732681655123001345', '1766998444679483393', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL);

-- ----------------------------
-- Table structure for t_permission_info
-- ----------------------------
DROP TABLE IF EXISTS `t_permission_info`;
CREATE TABLE `t_permission_info`  (
  `id` bigint NOT NULL,
  `permission_code` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '权限编码',
  `permission_name` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '权限名称',
  `summary` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '描述',
  `status` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '状态,字典(is_valid) 1：有效,0:失效)',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updater` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `deleter` bigint NULL DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  `operator` bigint NULL DEFAULT NULL COMMENT '修改人',
  `operate_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `options` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'url',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父级id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '权限信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_permission_info
-- ----------------------------
INSERT INTO `t_permission_info` VALUES (1, 'message', '消息管理', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (2, 'message:messageManager', '消息管理', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (3, 'user', '用户管理', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (5, 'finance', '财务管理', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (6, 'finance:financeManager', '财务信息', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (7, 'finance:financeAnalysis', '财务分析', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (8, 'finance:accountRecordInfo', '猫超管理', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (9, 'finance:dict', '字典信息', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (10, 'user:userManager', '用户信息', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (11, 'user:orgManager', '机构管理', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (12, 'user:menuInfo', '菜单管理', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (13, 'user:menuInfo:menuInfoDetail', '菜单管理详情', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (14, 'user:orgManager:orgManagerDetail', '机构管理详情', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (15, 'finance:dict:dictDetail', '字典信息详情', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (16, 'finance:financeManager:financeManagerDetail', '财务信息详情', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (17, 'finance:financeAnalysis:incomeAnalysis', '收入分析', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (18, 'finance:financeAnalysis:overview', '总览', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (19, 'finance:financeAnalysis:incomeDetail', '收入明细', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (20, 'finance:dict:dictDetail', '字典信息详情', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (21, 'finance:accountRecordInfo:accountRecordInfoDetail', '猫超管理详情', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (22, 'user:menuInfo:menuInfoDetail', '菜单管理详情', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (23, 'user:orgManager:orgInfoDetail', '机构管理详情', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (24, 'finance:dict:dictDetail', '字典信息详情', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (25, 'finance:financeManager:financeManagerDetail', '财务信息详情', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (29, 'finance:dict:dictDetail', '字典信息详情', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (30, 'finance:accountRecordInfo:accountRecordInfoDetail', '猫超管理详情', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (31, 'pmsAttrDetail', '商品属性详情', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (32, 'finance:shopFinance:shopFinanceDetail', '店财务管理详情', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, 1761018029702856705);
INSERT INTO `t_permission_info` VALUES (33, 'finance:shopFinanceAnalysis', '店铺财务分析', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (34, 'finance:shopFinanceAnalysis:shopIncomeAnalysis', '店铺收入分析', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (35, 'finance:shopFinanceAnalysis:shopOverview', '店铺总览', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (36, 'finance:shopFinanceAnalysis:shopIncomeDetail', '店铺收入明细', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (17673872060307, 'finance:saleTicket', '店铺销售单', NULL, '1', NULL, '2024-03-12 11:08:41', NULL, '2024-03-12 16:49:31', NULL, NULL, 0, NULL, '2024-03-12 16:49:31', '/finance/saleTicket', 5);
INSERT INTO `t_permission_info` VALUES (176738720603070, 'finance:shopProduct', '店铺商品', NULL, '1', NULL, '2024-03-12 11:08:41', NULL, '2024-03-12 16:49:31', NULL, NULL, 0, NULL, '2024-03-12 16:49:31', '/finance/shopProduct', 5);
INSERT INTO `t_permission_info` VALUES (176738723900212, 'finance:shopProduct:detail', '店铺商品详情', NULL, '1', NULL, '2024-03-12 11:08:49', NULL, NULL, NULL, NULL, 0, NULL, '2024-03-12 11:08:49', '/finance/shopProduct/shopProductDetail', 5);
INSERT INTO `t_permission_info` VALUES (1739906000547254274, 'product', '商品管理', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (1740169165491191809, 'product:pmsAttr', '商品属性', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (1740169166007091202, 'product:pmsAttr:pmsAttrDetail', '商品属性详情', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (1746531627376271361, 'user:roleInfo', '角色管理', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (1746531628089303041, 'user:roleInfo:roleInfoDetail', '角色管理详情', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (1746780102696022018, 'user:orgUserInfo', '用户公司信息', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (1746780103224504322, 'user:orgUserInfo:orgUserInfoDetail', '用户公司信息详情', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (1746780105615257602, 'user:roleUserInfo', '用户角色信息', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (1746780106194071554, 'user:roleUserInfo:roleUserInfoDetail', '用户角色信息详情', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (1747162769425887234, 'user:permissionInfo', '权限信息表', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (1747162770101170177, 'user:permissionInfo:permissionInfoDetail', '权限信息表', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (1747518319657431041, 'user:rolePermissionInfo', '角色权限信息', NULL, '1', NULL, '2024-01-17 15:16:49', NULL, '2024-01-19 14:52:23', NULL, NULL, 0, NULL, '2024-01-19 14:52:23', '/user/rolePermissionInfo', 3);
INSERT INTO `t_permission_info` VALUES (1747520680119422977, 'user:rolePermissionInfo:detail', '角色权限信息详情', NULL, '1', NULL, '2024-01-17 15:26:12', NULL, '2024-01-19 14:52:23', NULL, NULL, 0, NULL, '2024-01-19 14:52:23', '/user/rolePermissionInfo/rolePermissionInfoDetail', 3);
INSERT INTO `t_permission_info` VALUES (1761018029702856705, 'finance:shopFinance', '店财务管理', NULL, '1', NULL, '2024-02-23 21:19:51', NULL, NULL, NULL, NULL, 0, NULL, '2024-02-23 21:19:51', '/finance/shopFinance', 5);
INSERT INTO `t_permission_info` VALUES (1767387206030704642, 'finance:shopStock', '店铺库存', NULL, '1', NULL, '2024-03-12 11:08:41', NULL, '2024-03-12 16:49:31', NULL, NULL, 0, NULL, '2024-03-12 16:49:31', '/finance/shopStock', 5);
INSERT INTO `t_permission_info` VALUES (1767387239002128385, 'finance:shopStock:detail', '店铺库存详情', NULL, '1', NULL, '2024-03-12 11:08:49', NULL, NULL, NULL, NULL, 0, NULL, '2024-03-12 11:08:49', '/finance/shopStock/shopStockDetail', 5);

-- ----------------------------
-- Table structure for t_role_info
-- ----------------------------
DROP TABLE IF EXISTS `t_role_info`;
CREATE TABLE `t_role_info`  (
  `id` bigint NOT NULL,
  `role_code` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '角色编码',
  `role_name` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '角色名称',
  `summary` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '描述',
  `status` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '状态,字典(is_valid) 1：有效,0:失效)',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updater` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `deleter` bigint NULL DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  `operator` bigint NULL DEFAULT NULL COMMENT '修改人',
  `operate_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_role_info
-- ----------------------------
INSERT INTO `t_role_info` VALUES (1746681834919378946, 'super_super', '超级管理员', '超级管理员', '1', 1, '2024-01-15 07:52:56', NULL, NULL, NULL, NULL, 0, 1, '2024-01-15 07:52:56');

-- ----------------------------
-- Table structure for t_role_permission_info
-- ----------------------------
DROP TABLE IF EXISTS `t_role_permission_info`;
CREATE TABLE `t_role_permission_info`  (
  `id` bigint NOT NULL,
  `role_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '角色id',
  `permission_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '权限id',
  `summary` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '描述',
  `status` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '状态,字典(is_valid) 1：有效,0:失效)',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updater` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `deleter` bigint NULL DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  `operator` bigint NULL DEFAULT NULL COMMENT '修改人',
  `operate_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色权限信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_role_user_info
-- ----------------------------
DROP TABLE IF EXISTS `t_role_user_info`;
CREATE TABLE `t_role_user_info`  (
  `id` bigint NOT NULL,
  `role_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '角色id',
  `user_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '用户id',
  `summary` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '描述',
  `status` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '状态,字典(is_valid) 1：有效,0:失效)',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updater` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `deleter` bigint NULL DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  `operator` bigint NULL DEFAULT NULL COMMENT '修改人',
  `operate_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户角色信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_role_user_info
-- ----------------------------
INSERT INTO `t_role_user_info` VALUES (3, '1746681834919378946', '1759760891393945602', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL);

-- ----------------------------
-- Table structure for t_user
-- ----------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user`  (
  `id` bigint NOT NULL,
  `username` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '密码',
  `gender` tinyint(1) NULL DEFAULT NULL COMMENT '性别(1:男2:女)',
  `avatar` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '个人头像',
  `email` varchar(60) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `birthday` datetime NULL DEFAULT NULL COMMENT '出生年月日',
  `mobile` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '手机',
  `valid_code` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '邮箱验证码',
  `summary` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '自我简介最多150字',
  `status` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '状态',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updater` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `deleter` bigint NULL DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  `operator` bigint NULL DEFAULT NULL COMMENT '修改人',
  `operate_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `nick_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '昵称',
  `qq_number` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT 'QQ号',
  `we_chat` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '微信号',
  `occupation` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '职业',
  `github` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT 'github地址',
  `gitee` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT 'gitee地址',
  `role_id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '拥有的角色uid',
  `person_resume` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL COMMENT '履历',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_uesrname_index`(`username` ASC) USING BTREE,
  INDEX `t_username_status_IDX`(`username` ASC, `status` ASC, `is_delete` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '管理员表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_user
-- ----------------------------
INSERT INTO `t_user` VALUES (1745355151204110338, '123', '$2a$10$NvA.1EnUYhyn7YYf0eS/4e5yXF6R8qWk4AvnYxJhEjeNO.t9MYZ7G', NULL, NULL, '123@com', NULL, NULL, NULL, NULL, NULL, 1, '2024-01-11 16:01:10', NULL, NULL, NULL, NULL, 0, 1, '2024-01-11 16:01:10', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_user` VALUES (1745406245749886977, '1213', '$2a$10$lar0kjPsEK8IMPgtBkQi7OYCMtAwP5jYLP5QZcVFVMHFkFr3ttsQW', NULL, NULL, '1231@com', NULL, NULL, NULL, NULL, NULL, 1, '2024-01-11 19:24:12', NULL, NULL, NULL, NULL, 0, 1, '2024-01-11 19:24:12', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_user` VALUES (1759760891393945602, 'root', '$2a$10$JXbgisfYG9OSuFVjP/zv5etH6xQh1Qcq0FFs5cQQsuLmYH6GmSU3K', 1, NULL, NULL, '2020-01-01 10:03:42', '18222222222', NULL, 'super！super！！super！！！', '1', 2, '2024-02-20 10:04:26', NULL, NULL, NULL, NULL, 0, 2, '2024-02-20 10:04:26', 'supersuper', NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for t_user_login
-- ----------------------------
DROP TABLE IF EXISTS `t_user_login`;
CREATE TABLE `t_user_login`  (
  `id` bigint NOT NULL COMMENT 'id',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户id',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `token_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT 'token在redis中对应的tokenId',
  `os` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '系统',
  `broswer` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '登录浏览器',
  `login_ip` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT '127.0.0.1' COMMENT '最后登录IP',
  `creator` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `updater` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `deleter` bigint NULL DEFAULT NULL COMMENT '删除人',
  `delete_time` timestamp NULL DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  `operator` bigint NULL DEFAULT NULL COMMENT '修改人',
  `operate_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `token` varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT 'token信息',
  `login_location` varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '登录地址',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '用户登录表' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
