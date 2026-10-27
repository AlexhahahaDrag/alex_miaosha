CREATE DATABASE IF NOT EXISTS alex_user;
USE alex_user;
/*
 Navicat Premium Data Transfer

 Target Server Type    : MySQL
 File Encoding         : 65001
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_menu_info
-- ----------------------------
DROP TABLE IF EXISTS `t_menu_info`;
CREATE TABLE `t_menu_info` (

  `id` bigint NOT NULL,
  `name` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '菜单名称',
  `path` varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '菜单路径',
  `title` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '菜单标题',
  `component` varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '组件',
  `redirect` varchar(512) DEFAULT NULL COMMENT '跳转',
  `icon` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '菜单图标',
  `hide_in_menu` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '是否隐藏菜单,字典(true_or_false) 1：有效,0:失效)',
  `parent_id` bigint DEFAULT NULL COMMENT '父级机构id',
  `summary` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '备注',
  `status` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '状态,字典(is_valid) 1：有效,0:失效)',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleter` bigint DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint(1) DEFAULT '0' COMMENT '是否删除',
  `operator` bigint DEFAULT NULL COMMENT '修改人',
  `operate_time` datetime DEFAULT NULL COMMENT '更新时间',
  `order_by` int DEFAULT NULL COMMENT '排序',
  `show_in_home` varchar(1) DEFAULT NULL COMMENT '是否在home中展示,字典(true_or_false) 1：展示,0:不展示)',
  `permission_code` varchar(256) DEFAULT NULL COMMENT '权限编码',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `t_menu_info_p_id_IDX` (`parent_id`) USING BTREE,
  KEY `idx_menu_status_order` (`is_delete`,`status`,`order_by`) COMMENT '菜单有效性及排序索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='菜单管理表';

-- ----------------------------
-- Records of t_menu_info
-- ----------------------------
INSERT INTO `t_menu_info` VALUES
  (1,'message','/message','消息管理','Layout','/message/messageManager','messageManager','0',NULL,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,10,'0','message'),
  (2,'messageManager','/message/messageManager','消息管理','/src/views/message/index.vue',NULL,'message','0',1,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,'1','message:messageManager'),
  (3,'user','/user','系统管理','Layout','/user/userManager','userManagerIcon','0',NULL,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,20,'0','user'),
  (5,'finance','/finance','财务管理','Layout','/finance/shopFinance','financeManager','0',NULL,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,30,'0','finance'),
  (6,'financeManager','/selfFinance/financeManager','账单流水','/src/views/finance/financeManager/index.vue',NULL,'finance','0',6789,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,10,'1','selfFinance:financeManager'),
  (7,'financeAnalysis','/selfFinance/financeAnalysis','收支分析','/src/views/finance/financeAnalysis/index.vue',NULL,'financeAnalysis','0',6789,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,40,'1','selfFinance:financeAnalysis'),
  (8,'accountRecordInfo','/selfFinance/accountRecordInfo','账户流水信息','/src/views/selfFinance/accountRecordInfo/index.vue',NULL,'accountRecordInfo','0',NULL,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,80,'1','selfFinance:accountRecordInfo'),
  (9,'dict','/dict/dictInfo','字典信息','/src/views/dict/index.vue',NULL,'dict','0',2000000000000000001,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,10,'1','dict:dictInfo'),
  (10,'userManager','/user/userManager','用户管理','/src/views/user/userManager/index.vue',NULL,'user','0',3,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,20,'1','user:userManager'),
  (11,'orgManager','/user/orgManager','机构管理','/src/views/user/orgInfo/index.vue',NULL,'org','0',3,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,10,'1','user:orgManager'),
  (12,'menuInfo','/user/menuInfo','菜单管理','/src/views/user/menuInfo/index.vue',NULL,'menuInfo','0',3,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,40,'1','user:menuInfo'),
  (17,'incomeAnalysis','/selfFinance/financeAnalysis/incomeAnalysis','收入分析','/src/views/finance/financeAnalysis/detail/incomeAnalysis.vue',NULL,'incomeAnalysis','1',6789,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,50,'0','selfFinance:financeAnalysis:incomeAnalysis'),
  (18,'overview','/selfFinance/financeAnalysis/overview','总览','/src/views/finance/financeAnalysis/detail/overview.vue',NULL,'overview','1',6789,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,60,'0','selfFinance:financeAnalysis:overview'),
  (19,'incomeDetail','/selfFinance/financeAnalysis/incomeDetail','收入明细','/src/views/finance/financeAnalysis/detail/incomeDetail.vue',NULL,'incomeDetail','1',6789,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,70,'0','selfFinance:financeAnalysis:incomeDetail'),
  (22,'menuInfoDetail','/user/menuInfo/menuInfoDetail','菜单管理详情','/src/views/user/menuInfo/menuInfoDetail/index.vue',NULL,'menuInfoDetail','1',3,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,10,'0','user:menuInfo:menuInfoDetail'),
  (23,'orgInfoDetail','/user/orgManager/orgInfoDetail','机构管理详情','/src/views/user/orgInfo/orgInfoDetail/index.vue',NULL,'orgInfoDetail','1',3,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,10,'0','user:orgManager:orgManagerDetail'),
  (24,'dictDetail','/dict/dictInfo/dictDetail','字典信息详情','/src/views/dict/dict-detail/index.vue',NULL,'dictDetail','1',2000000000000000001,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,10,'0','dict:dictDetail'),
  (25,'financeManagerDetail','/selfFinance/financeManager/financeManagerDetail','账单流水详情','/src/views/finance/financeManager/financeManagerDetail/index.vue',NULL,'financeDetail','1',6789,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,10,'0','selfFinance:financeManager:financeManagerDetail'),
  (29,'dictDetail','/dict/dictInfo/dictDetail','字典信息详情','/src/views/dict/dict-detail/index.vue',NULL,'dictDetail','1',2000000000000000001,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,20,'0','dict:dictDetail'),
  (30,'accountRecordInfoDetail','/selfFinance/accountRecordInfo/accountRecordInfoDetail','猫超管理详情','/src/views/finance/accountRecordInfo/accountRecordInfoDetail/index.vue',NULL,'accountRecordInfoDetail','1',6789,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,80,'0','finance:accountRecordInfo:accountRecordInfoDetail'),
  (31,'pmsAttrDetail','/product/pmsAttr/pmsAttrDetail','商品属性详情','/src/views/product/pmsAttr/pmsAttrDetail/index.vue',NULL,'pmsAttrDetail','1',1739906000547254274,NULL,'1',NULL,'2023-12-28 08:33:55',NULL,'2023-12-28 19:50:55',NULL,NULL,0,NULL,'2023-12-28 19:50:55',10,'0','pmsAttrDetail'),
  (33,'shopFinanceAnalysis','/finance/shopFinanceAnalysis','店财务分析','/src/views/finance/shopFinanceAnalysis/index.vue',NULL,'shopFinanceAnalysis','0',5,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,150,'1','finance:shopFinanceAnalysis'),
  (34,'shopIncomeAnalysis','/finance/shopFinanceAnalysis/shopIncomeAnalysis','店收入分析','/src/views/finance/shopFinanceAnalysis/detail/shopIncomeAnalysis.vue',NULL,'shopIncomeAnalysis','1',5,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,50,'0','finance:shopFinanceAnalysis:shopIncomeAnalysis'),
  (35,'shopOverview','/finance/shopFinanceAnalysis/shopOverview','总览','/src/views/finance/shopFinanceAnalysis/detail/shopOverview.vue',NULL,'shopOverview','1',5,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,60,'0','finance:shopFinanceAnalysis:shopOverview'),
  (36,'shopIncomeDetail','/finance/shopFinanceAnalysis/shopIncomeDetail','店收入明细','/src/views/finance/shopFinanceAnalysis/detail/shopIncomeDetail.vue',NULL,'shopIncomeDetail','1',5,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,70,'0','finance:shopFinanceAnalysis:shopIncomeDetail'),
  (37,'shopStockAnalysis','/finance/shopStockAnalysis','店库存分析','/src/views/finance/shopStockAnalysis/index.vue',NULL,'shopStockAnalysis','0',5,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,150,'1','finance:shopStockAnalysis'),
  (38,'shopStockInfoAnalysis','/finance/shopStockAnalysis/shopStockInfoAnalysis','店收入分析','/src/views/finance/shopStockAnalysis/detail/shopStockInfoAnalysis.vue',NULL,'shopStockInfoAnalysis','1',5,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,50,'0','finance:shopStockAnalysis:shopStockInfoAnalysis'),
  (39,'shopStockOverview','/finance/shopStockAnalysis/shopStockOverview','总览','/src/views/finance/shopStockAnalysis/detail/shopStockOverview.vue',NULL,'shopStockOverview','1',5,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,60,'0','finance:shopStockAnalysis:shopStockOverview'),
  (40,'shopStockAnalysisDetail','/finance/shopStockAnalysis/shopStockshopStockDetailDetail','店库存分析明细','/src/views/finance/shopStockAnalysis/detail/shopStockAnalysisDetail.vue',NULL,'shopStockAnalysisDetail','1',5,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,70,'0','finance:shopStockAnalysis:shopStockAnalysisDetail'),
  (41,'cpnCoupon','/cpn-coupon','消费券管理','Layout','/cpn-coupon/cpn-coupon-info','cpnCouponManager','0',NULL,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,30,'0','cpnCoupon'),
  (6789,'selfFinance','/selfFinance','个人财务','Layout','/selfFinance/financeManager','financeManager','0',NULL,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,30,'0','selfFinance'),
  (176737064364695,'shopProduct','/finance/shopProduct','店铺商品','/src/views/finance/shopProduct/index.vue',NULL,'shopProduct','0',5,NULL,'1',NULL,'2024-03-12 10:02:52',NULL,'2024-03-12 16:49:31',NULL,NULL,0,NULL,'2024-03-12 16:49:31',610,'1','finance:shopProduct'),
  (176737064429707,'shopProductDetail','/finance/shopProduct/shopProductDetail','店铺商品详情','/src/views/finance/shopProduct/shopProductDetail/index.vue',NULL,'shopProductDetail','1',5,NULL,'1',NULL,'2024-03-12 10:02:52',NULL,'2024-03-12 16:49:31',NULL,NULL,0,NULL,'2024-03-12 16:49:31',610,'0','finance:shopProduct:detail'),
  (176737064364695123,'saleTicket','/finance/saleTicket','店铺销售单','/src/views/finance/saleTicket/index.vue',NULL,'saleTicket','0',5,NULL,'1',NULL,'2024-03-12 10:02:52',NULL,'2024-03-12 16:49:31',NULL,NULL,0,NULL,'2024-03-12 16:49:31',710,'0','finance:saleTicket'),
  (1739906000547254274,'product','/product','商品管理','Layout','/product/pmsAttr','product','0',NULL,NULL,'1',NULL,'2023-12-27 15:08:11',NULL,NULL,NULL,NULL,0,NULL,'2023-12-27 15:08:11',40,'0','product'),
  (1740169165491191809,'pmsAttr','/product/pmsAttr','商品属性','/src/views/product/pmsAttr/index.vue',NULL,'pmsAttr','0',1739906000547254274,NULL,'1',NULL,'2023-12-28 08:33:54',NULL,'2023-12-28 19:50:54',NULL,NULL,0,NULL,'2023-12-28 19:50:54',10,'1','product:pmsAttr'),
  (1740169166007091202,'pmsAttrDetail','/product/pmsAttr/pmsAttrDetail','商品属性详情','/src/views/product/pmsAttr/detail/index.vue',NULL,'pmsAttrDetail','1',1739906000547254274,NULL,'1',NULL,'2023-12-28 08:33:55',NULL,'2023-12-28 19:50:55',NULL,NULL,0,NULL,'2023-12-28 19:50:55',10,'0','pmsAttrDetail'),
  (1746531627376271361,'roleInfo','/user/roleInfo','角色管理','/src/views/user/roleInfo/index.vue',NULL,'roleInfo','0',3,NULL,'1',NULL,'2024-01-14 21:56:04',NULL,'2024-11-08 16:54:44',NULL,NULL,0,NULL,'2024-11-08 16:54:44',30,'1','user:roleInfo'),
  (1746531628089303041,'roleInfoDetail','/user/roleInfo/roleInfoDetail','角色表详情','/src/views/user/roleInfo/roleInfoDetail/index.vue',NULL,'roleInfoDetail','1',3,NULL,'1',NULL,'2024-01-14 21:56:04',NULL,'2024-11-08 16:54:44',NULL,NULL,0,NULL,'2024-11-08 16:54:44',220,'0','user:roleInfo:detail'),
  (1747162769425887234,'permissionInfo','/user/permissionInfo','权限管理','/src/views/user/permissionInfo/index.vue',NULL,'permissionInfo','0',3,NULL,'1',NULL,'2024-01-16 15:44:00',NULL,NULL,NULL,NULL,0,NULL,'2024-01-16 15:44:00',50,'1','user:permissionInfo'),
  (1747162770101170177,'permissionInfoDetail','/user/permissionInfo/permissionInfoDetail','权限管理详情','/src/views/user/permissionInfo/permissionInfoDetail/index.vue',NULL,'permissionInfoDetail','1',3,NULL,'1',NULL,'2024-01-16 15:44:00',NULL,NULL,NULL,NULL,0,NULL,'2024-01-16 15:44:00',10,'0','user:permissionInfo'),
  (1761015127361941505,'shopFinance','/finance/shopFinance','店财务管理','/src/views/finance/shopFinance/index.vue',NULL,'shopFinance','0',5,NULL,'1',NULL,'2024-02-23 21:08:19',NULL,'2024-02-23 21:19:51',NULL,NULL,0,NULL,'2024-02-23 21:19:51',110,'1','finance:shopFinance'),
  (1761015128049807361,'shopFinanceDetail','/finance/shopFinance/shopFinanceDetail','店财务管理详情','/src/views/finance/shopFinance/shopFinanceDetail/index.vue',NULL,'shopFinanceDetail','1',5,NULL,'1',NULL,'2024-02-23 21:08:19',NULL,'2024-02-23 21:19:51',NULL,NULL,0,NULL,'2024-02-23 21:19:51',110,'0','finance:shopFinance:shopFinanceDetail'),
  (1767370643646951232,'shoppingCart','/finance/shoppingCart','店铺购物车','/src/views/finance/shoppingCart/index.vue',NULL,'shoppingCart','0',5,NULL,'1',NULL,'2024-03-12 10:02:52',NULL,'2024-03-12 16:49:31',NULL,NULL,0,NULL,'2024-03-12 16:49:31',810,'0','finance:shoppingCart'),
  (1767370643646955521,'shopStock','/finance/shopStock','店铺库存','/src/views/finance/shopStock/index.vue',NULL,'shopStock','0',5,NULL,'1',NULL,'2024-03-12 10:02:52',NULL,'2024-03-12 16:49:31',NULL,NULL,0,NULL,'2024-03-12 16:49:31',610,'1','finance:shopStock'),
  (1767370644297072641,'shopStockDetail','/finance/shopStock/shopStockDetail','店铺库存详情','/src/views/finance/shopStock/shopStockDetail/index.vue',NULL,'shopStockDetail','1',5,NULL,'1',NULL,'2024-03-12 10:02:52',NULL,'2024-03-12 16:49:31',NULL,NULL,0,NULL,'2024-03-12 16:49:31',610,'0','finance:shopStock:detail'),
  (1775096129399533569,'shopCartDetail','/finance/shopCart/shopCartDetail','购物车详情','/src/views/finance/shopCart/shopCartDetail/index.vue',NULL,'shopCartDetail','1',5,NULL,'1',NULL,'2024-04-02 17:41:12',NULL,'2024-04-03 11:36:20',NULL,NULL,0,NULL,'2024-04-03 11:36:20',900,'0','finance:shopCart:detail'),
  (1777596694847160321,'shopOrder','/finance/shopOrder','销售单管理','/src/views/finance/shopOrder/index.vue',NULL,'shopOrder','0',5,NULL,'1',NULL,'2024-04-09 15:17:33',NULL,'2024-04-09 15:20:02',NULL,NULL,0,NULL,'2024-04-09 15:20:02',940,'1','finance:shopOrder'),
  (1777596695488888834,'shopOrderDetail','/finance/shopOrder/shopOrderDetail','销售单管理','/src/views/finance/shopOrder/shopOrderDetail/index.vue',NULL,'shopOrderDetail','0',5,NULL,'1',NULL,'2024-04-09 15:17:33',NULL,'2024-04-09 15:35:22',NULL,NULL,0,NULL,'2024-04-09 15:35:22',950,'0','finance:shopOrderDetail'),
  (1780202113667162114,'shopStockAttrs','/finance/shopStockAttrs','库存商品属性','/src/views/finance/shopStockAttrs/index.vue',NULL,'shopStockAttrs','0',5,NULL,'1',NULL,'2024-04-16 19:50:33',NULL,NULL,NULL,NULL,0,NULL,'2024-04-16 19:50:33',960,'0','finance:shopStockAttrs'),
  (1780202114447302658,'shopStockAttrsDetail','/finance/shopStockAttrs/shopStockAttrsDetail','库存商品属性详情','/src/views/finance/shopStockAttrs/shopStockAttrsDetail/index.vue',NULL,'shopStockAttrsDetail','1',5,NULL,'1',NULL,'2024-04-16 19:50:33',NULL,NULL,NULL,NULL,0,NULL,'2024-04-16 19:50:33',10,'0','finance:shopStockAttrs:detail'),
  (1788861688989581314,'shopStockBatch','/finance/shopStockBatch','商品库存批次','/src/views/finance/shopStockBatch/index.vue',NULL,'shopStockBatch','0',5,NULL,'1',NULL,'2024-05-10 17:20:37',NULL,'2024-05-10 17:30:41',NULL,NULL,0,NULL,'2024-05-10 17:30:41',990,'1','finance:shopStockBatch'),
  (1788861689564200962,'shopStockBatchDetail','/finance/shopStockBatch/shopStockBatchDetail','商品库存批次详情','/src/views/finance/shopStockBatch/shopStockBatchDetail/index.vue',NULL,'shopStockBatchDetail','1',5,NULL,'1',NULL,'2024-05-10 17:20:37',NULL,'2024-05-10 17:30:41',NULL,NULL,0,NULL,'2024-05-10 17:30:41',990,'0','finance:shopStockBatch:detail'),
  (1810856881968091138,'personalGift','/selfFinance/personalGift','个人随礼信息','/src/views/finance/personalGift/index.vue',NULL,'personalGift','0',6789,NULL,'1',NULL,'2024-07-10 10:01:39',NULL,'2026-09-19 13:07:52',NULL,NULL,1,NULL,'2024-07-10 10:01:39',1000,'1','selfFinance:personalGift'),
  (1810856882504962050,'personalGiftDetail','/selfFinance/personalGift/personalGiftDetail','个人随礼信息详情','/src/views/finance/personalGift/personalGiftDetail/index.vue',NULL,'personalGiftDetail','1',6789,NULL,'1',NULL,'2024-07-10 10:01:40',NULL,'2026-09-19 13:07:52',NULL,NULL,1,NULL,'2024-07-10 10:01:40',10,'0','selfFinance:personalGift:detail'),
  (1900000000000001000,'gift','/finance/gift','礼尚往来管理','Layout','/finance/gift/dashboard','finance','0',NULL,'礼尚往来管理','1',NULL,'2026-05-19 17:37:31',NULL,NULL,NULL,NULL,0,NULL,NULL,35,'0','gift'),
  (1900000000000001001,'giftDashboard','/finance/gift/giftDashboard','数据概览','/src/views/finance/gift/gift-dashboard/index.vue',NULL,'financeAnalysis','0',1900000000000001000,'礼尚往来数据概览','1',NULL,'2026-05-13 14:22:05',NULL,NULL,NULL,NULL,0,NULL,NULL,10,'1','gift:dashboard'),
  (1900000000000001002,'giftPerson','/finance/gift/person','亲友管理','/src/views/finance/gift/person/index.vue',NULL,'menuInfo','0',1900000000000001000,'礼尚往来亲友管理','1',NULL,'2026-05-13 14:22:05',NULL,NULL,NULL,NULL,0,NULL,NULL,20,'1','gift:person'),
  (1900000000000001003,'giftEvent','/finance/gift/event','事由管理','/src/views/finance/gift/event/index.vue',NULL,'dict','0',1900000000000001000,'礼尚往来事由管理','1',NULL,'2026-05-13 14:22:05',NULL,NULL,NULL,NULL,0,NULL,NULL,30,'1','gift:event'),
  (1900000000000001004,'giftRecord','/finance/gift/record','礼金记录','/src/views/finance/gift/record/index.vue',NULL,'finance','0',1900000000000001000,'礼尚往来礼金记录','1',NULL,'2026-05-13 14:22:05',NULL,NULL,NULL,NULL,0,NULL,NULL,40,'1','gift:record'),
  (1900000000000001005,'giftAnalysis','/finance/gift/analysis','统计报表','/src/views/finance/gift/analysis/index.vue',NULL,'financeAnalysis','0',1900000000000001000,'礼尚往来统计报表','1',NULL,'2026-05-13 14:22:05',NULL,NULL,NULL,NULL,0,NULL,NULL,50,'1','gift:analysis'),
  (1900000000000001012,'giftPersonDetail','/finance/gift/person/giftPersonDetail','亲友管理详情','/src/views/finance/gift/person/giftPersonDetail/index.vue',NULL,'giftPersonDetail','1',1900000000000001000,'礼尚往来亲友管理详情','1',NULL,'2026-07-24 14:10:33',NULL,NULL,NULL,NULL,0,NULL,NULL,21,'0','gift:person'),
  (1900000000000001013,'giftEventDetail','/finance/gift/event/giftEventDetail','事由管理详情','/src/views/finance/gift/event/giftEventDetail/index.vue',NULL,'giftEventDetail','1',1900000000000001000,'礼尚往来事由管理详情','1',NULL,'2026-07-28 11:24:48',NULL,NULL,NULL,NULL,0,NULL,NULL,31,'0','gift:event'),
  (1917366953136087041,'prepaidConsumeRecordT','/selfFinance/prepaidConsumeRecordT','消费卡交易记录','/src/views/selfFinance/prepaidConsumeRecordT/index.vue',NULL,'prepaidConsumeRecordT','0',6789,NULL,'1',NULL,'2025-04-30 07:54:39',NULL,'2025-04-30 08:21:48',NULL,NULL,0,NULL,'2025-04-30 08:21:48',1070,'1','selfFinance:prepaidConsumeRecordT'),
  (1917366953480019970,'prepaidConsumeRecordTDetail','/selfFinance/prepaidConsumeRecordT/prepaidConsumeRecordTDetail','消费卡交易记录详情','/src/views/selfFinance/prepaidConsumeRecordT/prepaidConsumeRecordTDetail/index.vue',NULL,'prepaidConsumeRecordTDetail','1',6789,NULL,'1',NULL,'2025-04-30 07:54:39',NULL,'2025-04-30 08:21:48',NULL,NULL,0,NULL,'2025-04-30 08:21:48',1070,'0','selfFinance:prepaidConsumeRecordT:detail'),
  (1917366957791764482,'prepaidCardInfoT','/selfFinance/prepaidCardInfoT','消费卡信息','/src/views/selfFinance/prepaidCardInfoT/index.vue',NULL,'prepaidCardInfoT','0',6789,NULL,'1',NULL,'2025-04-30 07:54:40',NULL,'2025-04-30 08:21:49',NULL,NULL,0,NULL,'2025-04-30 08:21:49',1080,'1','selfFinance:prepaidCardInfoT'),
  (1917366957791764483,'consumeWaterInfo','/selfFinance/prepaidCardInfoT/consume-water-info','消费流水信息','/src/views/selfFinance/prepaidCardInfoT/consume-water-info/index.vue',NULL,'consume-water-info','0',6789,NULL,'1',NULL,'2025-04-30 07:54:40',NULL,'2025-04-30 08:21:49',NULL,NULL,0,NULL,'2025-04-30 08:21:49',1080,'0','selfFinance:prepaidCardInfoT:consume-water-info'),
  (1917366958253137921,'prepaidCardInfoTDetail','/selfFinance/prepaidCardInfoT/prepaidCardInfoTDetail','消费卡信息详情','/src/views/selfFinance/prepaidCardInfoT/prepaidCardInfoTDetail/index.vue',NULL,'prepaidCardInfoTDetail','1',6789,NULL,'1',NULL,'2025-04-30 07:54:40',NULL,'2025-04-30 08:21:49',NULL,NULL,0,NULL,'2025-04-30 08:21:49',1080,'0','selfFinance:prepaidCardInfoT:detail'),
  (1917366958253137922,'prepaidCardInfoTConsumeInfo','/selfFinance/prepaidCardInfoT/consumeInfo','消费卡信息消费信息','/src/views/selfFinance/prepaidCardInfoT/consume-info/index.vue',NULL,'prepaidCardInfoTDetail','1',6789,NULL,'1',NULL,'2025-04-30 07:54:40',NULL,'2025-04-30 08:21:49',NULL,NULL,0,NULL,'2025-04-30 08:21:49',1080,'0','selfFinance:prepaidCardInfoT:consumeInfo'),
  (1930875849759154177,'userUser','/user/user','用户','/src/views/user/user/index.vue',NULL,'user','0',3,NULL,'1',NULL,'2025-06-06 14:34:11',NULL,'2026-10-15 22:27:13',NULL,NULL,1,NULL,'2025-06-06 14:34:11',230,'1','user:user'),
  (1930875850400882689,'userDetail','/user/user/userDetail','用户详情','/src/views/user/user/userDetail/index.vue',NULL,'userDetail','1',3,NULL,'1',NULL,'2025-06-06 14:34:11',NULL,'2026-10-15 22:27:13',NULL,NULL,1,NULL,'2025-06-06 14:34:11',10,'0','user:user:detail'),
  (2000000000000000001,'dictManage','/dict','系统配置','Layout','/dict/dictInfo','dict','0',NULL,'系统配置','1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,38,'0','dict'),
  (2001229416389832706,'cpnRedemptionRecordInfo','/cpn-coupon/cpn-redemption-record-info','消费券核销记录表','/src/views/cpn-coupon/cpn-redemption-record-info/index.vue',NULL,'cpnRedemptionRecordInfo','0',41,NULL,'1',NULL,'2025-12-17 17:54:09',NULL,NULL,NULL,NULL,0,NULL,'2025-12-17 17:54:09',1000,'1','cpnCoupon:cpnRedemptionRecordInfo'),
  (2001229426955284482,'cpnRedemptionRecordInfoDetail','/cpn-coupon/cpn-redemption-record-info/cpn-redemption-recordInfo-detail','消费券核销记录表详情','/src/views/cpn-coupon/cpn-redemption-record-info/cpn-redemption-record-info-detail/index.vue',NULL,'cpnRedemptionRecordInfoDetail','1',41,NULL,'1',NULL,'2025-12-17 17:54:11',NULL,NULL,NULL,NULL,0,NULL,'2025-12-17 17:54:11',10,'0','cpnCoupon:cpnRedemptionRecordInfo:detail'),
  (2001229574703837186,'cpnCouponInfo','/cpn-coupon/cpn-coupon-info','消费券信息表','/src/views/cpn-coupon/cpn-coupon-info/index.vue',NULL,'cpnCouponInfo','0',41,NULL,'1',NULL,'2025-12-17 17:54:46',NULL,NULL,NULL,NULL,0,NULL,'2025-12-17 17:54:46',1010,'1','cpnCoupon:cpnCouponInfo'),
  (2001229585252511746,'cpnCouponInfoDetail','/cpn-coupon/cpn-coupon-info/cpn-coupon-info-detail','消费券信息表详情','/src/views/cpn-coupon/cpn-coupon-info/cpn-coupon-info-detail/index.vue',NULL,'cpnCouponInfoDetail','1',41,NULL,'1',NULL,'2025-12-17 17:54:49',NULL,NULL,NULL,NULL,0,NULL,'2025-12-17 17:54:49',10,'0','cpnCoupon:cpnCouponInfo:detail'),
  (2001229585252511747,'cpnCouponInfoRedeem','/cpn-coupon/cpn-coupon-info/cpn-coupon-info-redeem','消费券信息表核销','/src/views/cpn-coupon/cpn-coupon-info/cpn-coupon-info-redeem/index.vue',NULL,'cpnCouponInfoRedeem','1',41,NULL,'1',NULL,'2025-12-17 17:54:49',NULL,NULL,NULL,NULL,0,NULL,'2025-12-17 17:54:49',10,'0','cpnCoupon:cpnCouponInfo:redeem'),
  (2001229780916793345,'cpnUserCouponInfo','/cpn-coupon/cpn-user-coupon-info','用户消费券库存表','/src/views/cpn-coupon/cpn-user-coupon-info/index.vue',NULL,'cpnUserCouponInfo','0',41,NULL,'1',NULL,'2025-12-17 17:55:35',NULL,NULL,NULL,NULL,0,NULL,'2025-12-17 17:55:35',1020,'1','cpnCoupon:cpnUserCouponInfo'),
  (2001229791423524866,'cpnUserCouponInfoDetail','/cpn-coupon/cpn-user-coupon-info/cpn-user-coupon-info-detail','用户消费券库存表详情','/src/views/cpn-coupon/cpn-user-coupon-info/cpn-user-coupon-info-detail/index.vue',NULL,'cpnUserCouponInfoDetail','1',41,NULL,'1',NULL,'2025-12-17 17:55:38',NULL,NULL,NULL,NULL,0,NULL,'2025-12-17 17:55:38',10,'0','cpnCoupon:cpnUserCouponInfo:detail'),
  (2026101200000000001,'financeBudgetManage','/finance/financeManager/budgetManage','零花钱预算管理','/src/views/finance/financeManager/budgetManage/index.vue',NULL,'budget','1',5,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,15,'1','finance:financeManager:budget:view');

-- ----------------------------
-- Table structure for t_menu_user_info
-- ----------------------------
DROP TABLE IF EXISTS `t_menu_user_info`;
CREATE TABLE `t_menu_user_info` (

  `id` bigint NOT NULL,
  `name` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '菜单名称',
  `path` varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '菜单路径',
  `title` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '菜单标题',
  `icon` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '菜单图标',
  `component` varchar(512) DEFAULT NULL COMMENT '组件',
  `redirect` varchar(512) DEFAULT NULL COMMENT '跳转',
  `hide_in_menu` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '是否隐藏菜单',
  `p_id` bigint DEFAULT NULL COMMENT '父级机构id',
  `user_id` bigint DEFAULT NULL COMMENT '用户id',
  `role_id` bigint DEFAULT NULL COMMENT '角色id',
  `summary` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '备注',
  `status` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '状态',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleter` bigint DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint(1) DEFAULT '0' COMMENT '是否删除',
  `operator` bigint DEFAULT NULL COMMENT '修改人',
  `operate_time` datetime DEFAULT NULL COMMENT '更新时间',
  `show_in_pc` tinyint DEFAULT NULL COMMENT '是否在pc端显示',
  `show_in_mobile` tinyint DEFAULT NULL COMMENT '是否在移动端显示',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_menu_user_uid` (`user_id`,`is_delete`,`status`) COMMENT '用户专属菜单检索索引',
  KEY `idx_menu_user_rid` (`role_id`,`is_delete`,`status`) COMMENT '角色专属菜单检索索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='菜单用户管理表';

-- ----------------------------
-- Table structure for t_org_info
-- ----------------------------
DROP TABLE IF EXISTS `t_org_info`;
CREATE TABLE `t_org_info` (

  `id` bigint NOT NULL,
  `org_code` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '机构编码',
  `org_name` varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '机构名称',
  `org_short_name` varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '机构简称',
  `parent_id` bigint DEFAULT NULL COMMENT '父级机构id',
  `summary` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '简介',
  `status` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '状态,字典(is_valid) 1：有效,0:失效)',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleter` bigint DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint(1) DEFAULT '0' COMMENT '是否删除',
  `operator` bigint DEFAULT NULL COMMENT '修改人',
  `operate_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_org_parent_id` (`parent_id`,`is_delete`,`status`) COMMENT '父子组织架构检索索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='机构表';

-- ----------------------------
-- Records of t_org_info
-- ----------------------------
INSERT INTO `t_org_info` VALUES
  (0,'org','机构',NULL,NULL,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (1,'cpj','臭屁家','臭屁家',NULL,'臭屁家','1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (2,'test1',NULL,NULL,NULL,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (1732681655123001345,'13','213','123',1735563603956748289,'213','1',NULL,'2023-12-07 16:41:13',NULL,NULL,NULL,NULL,0,NULL,'2023-12-07 16:41:13'),
  (1735563603956748289,'ddwd','丁丁尾单','ddwd',NULL,'丁丁尾单一家时尚衣服店','1',NULL,'2023-12-15 15:33:03',NULL,NULL,NULL,NULL,0,NULL,'2023-12-15 15:33:03'),
  (1735563796248809473,'test2','测试机构2','test2',NULL,'dsgsdg','1',NULL,'2023-12-15 15:33:49',NULL,NULL,NULL,NULL,0,NULL,'2023-12-15 15:33:49');

-- ----------------------------
-- Table structure for t_org_user_info
-- ----------------------------
DROP TABLE IF EXISTS `t_org_user_info`;
CREATE TABLE `t_org_user_info` (

  `id` bigint NOT NULL,
  `org_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '公司角色id',
  `user_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '用户id',
  `summary` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '描述',
  `status` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '状态,字典(is_valid) 1：有效,0:失效)',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleter` bigint DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint(1) DEFAULT '0' COMMENT '是否删除',
  `operator` bigint DEFAULT NULL COMMENT '修改人',
  `operate_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_org_user_uid_oid` (`user_id`,`org_id`,`is_delete`,`status`) COMMENT '用户组织联合检索索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户公司信息表';

-- ----------------------------
-- Records of t_org_user_info
-- ----------------------------
INSERT INTO `t_org_user_info` VALUES
  (-1,'0','1759760891393945602',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (1,'1','1',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (2,'1','2',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (3,'1735563603956748289','1763166043771621378',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (4,'1735563603956748289','1766998444679483393',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (5,'2','2046949449054392321',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (6,'2','2046950134013595649',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL);

-- ----------------------------
-- Table structure for t_permission_info
-- ----------------------------
DROP TABLE IF EXISTS `t_permission_info`;
CREATE TABLE `t_permission_info` (

  `id` bigint NOT NULL,
  `permission_code` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '权限编码',
  `permission_name` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '权限名称',
  `summary` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '描述',
  `status` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '状态,字典(is_valid) 1：有效,0:失效)',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleter` bigint DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint(1) DEFAULT '0' COMMENT '是否删除',
  `operator` bigint DEFAULT NULL COMMENT '修改人',
  `operate_time` datetime DEFAULT NULL COMMENT '更新时间',
  `options` text COMMENT 'url',
  `parent_id` bigint DEFAULT NULL COMMENT '父级id',
  PRIMARY KEY (`id`) USING BTREE

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='权限信息表';

-- ----------------------------
-- Records of t_permission_info
-- ----------------------------
INSERT INTO `t_permission_info` VALUES
  (1,'message','消息管理',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2,'message:messageManager','消息管理',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (5,'finance','财务管理',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (6,'selfFinance:financeManager','账单流水',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (7,'selfFinance:financeAnalysis','收支分析',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (8,'selfFinance:accountRecordInfo','猫超管理',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (9,'dict:dictInfo','字典信息',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (10,'user:userManager','用户信息',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (11,'user:orgManager','机构管理',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (12,'user:menuInfo','菜单管理',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (13,'user:menuInfo:menuInfoDetail','菜单管理详情',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (14,'user:orgManager:orgManagerDetail','机构管理详情',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (15,'finance:dict:dictDetail','字典信息详情',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (16,'selfFinance:financeManager:financeManagerDetail','账单流水详情',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (17,'selfFinance:financeAnalysis:incomeAnalysis','收入分析',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (18,'selfFinance:financeAnalysis:overview','总览',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (19,'selfFinance:financeAnalysis:incomeDetail','收入明细',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (20,'finance:dict:dictDetail','字典信息详情',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (21,'selfFinance:accountRecordInfo:accountRecordInfoDetail','猫超管理详情',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (22,'user:menuInfo:menuInfoDetail','菜单管理详情',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (23,'user:orgManager:orgInfoDetail','机构管理详情',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (24,'finance:dict:dictDetail','字典信息详情',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (25,'selfFinance:financeManager:financeManagerDetail','账单流水详情',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (29,'finance:dict:dictDetail','字典信息详情',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (30,'selfFinance:accountRecordInfo:accountRecordInfoDetail','猫超管理详情',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (31,'pmsAttrDetail','商品属性详情',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (32,'finance:shopFinance:shopFinanceDetail','店财务管理详情',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,1761018029702856705),
  (33,'finance:shopFinanceAnalysis','店铺财务分析',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (34,'finance:shopFinanceAnalysis:shopIncomeAnalysis','店铺收入分析',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (35,'finance:shopFinanceAnalysis:shopOverview','店铺总览',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (36,'finance:shopFinanceAnalysis:shopIncomeDetail','店铺收入明细',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (37,'finance:shopStockAnalysis','店铺库存分析',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (39,'finance:shopStockAnalysis:shopStockOverview','店铺库存总览',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (40,'finance:shopStockAnalysis:shopStockAnalysisDetail','店铺库存分析明细',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (41,'cpnCoupon','消费券管理',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (8768,'selfFinance','个人财务',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (17673872060307,'finance:saleTicket','店铺销售单',NULL,'1',NULL,'2024-03-12 11:08:41',NULL,'2024-03-12 16:49:31',NULL,NULL,0,NULL,'2024-03-12 16:49:31','/finance/saleTicket',5),
  (176738720603070,'finance:shopProduct','店铺商品',NULL,'1',NULL,'2024-03-12 11:08:41',NULL,'2024-03-12 16:49:31',NULL,NULL,0,NULL,'2024-03-12 16:49:31','/finance/shopProduct',5),
  (176738720603072,'finance:shoppingCart','店铺购物车',NULL,'1',NULL,'2024-03-12 11:08:41',NULL,'2024-03-12 16:49:31',NULL,NULL,0,NULL,'2024-03-12 16:49:31','1746681677402292226',5),
  (176738723900212,'finance:shopProduct:detail','店铺商品详情',NULL,'1',NULL,'2024-03-12 11:08:49',NULL,NULL,NULL,NULL,0,NULL,'2024-03-12 11:08:49','/finance/shopProduct/shopProductDetail',5),
  (1739906000547254274,'product','商品管理',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (1740169165491191809,'product:pmsAttr','商品属性',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (1740169166007091202,'product:pmsAttr:pmsAttrDetail','商品属性详情',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (1746531627376271361,'user:roleInfo','角色管理',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (1746531628089303041,'user:roleInfo:roleInfoDetail','角色管理详情',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (1746780102696022018,'user:orgUserInfo','用户公司信息',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (1746780103224504322,'user:orgUserInfo:orgUserInfoDetail','用户公司信息详情',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (1746780105615257602,'user:roleUserInfo','用户角色信息',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (1746780106194071554,'user:roleUserInfo:roleUserInfoDetail','用户角色信息详情',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (1747162769425887234,'user:permissionInfo','权限信息表',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (1747162770101170177,'user:permissionInfo:permissionInfoDetail','权限信息表',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (1747518319657431041,'user:rolePermissionInfo','角色权限信息',NULL,'1',NULL,'2024-01-17 15:16:49',NULL,'2024-01-19 14:52:23',NULL,NULL,0,NULL,'2024-01-19 14:52:23','/user/rolePermissionInfo',3),
  (1747520680119422977,'user:rolePermissionInfo:detail','角色权限信息详情',NULL,'1',NULL,'2024-01-17 15:26:12',NULL,'2024-01-19 14:52:23',NULL,NULL,0,NULL,'2024-01-19 14:52:23','/user/rolePermissionInfo/rolePermissionInfoDetail',3),
  (1761018029702856705,'finance:shopFinance','店财务管理',NULL,'1',NULL,'2024-02-23 21:19:51',NULL,NULL,NULL,NULL,0,NULL,'2024-02-23 21:19:51','/finance/shopFinance',5),
  (1767387206030704642,'finance:shopStock','店铺库存',NULL,'1',NULL,'2024-03-12 11:08:41',NULL,'2024-03-12 16:49:31',NULL,NULL,0,NULL,'2024-03-12 16:49:31','/finance/shopStock',5),
  (1767387239002128385,'finance:shopStock:detail','店铺库存详情',NULL,'1',NULL,'2024-03-12 11:08:49',NULL,NULL,NULL,NULL,0,NULL,'2024-03-12 11:08:49','/finance/shopStock/shopStockDetail',5),
  (1775097256744562690,'finance:shopCart:detail','购物车详情',NULL,'1',NULL,'2024-04-02 17:45:40',NULL,'2024-04-03 11:36:20',NULL,NULL,0,NULL,'2024-04-03 11:36:20','/finance/shopCart/shopCartDetail',5),
  (1775098343086075905,'finance:shopCart','购物车',NULL,'1',NULL,'2024-04-02 17:49:59',NULL,'2024-04-03 11:36:20',NULL,NULL,0,NULL,'2024-04-03 11:36:20','/finance/shopCart',5),
  (1777596696961089538,'finance:shopOrder','销售单管理',NULL,'1',NULL,'2024-04-09 15:17:33',NULL,'2024-04-09 15:20:02',NULL,NULL,0,NULL,'2024-04-09 15:20:02','/finance/shopOrder',5),
  (1777597320213692417,'finance:shopOrder:detail','销售单管理详情',NULL,'1',NULL,'2024-04-09 15:20:02',NULL,NULL,NULL,NULL,0,NULL,'2024-04-09 15:20:02','/finance/shopOrder/shopOrderDetail',5),
  (1780202115923697666,'finance:shopStockAttrs','库存商品属性',NULL,'1',NULL,'2024-04-16 19:50:34',NULL,NULL,NULL,NULL,0,NULL,'2024-04-16 19:50:34','/finance/shopStockAttrs',5),
  (1788861690541473794,'finance:shopStockBatch','商品库存批次',NULL,'1',NULL,'2024-05-10 17:20:37',NULL,'2024-05-10 17:30:41',NULL,NULL,0,NULL,'2024-05-10 17:30:41','/finance/shopStockBatch',5),
  (1788863926327803906,'finance:shopStockBatch:detail','商品库存批次详情',NULL,'1',NULL,'2024-05-10 17:29:30',NULL,'2024-05-10 17:30:41',NULL,NULL,0,NULL,'2024-05-10 17:30:41','/finance/shopStockBatch/shopStockBatchDetail',5),
  (1810856883473846273,'selfFinance:personalGift','个人随礼信息',NULL,'1',NULL,'2024-07-10 10:01:40',NULL,'2026-09-19 13:07:52',NULL,NULL,1,NULL,'2024-07-10 10:01:40','/finance/personalGift',5),
  (1854804436233646081,'user:roleInfo','角色表',NULL,'1',NULL,'2024-11-08 16:33:33',NULL,'2024-11-08 16:54:44',NULL,NULL,0,NULL,'2024-11-08 16:54:44','/user/roleInfo',3),
  (1854805445152501762,'user:roleInfo:detail','角色表详情',NULL,'1',NULL,'2024-11-08 16:37:33',NULL,'2024-11-08 16:54:44',NULL,NULL,0,NULL,'2024-11-08 16:54:44','/user/roleInfo/roleInfoDetail',3),
  (1900000000000002000,'gift','礼尚往来管理','礼尚往来管理一级菜单权限','1',NULL,'2026-05-19 17:37:30',NULL,NULL,NULL,NULL,0,NULL,NULL,'/finance/gift',5),
  (1900000000000002001,'gift:dashboard','礼尚往来-数据概览','礼尚往来数据概览页面权限','1',NULL,'2026-05-13 14:22:05',NULL,NULL,NULL,NULL,0,NULL,NULL,'/finance/gift/dashboard',1900000000000002000),
  (1900000000000002002,'gift:person','礼尚往来-亲友管理','礼尚往来亲友管理页面权限','1',NULL,'2026-05-13 14:22:05',NULL,NULL,NULL,NULL,0,NULL,NULL,'/finance/gift/person',1900000000000002000),
  (1900000000000002003,'gift:event','礼尚往来-事由管理','礼尚往来事由管理页面权限','1',NULL,'2026-05-13 14:22:05',NULL,NULL,NULL,NULL,0,NULL,NULL,'/finance/gift/event',1900000000000002000),
  (1900000000000002004,'gift:record','礼尚往来-礼金记录','礼尚往来礼金记录页面权限','1',NULL,'2026-05-13 14:22:05',NULL,NULL,NULL,NULL,0,NULL,NULL,'/finance/gift/record',1900000000000002000),
  (1900000000000002005,'gift:analysis','礼尚往来-统计报表','礼尚往来统计报表页面权限','1',NULL,'2026-05-13 14:22:05',NULL,NULL,NULL,NULL,0,NULL,NULL,'/finance/gift/analysis',1900000000000002000),
  (1900000000000002011,'gift:view','礼尚往来-查看','礼尚往来列表和详情查看权限','1',NULL,'2026-05-13 14:22:05',NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (1900000000000002012,'gift:add','礼尚往来-新增','礼尚往来新增按钮权限','1',NULL,'2026-05-13 14:22:05',NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (1900000000000002013,'gift:edit','礼尚往来-编辑','礼尚往来编辑按钮权限','1',NULL,'2026-05-13 14:22:05',NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (1900000000000002014,'gift:delete','礼尚往来-删除','礼尚往来删除按钮权限','1',NULL,'2026-05-13 14:22:05',NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (1900000000000002015,'gift:export','礼尚往来-导出','礼尚往来导出按钮权限','1',NULL,'2026-05-13 14:22:05',NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (1917366954864140289,'selfFinance:prepaidConsumeRecordT','消费卡交易记录',NULL,'1',NULL,'2025-04-30 07:54:40',NULL,'2025-04-30 08:21:48',NULL,NULL,0,NULL,'2025-04-30 08:21:48','/selfFinance/prepaidConsumeRecordT',8768),
  (1917366959108775938,'selfFinance:prepaidCardInfoT','消费卡信息',NULL,'1',NULL,'2025-04-30 07:54:41',NULL,'2025-04-30 08:21:49',NULL,NULL,0,NULL,'2025-04-30 08:21:49','/selfFinance/prepaidCardInfoT',8768),
  (1917366959108775939,'selfFinance:prepaidCardInfoT:consume-water-info','消费流水信息',NULL,'1',NULL,'2025-04-30 07:54:41',NULL,'2025-04-30 08:21:49',NULL,NULL,0,NULL,'2025-04-30 08:21:49','/selfFinance/prepaidCardInfoT/consume-water-info',8768),
  (1917373700634566658,'selfFinance:prepaidConsumeRecordT:detail','消费卡交易记录详情',NULL,'1',NULL,'2025-04-30 08:21:28',NULL,'2025-04-30 08:21:48',NULL,NULL,0,NULL,'2025-04-30 08:21:48','/selfFinance/prepaidConsumeRecordT/prepaidConsumeRecordTDetail',8768),
  (1917373745928855554,'selfFinance:prepaidCardInfoT:detail','消费卡信息详情',NULL,'1',NULL,'2025-04-30 08:21:39',NULL,'2025-04-30 08:21:49',NULL,NULL,0,NULL,'2025-04-30 08:21:49','/selfFinance/prepaidCardInfoT/prepaidCardInfoTDetail',8768),
  (1917373745928855555,'selfFinance:prepaidCardInfoT:consumeInfo','消费卡信息消费',NULL,'1',NULL,'2025-04-30 08:21:39',NULL,'2025-04-30 08:21:49',NULL,NULL,0,NULL,'2025-04-30 08:21:49','/selfFinance/prepaidCardInfoT/prepaidCardInfoTConsumeInfo',8768),
  (1930875851365572610,'user:user','用户',NULL,'1',NULL,'2025-06-06 14:34:12',NULL,'2026-10-15 22:27:13',NULL,NULL,1,NULL,'2025-06-06 14:34:12','/user/user',3),
  (2000000000000000001,'dict','系统配置',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2001229442029613057,'cpnCoupon:cpnRedemptionRecordInfo','消费券核销记录表',NULL,'1',NULL,'2025-12-17 17:54:15',NULL,NULL,NULL,NULL,0,NULL,'2025-12-17 17:54:15','/cpnCoupon/cpnRedemptionRecordInfo',41),
  (2001229454939680769,'cpnCoupon:cpnRedemptionRecordInfo:detail','消费券核销记录表详情',NULL,'1',NULL,'2025-12-17 17:54:18',NULL,NULL,NULL,NULL,0,NULL,'2025-12-17 17:54:18','/cpnCoupon/cpnRedemptionRecordInfo/cpnRedemptionRecordInfoDetail',41),
  (2001229602495295489,'cpnCoupon:cpnCouponInfo','消费券信息表',NULL,'1',NULL,'2025-12-17 17:54:53',NULL,NULL,NULL,NULL,0,NULL,'2025-12-17 17:54:53','/cpnCoupon/cpnCouponInfo',41),
  (2001229613320794113,'cpnCoupon:cpnCouponInfo:detail','消费券信息表详情',NULL,'1',NULL,'2025-12-17 17:54:56',NULL,NULL,NULL,NULL,0,NULL,'2025-12-17 17:54:56','/cpnCoupon/cpnCouponInfo/cpnCouponInfoDetail',41),
  (2001229613320794114,'cpnCoupon:cpnCouponInfo:redeem','消费券信息表核销',NULL,'1',NULL,'2025-12-17 17:54:56',NULL,NULL,NULL,NULL,0,NULL,'2025-12-17 17:54:56','/cpnCoupon/cpnCouponInfo/cpnCouponInfoRedeem',41),
  (2001229808569839617,'cpnCoupon:cpnUserCouponInfo','用户消费券库存表',NULL,'1',NULL,'2025-12-17 17:55:42',NULL,NULL,NULL,NULL,0,NULL,'2025-12-17 17:55:42','/cpnCoupon/cpnUserCouponInfo',41),
  (2001229819143680002,'cpnCoupon:cpnUserCouponInfo:detail','用户消费券库存表详情',NULL,'1',NULL,'2025-12-17 17:55:45',NULL,NULL,NULL,NULL,0,NULL,'2025-12-17 17:55:45','/cpnCoupon/cpnUserCouponInfo/cpnUserCouponInfoDetail',41),
  (2002100000000100000,'user','用户域','用户域入口权限','1',NULL,NULL,NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2002100000000200000,'user:menuInfo:add','菜单新增','菜单管理新增权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2002100000000300000,'user:menuInfo:delete','菜单删除','菜单管理删除权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2002100000000400000,'user:menuInfo:update','菜单编辑','菜单管理编辑权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2002100000000500000,'user:menuInfo:view','菜单查看','菜单管理查看权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2002100000000600000,'user:orgManager:add','机构新增','机构管理新增权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2002100000000700000,'user:orgManager:delete','机构删除','机构管理删除权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2002100000000800000,'user:orgManager:update','机构编辑','机构管理编辑权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2002100000000900000,'user:orgManager:view','机构查看','机构管理查看权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2002100000001000000,'user:orgUserInfo:add','组织用户新增','组织用户关系新增权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2002100000001100000,'user:orgUserInfo:delete','组织用户删除','组织用户关系删除权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2002100000001200000,'user:orgUserInfo:update','组织用户编辑','组织用户关系编辑权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2002100000001300000,'user:orgUserInfo:view','组织用户查看','组织用户关系查看权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2002100000001400000,'user:permissionInfo:add','权限新增','权限信息新增权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2002100000001500000,'user:permissionInfo:delete','权限删除','权限信息删除权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2002100000001600000,'user:permissionInfo:update','权限编辑','权限信息编辑权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2002100000001700000,'user:permissionInfo:view','权限查看','权限信息查看权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2002100000001800000,'user:roleInfo:add','角色新增','角色管理新增权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2002100000001900000,'user:roleInfo:assignPermission','角色分配权限','角色分配权限权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2002100000002000000,'user:roleInfo:assignUser','角色分配用户','角色分配用户权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2002100000002100000,'user:roleInfo:delete','角色删除','角色管理删除权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2002100000002200000,'user:roleInfo:update','角色编辑','角色管理编辑权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2002100000002300000,'user:roleInfo:view','角色查看','角色管理查看权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2002100000002400000,'user:rolePermissionInfo:add','角色权限新增','角色权限关系新增权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2002100000002500000,'user:rolePermissionInfo:delete','角色权限删除','角色权限关系删除权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2002100000002600000,'user:rolePermissionInfo:update','角色权限编辑','角色权限关系编辑权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2002100000002700000,'user:rolePermissionInfo:view','角色权限查看','角色权限关系查看权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2002100000002800000,'user:roleUserInfo:add','用户角色新增','用户角色关系新增权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2002100000002900000,'user:roleUserInfo:delete','用户角色删除','用户角色关系删除权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2002100000003000000,'user:roleUserInfo:update','用户角色编辑','用户角色关系编辑权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2002100000003100000,'user:roleUserInfo:view','用户角色查看','用户角色关系查看权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2002100000003200000,'user:user:add','用户新增','用户信息新增权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2002100000003300000,'user:user:delete','用户删除','用户信息删除权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2002100000003400000,'user:user:update','用户编辑','用户信息编辑权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2002100000003500000,'user:user:view','用户查看','用户信息查看权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL,NULL,NULL),
  (2026092300000000001,'user:add','新增用户','用户管理页面新增用户按钮','1',NULL,'2026-09-23 07:15:30',NULL,NULL,NULL,NULL,0,NULL,'2026-09-23 07:15:30',NULL,10),
  (2026092300000000002,'user:edit','编辑用户','用户管理页面编辑用户按钮','1',NULL,'2026-09-23 07:15:30',NULL,NULL,NULL,NULL,0,NULL,'2026-09-23 07:15:30',NULL,10),
  (2026092300000000003,'user:delete','删除用户','用户管理页面删除用户按钮','1',NULL,'2026-09-23 07:15:30',NULL,NULL,NULL,NULL,0,NULL,'2026-09-23 07:15:30',NULL,10);

-- ----------------------------
-- Table structure for t_role_info
-- ----------------------------
DROP TABLE IF EXISTS `t_role_info`;
CREATE TABLE `t_role_info` (

  `id` bigint NOT NULL,
  `role_code` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '角色编码',
  `role_name` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '角色名称',
  `summary` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '描述',
  `status` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '状态,字典(is_valid) 1：有效,0:失效)',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleter` bigint DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint(1) DEFAULT '0' COMMENT '是否删除',
  `operator` bigint DEFAULT NULL COMMENT '修改人',
  `operate_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色信息表';

-- ----------------------------
-- Records of t_role_info
-- ----------------------------
INSERT INTO `t_role_info` VALUES
  (1746681527472701441,'family_admin','家庭管理员','家庭管理员','1',1,'2024-01-15 07:51:43',NULL,NULL,NULL,NULL,0,1,'2024-01-15 07:51:43'),
  (1746681677402292226,'shop_admin','商店管理员','商店管理员','1',1,'2024-01-15 07:52:18',NULL,NULL,NULL,NULL,0,1,'2024-01-15 07:52:18'),
  (1746681834919378946,'super_super','超级管理员','超级管理员','1',1,'2024-01-15 07:52:56',NULL,NULL,NULL,NULL,0,1,'2024-01-15 07:52:56'),
  (1746681960593309698,'finance_user','财务管理用户','财务管理用户','1',1,'2024-01-15 07:53:26',NULL,NULL,NULL,NULL,0,1,'2024-01-15 07:53:26'),
  (1900000000000004001,'gift_admin','人情管理','礼尚往来管理角色，组织级数据权限','1',NULL,'2026-05-13 14:22:05',NULL,NULL,NULL,NULL,0,NULL,NULL),
  (1900000000000004002,'gift_user','人情用户','礼尚往来普通用户角色，用户级数据权限','1',NULL,'2026-05-13 14:22:05',NULL,NULL,NULL,NULL,0,NULL,NULL),
  (2002000000000000001,'org_user_admin','组织用户管理','组织用户管理角色，组织、用户、菜单、角色、权限维护','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:24:39',NULL,NULL,0,NULL,NULL),
  (2002000000000100002,'org_user','组织用户','组织用户普通用户角色，组织、用户、菜单、角色、权限查看','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2090011269972103169,'22','33','22','1',1759760891393945602,'2026-08-19 17:41:32',NULL,NULL,NULL,NULL,0,1759760891393945602,'2026-08-19 17:41:32');

-- ----------------------------
-- Table structure for t_role_permission_info
-- ----------------------------
DROP TABLE IF EXISTS `t_role_permission_info`;
CREATE TABLE `t_role_permission_info` (

  `id` bigint NOT NULL,
  `role_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '角色id',
  `permission_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '权限id',
  `summary` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '描述',
  `status` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '状态,字典(is_valid) 1：有效,0:失效)',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleter` bigint DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint(1) DEFAULT '0' COMMENT '是否删除',
  `operator` bigint DEFAULT NULL COMMENT '修改人',
  `operate_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_role_permission_rid_pid` (`role_id`,`permission_id`,`is_delete`,`status`) COMMENT '角色权限关联检索索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='角色权限信息表';

-- ----------------------------
-- Records of t_role_permission_info
-- ----------------------------
INSERT INTO `t_role_permission_info` VALUES
  (1,'1746681960593309698','8768',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (2,'1746681960593309698','6',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (3,'1746681960593309698','16',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (4,'1746681527472701441','8768',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (5,'1746681527472701441','6',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (6,'1746681527472701441','7',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (7,'1746681527472701441','8',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (8,'1746681960593309698','25',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (9,'1746681527472701441','25',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (10,'1746681677402292226','1761018029702856705',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (11,'1746681677402292226','5',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (12,'1746681677402292226','32',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (13,'1746681960593309698','7',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (14,'1746681677402292226','33',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (15,'1746681677402292226','34',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (16,'1746681677402292226','35',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (17,'1746681677402292226','36',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (18,'1746681677402292226','1767387206030704642',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (19,'1746681677402292226','1767387239002128385',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (20,'1746681677402292226','176738720603070',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (22,'1746681677402292226','176738723900212',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (23,'1746681677402292226','17673872060307',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (25,'1746681677402292226','176738720603072',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (26,'1746681677402292226','9',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (28,'1746681527472701441','1810856883473846273',NULL,'1',NULL,NULL,NULL,'2026-09-19 13:07:52',NULL,NULL,1,NULL,NULL),
  (29,'1746681960593309698','1810856883473846273',NULL,'1',NULL,NULL,NULL,'2026-09-19 13:07:52',NULL,NULL,1,NULL,NULL),
  (100,'1746681527472701441','2001229442029613057',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (101,'1746681527472701441','2001229454939680769',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (102,'1746681527472701441','2001229602495295489',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (103,'1746681527472701441','2001229613320794113',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (104,'1746681527472701441','2001229808569839617',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (105,'1746681527472701441','2001229819143680002',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (106,'1746681527472701441','41',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (107,'1746681527472701441','2001229613320794114',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (400,'1746681527472701441','1915063559901908993',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (401,'1746681527472701441','1915064182307262466',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (402,'1746681527472701441','1916839139550879746',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (403,'1746681527472701441','1916839382568853505',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (404,'1746681527472701441','1917366959108775939',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (405,'1746681527472701441','1917366959108775938',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (406,'1746681527472701441','2001229819143680002',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (1222,'1746681677402292226','1788861690541473794',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (1223,'1746681677402292226','1788863926327803906',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (44444,'1746681677402292226','1788861690541473794',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (20250708,'1746681527472701441','1917373745928855555',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (20260419001,'1746681677402292226','1788861690541473794',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (20260419002,'1746681677402292226','1788863926327803906',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (1790285547969,'1746681527472701441','9',NULL,'1',NULL,'2026-09-25 05:32:27',NULL,'2026-09-25 05:32:27',NULL,NULL,0,NULL,NULL),
  (1790285548057,'1746681834919378946','9',NULL,'1',NULL,'2026-09-25 05:32:27',NULL,'2026-09-25 05:32:27',NULL,NULL,0,NULL,NULL),
  (1790285548106,'2002000000000000001','9',NULL,'1',NULL,'2026-09-25 05:32:27',NULL,'2026-09-25 05:32:27',NULL,NULL,0,NULL,NULL),
  (1790118930750234,'1746681527472701441','2026092300000000001','migrate: family_admin -> user:add','1',NULL,'2026-09-23 07:15:31',NULL,NULL,NULL,NULL,0,NULL,'2026-09-23 07:15:31'),
  (1790118930853299,'1746681527472701441','2026092300000000002','migrate: family_admin -> user:edit','1',NULL,'2026-09-23 07:15:31',NULL,NULL,NULL,NULL,0,NULL,'2026-09-23 07:15:31'),
  (1790118930954413,'1746681527472701441','2026092300000000003','migrate: family_admin -> user:delete','1',NULL,'2026-09-23 07:15:31',NULL,NULL,NULL,NULL,0,NULL,'2026-09-23 07:15:31'),
  (1790118931055719,'2002000000000000001','2026092300000000001','migrate: org_user_admin -> user:add','1',NULL,'2026-09-23 07:15:31',NULL,NULL,NULL,NULL,0,NULL,'2026-09-23 07:15:31'),
  (1790118931154886,'2002000000000000001','2026092300000000002','migrate: org_user_admin -> user:edit','1',NULL,'2026-09-23 07:15:31',NULL,NULL,NULL,NULL,0,NULL,'2026-09-23 07:15:31'),
  (1790118931258116,'2002000000000000001','2026092300000000003','migrate: org_user_admin -> user:delete','1',NULL,'2026-09-23 07:15:31',NULL,NULL,NULL,NULL,0,NULL,'2026-09-23 07:15:31'),
  (1900000000000005000,'1900000000000004001','1900000000000002000',NULL,'1',NULL,'2026-05-19 17:37:33',NULL,NULL,NULL,NULL,0,NULL,NULL),
  (1900000000000005001,'1900000000000004001','1900000000000002001',NULL,'1',NULL,'2026-05-13 14:22:05',NULL,NULL,NULL,NULL,0,NULL,NULL),
  (1900000000000005002,'1900000000000004001','1900000000000002002',NULL,'1',NULL,'2026-05-13 14:22:05',NULL,NULL,NULL,NULL,0,NULL,NULL),
  (1900000000000005003,'1900000000000004001','1900000000000002003',NULL,'1',NULL,'2026-05-13 14:22:05',NULL,NULL,NULL,NULL,0,NULL,NULL),
  (1900000000000005004,'1900000000000004001','1900000000000002004',NULL,'1',NULL,'2026-05-13 14:22:05',NULL,NULL,NULL,NULL,0,NULL,NULL),
  (1900000000000005005,'1900000000000004001','1900000000000002005',NULL,'1',NULL,'2026-05-18 11:46:34',NULL,NULL,NULL,NULL,0,NULL,NULL),
  (1900000000000005011,'1900000000000004001','1900000000000002011',NULL,'1',NULL,'2026-05-13 14:22:05',NULL,NULL,NULL,NULL,0,NULL,NULL),
  (1900000000000005012,'1900000000000004001','1900000000000002012',NULL,'1',NULL,'2026-05-13 14:22:05',NULL,NULL,NULL,NULL,0,NULL,NULL),
  (1900000000000005013,'1900000000000004001','1900000000000002013',NULL,'1',NULL,'2026-05-13 14:22:05',NULL,NULL,NULL,NULL,0,NULL,NULL),
  (1900000000000005014,'1900000000000004001','1900000000000002014',NULL,'1',NULL,'2026-05-13 14:22:05',NULL,NULL,NULL,NULL,0,NULL,NULL),
  (1900000000000005015,'1900000000000004001','1900000000000002015',NULL,'1',NULL,'2026-05-18 11:46:35',NULL,NULL,NULL,NULL,0,NULL,NULL),
  (1900000000000006000,'1900000000000004002','1900000000000002000',NULL,'1',NULL,'2026-05-19 17:37:33',NULL,NULL,NULL,NULL,0,NULL,NULL),
  (1900000000000006001,'1900000000000004002','1900000000000002001',NULL,'1',NULL,'2026-05-13 14:22:05',NULL,NULL,NULL,NULL,0,NULL,NULL),
  (1900000000000006002,'1900000000000004002','1900000000000002002',NULL,'1',NULL,'2026-05-13 14:22:05',NULL,NULL,NULL,NULL,0,NULL,NULL),
  (1900000000000006003,'1900000000000004002','1900000000000002003',NULL,'1',NULL,'2026-05-13 14:22:05',NULL,NULL,NULL,NULL,0,NULL,NULL),
  (1900000000000006004,'1900000000000004002','1900000000000002004',NULL,'1',NULL,'2026-05-13 14:22:05',NULL,NULL,NULL,NULL,0,NULL,NULL),
  (1900000000000006011,'1900000000000004002','1900000000000002011',NULL,'1',NULL,'2026-05-13 14:22:05',NULL,NULL,NULL,NULL,0,NULL,NULL),
  (1900000000000006012,'1900000000000004002','1900000000000002012',NULL,'1',NULL,'2026-05-13 14:22:05',NULL,NULL,NULL,NULL,0,NULL,NULL),
  (1900000000000006013,'1900000000000004002','1900000000000002013',NULL,'1',NULL,'2026-05-13 14:22:05',NULL,NULL,NULL,NULL,0,NULL,NULL),
  (1900000000000006014,'1900000000000004002','1900000000000002014',NULL,'1',NULL,'2026-05-18 11:46:35',NULL,NULL,NULL,NULL,0,NULL,NULL),
  (2002000000001000001,'2002000000000000001','2002100000000100000','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000001000002,'2002000000000000001','10','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:24:39',NULL,NULL,0,NULL,NULL),
  (2002000000001000003,'2002000000000000001','11','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:24:39',NULL,NULL,0,NULL,NULL),
  (2002000000001000006,'2002000000000000001','14','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:24:39',NULL,NULL,0,NULL,NULL),
  (2002000000001000008,'2002000000000000001','23','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:24:39',NULL,NULL,0,NULL,NULL),
  (2002000000001000009,'2002000000000000001','1746531627376271361','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:24:39',NULL,NULL,0,NULL,NULL),
  (2002000000001000010,'2002000000000000001','1746531628089303041','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:24:39',NULL,NULL,0,NULL,NULL),
  (2002000000001000011,'2002000000000000001','1746780102696022018','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:24:39',NULL,NULL,0,NULL,NULL),
  (2002000000001000012,'2002000000000000001','1746780103224504322','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:24:39',NULL,NULL,0,NULL,NULL),
  (2002000000001000013,'2002000000000000001','1746780105615257602','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:24:39',NULL,NULL,0,NULL,NULL),
  (2002000000001000014,'2002000000000000001','1746780106194071554','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:24:39',NULL,NULL,0,NULL,NULL),
  (2002000000001000017,'2002000000000000001','1747518319657431041','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:24:39',NULL,NULL,0,NULL,NULL),
  (2002000000001000018,'2002000000000000001','1747520680119422977','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:24:39',NULL,NULL,0,NULL,NULL),
  (2002000000001000019,'2002000000000000001','1854804436233646081','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:24:39',NULL,NULL,0,NULL,NULL),
  (2002000000001000020,'2002000000000000001','1854805445152501762','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:24:39',NULL,NULL,0,NULL,NULL),
  (2002000000001000021,'2002000000000000001','1930875851365572610','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:24:39',NULL,NULL,0,NULL,NULL),
  (2002000000001000022,'2002000000000000001','2002100000003500000','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000001000023,'2002000000000000001','2002100000003200000','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000001000024,'2002000000000000001','2002100000003400000','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000001000025,'2002000000000000001','2002100000003300000','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000001000026,'2002000000000000001','2002100000000900000','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000001000027,'2002000000000000001','2002100000000600000','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000001000028,'2002000000000000001','2002100000000800000','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000001000029,'2002000000000000001','2002100000000700000','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000001000034,'2002000000000000001','2002100000002300000','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000001000035,'2002000000000000001','2002100000001800000','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000001000036,'2002000000000000001','2002100000002200000','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000001000037,'2002000000000000001','2002100000002100000','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000001000038,'2002000000000000001','2002100000002000000','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000001000039,'2002000000000000001','2002100000001900000','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000001000044,'2002000000000000001','2002100000001300000','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000001000045,'2002000000000000001','2002100000001000000','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000001000046,'2002000000000000001','2002100000001200000','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000001000047,'2002000000000000001','2002100000001100000','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000001000048,'2002000000000000001','2002100000003100000','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000001000049,'2002000000000000001','2002100000002800000','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000001000050,'2002000000000000001','2002100000003000000','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000001000051,'2002000000000000001','2002100000002900000','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000001000052,'2002000000000000001','2002100000002700000','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000001000053,'2002000000000000001','2002100000002400000','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000001000054,'2002000000000000001','2002100000002600000','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000001000055,'2002000000000000001','2002100000002500000','组织用户管理角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000002000001,'2002000000000100002','2002100000000100000','组织用户普通角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000002000002,'2002000000000100002','10','组织用户普通角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000002000003,'2002000000000100002','11','组织用户普通角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000002000006,'2002000000000100002','14','组织用户普通角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000002000008,'2002000000000100002','23','组织用户普通角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000002000009,'2002000000000100002','1746531627376271361','组织用户普通角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000002000010,'2002000000000100002','1746531628089303041','组织用户普通角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000002000011,'2002000000000100002','1746780102696022018','组织用户普通角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000002000012,'2002000000000100002','1746780103224504322','组织用户普通角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000002000013,'2002000000000100002','1746780105615257602','组织用户普通角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000002000014,'2002000000000100002','1746780106194071554','组织用户普通角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000002000017,'2002000000000100002','1747518319657431041','组织用户普通角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000002000018,'2002000000000100002','1747520680119422977','组织用户普通角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000002000019,'2002000000000100002','1854804436233646081','组织用户普通角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000002000020,'2002000000000100002','1854805445152501762','组织用户普通角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000002000021,'2002000000000100002','1930875851365572610','组织用户普通角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000002000022,'2002000000000100002','2002100000003500000','组织用户普通角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000002000023,'2002000000000100002','2002100000000900000','组织用户普通角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000002000025,'2002000000000100002','2002100000002300000','组织用户普通角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000002000027,'2002000000000100002','2002100000001300000','组织用户普通角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000002000028,'2002000000000100002','2002100000003100000','组织用户普通角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (2002000000002000029,'2002000000000100002','2002100000002700000','组织用户普通角色权限','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL),
  (3746681527472701441,'1746681527472701441','2000000000000000001',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (3746681677402292226,'1746681677402292226','2000000000000000001',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (3746681834919378946,'1746681834919378946','2000000000000000001',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (3746681960593309698,'1746681960593309698','2000000000000000001',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (3900000000000004001,'1900000000000004001','2000000000000000001',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (3900000000000004002,'1900000000000004002','2000000000000000001',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (4002000000000000001,'2002000000000000001','2000000000000000001',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (4002000000000100002,'2002000000000100002','2000000000000000001',NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL);

-- ----------------------------
-- Table structure for t_role_user_info
-- ----------------------------
DROP TABLE IF EXISTS `t_role_user_info`;
CREATE TABLE `t_role_user_info` (

  `id` bigint NOT NULL,
  `role_id` bigint DEFAULT NULL COMMENT '角色id',
  `user_id` bigint DEFAULT NULL COMMENT '用户id',
  `summary` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '描述',
  `status` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '状态,字典(is_valid) 1：有效,0:失效)',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleter` bigint DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint(1) DEFAULT '0' COMMENT '是否删除',
  `operator` bigint DEFAULT NULL COMMENT '修改人',
  `operate_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_role_user_uid_rid` (`user_id`,`role_id`,`is_delete`,`status`) COMMENT '用户角色联合检索索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色信息表';

-- ----------------------------
-- Records of t_role_user_info
-- ----------------------------
INSERT INTO `t_role_user_info` VALUES
  (1,1746681960593309698,1,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (2,1746681527472701441,2,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (3,1746681834919378946,1759760891393945602,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (4,1746681677402292226,1763166043771621378,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (5,1746681677402292226,1766998444679483393,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (6,1746681677402292226,2046949449054392321,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (7,1746681677402292226,2046950134013595649,NULL,'1',NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),
  (1900000000000007001,1900000000000004001,2,'czp assigned gift_admin by Codex','1',NULL,'2026-05-13 14:33:55',NULL,NULL,NULL,NULL,0,NULL,NULL),
  (1900000000000007002,1900000000000004002,1,'mj assigned gift_user by Codex','1',NULL,'2026-05-13 14:33:57',NULL,NULL,NULL,NULL,0,NULL,NULL),
  (2002000000003000001,2002000000000000001,2,'czp 绑定组织用户管理角色','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:24:39',NULL,NULL,0,NULL,NULL),
  (2002000000003000002,2002000000000100002,1,'mj 绑定组织用户普通角色','1',NULL,'2026-05-25 08:24:39',NULL,'2026-05-25 08:26:29',NULL,NULL,0,NULL,NULL);

-- ----------------------------
-- Table structure for t_role_org_info
-- ----------------------------
DROP TABLE IF EXISTS `t_role_org_info`;
CREATE TABLE `t_role_org_info` (

  `id` bigint NOT NULL,
  `role_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '角色id',
  `org_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '机构id',
  `summary` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '描述',
  `status` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '状态,字典(is_valid) 1：有效,0:失效)',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleter` bigint DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint(1) DEFAULT '0' COMMENT '是否删除',
  `operator` bigint DEFAULT NULL COMMENT '修改人',
  `operate_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_role_org_active` (`role_id`,`org_id`,`status`,`is_delete`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='角色机构信息表';

-- ----------------------------
-- Table structure for t_user
-- ----------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (

  `id` bigint NOT NULL,
  `username` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '密码',
  `gender` varchar(2) DEFAULT NULL COMMENT '性别(1:男2:女)',
  `avatar` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '个人头像',
  `email` varchar(60) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '邮箱',
  `birthday` datetime DEFAULT NULL COMMENT '出生年月日',
  `mobile` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '手机',
  `valid_code` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '邮箱验证码',
  `summary` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '自我简介最多150字',
  `status` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '状态',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleter` bigint DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint(1) DEFAULT '0' COMMENT '是否删除',
  `operator` bigint DEFAULT NULL COMMENT '修改人',
  `operate_time` datetime DEFAULT NULL COMMENT '更新时间',
  `nick_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '昵称',
  `qq_number` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'QQ号',
  `we_chat` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '微信号',
  `occupation` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '职业',
  `github` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'github地址',
  `gitee` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'gitee地址',
  `role_id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '拥有的角色uid',
  `person_resume` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '履历',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `user_uesrname_index` (`username`) USING BTREE,
  KEY `t_username_status_IDX` (`username`,`status`,`is_delete`) USING BTREE,
  KEY `idx_user_email` (`email`,`status`,`is_delete`) COMMENT '用户邮箱登录检索索引',
  KEY `idx_user_mobile` (`mobile`,`status`,`is_delete`) COMMENT '用户手机登录检索索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='管理员表';

-- ----------------------------
-- Records of t_user
-- ----------------------------
INSERT INTO `t_user` VALUES
  (1,'mj','$2a$10$iX7lRKOuYXKUgWVCQL.x1u4FBUrb1WKkljG8FT47m8BVdar.FMQ5G','1','1881889800521650178','734663446@qq.com','1992-11-25 23:38:43','18242311493',NULL,'臭屁家的小疯子','1',1,NULL,1759760891393945602,'2026-03-08 21:40:39',NULL,NULL,0,1,'2026-03-08 21:40:39','小疯子','734663446','18242311493','程序员',NULL,NULL,NULL,NULL),
  (2,'czp','$2a$10$u6C5ENTtE89jaBU9EorCVu.boBAut/o9u7LqDEqTK7wgbb5BIaACu','2','1713168708133130242',NULL,'1994-02-10 23:54:12','18540197673',NULL,'臭屁家臭屁宝','1',1,NULL,1759760891393945602,'2026-03-08 21:52:21',NULL,NULL,0,2,'2026-03-08 21:52:21','臭屁宝',NULL,NULL,NULL,NULL,NULL,NULL,NULL),
  (1745355151204110338,'123','$2a$10$NvA.1EnUYhyn7YYf0eS/4e5yXF6R8qWk4AvnYxJhEjeNO.t9MYZ7G',NULL,NULL,'123@com',NULL,NULL,NULL,NULL,NULL,1,'2024-01-11 16:01:10',NULL,NULL,NULL,NULL,1,1,'2024-01-11 16:01:10',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
  (1745406245749886977,'1213','$2a$10$lar0kjPsEK8IMPgtBkQi7OYCMtAwP5jYLP5QZcVFVMHFkFr3ttsQW',NULL,NULL,'1231@com',NULL,NULL,NULL,NULL,NULL,1,'2024-01-11 19:24:12',NULL,NULL,NULL,NULL,1,1,'2024-01-11 19:24:12',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
  (1759760891393945602,'superman','$2a$10$JXbgisfYG9OSuFVjP/zv5etH6xQh1Qcq0FFs5cQQsuLmYH6GmSU3K','1',NULL,NULL,'2020-01-01 10:03:42','18222222222',NULL,'super！super！！super！！！','1',2,'2024-02-20 10:04:26',1759760891393945602,'2025-01-21 16:29:14',NULL,NULL,0,1759760891393945602,'2025-01-21 16:29:14','supersuper',NULL,NULL,NULL,NULL,NULL,NULL,NULL),
  (1763166043771621378,'Bxh','$2a$10$RmsVzKbAmJ634H84AJMhL.qtQuotAbqnCfTcLR0woLBiyOjf0xQOm','2',NULL,NULL,NULL,'18241370477',NULL,NULL,'1',1759760891393945602,'2024-02-29 19:35:18',NULL,NULL,NULL,NULL,0,1759760891393945602,'2024-02-29 19:35:18','bxh',NULL,NULL,NULL,NULL,NULL,NULL,NULL),
  (1766998444679483393,'dd','$2a$10$2K6B4NYNTaPEjbE6eGG1Luralha4cNIgZc5vev2o96PU/AaJSFv0e','2',NULL,NULL,NULL,'18111111111',NULL,'丁丁姐','1',1759760891393945602,'2024-03-11 09:23:53',1759760891393945602,'2025-01-22 10:17:16',NULL,NULL,0,1759760891393945602,'2025-01-22 10:17:16','dd',NULL,NULL,NULL,NULL,NULL,NULL,NULL),
  (2046949449054392321,'mm','$2a$10$MvduNsMZtzo89ZuqiWvCiurTazLL1egLGX2.N/wuP2CX7EVH.Jd8q','1',NULL,NULL,NULL,'18222222221',NULL,NULL,'1',1759760891393945602,'2026-04-22 21:49:14',1759760891393945602,'2026-04-22 21:51:41',NULL,NULL,0,1759760891393945602,'2026-04-22 21:51:41','丁宇明',NULL,NULL,NULL,NULL,NULL,NULL,NULL),
  (2046950134013595649,'ll','$2a$10$q36d/T1IBHFq6beIrKKV6.kXI9ccCPZXpZ6UeBbfhoRiiYBgRkV2W','2',NULL,NULL,NULL,'18222222220',NULL,NULL,'1',1759760891393945602,'2026-04-22 21:51:57',NULL,NULL,NULL,NULL,0,1759760891393945602,'2026-04-22 21:51:57','ll',NULL,NULL,NULL,NULL,NULL,NULL,NULL);

-- ----------------------------
-- Table structure for t_user_login
-- ----------------------------
DROP TABLE IF EXISTS `t_user_login`;
CREATE TABLE `t_user_login` (

  `id` bigint NOT NULL COMMENT 'id',
  `user_id` bigint DEFAULT NULL COMMENT '用户id',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `token_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'token在redis中对应的tokenId',
  `os` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '系统',
  `broswer` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '登录浏览器',
  `login_ip` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT '127.0.0.1' COMMENT '最后登录IP',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '修改人',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `deleter` bigint DEFAULT NULL COMMENT '删除人',
  `delete_time` timestamp NULL DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint(1) DEFAULT '0' COMMENT '是否删除',
  `operator` bigint DEFAULT NULL COMMENT '修改人',
  `operate_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `token` varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'token信息',
  `login_location` varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '登录地址',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_login_uid` (`user_id`,`last_login_time` DESC) COMMENT '用户登录历史回溯索引',
  KEY `idx_user_login_tid` (`token_id`) COMMENT 'Token指追踪定位索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC COMMENT='用户登录表';

SET FOREIGN_KEY_CHECKS = 1;
