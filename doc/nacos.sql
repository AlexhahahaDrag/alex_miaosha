/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 50739 (5.7.39-log)
 Source Host           : localhost:3306
 Source Schema         : nacos

 Target Server Type    : MySQL
 Target Server Version : 50739 (5.7.39-log)
 File Encoding         : 65001

 Date: 08/11/2022 17:59:58
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for config_info
-- ----------------------------
DROP TABLE IF EXISTS `config_info`;
CREATE TABLE `config_info`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `content` longtext CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'content',
  `md5` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `src_user` text CHARACTER SET utf8 COLLATE utf8_bin NULL COMMENT 'source user',
  `src_ip` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'source ip',
  `app_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '' COMMENT '租户字段',
  `c_desc` varchar(256) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `c_use` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `effect` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `type` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `c_schema` text CHARACTER SET utf8 COLLATE utf8_bin NULL,
  `encrypted_data_key` text CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '秘钥',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_configinfo_datagrouptenant`(`data_id`, `group_id`, `tenant_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = 'config_info' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of config_info
-- ----------------------------
INSERT INTO `config_info` VALUES (1, 'redis.yaml', 'alex-miaosha', 'spring:\n    redis:\n        cluster:\n            nodes: 10.10.20.100:7001,10.10.20.100:7002,10.10.20.100:7003,10.10.20.100:7004,10.10.20.100:7005,10.10.20.100:7006\n            max-redicts: 5\n        password: 123456', 'ee719b63412e45080a571f549868e04c', '2022-11-08 09:10:33', '2022-11-08 09:25:24', 'nacos', '0:0:0:0:0:0:0:1', '', '033377eb-973b-4dac-a0e9-e99c87325009', '', '', '', 'yaml', '', '');
INSERT INTO `config_info` VALUES (5, 'nacos-service.yaml', 'alex-miaosha', 'spring:\r\n    cloud:\r\n        nacos:\r\n        discovery:\r\n            server-addr: ${nacos.url}\r\n            group: ${nacos.group}\r\n            namespace: ${nacos.namaspace}\r\n        server-addr: ${nacos.url}', '8c2ea29ef4b948c3af107adf6149a252', '2022-11-08 09:30:09', '2022-11-08 09:30:09', 'nacos', '0:0:0:0:0:0:0:1', '', '033377eb-973b-4dac-a0e9-e99c87325009', NULL, NULL, NULL, 'yaml', NULL, '');
INSERT INTO `config_info` VALUES (6, 'knife4j.yaml', 'alex-miaosha', '#配置knife4j\r\nknife4j:\r\n  #开启增强\r\n  enable: true\r\n  production: false', '0f2c8de6d4b9399fcb151c9d69acd36d', '2022-11-08 09:41:39', '2022-11-08 09:41:39', 'nacos', '0:0:0:0:0:0:0:1', '', '033377eb-973b-4dac-a0e9-e99c87325009', NULL, NULL, NULL, 'yaml', NULL, '');
INSERT INTO `config_info` VALUES (7, 'springbootadmin.yaml', 'alex-miaosha', 'spring:\n  boot:\n    admin:\n      client:\n        #        url: http://localhost:30099\n        instance:\n          service-url: http://localhost:${server.port}\n          name: ${spring.application.name}\n  #        username: admin\n  #        password: 123456\n  mvc:\n    pathmatch:\n      matching-strategy: ant_path_matcher\nmanagement:\n  endpoints:\n    web:\n      exposure:\n        include: \'*\'\n    #   在访问/actuator/health时显示完整信息\n  endpoint:\n    health:\n      show-details: ALWAYS', 'de216e6ed7e7ca2c73b0e42e6e9edb6a', '2022-11-08 09:46:42', '2022-11-08 09:58:14', 'nacos', '0:0:0:0:0:0:0:1', '', '033377eb-973b-4dac-a0e9-e99c87325009', '', '', '', 'yaml', '', '');

-- ----------------------------
-- Table structure for config_info_aggr
-- ----------------------------
DROP TABLE IF EXISTS `config_info_aggr`;
CREATE TABLE `config_info_aggr`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'group_id',
  `datum_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'datum_id',
  `content` longtext CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '内容',
  `gmt_modified` datetime NOT NULL COMMENT '修改时间',
  `app_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '' COMMENT '租户字段',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_configinfoaggr_datagrouptenantdatum`(`data_id`, `group_id`, `tenant_id`, `datum_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = '增加租户字段' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of config_info_aggr
-- ----------------------------

-- ----------------------------
-- Table structure for config_info_beta
-- ----------------------------
DROP TABLE IF EXISTS `config_info_beta`;
CREATE TABLE `config_info_beta`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'group_id',
  `app_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'app_name',
  `content` longtext CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'content',
  `beta_ips` varchar(1024) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'betaIps',
  `md5` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `src_user` text CHARACTER SET utf8 COLLATE utf8_bin NULL COMMENT 'source user',
  `src_ip` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'source ip',
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '' COMMENT '租户字段',
  `encrypted_data_key` text CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '秘钥',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_configinfobeta_datagrouptenant`(`data_id`, `group_id`, `tenant_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = 'config_info_beta' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of config_info_beta
-- ----------------------------

-- ----------------------------
-- Table structure for config_info_tag
-- ----------------------------
DROP TABLE IF EXISTS `config_info_tag`;
CREATE TABLE `config_info_tag`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'group_id',
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '' COMMENT 'tenant_id',
  `tag_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'tag_id',
  `app_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'app_name',
  `content` longtext CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'content',
  `md5` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `src_user` text CHARACTER SET utf8 COLLATE utf8_bin NULL COMMENT 'source user',
  `src_ip` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'source ip',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_configinfotag_datagrouptenanttag`(`data_id`, `group_id`, `tenant_id`, `tag_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = 'config_info_tag' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of config_info_tag
-- ----------------------------

-- ----------------------------
-- Table structure for config_tags_relation
-- ----------------------------
DROP TABLE IF EXISTS `config_tags_relation`;
CREATE TABLE `config_tags_relation`  (
  `id` bigint(20) NOT NULL COMMENT 'id',
  `tag_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'tag_name',
  `tag_type` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'tag_type',
  `data_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'group_id',
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '' COMMENT 'tenant_id',
  `nid` bigint(20) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`nid`) USING BTREE,
  UNIQUE INDEX `uk_configtagrelation_configidtag`(`id`, `tag_name`, `tag_type`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = 'config_tag_relation' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of config_tags_relation
-- ----------------------------

-- ----------------------------
-- Table structure for group_capacity
-- ----------------------------
DROP TABLE IF EXISTS `group_capacity`;
CREATE TABLE `group_capacity`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'Group ID，空字符表示整个集群',
  `quota` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '配额，0表示使用默认值',
  `usage` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '使用量',
  `max_size` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '单个配置大小上限，单位为字节，0表示使用默认值',
  `max_aggr_count` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '聚合子配置最大个数，，0表示使用默认值',
  `max_aggr_size` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '单个聚合数据的子配置大小上限，单位为字节，0表示使用默认值',
  `max_history_count` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '最大变更历史数量',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_group_id`(`group_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = '集群、各Group容量信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of group_capacity
-- ----------------------------

-- ----------------------------
-- Table structure for his_config_info
-- ----------------------------
DROP TABLE IF EXISTS `his_config_info`;
CREATE TABLE `his_config_info`  (
  `id` bigint(64) UNSIGNED NOT NULL,
  `nid` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `data_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `group_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `app_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'app_name',
  `content` longtext CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `md5` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `src_user` text CHARACTER SET utf8 COLLATE utf8_bin NULL,
  `src_ip` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `op_type` char(10) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '' COMMENT '租户字段',
  `encrypted_data_key` text CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '秘钥',
  PRIMARY KEY (`nid`) USING BTREE,
  INDEX `idx_gmt_create`(`gmt_create`) USING BTREE,
  INDEX `idx_gmt_modified`(`gmt_modified`) USING BTREE,
  INDEX `idx_did`(`data_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = '多租户改造' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of his_config_info
-- ----------------------------
INSERT INTO `his_config_info` VALUES (0, 1, 'redis.yaml', 'alex-miaosha', '', 'spring:\r\n    redis:\r\n        cluster:\r\n        nodes: 10.10.20.100:7001,10.10.20.100:7002,10.10.20.100:7003,10.10.20.100:7004,10.10.20.100:7005,10.10.20.100:7006\r\n        max-redicts: 5\r\n    password: 123456', '0ebc7197dad0d9cfdf42b0a65317d18c', '2022-11-08 17:10:33', '2022-11-08 09:10:33', 'nacos', '0:0:0:0:0:0:0:1', 'I', '033377eb-973b-4dac-a0e9-e99c87325009', '');
INSERT INTO `his_config_info` VALUES (1, 2, 'redis.yaml', 'alex-miaosha', '', 'spring:\r\n    redis:\r\n        cluster:\r\n        nodes: 10.10.20.100:7001,10.10.20.100:7002,10.10.20.100:7003,10.10.20.100:7004,10.10.20.100:7005,10.10.20.100:7006\r\n        max-redicts: 5\r\n    password: 123456', '0ebc7197dad0d9cfdf42b0a65317d18c', '2022-11-08 17:24:55', '2022-11-08 09:24:55', 'nacos', '0:0:0:0:0:0:0:1', 'U', '033377eb-973b-4dac-a0e9-e99c87325009', '');
INSERT INTO `his_config_info` VALUES (1, 3, 'redis.yaml', 'alex-miaosha', '', 'spring:\n    redis:\n        cluster:\n            nodes: 10.10.20.100:7001,10.10.20.100:7002,10.10.20.100:7003,10.10.20.100:7004,10.10.20.100:7005,10.10.20.100:7006\n            max-redicts: 5\n    password: 123456', 'b7ba0ef4e05481d627a777713d272337', '2022-11-08 17:25:23', '2022-11-08 09:25:24', 'nacos', '0:0:0:0:0:0:0:1', 'U', '033377eb-973b-4dac-a0e9-e99c87325009', '');
INSERT INTO `his_config_info` VALUES (0, 4, 'nacos.yaml', 'alex-miaosha', '', 'spring:\r\n    cloud:\r\n        nacos:\r\n        discovery:\r\n            server-addr: ${nacos.url}\r\n            group: ${nacos.group}\r\n            namespace: ${nacos.namaspace}\r\n        server-addr: ${nacos.url}', '8c2ea29ef4b948c3af107adf6149a252', '2022-11-08 17:28:43', '2022-11-08 09:28:43', 'nacos', '0:0:0:0:0:0:0:1', 'I', '033377eb-973b-4dac-a0e9-e99c87325009', '');
INSERT INTO `his_config_info` VALUES (0, 5, 'nacos-service.yaml', 'alex-miaosha', '', 'spring:\r\n    cloud:\r\n        nacos:\r\n        discovery:\r\n            server-addr: ${nacos.url}\r\n            group: ${nacos.group}\r\n            namespace: ${nacos.namaspace}\r\n        server-addr: ${nacos.url}', '8c2ea29ef4b948c3af107adf6149a252', '2022-11-08 17:30:08', '2022-11-08 09:30:09', 'nacos', '0:0:0:0:0:0:0:1', 'I', '033377eb-973b-4dac-a0e9-e99c87325009', '');
INSERT INTO `his_config_info` VALUES (4, 6, 'nacos.yaml', 'alex-miaosha', '', 'spring:\r\n    cloud:\r\n        nacos:\r\n        discovery:\r\n            server-addr: ${nacos.url}\r\n            group: ${nacos.group}\r\n            namespace: ${nacos.namaspace}\r\n        server-addr: ${nacos.url}', '8c2ea29ef4b948c3af107adf6149a252', '2022-11-08 17:30:12', '2022-11-08 09:30:13', 'nacos', '0:0:0:0:0:0:0:1', 'D', '033377eb-973b-4dac-a0e9-e99c87325009', '');
INSERT INTO `his_config_info` VALUES (0, 7, 'knife4j.yaml', 'alex-miaosha', '', '#配置knife4j\r\nknife4j:\r\n  #开启增强\r\n  enable: true\r\n  production: false', '0f2c8de6d4b9399fcb151c9d69acd36d', '2022-11-08 17:41:38', '2022-11-08 09:41:39', 'nacos', '0:0:0:0:0:0:0:1', 'I', '033377eb-973b-4dac-a0e9-e99c87325009', '');
INSERT INTO `his_config_info` VALUES (0, 8, 'springbootadmin.yaml', 'alex-miaosha', '', 'spring:\r\n  boot:\r\n    admin:\r\n      client:\r\n        #        url: http://localhost:30099\r\n        instance:\r\n          service-url: http://localhost:${server.port}\r\n          name: ${spring.application.name}\r\n  #        username: admin\r\n  #        password: 123456', '50dd9d74e9a6940a4d7c6f5dee3f5ca1', '2022-11-08 17:46:41', '2022-11-08 09:46:42', 'nacos', '0:0:0:0:0:0:0:1', 'I', '033377eb-973b-4dac-a0e9-e99c87325009', '');
INSERT INTO `his_config_info` VALUES (7, 9, 'springbootadmin.yaml', 'alex-miaosha', '', 'spring:\r\n  boot:\r\n    admin:\r\n      client:\r\n        #        url: http://localhost:30099\r\n        instance:\r\n          service-url: http://localhost:${server.port}\r\n          name: ${spring.application.name}\r\n  #        username: admin\r\n  #        password: 123456', '50dd9d74e9a6940a4d7c6f5dee3f5ca1', '2022-11-08 17:50:58', '2022-11-08 09:50:59', 'nacos', '0:0:0:0:0:0:0:1', 'U', '033377eb-973b-4dac-a0e9-e99c87325009', '');
INSERT INTO `his_config_info` VALUES (7, 10, 'springbootadmin.yaml', 'alex-miaosha', '', 'spring:\n  boot:\n    admin:\n      client:\n        #        url: http://localhost:30099\n        instance:\n          service-url: http://localhost:${server.port}\n          name: ${spring.application.name}\n  #        username: admin\n  #        password: 123456\n  mvc:\n    pathmatch:\n      matching-strategy: ant_path_matcher', 'dfe1bda6607e4f679aef46f58bdad5f0', '2022-11-08 17:51:30', '2022-11-08 09:51:31', 'nacos', '0:0:0:0:0:0:0:1', 'U', '033377eb-973b-4dac-a0e9-e99c87325009', '');
INSERT INTO `his_config_info` VALUES (7, 11, 'springbootadmin.yaml', 'alex-miaosha', '', 'spring:\n  boot:\n    admin:\n      client:\n        #        url: http://localhost:30099\n        instance:\n          service-url: http://localhost:${server.port}\n          name: ${spring.application.name}\n  #        username: admin\n  #        password: 123456\n  mvc:\n    pathmatch:\n      matching-strategy: ant_path_matcher\nmanagement:\n  endpoints:\n    web:\n      exposure:\n        include: \'*\'\n    #   在访问/actuator/health时显示完整信息', 'e77fa934d3ce4aea00c49b77b33230ce', '2022-11-08 17:52:59', '2022-11-08 09:52:59', 'nacos', '0:0:0:0:0:0:0:1', 'U', '033377eb-973b-4dac-a0e9-e99c87325009', '');
INSERT INTO `his_config_info` VALUES (7, 12, 'springbootadmin.yaml', 'alex-miaosha', '', 'spring:\n  boot:\n    admin:\n      client:\n        #        url: http://localhost:30099\n        instance:\n          service-url: http://localhost:${server.port}\n          name: ${spring.application.name}\n  #        username: admin\n  #        password: 123456\n  mvc:\n    pathmatch:\n      matching-strategy: ant_path_matcher\nmanagement:\n  endpoints:\n    web:\n      exposure:\n        include: \'*\'\n    #   在访问/actuator/health时显示完整信息', 'e77fa934d3ce4aea00c49b77b33230ce', '2022-11-08 17:58:14', '2022-11-08 09:58:14', 'nacos', '0:0:0:0:0:0:0:1', 'U', '033377eb-973b-4dac-a0e9-e99c87325009', '');

-- ----------------------------
-- Table structure for permissions
-- ----------------------------
DROP TABLE IF EXISTS `permissions`;
CREATE TABLE `permissions`  (
  `role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `resource` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `action` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  UNIQUE INDEX `uk_role_permission`(`role`, `resource`, `action`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of permissions
-- ----------------------------

-- ----------------------------
-- Table structure for roles
-- ----------------------------
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles`  (
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  UNIQUE INDEX `idx_user_role`(`username`, `role`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of roles
-- ----------------------------
INSERT INTO `roles` VALUES ('nacos', 'ROLE_ADMIN');

-- ----------------------------
-- Table structure for tenant_capacity
-- ----------------------------
DROP TABLE IF EXISTS `tenant_capacity`;
CREATE TABLE `tenant_capacity`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'Tenant ID',
  `quota` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '配额，0表示使用默认值',
  `usage` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '使用量',
  `max_size` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '单个配置大小上限，单位为字节，0表示使用默认值',
  `max_aggr_count` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '聚合子配置最大个数',
  `max_aggr_size` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '单个聚合数据的子配置大小上限，单位为字节，0表示使用默认值',
  `max_history_count` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '最大变更历史数量',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_id`(`tenant_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = '租户容量信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tenant_capacity
-- ----------------------------

-- ----------------------------
-- Table structure for tenant_info
-- ----------------------------
DROP TABLE IF EXISTS `tenant_info`;
CREATE TABLE `tenant_info`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `kp` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'kp',
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '' COMMENT 'tenant_id',
  `tenant_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '' COMMENT 'tenant_name',
  `tenant_desc` varchar(256) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'tenant_desc',
  `create_source` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'create_source',
  `gmt_create` bigint(20) NOT NULL COMMENT '创建时间',
  `gmt_modified` bigint(20) NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_info_kptenantid`(`kp`, `tenant_id`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = 'tenant_info' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tenant_info
-- ----------------------------
INSERT INTO `tenant_info` VALUES (1, '1', '033377eb-973b-4dac-a0e9-e99c87325009', 'alex-miaosha', 'alex秒杀项目配置文件', 'nacos', 1659424590106, 1659424590106);

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `enabled` tinyint(1) NOT NULL,
  PRIMARY KEY (`username`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES ('nacos', '$2a$10$EuWPZHzz32dJN7jexM34MOeYirDdFAZm2kuWj7VEOJhhZkDrxfvUu', 1);

SET FOREIGN_KEY_CHECKS = 1;
