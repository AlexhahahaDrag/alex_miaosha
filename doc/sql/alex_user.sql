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
INSERT INTO `t_menu_info` VALUES (3, 'user', '/user', '系统管理', 'Layout', '/user/userManager', 'userManagerIcon', '0', NULL, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 20, '0', 'user');
INSERT INTO `t_menu_info` VALUES (5, 'finance', '/finance', '财务管理', 'Layout', '/finance/financeManager', 'financeManager', '0', NULL, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 30, '0', 'finance');
INSERT INTO `t_menu_info` VALUES (6, 'financeManager', '/finance/financeManager', '财务信息', '/src/views/finance/financeManager/index.vue', NULL, 'finance', '0', 5, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 10, '1', 'finance:financeManager');
INSERT INTO `t_menu_info` VALUES (7, 'financeAnalysis', '/finance/financeAnalysis', '财务分析', '/src/views/finance/financeAnalysis/index.vue', NULL, 'financeAnalysis', '0', 5, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 40, '1', 'finance:financeAnalysis');
INSERT INTO `t_menu_info` VALUES (8, 'accountRecordInfo', '/finance/accountRecordInfo', '猫超管理', '/src/views/finance/accountRecordInfo/index.vue', NULL, 'accountRecordInfo', '0', 5, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 80, '1', 'finance:accountRecordInfo');
INSERT INTO `t_menu_info` VALUES (2000000000000000001, 'dictManage', '/dict', '系统配置', 'Layout', '/dict/dictInfo', 'dict', '0', NULL, '系统配置', '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 38, '0', 'dict');
INSERT INTO `t_menu_info` VALUES (9, 'dict', '/dict/dictInfo', '字典信息', '/src/views/dict/index.vue', NULL, 'dict', '0', 2000000000000000001, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 10, '1', 'dict:dictInfo');
INSERT INTO `t_menu_info` VALUES (10, 'userManager', '/user/userManager', '用户管理', '/src/views/user/userManager/index.vue', NULL, 'user', '0', 3, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 20, '1', 'user:userManager');
INSERT INTO `t_menu_info` VALUES (11, 'orgManager', '/user/orgManager', '机构管理', '/src/views/user/orgInfo/index.vue', NULL, 'org', '0', 3, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 10, '1', 'user:orgManager');
INSERT INTO `t_menu_info` VALUES (12, 'menuInfo', '/user/menuInfo', '菜单管理', '/src/views/user/menuInfo/index.vue', NULL, 'menuInfo', '0', 3, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 40, '1', 'user:menuInfo');
INSERT INTO `t_menu_info` VALUES (17, 'incomeAnalysis', '/finance/financeAnalysis/incomeAnalysis', '收入分析', '/src/views/finance/financeAnalysis/detail/incomeAnalysis.vue', NULL, 'incomeAnalysis', '1', 5, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 50, '0', 'finance:financeAnalysis:incomeAnalysis');
INSERT INTO `t_menu_info` VALUES (18, 'overview', '/finance/financeAnalysis/overview', '总览', '/src/views/finance/financeAnalysis/detail/overview.vue', NULL, 'overview', '1', 5, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 60, '0', 'finance:financeAnalysis:overview');
INSERT INTO `t_menu_info` VALUES (19, 'incomeDetail', '/finance/financeAnalysis/incomeDetail', '收入明细', '/src/views/finance/financeAnalysis/detail/incomeDetail.vue', NULL, 'incomeDetail', '1', 5, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 70, '0', 'finance:financeAnalysis:incomeDetail');
INSERT INTO `t_menu_info` VALUES (22, 'menuInfoDetail', '/user/menuInfo/menuInfoDetail', '菜单管理详情', '/src/views/user/menuInfo/menuInfoDetail/index.vue', NULL, 'menuInfoDetail', '1', 3, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 10, '0', 'user:menuInfo:menuInfoDetail');
INSERT INTO `t_menu_info` VALUES (23, 'orgInfoDetail', '/user/orgManager/orgInfoDetail', '机构管理详情', '/src/views/user/orgInfo/orgInfoDetail/index.vue', NULL, 'orgInfoDetail', '1', 3, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 10, '0', 'user:orgManager:orgManagerDetail');
INSERT INTO `t_menu_info` VALUES (24, 'dictDetail', '/dict/dictInfo/dictDetail', '字典信息详情', '/src/views/dict/dict-detail/index.vue', NULL, 'dictDetail', '1', 2000000000000000001, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 10, '0', 'dict:dictDetail');
INSERT INTO `t_menu_info` VALUES (25, 'financeManagerDetail', '/finance/financeManager/financeManagerDetail', '财务信息详情', '/src/views/finance/financeManager/financeManagerDetail/index.vue', NULL, 'financeDetail', '1', 5, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 10, '0', 'finance:financeManager:financeManagerDetail');
INSERT INTO `t_menu_info` VALUES (29, 'dictDetail', '/dict/dictInfo/dictDetail', '字典信息详情', '/src/views/dict/dict-detail/index.vue', NULL, 'dictDetail', '1', 2000000000000000001, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 20, '0', 'dict:dictDetail');
INSERT INTO `t_menu_info` VALUES (30, 'accountRecordInfoDetail', '/finance/accountRecordInfo/accountRecordInfoDetail', '猫超管理详情', '/src/views/finance/accountRecordInfo/accountRecordInfoDetail/index.vue', NULL, 'accountRecordInfoDetail', '1', 5, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 80, '0', 'finance:accountRecordInfo:accountRecordInfoDetail');
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
INSERT INTO `t_menu_info` VALUES (1746531627376271361, 'roleInfo', '/user/roleInfo', '角色管理', '/src/views/user/roleInfo/index.vue', NULL, 'roleInfo', '0', 3, NULL, '1', NULL, '2024-01-14 21:56:04', NULL, '2024-01-14 21:56:19', NULL, NULL, 0, NULL, '2024-01-14 21:56:19', 30, '1', 'user:roleInfo');
INSERT INTO `t_menu_info` VALUES (1746531628089303041, 'roleInfoDetail', '/user/roleInfo/roleInfoDetail', '角色管理详情', '/src/views/user/roleInfo/roleInfoDetail/index.vue', NULL, 'roleInfoDetail', '1', 3, NULL, '1', NULL, '2024-01-14 21:56:04', NULL, '2024-01-14 21:56:19', NULL, NULL, 0, NULL, '2024-01-14 21:56:19', 10, '0', 'user:roleInfo:roleInfoDetail');
INSERT INTO `t_menu_info` VALUES (1747162769425887234, 'permissionInfo', '/user/permissionInfo', '权限管理', '/src/views/user/permissionInfo/index.vue', NULL, 'permissionInfo', '0', 3, NULL, '1', NULL, '2024-01-16 15:44:00', NULL, NULL, NULL, NULL, 0, NULL, '2024-01-16 15:44:00', 50, '1', 'user:permissionInfo');
INSERT INTO `t_menu_info` VALUES (1747162770101170177, 'permissionInfoDetail', '/user/permissionInfo/permissionInfoDetail', '权限管理详情', '/src/views/user/permissionInfo/permissionInfoDetail/index.vue', NULL, 'permissionInfoDetail', '1', 3, NULL, '1', NULL, '2024-01-16 15:44:00', NULL, NULL, NULL, NULL, 0, NULL, '2024-01-16 15:44:00', 10, '0', 'user:permissionInfo');
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
INSERT INTO `t_permission_info` VALUES (2000000000000000001, 'dict', '系统配置', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (8, 'finance:accountRecordInfo', '猫超管理', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_permission_info` VALUES (9, 'dict:dictInfo', '字典信息', NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);
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
INSERT INTO `t_permission_info` VALUES (2026081900000000001, 'role:add', '新增角色', '角色管理页面新增角色按钮', '1', NULL, '2026-08-19 10:00:00', NULL, NULL, NULL, NULL, 0, NULL, '2026-08-19 10:00:00', NULL, 1746531627376271361);
INSERT INTO `t_permission_info` VALUES (2026081900000000002, 'role:edit', '编辑角色', '角色管理页面编辑角色按钮', '1', NULL, '2026-08-19 10:00:00', NULL, NULL, NULL, NULL, 0, NULL, '2026-08-19 10:00:00', NULL, 1746531627376271361);
INSERT INTO `t_permission_info` VALUES (2026081900000000003, 'role:auth', '角色授权', '角色管理页面权限授权按钮', '1', NULL, '2026-08-19 10:00:00', NULL, NULL, NULL, NULL, 0, NULL, '2026-08-19 10:00:00', NULL, 1746531627376271361);
INSERT INTO `t_permission_info` VALUES (2026081900000000004, 'role:delete', '删除角色', '角色管理页面删除角色按钮', '1', NULL, '2026-08-19 10:00:00', NULL, NULL, NULL, NULL, 0, NULL, '2026-08-19 10:00:00', NULL, 1746531627376271361);
INSERT INTO `t_permission_info` VALUES (2026092300000000001, 'user:add', '新增用户', '用户管理页面新增用户按钮', '1', NULL, '2026-09-23 09:30:00', NULL, NULL, NULL, NULL, 0, NULL, '2026-09-23 09:30:00', NULL, 10);
INSERT INTO `t_permission_info` VALUES (2026092300000000002, 'user:edit', '编辑用户', '用户管理页面编辑用户按钮', '1', NULL, '2026-09-23 09:30:00', NULL, NULL, NULL, NULL, 0, NULL, '2026-09-23 09:30:00', NULL, 10);
INSERT INTO `t_permission_info` VALUES (2026092300000000003, 'user:delete', '删除用户', '用户管理页面删除用户按钮', '1', NULL, '2026-09-23 09:30:00', NULL, NULL, NULL, NULL, 0, NULL, '2026-09-23 09:30:00', NULL, 10);

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
-- Table structure for t_role_org_info
-- ----------------------------
DROP TABLE IF EXISTS `t_role_org_info`;
CREATE TABLE `t_role_org_info` (
  `id` bigint NOT NULL,
  `role_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '角色id',
  `org_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '机构id',
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
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_role_org_active` (`role_id`, `org_id`, `status`, `is_delete`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色机构信息表' ROW_FORMAT = Dynamic;

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

-- ----------------------------
-- Incremental Button Permissions Grants
-- ----------------------------
-- 为 admin, family_admin, org_user_admin 等角色幂等授予按钮级权限
INSERT INTO `t_role_permission_info` (`id`, `role_id`, `permission_id`, `summary`, `status`, `is_delete`, `create_time`, `operate_time`)
SELECT
  (2026092350000000000 + ri.id + pi.id) AS `id`,
  CAST(ri.id AS CHAR) AS `role_id`,
  CAST(pi.id AS CHAR) AS `permission_id`,
  CONCAT('init: grant ', pi.permission_code, ' to ', ri.role_code) AS `summary`,
  '1' AS `status`,
  0 AS `is_delete`,
  NOW(),
  NOW()
FROM `t_permission_info` pi
CROSS JOIN `t_role_info` ri
WHERE pi.permission_code IN ('user:add', 'user:edit', 'user:delete', 'role:add', 'role:edit', 'role:auth', 'role:delete')
  AND pi.is_delete = 0
  AND pi.status = '1'
  AND ri.role_code IN ('admin', 'family_admin', 'org_user_admin')
  AND ri.is_delete = 0
  AND ri.status = '1'
  AND NOT EXISTS (
    SELECT 1
    FROM `t_role_permission_info` existing
    WHERE existing.role_id = CAST(ri.id AS CHAR)
      AND existing.permission_id = CAST(pi.id AS CHAR)
      AND existing.is_delete = 0
  );

-- ----------------------------
-- 零钱预算管理独立动态路由菜单与细粒度权限 (2026-10-12)
-- ----------------------------
INSERT INTO `t_menu_info` VALUES (2026101200000000001, 'financeBudgetManage', '/finance/financeManager/budgetManage', '零花钱预算管理', '/src/views/finance/financeManager/budgetManage/index.vue', NULL, 'budget', '1', 5, NULL, '1', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 15, '1', 'finance:financeManager:budget:view');

INSERT INTO `t_permission_info` (`id`, `permission_code`, `name`, `description`, `status`) VALUES
(2026101200000000011, 'finance:financeManager:query', '零钱查询', '财务零钱记录查询与列表查看', '1'),
(2026101200000000012, 'finance:financeManager:add', '零钱新增', '新增收支记录与记账按钮', '1'),
(2026101200000000013, 'finance:financeManager:edit', '零钱修改', '编辑收支记录（家庭成员仅限个人流水）', '1'),
(2026101200000000014, 'finance:financeManager:delete', '零钱删除', '删除收支记录（家庭成员仅限个人流水）', '1'),
(2026101200000000015, 'finance:financeManager:budget:view', '零钱预算查看', '查看零钱预算页面与试算数据', '1'),
(2026101200000000016, 'finance:financeManager:budget:edit', '零钱预算修改', '保存月度预算金额与纳入分类配置（仅家庭管理员）', '1');

-- (A) 家庭成员、普通用户：授予流水查询、记账新增、个人流水修改删除与预算查看权限
INSERT INTO `t_role_permission_info` (`id`, `role_id`, `permission_id`, `summary`, `status`, `is_delete`, `create_time`, `operate_time`)
SELECT
  (2026101260000000000 + ri.id + pi.id) AS `id`,
  CAST(ri.id AS CHAR) AS `role_id`,
  CAST(pi.id AS CHAR) AS `permission_id`,
  CONCAT('init: grant ', pi.permission_code, ' to ', ri.role_code) AS `summary`,
  '1' AS `status`,
  0 AS `is_delete`,
  NOW(),
  NOW()
FROM `t_permission_info` pi
CROSS JOIN `t_role_info` ri
WHERE pi.permission_code IN (
    'finance:financeManager:query',
    'finance:financeManager:add',
    'finance:financeManager:edit',
    'finance:financeManager:delete',
    'finance:financeManager:budget:view'
)
  AND ri.role_code IN ('user', 'family_member')
  AND pi.is_delete = 0 AND ri.is_delete = 0
  AND NOT EXISTS (
    SELECT 1 FROM `t_role_permission_info` ex
    WHERE ex.role_id = CAST(ri.id AS CHAR) AND ex.permission_id = CAST(pi.id AS CHAR) AND ex.is_delete = 0
  );

-- (B) 家庭管理员 (family_admin)、机构管理员 (admin)、超级管理员 (super_super)：授予全量权限（含预算编辑）
INSERT INTO `t_role_permission_info` (`id`, `role_id`, `permission_id`, `summary`, `status`, `is_delete`, `create_time`, `operate_time`)
SELECT
  (2026101270000000000 + ri.id + pi.id) AS `id`,
  CAST(ri.id AS CHAR) AS `role_id`,
  CAST(pi.id AS CHAR) AS `permission_id`,
  CONCAT('init: grant ', pi.permission_code, ' to ', ri.role_code) AS `summary`,
  '1' AS `status`,
  0 AS `is_delete`,
  NOW(),
  NOW()
FROM `t_permission_info` pi
CROSS JOIN `t_role_info` ri
WHERE pi.permission_code LIKE 'finance:financeManager:%'
  AND ri.role_code IN ('family_admin', 'admin', 'super_super')
  AND pi.is_delete = 0 AND ri.is_delete = 0
  AND NOT EXISTS (
    SELECT 1 FROM `t_role_permission_info` ex
    WHERE ex.role_id = CAST(ri.id AS CHAR) AND ex.permission_id = CAST(pi.id AS CHAR) AND ex.is_delete = 0
  );

-- ------------------------------------------------------------------------------
-- 系统管理与 RBAC 菜单标题规范化及排序优化 (2026-10-16)
-- ------------------------------------------------------------------------------
-- 1. 更新一级菜单「用户管理」为「系统管理」
UPDATE `t_menu_info` SET `title` = '系统管理' WHERE `id` = 3 AND `name` = 'user';

-- 2. 更新二级子菜单标题
UPDATE `t_menu_info` SET `title` = '用户管理' WHERE `id` = 10 AND `name` = 'userManager';
UPDATE `t_menu_info` SET `title` = '权限管理' WHERE `id` = 1747162769425887234 OR `name` = 'permissionInfo';
UPDATE `t_menu_info` SET `title` = '权限管理详情' WHERE `id` = 1747162770101170177 OR `name` = 'permissionInfoDetail';

-- 3. 规范排序 (组织机构 -> 归属用户 -> 角色定义 -> 页面菜单 -> 资源权限)
UPDATE `t_menu_info` SET `order_by` = 10 WHERE `name` = 'orgManager';
UPDATE `t_menu_info` SET `order_by` = 20 WHERE `name` = 'userManager';
UPDATE `t_menu_info` SET `order_by` = 30 WHERE `name` = 'roleInfo';
UPDATE `t_menu_info` SET `order_by` = 40 WHERE `name` = 'menuInfo';
UPDATE `t_menu_info` SET `order_by` = 50 WHERE `name` = 'permissionInfo';

-- -----------------------------------------------------------------------------
-- 礼尚往来管理模块 (Gift) 权限、菜单与角色定义 (2026-09-28)
-- -----------------------------------------------------------------------------
SET @gift_menu_id := 1900000000000001000;
SET @gift_permission_id := 1900000000000002000;
SET @gift_admin_role_id := '1900000000000004001';
SET @gift_user_role_id := '1900000000000004002';

-- Gift page permissions
INSERT IGNORE INTO `t_permission_info`
(`id`, `permission_code`, `permission_name`, `summary`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleter`, `delete_time`, `is_delete`, `operator`, `operate_time`, `options`, `parent_id`)
VALUES
(1900000000000002001, 'gift:dashboard', '礼尚往来-数据概览', '礼尚往来数据概览页面权限', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, '/finance/gift/dashboard', 5),
(1900000000000002002, 'gift:person', '礼尚往来-亲友管理', '礼尚往来亲友管理页面权限', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, '/finance/gift/person', 5),
(1900000000000002003, 'gift:event', '礼尚往来-事由管理', '礼尚往来事由管理页面权限', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, '/finance/gift/event', 5),
(1900000000000002004, 'gift:record', '礼尚往来-礼金记录', '礼尚往来礼金记录页面权限', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, '/finance/gift/record', 5),
(1900000000000002005, 'gift:analysis', '礼尚往来-统计报表', '礼尚往来统计报表页面权限', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, '/finance/gift/analysis', 5);

-- Gift action permissions
INSERT IGNORE INTO `t_permission_info`
(`id`, `permission_code`, `permission_name`, `summary`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleter`, `delete_time`, `is_delete`, `operator`, `operate_time`, `options`, `parent_id`)
VALUES
(1900000000000002011, 'gift:view', '礼尚往来-查看', '礼尚往来列表和详情查看权限', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL),
(1900000000000002012, 'gift:add', '礼尚往来-新增', '礼尚往来新增按钮权限', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL),
(1900000000000002013, 'gift:edit', '礼尚往来-编辑', '礼尚往来编辑按钮权限', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL),
(1900000000000002014, 'gift:delete', '礼尚往来-删除', '礼尚往来删除按钮权限', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL),
(1900000000000002015, 'gift:export', '礼尚往来-导出', '礼尚往来导出按钮权限', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL);

-- Gift page menus
INSERT IGNORE INTO `t_menu_info`
(`id`, `name`, `path`, `title`, `component`, `redirect`, `icon`, `hide_in_menu`, `parent_id`, `summary`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleter`, `delete_time`, `is_delete`, `operator`, `operate_time`, `order_by`, `show_in_home`, `permission_code`)
VALUES
(1900000000000001001, 'giftDashboard', '/finance/gift/dashboard', '数据概览', '/src/views/finance/gift/gift-dashboard/index.vue', NULL, 'financeAnalysis', '0', 5, '礼尚往来数据概览', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, 810, '1', 'gift:dashboard'),
(1900000000000001002, 'giftPerson', '/finance/gift/person', '亲友管理', '/src/views/finance/gift/person/index.vue', NULL, 'menuInfo', '0', 5, '礼尚往来亲友管理', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, 820, '1', 'gift:person'),
(1900000000000001003, 'giftEvent', '/finance/gift/event', '事由管理', '/src/views/finance/gift/event/index.vue', NULL, 'dict', '0', 5, '礼尚往来事由管理', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, 830, '1', 'gift:event'),
(1900000000000001004, 'giftRecord', '/finance/gift/record', '礼金记录', '/src/views/finance/gift/record/index.vue', NULL, 'finance', '0', 5, '礼尚往来礼金记录', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, 840, '1', 'gift:record'),
(1900000000000001005, 'giftAnalysis', '/finance/gift/analysis', '统计报表', '/src/views/finance/gift/analysis/index.vue', NULL, 'financeAnalysis', '0', 5, '礼尚往来统计报表', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL, 850, '1', 'gift:analysis');

-- Gift roles
INSERT IGNORE INTO `t_role_info`
(`id`, `role_code`, `role_name`, `summary`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleter`, `delete_time`, `is_delete`, `operator`, `operate_time`)
VALUES
(1900000000000004001, 'gift_admin', '人情管理', '礼尚往来管理角色，组织级数据权限', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL),
(1900000000000004002, 'gift_user', '人情用户', '礼尚往来普通用户角色，用户级数据权限', '1', NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL);

-- gift_admin: all gift page and action permissions
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

-- gift_user: core pages + view/add/edit/delete
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

-- Top-level menu regroup（礼尚往来独立一级菜单）
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

-- Role permission scope fix
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

-- Mobile 亲友详情隐藏菜单（修复新增 404）
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

-- Mobile 事由详情隐藏菜单（修复新增/编辑 404）
INSERT INTO `t_menu_info`
(`id`, `name`, `path`, `title`, `component`, `redirect`, `icon`, `hide_in_menu`, `parent_id`, `summary`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleter`, `delete_time`, `is_delete`, `operator`, `operate_time`, `order_by`, `show_in_home`, `permission_code`)
SELECT
  1900000000000001013,
  'giftEventDetail',
  '/finance/gift/event/giftEventDetail',
  '事由管理详情',
  '/src/views/finance/gift/event/giftEventDetail/index.vue',
  NULL,
  'giftEventDetail',
  '1',
  @gift_menu_id,
  '礼尚往来事由管理详情',
  '1',
  NULL, NOW(), NULL, NULL, NULL, NULL, 0, NULL, NULL,
  31,
  '0',
  'gift:event'
WHERE NOT EXISTS (
  SELECT 1 FROM `t_menu_info` WHERE `name` = 'giftEventDetail'
);

-- ------------------------------------------------------------------------------
-- 个人财务 (selfFinance) 统一命名空间菜单与权限配置 (2026-10-14)
-- 目标: 将个人财务（日常记账、零钱预算、多维分析、账户流水、消费卡）全链路收拢至 selfFinance 空间
-- ------------------------------------------------------------------------------

-- 1. 确保一级菜单「个人财务」存在且路径为 /selfFinance
INSERT INTO `t_menu_info` (
  `id`, `name`, `path`, `title`, `component`, `redirect`, `icon`, `is_frame`, `parent_id`, `show_in_home`, `status`, `sort`, `hide_in_menu`, `permission_code`
) VALUES (
  1800000000000000001, 'selfFinance', '/selfFinance', '个人财务', 'Layout', '/selfFinance/financeManager', 'finance', '0', NULL, '1', '1', 35, '0', 'selfFinance'
) ON DUPLICATE KEY UPDATE
  `name` = 'selfFinance',
  `path` = '/selfFinance',
  `title` = '个人财务',
  `component` = 'Layout',
  `redirect` = '/selfFinance/financeManager',
  `permission_code` = 'selfFinance',
  `status` = '1';

-- 获取个人财务一级菜单 ID 变量
SET @self_finance_parent_id = 1800000000000000001;

-- 2. 更新或挂载个人财务二级子菜单 (全部指向 /src/views/selfFinance/ 路径)
-- (1) 财务信息 (日常收支记账)
UPDATE `t_menu_info`
SET `parent_id` = @self_finance_parent_id,
    `path` = '/selfFinance/financeManager',
    `component` = '/src/views/selfFinance/financeManager/index.vue',
    `permission_code` = 'selfFinance:financeManager',
    `sort` = 10
WHERE `name` = 'financeManager';

-- (2) 零花钱预算管理 (独立动态子路由)
UPDATE `t_menu_info`
SET `parent_id` = @self_finance_parent_id,
    `path` = '/selfFinance/financeManager/budgetManage',
    `component` = '/src/views/selfFinance/financeManager/budgetManage/index.vue',
    `permission_code` = 'selfFinance:financeManager:budget:view',
    `sort` = 15
WHERE `name` = 'financeBudgetManage';

-- (3) 财务多维分析
UPDATE `t_menu_info`
SET `parent_id` = @self_finance_parent_id,
    `path` = '/selfFinance/financeAnalysis',
    `component` = '/src/views/selfFinance/financeAnalysis/index.vue',
    `permission_code` = 'selfFinance:financeAnalysis',
    `sort` = 20
WHERE `name` = 'financeAnalysis';

-- (4) 账户流水信息 (原“猫超管理”)
UPDATE `t_menu_info`
SET `parent_id` = @self_finance_parent_id,
    `title` = '账户流水信息',
    `path` = '/selfFinance/accountRecordInfo',
    `component` = '/src/views/selfFinance/accountRecordInfo/index.vue',
    `permission_code` = 'selfFinance:accountRecordInfo',
    `order_by` = 80
WHERE `name` = 'accountRecordInfo';

-- (5) 消费卡信息 (储值卡总览与开卡)
UPDATE `t_menu_info`
SET `parent_id` = @self_finance_parent_id,
    `path` = '/selfFinance/prepaidCardInfoT',
    `component` = '/src/views/selfFinance/prepaidCardInfoT/index.vue',
    `permission_code` = 'selfFinance:prepaidCardInfoT',
    `sort` = 40
WHERE `name` = 'prepaidCardInfoT';

-- (6) 消费卡交易记录 (充销流水明细)
UPDATE `t_menu_info`
SET `parent_id` = @self_finance_parent_id,
    `path` = '/selfFinance/prepaidConsumeRecordT',
    `component` = '/src/views/selfFinance/prepaidConsumeRecordT/index.vue',
    `permission_code` = 'selfFinance:prepaidConsumeRecordT',
    `sort` = 50
WHERE `name` = 'prepaidConsumeRecordT';

-- 3. 补充/更新 t_permission_info 权限标识码
INSERT INTO `t_permission_info` (`id`, `permission_code`, `name`, `description`, `status`)
VALUES
(1800000000000000010, 'selfFinance', '个人财务', '个人财务一级菜单', '1'),
(1800000000000000011, 'selfFinance:financeManager', '财务信息', '个人/家庭收支记账列表与查看', '1'),
(1800000000000000012, 'selfFinance:financeManager:query', '零钱查询', '日常收支记录查询', '1'),
(1800000000000000013, 'selfFinance:financeManager:add', '零钱记账', '新增日常收支记录', '1'),
(1800000000000000014, 'selfFinance:financeManager:edit', '零钱修改', '修改日常收支记录', '1'),
(1800000000000000015, 'selfFinance:financeManager:delete', '零钱删除', '删除日常收支记录', '1'),
(1800000000000000016, 'selfFinance:financeManager:budget:view', '零钱预算查看', '查看零钱月度预算及试算数据', '1'),
(1800000000000000017, 'selfFinance:financeManager:budget:edit', '零钱预算修改', '保存与配置月度零钱预算额度与分类', '1'),
(1800000000000000018, 'selfFinance:financeAnalysis', '财务分析', '财务多维收支走势与资产看板分析', '1'),
(1800000000000000019, 'selfFinance:accountRecordInfo', '账户流水', '各资金账户流水明细查看与管理', '1'),
(1800000000000000020, 'selfFinance:prepaidCardInfoT', '消费卡信息', '储值卡/消费卡总览与开卡管理', '1'),
(1800000000000000021, 'selfFinance:prepaidConsumeRecordT', '消费卡交易记录', '储值卡充值与消费交易流水查看', '1')
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `description` = VALUES(`description`),
  `status` = '1';

-- 4. 为系统角色授予新的 selfFinance 权限集 (幂等插入)
-- (A) 普通成员与家庭成员 (family_member, user)
INSERT INTO `t_role_permission_info` (`id`, `role_id`, `permission_id`, `summary`, `status`, `is_delete`, `create_time`, `operate_time`)
SELECT
  (2026101410000000000 + ri.id + pi.id) AS `id`,
  CAST(ri.id AS CHAR) AS `role_id`,
  CAST(pi.id AS CHAR) AS `permission_id`,
  CONCAT('grant ', pi.permission_code, ' to ', ri.role_code) AS `summary`,
  '1' AS `status`,
  0 AS `is_delete`,
  NOW(),
  NOW()
FROM `t_permission_info` pi
CROSS JOIN `t_role_info` ri
WHERE pi.permission_code IN (
    'selfFinance',
    'selfFinance:financeManager',
    'selfFinance:financeManager:query',
    'selfFinance:financeManager:add',
    'selfFinance:financeManager:edit',
    'selfFinance:financeManager:delete',
    'selfFinance:financeManager:budget:view',
    'selfFinance:financeAnalysis',
    'selfFinance:accountRecordInfo',
    'selfFinance:prepaidCardInfoT',
    'selfFinance:prepaidConsumeRecordT'
)
  AND ri.role_code IN ('user', 'family_member')
  AND pi.is_delete = 0 AND ri.is_delete = 0
  AND NOT EXISTS (
    SELECT 1 FROM `t_role_permission_info` ex
    WHERE ex.role_id = CAST(ri.id AS CHAR) AND ex.permission_id = CAST(pi.id AS CHAR) AND ex.is_delete = 0
  );

-- (B) 管理员与超管 (family_admin, admin, super_super) -> 授予全量 selfFinance 权限 (含预算编辑)
INSERT INTO `t_role_permission_info` (`id`, `role_id`, `permission_id`, `summary`, `status`, `is_delete`, `create_time`, `operate_time`)
SELECT
  (2026101420000000000 + ri.id + pi.id) AS `id`,
  CAST(ri.id AS CHAR) AS `role_id`,
  CAST(pi.id AS CHAR) AS `permission_id`,
  CONCAT('grant ', pi.permission_code, ' to ', ri.role_code) AS `summary`,
  '1' AS `status`,
  0 AS `is_delete`,
  NOW(),
  NOW()
FROM `t_permission_info` pi
CROSS JOIN `t_role_info` ri
WHERE pi.permission_code LIKE 'selfFinance%'
  AND ri.role_code IN ('family_admin', 'admin', 'super_super')
  AND pi.is_delete = 0 AND ri.is_delete = 0
  AND NOT EXISTS (
    SELECT 1 FROM `t_role_permission_info` ex
    WHERE ex.role_id = CAST(ri.id AS CHAR) AND ex.permission_id = CAST(pi.id AS CHAR) AND ex.is_delete = 0
  );

SET FOREIGN_KEY_CHECKS = 1;
