CREATE DATABASE IF NOT EXISTS alex_finance;
USE alex_finance;
/*
 Navicat Premium Data Transfer

 Target Server Type    : MySQL
 File Encoding         : 65001
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for account_record_info
-- ----------------------------
DROP TABLE IF EXISTS `account_record_info`;
CREATE TABLE `account_record_info` (
`id` bigint NOT NULL COMMENT 'id',
  `name` varchar(64) DEFAULT NULL COMMENT '名称',
  `avli_date` timestamp NULL DEFAULT NULL COMMENT '有效期',
  `amount` decimal(10,2) DEFAULT NULL COMMENT '金额',
  `account` varchar(64) DEFAULT NULL COMMENT '账号',
  `is_send` tinyint DEFAULT NULL COMMENT '是否发送提醒',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for account_record_info_test
-- ----------------------------
DROP TABLE IF EXISTS `account_record_info_test`;
CREATE TABLE `account_record_info_test` (
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
CREATE TABLE `dict_info` (
`id` bigint NOT NULL COMMENT 'id',
  `type_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '类别编码',
  `type_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '类别',
  `belong_to` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '分类编码',
  `belong_to_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '分类',
  `order_by` int DEFAULT NULL COMMENT '排序',
  `is_valid` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '是否有效',
  `is_delete` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '是否删除',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `updater` bigint DEFAULT NULL COMMENT '更新人',
  `deleter` bigint DEFAULT NULL COMMENT '删除人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `delete_time` datetime DEFAULT NULL COMMENT '更新时间',
  `operator` bigint DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `dict_info_type_code_IDX` (`type_code`,`belong_to`,`is_valid`,`is_delete`,`order_by`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='字典表';

-- ----------------------------
-- Records of dict_info
-- ----------------------------
INSERT INTO `dict_info` VALUES
  (23,'jd','京东','pay_way','支付方式',45,'1','0',NULL,NULL,NULL,'2022-10-18 21:47:46',NULL,NULL,NULL,'2022-10-18 21:47:49'),
  (51,'income','收入','income_expense_type','收支类型',10,'1','0',NULL,NULL,NULL,'2022-10-19 22:37:09',NULL,NULL,NULL,'2022-10-19 22:37:12'),
  (52,'expense','支出','income_expense_type','收支类型',10,'1','0',NULL,NULL,NULL,'2022-10-19 22:37:09',NULL,NULL,NULL,'2022-10-19 22:37:12'),
  (53,'dfw','电费w','pay_way','支付方式',49,'1','0',NULL,NULL,NULL,'2022-10-11 21:55:41',NULL,NULL,NULL,'2022-10-11 21:55:41'),
  (54,'sf','水费','pay_way','支付方式',52,'1','0',NULL,NULL,NULL,'2022-10-11 21:55:41',NULL,NULL,NULL,'2022-10-11 21:55:41'),
  (55,'rqf','燃气费','pay_way','支付方式',55,'1','0',NULL,NULL,NULL,'2022-10-11 21:55:41',NULL,NULL,NULL,'2022-10-11 21:55:41'),
  (56,'bt','白条','pay_way','支付方式',48,'1','0',NULL,NULL,NULL,'2022-10-11 21:55:41',NULL,NULL,NULL,'2022-10-11 21:55:41'),
  (100,'hfw','话费w','pay_way','支付方式',200,'1','0',NULL,NULL,NULL,'2022-10-11 21:55:41',NULL,NULL,NULL,'2022-10-11 21:55:41'),
  (101,'hfm','话费m','pay_way','支付方式',210,'1','0',NULL,NULL,NULL,'2022-10-11 21:55:41',NULL,NULL,NULL,'2022-10-11 21:55:41'),
  (102,'hfb','话费b','pay_way','支付方式',220,'1','0',NULL,NULL,NULL,'2022-10-11 21:55:41',NULL,NULL,NULL,'2022-10-11 21:55:41'),
  (103,'hfmj','话费mj','pay_way','支付方式',230,'1','0',NULL,NULL,NULL,'2022-10-11 21:55:41',NULL,NULL,NULL,'2022-10-11 21:55:41'),
  (104,'dfm','电费m','pay_way','支付方式',51,'1','0',NULL,NULL,NULL,'2022-10-11 21:55:41',NULL,NULL,NULL,'2022-10-11 21:55:41'),
  (105,'hb','花呗','pay_way','支付方式',42,'1','0',NULL,NULL,NULL,'2022-10-11 21:55:41',NULL,NULL,NULL,'2022-10-11 21:55:41'),
  (110,'1','男','gender','性别',10,'1','0',NULL,NULL,NULL,'2023-01-31 14:58:07',NULL,NULL,NULL,'2023-01-31 14:58:13'),
  (111,'11','半袖','shop_category','商品分类',110,'1','0',1759760891393945602,1759760891393945602,NULL,'2024-03-12 16:42:07','2024-03-19 14:28:36',NULL,1759760891393945602,'2024-03-19 14:28:36'),
  (112,'12','帽衫','shop_category','商品分类',120,'1','0',1759760891393945602,1759760891393945602,NULL,'2024-03-12 16:42:07','2024-03-19 14:26:55',NULL,1759760891393945602,'2024-03-19 14:26:55'),
  (113,'13','牛仔裤','shop_category','商品分类',130,'1','0',1759760891393945602,1759760891393945602,NULL,'2024-03-12 16:42:07','2024-03-19 14:26:36',NULL,1759760891393945602,'2024-03-19 14:26:36'),
  (114,'14','小衫','shop_category','商品分类',140,'1','0',1759760891393945602,1759760891393945602,NULL,'2024-03-12 16:42:07','2024-03-19 14:27:05',NULL,1759760891393945602,'2024-03-19 14:27:05'),
  (115,'15','睡衣','shop_category','商品分类',150,'1','0',1759760891393945602,1759760891393945602,NULL,'2024-03-12 16:42:07','2024-03-19 14:26:59',NULL,1759760891393945602,'2024-03-19 14:26:59'),
  (116,'16','羽绒服','shop_category','商品分类',160,'1','0',1759760891393945602,1759760891393945602,NULL,'2024-03-12 16:42:07','2024-03-19 14:26:51',NULL,1759760891393945602,'2024-03-19 14:26:51'),
  (117,'17','双面绒','shop_category','商品分类',170,'1','0',1759760891393945602,1759760891393945602,NULL,'2024-03-12 16:42:07','2024-03-19 14:26:44',NULL,1759760891393945602,'2024-03-19 14:26:44'),
  (118,'18','大衫','shop_category','商品分类',180,'1','0',1759760891393945602,1759760891393945602,NULL,'2024-03-12 16:42:07','2024-03-19 14:26:19',NULL,1759760891393945602,'2024-03-19 14:26:19'),
  (119,'19','工装裤','shop_category','商品分类',190,'1','0',1759760891393945602,1759760891393945602,NULL,'2024-03-12 16:42:07','2024-03-19 14:26:30',NULL,1759760891393945602,'2024-03-19 14:26:30'),
  (120,'20','针织衫','shop_category','商品分类',200,'1','0',1759760891393945602,1759760891393945602,NULL,'2024-03-12 16:42:07','2024-03-19 14:28:10',NULL,1759760891393945602,'2024-03-19 14:28:10'),
  (121,'21','西服','shop_category','商品分类',210,'1','0',1759760891393945602,1759760891393945602,NULL,'2024-03-12 16:42:07','2024-03-19 14:28:15',NULL,1759760891393945602,'2024-03-19 14:28:15'),
  (122,'22','吊带裙','shop_category','商品分类',220,'1','0',1759760891393945602,1759760891393945602,NULL,'2024-03-12 16:42:07','2024-03-19 14:28:19',NULL,1759760891393945602,'2024-03-19 14:28:19'),
  (123,'23','针织衫','shop_category','商品分类',230,'1','0',1759760891393945602,1759760891393945602,NULL,'2024-03-12 16:42:07','2024-03-19 14:28:01',NULL,1759760891393945602,'2024-03-19 14:28:01'),
  (124,'24','大衣','shop_category','商品分类',240,'1','0',1759760891393945602,1759760891393945602,NULL,'2024-03-12 16:42:07','2024-03-19 14:28:24',NULL,1759760891393945602,'2024-03-19 14:28:24'),
  (125,'25','打底','shop_category','商品分类',250,'1','0',1759760891393945602,1759760891393945602,NULL,'2024-03-12 16:42:07','2024-03-19 14:28:31',NULL,1759760891393945602,'2024-03-19 14:28:31'),
  (157983311610776371,'mt','美团','pay_way','支付方式',50,'1','1',NULL,NULL,NULL,'2022-10-11 21:55:41',NULL,NULL,NULL,'2022-10-11 21:55:41'),
  (1579830582328774657,'wx','微信','pay_way','支付方式',30,'1','0',NULL,NULL,NULL,'2022-10-11 21:45:37',NULL,NULL,NULL,'2022-10-11 21:45:37'),
  (1579832088037806082,'zfb','支付宝','pay_way','支付方式',40,'1','0',NULL,NULL,NULL,'2022-10-11 21:51:36',NULL,NULL,NULL,'2022-10-11 21:51:36'),
  (1579832177447784449,'xj','现金','pay_way','支付方式',10,'1','0',NULL,NULL,NULL,'2022-10-11 21:51:58',NULL,NULL,NULL,'2022-10-11 21:51:58'),
  (1579833116107763714,'yhk','银行卡','pay_way','支付方式',20,'1','0',NULL,NULL,NULL,'2022-10-11 21:55:41',NULL,NULL,NULL,'2022-10-11 21:55:41'),
  (1612690168666439682,'1','有效','is_valid','是否有效',10,'1','0',NULL,NULL,NULL,'2023-01-10 13:57:53',NULL,NULL,NULL,'2023-01-10 13:57:53'),
  (1612690346328768514,'0','无效','is_valid','是否有效',20,'1','0',NULL,NULL,NULL,'2023-01-10 13:58:36',NULL,NULL,NULL,'2023-01-10 13:58:36'),
  (1620315693635387393,'2','女','gender','性别',20,'1','0',1,NULL,NULL,'2023-01-31 14:59:00',NULL,NULL,1,'2023-01-31 14:59:00'),
  (1646041304975605762,'mm','妈妈','account_type','账号分类',3,'1','0',NULL,NULL,NULL,'2023-04-12 22:43:24','2023-04-12 14:43:30',NULL,NULL,'2023-04-12 14:43:30'),
  (1646041414962839553,'bb','爸爸','account_type','账号分类',5,'1','0',NULL,NULL,NULL,'2023-04-12 14:43:50',NULL,NULL,NULL,'2023-04-12 14:43:50'),
  (1646041522651594753,'zp','宝宝','account_type','账号分类',1,'1','0',NULL,NULL,NULL,'2023-04-12 22:44:16','2023-04-12 14:44:26',NULL,NULL,'2023-04-12 14:44:26'),
  (1646041630751391746,'mj','疯疯','account_type','账号分类',7,'1','0',NULL,NULL,NULL,'2023-04-12 22:44:41','2023-04-13 09:06:14',NULL,NULL,'2023-04-13 09:06:14'),
  (1659135817042448385,'jd','京东','shop_type','商品类型',1,'1','0',NULL,NULL,NULL,'2023-05-18 17:56:19',NULL,NULL,NULL,'2023-05-18 17:56:19'),
  (1659135907698135042,'tb','淘宝','shop_type','商品类型',5,'1','0',NULL,NULL,NULL,'2023-05-18 17:56:40',NULL,NULL,NULL,'2023-05-18 17:56:40'),
  (1737023252031676418,'1','是','true_or_false','是否',1,'1','0',NULL,NULL,NULL,'2023-12-19 16:13:10',NULL,NULL,NULL,'2023-12-19 16:13:10'),
  (1737023313713111042,'0','否','true_or_false','是否',2,'1','0',NULL,NULL,NULL,'2023-12-19 16:13:25',NULL,NULL,NULL,'2023-12-19 16:13:25'),
  (1764810775592964097,'wx','微信','shop_pay_way','店支付方式',1,'1','0',1759760891393945602,NULL,NULL,'2024-03-05 08:30:52',NULL,NULL,1759760891393945602,'2024-03-05 08:30:52'),
  (1764810873492213761,'zfb','支付宝','shop_pay_way','店支付方式',10,'1','0',1759760891393945602,NULL,NULL,'2024-03-05 08:31:15',NULL,NULL,1759760891393945602,'2024-03-05 08:31:15'),
  (1764811038806511617,'xj','现金','shop_pay_way','店支付方式',20,'1','0',1759760891393945602,NULL,NULL,'2024-03-05 08:31:55',NULL,NULL,1759760891393945602,'2024-03-05 08:31:55'),
  (1767469743423361026,'sz','深圳','stock_place','进货地点',10,'1','0',1759760891393945602,NULL,NULL,'2024-03-12 16:36:39',NULL,NULL,1759760891393945602,'2024-03-12 16:36:39'),
  (1767469841217753090,'heb','哈尔滨','stock_place','进货地点',20,'1','0',1759760891393945602,NULL,NULL,'2024-03-12 16:37:03',NULL,NULL,1759760891393945602,'2024-03-12 16:37:03'),
  (1767469908796379137,'sy','沈阳','stock_place','进货地点',30,'1','0',1759760891393945602,NULL,NULL,'2024-03-12 16:37:19',NULL,NULL,1759760891393945602,'2024-03-12 16:37:19'),
  (1767471119142490113,'10','卫衣','shop_category','商品分类',100,'1','0',1759760891393945602,1759760891393945602,NULL,'2024-03-12 16:42:07','2024-03-19 14:28:44',NULL,1759760891393945602,'2024-03-19 14:28:44'),
  (1769970887173345282,'30','老爹裤','shop_category','商品分类',300,'1','0',1759760891393945602,1759760891393945602,NULL,'2024-03-19 14:15:19','2024-03-19 14:27:41',NULL,1759760891393945602,'2024-03-19 14:27:41'),
  (1769970982467932162,'29','西服裤','shop_category','商品分类',290,'1','0',1759760891393945602,1759760891393945602,NULL,'2024-03-19 14:15:41','2024-03-19 14:27:32',NULL,1759760891393945602,'2024-03-19 14:27:32'),
  (1779315162176524289,'50','套装','shop_category','商品分类',50,'1','0',1759760891393945602,NULL,NULL,'2024-04-14 09:06:07',NULL,NULL,1759760891393945602,'2024-04-14 09:06:07'),
  (1779315292678098945,'51','卫裤','shop_category','商品分类',51,'1','0',1759760891393945602,NULL,NULL,'2024-04-14 09:06:39',NULL,NULL,1759760891393945602,'2024-04-14 09:06:39'),
  (1779315374257311746,'52','七分裤','shop_category','商品分类',52,'1','0',1759760891393945602,NULL,NULL,'2024-04-14 09:06:58',NULL,NULL,1759760891393945602,'2024-04-14 09:06:58'),
  (1779315777992626178,'53','短裤','shop_category','商品分类',53,'1','0',1759760891393945602,NULL,NULL,'2024-04-14 09:08:34',NULL,NULL,1759760891393945602,'2024-04-14 09:08:34'),
  (1779315847316082689,'54','西装裤','shop_category','商品分类',54,'1','0',1759760891393945602,NULL,NULL,'2024-04-14 09:08:51',NULL,NULL,1759760891393945602,'2024-04-14 09:08:51'),
  (1779315903247126529,'55','皮裤','shop_category','商品分类',55,'1','0',1759760891393945602,NULL,NULL,'2024-04-14 09:09:04',NULL,NULL,1759760891393945602,'2024-04-14 09:09:04'),
  (1779315983245086721,'56','运动裤','shop_category','商品分类',56,'1','0',1759760891393945602,NULL,NULL,'2024-04-14 09:09:23',NULL,NULL,1759760891393945602,'2024-04-14 09:09:23'),
  (1779316081282748417,'57','牛仔裙','shop_category','商品分类',57,'1','0',1759760891393945602,NULL,NULL,'2024-04-14 09:09:47',NULL,NULL,1759760891393945602,'2024-04-14 09:09:47'),
  (1779316160727060481,'58','工装裙','shop_category','商品分类',58,'1','0',1759760891393945602,NULL,NULL,'2024-04-14 09:10:05',NULL,NULL,1759760891393945602,'2024-04-14 09:10:05'),
  (1779316246299250689,'59','裙子','shop_category','商品分类',59,'1','0',1759760891393945602,NULL,NULL,'2024-04-14 09:10:26',NULL,NULL,1759760891393945602,'2024-04-14 09:10:26'),
  (1779316346312429570,'60','风衣','shop_category','商品分类',60,'1','0',1759760891393945602,NULL,NULL,'2024-04-14 09:10:50',NULL,NULL,1759760891393945602,'2024-04-14 09:10:50'),
  (1779316436141838338,'61','外套','shop_category','商品分类',61,'1','0',1759760891393945602,NULL,NULL,'2024-04-14 09:11:11',NULL,NULL,1759760891393945602,'2024-04-14 09:11:11'),
  (1779316535421014017,'62','皮衣','shop_category','商品分类',62,'1','0',1759760891393945602,NULL,NULL,'2024-04-14 09:11:35',NULL,NULL,1759760891393945602,'2024-04-14 09:11:35'),
  (1779316617230913537,'63','毛衣','shop_category','商品分类',63,'1','0',1759760891393945602,NULL,NULL,'2024-04-14 09:11:54',NULL,NULL,1759760891393945602,'2024-04-14 09:11:54'),
  (1779316712546471937,'64','衬衫','shop_category','商品分类',64,'1','0',1759760891393945602,NULL,NULL,'2024-04-14 09:12:17',NULL,NULL,1759760891393945602,'2024-04-14 09:12:17'),
  (1779316828003078146,'65','连体裙','shop_category','商品分类',65,'1','0',1759760891393945602,NULL,NULL,'2024-04-14 09:12:45',NULL,NULL,1759760891393945602,'2024-04-14 09:12:45'),
  (1779316902670077953,'66','马甲','shop_category','商品分类',66,'1','0',1759760891393945602,NULL,NULL,'2024-04-14 09:13:02',NULL,NULL,1759760891393945602,'2024-04-14 09:13:02'),
  (1779343714716401666,'home','店','stock_place','进货地点',100,'1','0',1759760891393945602,NULL,NULL,'2024-04-14 10:59:35',NULL,NULL,1759760891393945602,'2024-04-14 10:59:35'),
  (1782633500466221058,'67','上衣','shop_category','商品分类',67,'1','0',1759760891393945602,NULL,NULL,'2024-04-23 12:52:01',NULL,NULL,1759760891393945602,'2024-04-23 12:52:01'),
  (1782634140147912706,'68','裤子','shop_category','商品分类',68,'1','0',1759760891393945602,NULL,NULL,'2024-04-23 12:54:33',NULL,NULL,1759760891393945602,'2024-04-23 12:54:33'),
  (1810860592752635905,'give','随礼','gift_action','随礼分类',1,'1','0',1759760891393945602,NULL,NULL,'2024-07-10 10:16:24',NULL,NULL,1759760891393945602,'2024-07-10 10:16:24'),
  (1810862765087535105,'recieve','收礼','gift_action','随礼分类',2,'1','0',1759760891393945602,NULL,NULL,'2024-07-10 10:25:02',NULL,NULL,1759760891393945602,'2024-07-10 10:25:02'),
  (1810865324653162498,'test','test','gift_action','随礼分类',3,'1','0',1759760891393945602,NULL,NULL,'2024-07-10 10:35:12',NULL,NULL,1759760891393945602,'2024-07-10 10:35:12');

-- ----------------------------
-- Table structure for finance_base_info
-- ----------------------------
DROP TABLE IF EXISTS `finance_base_info`;
CREATE TABLE `finance_base_info` (
`id` bigint NOT NULL COMMENT 'id',
  `amount` decimal(10,2) DEFAULT NULL COMMENT '钱数',
  `from_source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '来源',
  `is_valid` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '是否有效',
  `is_delete` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '是否删除',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `updater` bigint DEFAULT NULL COMMENT '更新人',
  `deleter` bigint DEFAULT NULL COMMENT '删除人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `delete_time` datetime DEFAULT NULL COMMENT '更新时间',
  `operator` bigint DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  `belong_to` bigint DEFAULT NULL COMMENT '属于',
  `info_date` datetime DEFAULT NULL COMMENT '业务日期',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `finance_base_info_from_source_IDX` (`from_source`,`is_valid`,`is_delete`,`belong_to`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='财务表';

-- ----------------------------
-- Table structure for finance_info
-- ----------------------------
DROP TABLE IF EXISTS `finance_info`;
CREATE TABLE `finance_info` (
`id` bigint NOT NULL COMMENT 'id',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '名称',
  `type_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '类别',
  `amount` decimal(10,2) DEFAULT NULL COMMENT '钱数',
  `from_source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '来源',
  `income_and_expenses` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '收入类型',
  `is_valid` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '是否有效',
  `is_delete` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '是否删除',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `updater` bigint DEFAULT NULL COMMENT '更新人',
  `deleter` bigint DEFAULT NULL COMMENT '删除人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `delete_time` datetime DEFAULT NULL COMMENT '更新时间',
  `operator` bigint DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  `belong_to` bigint DEFAULT NULL COMMENT '属于',
  `info_date` datetime DEFAULT NULL COMMENT '业务日期',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `finance_status_info_date_IDX` (`is_valid`,`is_delete`,`info_date`) USING BTREE,
  KEY `finance_info_from_source_IDX` (`from_source`,`belong_to`,`is_valid`,`is_delete`,`info_date`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='财务表';

-- ----------------------------
-- Table structure for finance_budget_info
-- ----------------------------
DROP TABLE IF EXISTS `finance_budget_info`;
CREATE TABLE `finance_budget_info` (
`id` bigint NOT NULL COMMENT 'id',
  `org_id` bigint NOT NULL DEFAULT '20' COMMENT '家庭组/机构ID',
  `belong_to` bigint DEFAULT NULL COMMENT '属于(用户ID，预留家庭组下特定个人)',
  `budget_month` varchar(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '预算月份(格式: YYYY-MM)',
  `income_and_expenses` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT 'expense' COMMENT '收支类型(expense:支出, income:收入)',
  `budget_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '月度零花钱预算金额',
  `category_codes` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '纳入统计的支出类别编码列表(逗号分隔，为空表示统计全部非转账支出)',
  `is_valid` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT '1' COMMENT '是否有效(1:有效 0:无效)',
  `is_delete` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT '0' COMMENT '是否删除(0:未删除 1:已删除)',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `updater` bigint DEFAULT NULL COMMENT '更新人',
  `deleter` bigint DEFAULT NULL COMMENT '删除人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
  `operator` bigint DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `finance_budget_org_month_IDX` (`org_id`,`budget_month`,`is_delete`) USING BTREE,
  KEY `finance_budget_belong_month_IDX` (`belong_to`,`budget_month`,`is_delete`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='财务月度零花钱与分类预算表';

-- ----------------------------
-- Table structure for cpn_coupon_info_t
-- ----------------------------
DROP TABLE IF EXISTS `cpn_coupon_info_t`;
CREATE TABLE `cpn_coupon_info_t` (
`id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID (消费券ID)',
  `coupon_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消费券名称',
  `total_quantity` int unsigned NOT NULL DEFAULT '0' COMMENT '消费券总发行数量',
  `start_date` datetime DEFAULT NULL COMMENT '有效期开始时间',
  `end_date` datetime NOT NULL COMMENT '有效期结束时间',
  `unit_value` decimal(10,2) DEFAULT NULL COMMENT '消费券单张面值',
  `min_spend` decimal(10,2) DEFAULT '0.00' COMMENT '最低消费门槛',
  `creator` bigint unsigned DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint unsigned DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `operator` bigint unsigned DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  `deleter` bigint unsigned DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否删除 (0:未删除, 1:已删除)',
  `payment_status` int DEFAULT NULL COMMENT '支付状态（1：已支付，0：未支付）',
  PRIMARY KEY (`id`),
  KEY `idx_date_range` (`start_date`,`end_date`)
) ENGINE = InnoDB AUTO_INCREMENT=2043623373755052034 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费券信息表';

-- ----------------------------
-- Table structure for cpn_user_coupon_info_t
-- ----------------------------
DROP TABLE IF EXISTS `cpn_user_coupon_info_t`;
CREATE TABLE `cpn_user_coupon_info_t` (
`id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID (用户消费券实例ID)',
  `user_id` bigint NOT NULL COMMENT '领取用户ID',
  `coupon_id` bigint unsigned NOT NULL COMMENT '对应的消费券ID (外键关联 cpn_coupon_info_t.id)',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '状态（UNUSED, USED, EXPIRED）',
  `receive_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  `expire_time` datetime DEFAULT NULL COMMENT '有效期截止时间',
  `creator` bigint unsigned DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint unsigned DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `operator` bigint unsigned DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  `deleter` bigint unsigned DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否删除 (0:未删除, 1:已删除)',
  `redemption_quantity` int unsigned DEFAULT NULL COMMENT '核销数量',
  PRIMARY KEY (`id`),
  KEY `idx_user_status` (`user_id`,`status`)
) ENGINE = InnoDB AUTO_INCREMENT=2051134076379828227 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户消费券库存表 (按数量核销)';

-- ----------------------------
-- Table structure for cpn_redemption_record_info_t
-- ----------------------------
DROP TABLE IF EXISTS `cpn_redemption_record_info_t`;
CREATE TABLE `cpn_redemption_record_info_t` (
`id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID (核销记录ID)',
  `user_coupon_id` bigint unsigned NOT NULL COMMENT '被核销的券实例ID (外键关联 cpn_user_coupon_info_t.id)',
  `user_id` bigint unsigned NOT NULL COMMENT '核销用户ID',
  `order_id` bigint unsigned DEFAULT NULL COMMENT '关联的订单ID',
  `redemption_quantity` int NOT NULL DEFAULT '1' COMMENT '本次核销数量 (固定为1)',
  `redemption_value` decimal(10,2) DEFAULT NULL COMMENT '核销券的面值（参考）',
  `redemption_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '核销时间',
  `merchant_id` int unsigned DEFAULT NULL COMMENT '核销商家ID',
  `creator` bigint unsigned DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` bigint unsigned DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `operator` bigint unsigned DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  `deleter` bigint unsigned DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否删除 (0:未删除, 1:已删除)',
  `remarks` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_user_coupon_final` (`user_coupon_id`),
  KEY `idx_order_id_final` (`order_id`),
  KEY `idx_user_redemption_final` (`user_id`,`redemption_time`)
) ENGINE = InnoDB AUTO_INCREMENT=2051134153240449027 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费券核销记录表 (按数量核销)';

-- ----------------------------
-- Table structure for prepaid_card_info_t
-- ----------------------------
DROP TABLE IF EXISTS `prepaid_card_info_t`;
CREATE TABLE `prepaid_card_info_t` (
`id` bigint NOT NULL COMMENT '主键ID（雪花算法）',
  `card_id` bigint DEFAULT NULL COMMENT '卡号（业务唯一标识）',
  `card_name` varchar(50) NOT NULL COMMENT '卡名称',
  `user_id` varchar(20) DEFAULT NULL COMMENT '持卡人ID',
  `initial_balance` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '初始金额',
  `current_balance` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '当前余额',
  `expire_date` date DEFAULT NULL COMMENT '过期日期',
  `card_status` varchar(10) DEFAULT NULL COMMENT '状态（正常/冻结/挂失/过期）',
  `creator` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `operator` bigint DEFAULT NULL COMMENT '操作人ID',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  `deleter` bigint DEFAULT NULL COMMENT '删除人ID',
  `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint DEFAULT '0' COMMENT '逻辑删除标记（0=正常，1=删除）',
  `version` int DEFAULT NULL COMMENT '版本',
  `order_no` int DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_card_id` (`card_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`card_status`)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='消费卡信息表';

-- ----------------------------
-- Table structure for prepaid_consume_record_t
-- ----------------------------
DROP TABLE IF EXISTS `prepaid_consume_record_t`;
CREATE TABLE `prepaid_consume_record_t` (
`id` bigint NOT NULL COMMENT '主键ID（雪花算法）',
  `card_id` bigint NOT NULL COMMENT '卡号（关联prepaid_card_info_t.card_id）',
  `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '交易流水号（业务唯一）',
  `amount` decimal(10,2) NOT NULL COMMENT '交易金额（正消费，负充值）',
  `balance_after` decimal(10,2) DEFAULT NULL COMMENT '交易后余额',
  `merchant_name` varchar(100) DEFAULT NULL COMMENT '商户名称',
  `consume_time` datetime NOT NULL COMMENT '交易时间',
  `description` varchar(200) DEFAULT NULL COMMENT '交易描述',
  `creator` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `operator` bigint DEFAULT NULL COMMENT '操作人ID',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  `deleter` bigint DEFAULT NULL COMMENT '删除人ID',
  `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint DEFAULT '0' COMMENT '逻辑删除（0=正常，1=删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_card_id` (`card_id`)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='消费卡交易记录表';

-- ----------------------------
-- Table structure for t_shop_finance
-- ----------------------------
DROP TABLE IF EXISTS `t_shop_finance`;
CREATE TABLE `t_shop_finance` (
`id` bigint NOT NULL COMMENT 'id',
  `shop_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '商品名称',
  `shop_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '商品编码',
  `sale_amount` decimal(10,2) DEFAULT NULL COMMENT '售价',
  `is_valid` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '是否有效',
  `is_delete` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '是否删除',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `updater` bigint DEFAULT NULL COMMENT '更新人',
  `deleter` bigint DEFAULT NULL COMMENT '删除人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `delete_time` datetime DEFAULT NULL COMMENT '更新时间',
  `operator` bigint DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  `sale_date` datetime DEFAULT NULL COMMENT '销售日期',
  `pay_way` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '支付方式',
  `income_and_expenses` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '收支类型',
  `sale_num` decimal(10,0) DEFAULT NULL COMMENT '个数',
  `shop_order_id` bigint DEFAULT NULL COMMENT '商品订单id',
  `shop_stock_id` bigint DEFAULT NULL COMMENT '商品库存id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='商店财务表';

-- ----------------------------
-- Table structure for t_shop_order
-- ----------------------------
DROP TABLE IF EXISTS `t_shop_order`;
CREATE TABLE `t_shop_order` (
`id` bigint NOT NULL COMMENT 'id',
  `sale_order_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '订单编码',
  `sale_order_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '订单名称',
  `sale_amount` decimal(10,2) DEFAULT NULL COMMENT '总销售金额',
  `is_valid` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '状态,字典(is_valid) 1：有效,0:失效)',
  `is_delete` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '是否删除',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `updater` bigint DEFAULT NULL COMMENT '更新人',
  `deleter` bigint DEFAULT NULL COMMENT '删除人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `delete_time` datetime DEFAULT NULL COMMENT '更新时间',
  `operator` bigint DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  `sale_date` datetime DEFAULT NULL COMMENT '销售日期',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin COMMENT '描述',
  `pay_way` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '支付方式',
  `sale_count` decimal(10,0) NOT NULL COMMENT '销售数量',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='商店订单表';

-- ----------------------------
-- Table structure for t_shop_order_detail
-- ----------------------------
DROP TABLE IF EXISTS `t_shop_order_detail`;
CREATE TABLE `t_shop_order_detail` (
`id` bigint NOT NULL COMMENT 'id',
  `order_id` bigint NOT NULL COMMENT '订单id',
  `shop_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '商品名称',
  `shop_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '商品编码',
  `sale_amount` decimal(10,2) DEFAULT NULL COMMENT '售价',
  `is_valid` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '是否有效',
  `is_delete` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '是否删除',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `updater` bigint DEFAULT NULL COMMENT '更新人',
  `deleter` bigint DEFAULT NULL COMMENT '删除人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `delete_time` datetime DEFAULT NULL COMMENT '更新时间',
  `operator` bigint DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  `sale_date` datetime DEFAULT NULL COMMENT '销售日期',
  `pay_way` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '支付方式',
  `sale_num` decimal(10,0) DEFAULT NULL COMMENT '个数',
  `shop_stock_id` bigint DEFAULT NULL COMMENT '商品库存id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='商店订单明细表';

-- ----------------------------
-- Table structure for t_shop_stock
-- ----------------------------
DROP TABLE IF EXISTS `t_shop_stock`;
CREATE TABLE `t_shop_stock` (
`id` bigint NOT NULL COMMENT 'id',
  `shop_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '商品名称',
  `shop_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '商品编码',
  `cost_amount` decimal(10,2) DEFAULT NULL COMMENT '成本价',
  `sale_amount` decimal(10,2) DEFAULT NULL COMMENT '零售价',
  `is_valid` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '状态,字典(is_valid) 1：有效,0:失效)',
  `is_delete` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '是否删除',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `updater` bigint DEFAULT NULL COMMENT '更新人',
  `deleter` bigint DEFAULT NULL COMMENT '删除人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `delete_time` datetime DEFAULT NULL COMMENT '更新时间',
  `operator` bigint DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  `sale_date` datetime DEFAULT NULL COMMENT '进货日期',
  `category` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '类别,字典(shop_category)',
  `purchase_place` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '进货地点,字典(stock_place) ',
  `sale_num` decimal(10,0) DEFAULT NULL COMMENT '数量',
  `old_shop_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '原商品编码',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin COMMENT '描述',
  `stock_batch` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '批次编码',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='商店库存表';

-- ----------------------------
-- Table structure for t_shop_cart
-- ----------------------------
DROP TABLE IF EXISTS `t_shop_cart`;
CREATE TABLE `t_shop_cart` (
`id` bigint NOT NULL COMMENT 'id',
  `shop_id` bigint DEFAULT NULL COMMENT '商品id',
  `user_id` bigint DEFAULT NULL COMMENT '人员id',
  `customer_id` bigint DEFAULT NULL COMMENT '客户id',
  `is_valid` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '是否有效',
  `is_delete` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '是否删除',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `updater` bigint DEFAULT NULL COMMENT '更新人',
  `deleter` bigint DEFAULT NULL COMMENT '删除人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `delete_time` datetime DEFAULT NULL COMMENT '更新时间',
  `operator` bigint DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  `sale_num` decimal(10,0) DEFAULT NULL COMMENT '数量',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='购物车表';

-- ----------------------------
-- Table structure for t_shop_stock_attrs
-- ----------------------------
DROP TABLE IF EXISTS `t_shop_stock_attrs`;
CREATE TABLE `t_shop_stock_attrs` (
`id` bigint NOT NULL COMMENT 'id',
  `stock_id` bigint DEFAULT NULL COMMENT '库存id',
  `attr_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '商品属性编码',
  `attr_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '商品属性名称',
  `attr_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '商品属性值',
  `is_valid` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '状态,字典(is_valid) 1：有效,0:失效)',
  `is_delete` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '是否删除',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `updater` bigint DEFAULT NULL COMMENT '更新人',
  `deleter` bigint DEFAULT NULL COMMENT '删除人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `delete_time` datetime DEFAULT NULL COMMENT '更新时间',
  `operator` bigint DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin COMMENT '描述',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='商店库存属性表';

-- ----------------------------
-- Table structure for t_shop_stock_batch
-- ----------------------------
DROP TABLE IF EXISTS `t_shop_stock_batch`;
CREATE TABLE `t_shop_stock_batch` (
`id` bigint NOT NULL COMMENT 'id',
  `batch_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '订单编码',
  `batch_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '订单名称',
  `is_valid` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '状态,字典(is_valid) 1：有效,0:失效)',
  `is_delete` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '是否删除',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `updater` bigint DEFAULT NULL COMMENT '更新人',
  `deleter` bigint DEFAULT NULL COMMENT '删除人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `delete_time` datetime DEFAULT NULL COMMENT '更新时间',
  `operator` bigint DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin COMMENT '描述',
  `purchase_place` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '库存地',
  `cost` decimal(10,2) DEFAULT NULL COMMENT '成本',
  `travel_expense` decimal(10,2) DEFAULT NULL COMMENT '差旅费',
  `purchase_date` datetime DEFAULT NULL COMMENT '进货日期',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='商店库存批次表';

-- ----------------------------
-- Table structure for gift_person_info_t
-- ----------------------------
DROP TABLE IF EXISTS `gift_person_info_t`;
CREATE TABLE `gift_person_info_t` (
`id` bigint NOT NULL COMMENT '主键',
  `org_id` bigint NOT NULL COMMENT '组织ID',
  `user_id` bigint NOT NULL COMMENT '归属用户ID',
  `bind_user_id` bigint DEFAULT NULL COMMENT '绑定系统用户ID',
  `person_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '人员姓名',
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '手机号',
  `avatar` bigint DEFAULT NULL COMMENT '头像 OSS 文件ID',
  `relation_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '关系类型',
  `relation_grade` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT 'NORMAL' COMMENT '关系等级: CORE核心, IMPORTANT重要, NORMAL普通, WEAK弱关系',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '备注',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `operator` bigint DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  `deleter` bigint DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_gift_person_org_user_name` (`org_id`,`user_id`,`person_name`) USING BTREE,
  KEY `idx_gift_person_org_phone` (`org_id`,`phone`) USING BTREE,
  KEY `idx_gift_person_bind_user` (`org_id`,`bind_user_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='礼尚往来人员表';

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
  `event_time` datetime DEFAULT NULL COMMENT '事件时间',
  `host_person_id` bigint DEFAULT NULL COMMENT '主办人员ID',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '备注',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `operator` bigint DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  `deleter` bigint DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_gift_event_org_user_time` (`org_id`,`user_id`,`event_time`) USING BTREE,
  KEY `idx_gift_event_org_type_time` (`org_id`,`event_type`,`event_time`) USING BTREE,
  KEY `idx_gift_event_host_person` (`org_id`,`host_person_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='礼尚往来事件表';

-- ----------------------------
-- Table structure for gift_record_info_t
-- ----------------------------
DROP TABLE IF EXISTS `gift_record_info_t`;
CREATE TABLE `gift_record_info_t` (
`id` bigint NOT NULL COMMENT '主键',
  `org_id` bigint NOT NULL COMMENT '组织ID',
  `user_id` bigint NOT NULL COMMENT '归属用户ID',
  `event_id` bigint DEFAULT NULL COMMENT '事件ID',
  `giver_person_id` bigint NOT NULL COMMENT '送礼人员ID',
  `receiver_person_id` bigint NOT NULL COMMENT '收礼人员ID',
  `related_record_id` bigint DEFAULT NULL COMMENT '关联原始收礼记录ID',
  `direction` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '礼金方向：GIVE/RECEIVE/RETURN',
  `amount` decimal(12,2) NOT NULL COMMENT '金额',
  `pay_time` datetime NOT NULL COMMENT '礼金时间',
  `returned_flag` tinyint DEFAULT '0' COMMENT '是否已回礼',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '备注',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `operator` bigint DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  `deleter` bigint DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_gift_record_org_pay_time` (`org_id`,`pay_time`,`id`) USING BTREE,
  KEY `idx_gift_record_org_user_pay_time` (`org_id`,`user_id`,`pay_time`,`id`) USING BTREE,
  KEY `idx_gift_record_event_direction_time` (`org_id`,`event_id`,`direction`,`pay_time`) USING BTREE,
  KEY `idx_gift_record_giver_time` (`org_id`,`giver_person_id`,`pay_time`) USING BTREE,
  KEY `idx_gift_record_receiver_time` (`org_id`,`receiver_person_id`,`pay_time`) USING BTREE,
  KEY `idx_gift_record_related` (`org_id`,`related_record_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='礼尚往来礼金记录表';

-- ----------------------------
-- Table structure for gift_person_relation_option_t
-- ----------------------------
DROP TABLE IF EXISTS `gift_person_relation_option_t`;
CREATE TABLE `gift_person_relation_option_t` (
`id` bigint NOT NULL COMMENT '主键',
  `org_id` bigint NOT NULL COMMENT '组织ID',
  `user_id` bigint NOT NULL COMMENT '归属用户ID',
  `option_type` varchar(16) COLLATE utf8mb4_bin NOT NULL DEFAULT 'CUSTOM' COMMENT 'SYSTEM|CUSTOM',
  `relation_code` varchar(32) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '预设code',
  `relation_label` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '自定义关系文案',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `last_used_time` datetime DEFAULT NULL COMMENT '最近使用时间',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `operator` bigint DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  `deleter` bigint DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_gift_person_relation_option_user_label` (`user_id`,`relation_label`) USING BTREE,
  UNIQUE KEY `uk_gift_person_relation_option_system_code` (`user_id`,`relation_code`),
  KEY `idx_gift_person_relation_option_user_time` (`user_id`,`last_used_time` DESC) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='礼尚往来联系人关系词典';

-- ----------------------------
-- Records of gift_person_relation_option_t
-- ----------------------------
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
  `event_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '预设code',
  `event_label` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '类型展示文案',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `last_used_time` datetime DEFAULT NULL COMMENT '最近使用时间',
  `category` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT '其他' COMMENT '事由分类: 婚庆类, 家庭类, 节日类, 其他',
  `icon` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '图标/Emoji',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0禁用, 1启用',
  `use_count` int NOT NULL DEFAULT '0' COMMENT '累计使用次数',
  `default_amount` decimal(12,2) DEFAULT NULL COMMENT '默认推荐金额',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updater` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `operator` bigint DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  `deleter` bigint DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_gift_event_type_option_org_label` (`org_id`,`event_label`) USING BTREE,
  UNIQUE KEY `uk_gift_event_type_option_system_code` (`user_id`,`event_code`) USING BTREE,
  KEY `idx_gift_event_type_option_org_time` (`org_id`,`last_used_time` DESC) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='礼尚往来事由类型词典';

-- ----------------------------
-- Records of gift_event_type_option_t
-- ----------------------------
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
-- Table structure for gift_event_type_user_config_t
-- ----------------------------
DROP TABLE IF EXISTS `gift_event_type_user_config_t`;
CREATE TABLE `gift_event_type_user_config_t` (
`id` bigint NOT NULL COMMENT '主键ID',
  `option_id` bigint NOT NULL COMMENT '关联的事由分类选项ID',
  `org_id` bigint DEFAULT NULL COMMENT '机构/家庭组ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '启用状态(1:启用, 0:停用)',
  `custom_amount` decimal(10,2) DEFAULT NULL COMMENT '个性化推荐金额',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `creator` bigint DEFAULT NULL COMMENT '创建人',
  `updater` bigint DEFAULT NULL COMMENT '更新人',
  `operator` bigint DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  `deleter` bigint DEFAULT NULL COMMENT '删除人',
  `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_org_option` (`org_id`,`option_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='事由分类个性化配置表';

-- ----------------------------
-- Table structure for sys_message_info_t
-- ----------------------------
DROP TABLE IF EXISTS `sys_message_info_t`;
CREATE TABLE `sys_message_info_t` (
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
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统消息与业务到期提醒表';

-- ----------------------------
-- Table structure for user_notice_config_t
-- ----------------------------
DROP TABLE IF EXISTS `user_notice_config_t`;
CREATE TABLE `user_notice_config_t` (
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
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户个性化消息渠道开关配置表';

SET FOREIGN_KEY_CHECKS = 1;
