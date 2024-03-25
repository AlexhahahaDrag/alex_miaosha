/*
 Navicat Premium Data Transfer

 Source Server         : 10.10.20.38
 Source Server Type    : MySQL
 Source Server Version : 80200 (8.2.0)
 Source Host           : 10.10.20.38:3336
 Source Schema         : nacos

 Target Server Type    : MySQL
 Target Server Version : 80200 (8.2.0)
 File Encoding         : 65001

 Date: 25/03/2024 21:56:54
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for config_info
-- ----------------------------
DROP TABLE IF EXISTS `config_info`;
CREATE TABLE `config_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL,
  `content` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'content',
  `md5` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `src_user` text CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL COMMENT 'source user',
  `src_ip` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'source ip',
  `app_name` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL,
  `tenant_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT '' COMMENT '租户字段',
  `c_desc` varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL,
  `c_use` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL,
  `effect` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL,
  `type` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL,
  `c_schema` text CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL,
  `encrypted_data_key` text CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '秘钥',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_configinfo_datagrouptenant`(`data_id` ASC, `group_id` ASC, `tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 97 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_bin COMMENT = 'config_info' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of config_info
-- ----------------------------
INSERT INTO `config_info` VALUES (1, 'redis.yaml', 'alex-miaosha', '# spring:\n#   redis:\n#     cluster:\n#       # 集群节点\n#       nodes: 123.249.83.33:7001,123.249.83.33:7002,123.249.83.33:7003,123.249.83.33:7004,123.249.83.33:7005,123.249.83.33:7006\n#       # 最大重定向次数\n#       max-redirects: 5\n#     # 密码\n#     password: 123456\n\nspring:\n  redis:\n    # Redis服务器地址\n    host: 10.10.20.38\n    # Redis服务器端口号\n    port: 6679\n    # 使用的数据库索引，默认是0\n    database: 0\n    # 连接超时时间\n    timeout: 1800000\n     # 设置密码\n    password: \"ma_redis\"', '3d287569cb0508fe6e9232ed76112113', '2022-11-08 09:10:33', '2024-03-19 11:37:45', 'nacos', '116.2.220.42', '', '033377eb-973b-4dac-a0e9-e99c87325009', '', '', '', 'yaml', '', '');
INSERT INTO `config_info` VALUES (5, 'nacos-service.yaml', 'alex-miaosha', 'spring:\n  cloud:\n    nacos:\n      discovery:\n        server-addr: ${nacos.url}\n        group: ${nacos.group}\n        namespace: ${nacos.namaspace}\n        username: ${nacos.username}\n        password: ${nacos.password}', 'ab883e1d24609e452f4f63e871fceb9e', '2022-11-08 09:30:09', '2022-11-10 09:19:24', 'nacos', '0:0:0:0:0:0:0:1', '', '033377eb-973b-4dac-a0e9-e99c87325009', '', '', '', 'yaml', '', '');
INSERT INTO `config_info` VALUES (6, 'knife4j.yaml', 'alex-miaosha', '#配置knife4j\r\nknife4j:\r\n  #开启增强\r\n  enable: true\r\n  production: false', '0f2c8de6d4b9399fcb151c9d69acd36d', '2022-11-08 09:41:39', '2022-11-08 09:41:39', 'nacos', '0:0:0:0:0:0:0:1', '', '033377eb-973b-4dac-a0e9-e99c87325009', NULL, NULL, NULL, 'yaml', NULL, '');
INSERT INTO `config_info` VALUES (7, 'springbootadmin.yaml', 'alex-miaosha', 'spring:\n  boot:\n    admin:\n      client:\n        instance:\n          service-url: http://123.249.83.33:${server.port}\n          name: ${spring.application.name}\n  #        username: admin\n  #        password: 123456\n  mvc:\n    pathmatch:\n      matching-strategy: ant_path_matcher\nmanagement:\n  endpoints:\n    web:\n      exposure:\n        include: \'*\'\n    #   在访问/actuator/health时显示完整信息\n  endpoint:\n    prometheus:\n      enabled: true\n    health:\n      show-details: ALWAYS\n  metrics:\n    export:\n      prometheus:\n        enabled: true', 'a3ab371711d363dfa7e6057f8487c49a', '2022-11-08 09:46:42', '2022-12-29 15:35:12', 'nacos', '172.19.0.5', '', '033377eb-973b-4dac-a0e9-e99c87325009', '', '', '', 'yaml', '', '');
INSERT INTO `config_info` VALUES (15, 'jasypt-user.yaml', 'alex-miaosha', 'jasypt:\r\n encryptor:\r\n   password: 02700083-9fd9-4b82-a4b4-9177e0560e92\r\n   algorithm: PBEWithMD5AndDES\r\n   iv-generator-classname: org.jasypt.iv.NoIvGenerator\r\n', 'c6c4f2100d51e1d640bba8a8778f9eaf', '2022-12-29 09:41:44', '2022-12-29 09:41:44', NULL, '172.19.0.5', '', '033377eb-973b-4dac-a0e9-e99c87325009', NULL, NULL, NULL, 'yaml', NULL, '');
INSERT INTO `config_info` VALUES (20, 'audience.yaml', 'alex-miaosha', '#设置jwt\naudience:\n  clientId: 098f6bcd4621d373cade4e832627b4f6\n  base64Secret: MDk4ZjZiY2Q0NjIxZDM3M2NhZGU0ZTgzMjYyN2I0ZjY=\n  name: alex_miaosha\n  expiresSecond: 3600  #1个小时 3600\n  refreshSecond: 3600\n  tokenHead: bearer_\n  tokenHeader: Authorization\n  whiteList: /am-user/api/v1/user/login,/swagger-resources/**,/doc.html,/**/v3/api-docs/**', 'e8e55c9f147c5a40d0fefcbd96a52485', '2023-01-05 16:29:56', '2023-12-20 17:57:21', 'nacos', '175.160.218.254', '', '033377eb-973b-4dac-a0e9-e99c87325009', '', '', '', 'yaml', '', '');
INSERT INTO `config_info` VALUES (40, 'redis.yaml', 'alex-miaosha-prod', '# spring:\n#   redis:\n#     cluster:\n#       # 集群节点\n#       nodes: redis-1:7001,redis-2:7002,redis-3:7003,redis-4:7004,redis-5:7005,redis-6:7006\n#       # 最大重定向次数\n#       max-redirects: 5\n#     # 密码\n#     password: 123456\n\n\nspring:\n  redis:\n    # Redis服务器地址\n    host: redis\n    # Redis服务器端口号\n    port: 6679\n    # 使用的数据库索引，默认是0\n    database: 0\n    # 连接超时时间\n    timeout: 1800000\n     # 设置密码\n    password: \"ma_redis\"', 'fd7212951aec2070e117f83305b74fab', '2023-02-03 22:47:46', '2024-03-19 11:33:26', 'nacos', '116.2.220.42', '', '439dbcdc-b721-4afd-abee-08184739eb75', '', '', '', 'yaml', '', '');
INSERT INTO `config_info` VALUES (41, 'nacos-service.yaml', 'alex-miaosha-prod', 'spring:\n  cloud:\n    nacos:\n      discovery:\n        server-addr: ${nacos.url}\n        group: ${nacos.group}\n        namespace: ${nacos.namaspace}\n        username: ${nacos.username}\n        password: ${nacos.password}', 'ab883e1d24609e452f4f63e871fceb9e', '2023-02-03 22:47:46', '2023-02-03 22:47:46', NULL, '112.41.59.39', '', '439dbcdc-b721-4afd-abee-08184739eb75', '', NULL, NULL, 'yaml', NULL, '');
INSERT INTO `config_info` VALUES (42, 'knife4j.yaml', 'alex-miaosha-prod', '#配置knife4j\nknife4j:\n  #开启增强\n  enable: true\n  production: true', '2563ea4f3d2fa5a2636e900c37db1755', '2023-02-03 22:47:46', '2023-02-10 17:38:48', 'nacos', '175.160.214.76', '', '439dbcdc-b721-4afd-abee-08184739eb75', '', '', '', 'yaml', '', '');
INSERT INTO `config_info` VALUES (43, 'springbootadmin.yaml', 'alex-miaosha-prod', 'spring:\n  boot:\n    admin:\n      client:\n        instance:\n          service-url: http://monitor:${server.port}\n          name: ${spring.application.name}\n  #        username: admin\n  #        password: 123456\n  mvc:\n    pathmatch:\n      matching-strategy: ant_path_matcher\nmanagement:\n  endpoints:\n    web:\n      exposure:\n        include: \'*\'\n    #   在访问/actuator/health时显示完整信息\n  endpoint:\n    prometheus:\n      enabled: true\n    health:\n      show-details: ALWAYS\n  metrics:\n    export:\n      prometheus:\n        enabled: true', '5896bd1a9cd1584b7c50dba1d5b427d5', '2023-02-03 22:47:46', '2023-02-03 22:47:46', NULL, '112.41.59.39', '', '439dbcdc-b721-4afd-abee-08184739eb75', '', NULL, NULL, 'yaml', NULL, '');
INSERT INTO `config_info` VALUES (44, 'jasypt-user.yaml', 'alex-miaosha-prod', 'jasypt:\r\n encryptor:\r\n   password: 02700083-9fd9-4b82-a4b4-9177e0560e92\r\n   algorithm: PBEWithMD5AndDES\r\n   iv-generator-classname: org.jasypt.iv.NoIvGenerator\r\n', 'c6c4f2100d51e1d640bba8a8778f9eaf', '2023-02-03 22:47:46', '2023-02-03 22:47:46', NULL, '112.41.59.39', '', '439dbcdc-b721-4afd-abee-08184739eb75', NULL, NULL, NULL, 'yaml', NULL, '');
INSERT INTO `config_info` VALUES (45, 'audience.yaml', 'alex-miaosha-prod', '#设置jwt\naudience:\n  clientId: 098f6bcd4621d373cade4e832627b4f6\n  base64Secret: MDk4ZjZiY2Q0NjIxZDM3M2NhZGU0ZTgzMjYyN2I0ZjY=\n  name: alex_miaosha\n  expiresSecond: 3600  #1个小时 3600\n  refreshSecond: 300\n  tokenHead: bearer_\n  tokenHeader: Authorization\n  whiteList: /am-user/api/v1/user/login,/swagger-resources/**,/doc.html,/**/v3/api-docs/**,/am-user/api/v1/menu-info/list,/am-user/api/v1/menu-info', '3cc861a7e49a69a39a6dbb10eec52641', '2023-02-03 22:47:46', '2023-12-20 17:51:20', 'nacos', '175.160.218.254', '', '439dbcdc-b721-4afd-abee-08184739eb75', '', '', '', 'yaml', '', '');
INSERT INTO `config_info` VALUES (59, 'minio.yaml', 'alex-miaosha', 'minio:\n  url: 123.249.83.33\n  port: 9000  # 9001 访问minio\n  accessKey: gyr1YGRn2wr4jQgGz4Zf\n  secretKey: EUMhzE1fCBkTgZWU4JwfjXrIrvF8vHwgsSM34zcR\n  bucketName: user-bucket', '33cb7bce8aed70e3e22a9fcacf92bf66', '2023-03-24 11:23:38', '2024-02-27 12:02:25', 'nacos', '175.160.222.200', '', '033377eb-973b-4dac-a0e9-e99c87325009', '', '', '', 'yaml', '', '');
INSERT INTO `config_info` VALUES (60, 'minio.yaml', 'alex-miaosha-prod', 'minio:\n  url: minio\n  port: 9000\n  accessKey: gyr1YGRn2wr4jQgGz4Zf\n  secretKey: EUMhzE1fCBkTgZWU4JwfjXrIrvF8vHwgsSM34zcR\n  bucketName: user-bucket', 'dfa6ce0503123fa002f6d20b563da989', '2023-03-24 11:24:14', '2024-02-27 12:05:04', 'nacos', '175.160.222.200', '', '439dbcdc-b721-4afd-abee-08184739eb75', '', '', '', 'yaml', '', '');
INSERT INTO `config_info` VALUES (63, 'wechat.yaml', 'alex-miaosha', 'wechat:\n  #测试号信息中的appID\n  appId: wxec93a0ddb72c8cff\n  #测试号信息中的appsecret\n  secret: 1240434ae0be6dc4b0ba979d7c1f9b7a\n  #用户列表中的微信号id\n  userId: ol6WP5zaolNHhOuuouTOzq6_eNto\n  url: https://api.weixin.qq.com\n  #模板消息接口中模板ID\n  accountTimeOutTemplateId: \n  shopFinanceTemplateId: \n  accountUserId: \n  shopFinanceUserId: \n  #在一起的时间\n  loveDate: 2022-05-20\n  #生日\n  birthday: 1994-02-10\n  #彩虹屁平台apiKey\n  rainbowKey:\n\naccountNotice:\n  difDay: 2', '20b80f67b17967a1b21cf76170034cef', '2023-04-12 20:50:25', '2024-03-10 21:48:22', 'nacos', '112.39.98.161', '', '033377eb-973b-4dac-a0e9-e99c87325009', '', '', '', 'yaml', '', '');
INSERT INTO `config_info` VALUES (64, 'wechat.yaml', 'alex-miaosha-prod', 'wechat:\n  #测试号信息中的appID\n  appId: wxec93a0ddb72c8cff\n  #测试号信息中的appsecret\n  secret: 1240434ae0be6dc4b0ba979d7c1f9b7a\n  #用户列表中的微信号id\n  userId: ol6WP5zaolNHhOuuouTOzq6_eNto\n  url: https://api.weixin.qq.com\n  #模板消息接口中模板ID\n  accountTimeOutTemplateId: \n  shopFinanceTemplateId: \n  accountUserId: \n  shopFinanceUserId: \n  #在一起的时间\n  loveDate: 2022-05-20\n  #生日\n  birthday: 1994-02-10\n  #彩虹屁平台apiKey\n  rainbowKey:\n\naccountNotice:\n  difDay: 2', 'd2f9a33d6eb168917d405f575b3e3664', '2023-04-12 20:51:00', '2024-03-10 22:08:48', 'nacos', '112.39.98.161', '', '439dbcdc-b721-4afd-abee-08184739eb75', '', '', '', 'yaml', '', '');
INSERT INTO `config_info` VALUES (76, 'xxl.yaml', 'alex-miaosha', 'xxl:\n  job:\n    admin:\n      addresses: http://localhost:8080/xxl-job-admin', 'eb2a786fb1bca52c4d41236950593edf', '2023-12-28 11:58:58', '2024-03-08 08:13:10', 'nacos', '112.39.98.161', '', '033377eb-973b-4dac-a0e9-e99c87325009', '', '', '', 'yaml', '', '');
INSERT INTO `config_info` VALUES (78, 'xxl.yaml', 'alex-miaosha-prod', 'xxl:\n  job:\n    admin:\n      addresses: http://xxl-job-admin:8080/xxl-job-admin', '88ebf24f667ac34786dd85afcc4958f2', '2023-12-28 11:59:38', '2023-12-28 11:59:50', 'nacos', '175.160.218.254', '', '439dbcdc-b721-4afd-abee-08184739eb75', '', '', '', 'yaml', '', '');

-- ----------------------------
-- Table structure for config_info_aggr
-- ----------------------------
DROP TABLE IF EXISTS `config_info_aggr`;
CREATE TABLE `config_info_aggr`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'group_id',
  `datum_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'datum_id',
  `content` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '内容',
  `gmt_modified` datetime NOT NULL COMMENT '修改时间',
  `app_name` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL,
  `tenant_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT '' COMMENT '租户字段',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_configinfoaggr_datagrouptenantdatum`(`data_id` ASC, `group_id` ASC, `tenant_id` ASC, `datum_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_bin COMMENT = '增加租户字段' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of config_info_aggr
-- ----------------------------

-- ----------------------------
-- Table structure for config_info_beta
-- ----------------------------
DROP TABLE IF EXISTS `config_info_beta`;
CREATE TABLE `config_info_beta`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'group_id',
  `app_name` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'app_name',
  `content` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'content',
  `beta_ips` varchar(1024) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'betaIps',
  `md5` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `src_user` text CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL COMMENT 'source user',
  `src_ip` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'source ip',
  `tenant_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT '' COMMENT '租户字段',
  `encrypted_data_key` text CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '秘钥',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_configinfobeta_datagrouptenant`(`data_id` ASC, `group_id` ASC, `tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_bin COMMENT = 'config_info_beta' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of config_info_beta
-- ----------------------------

-- ----------------------------
-- Table structure for config_info_tag
-- ----------------------------
DROP TABLE IF EXISTS `config_info_tag`;
CREATE TABLE `config_info_tag`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'group_id',
  `tenant_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT '' COMMENT 'tenant_id',
  `tag_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'tag_id',
  `app_name` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'app_name',
  `content` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'content',
  `md5` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `src_user` text CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL COMMENT 'source user',
  `src_ip` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'source ip',
  `encrypted_data_key` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '加密key',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_configinfotag_datagrouptenanttag`(`data_id` ASC, `group_id` ASC, `tenant_id` ASC, `tag_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_bin COMMENT = 'config_info_tag' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of config_info_tag
-- ----------------------------

-- ----------------------------
-- Table structure for config_tags_relation
-- ----------------------------
DROP TABLE IF EXISTS `config_tags_relation`;
CREATE TABLE `config_tags_relation`  (
  `id` bigint NOT NULL COMMENT 'id',
  `tag_name` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'tag_name',
  `tag_type` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'tag_type',
  `data_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'group_id',
  `tenant_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT '' COMMENT 'tenant_id',
  `nid` bigint NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`nid`) USING BTREE,
  UNIQUE INDEX `uk_configtagrelation_configidtag`(`id` ASC, `tag_name` ASC, `tag_type` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_bin COMMENT = 'config_tag_relation' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of config_tags_relation
-- ----------------------------

-- ----------------------------
-- Table structure for group_capacity
-- ----------------------------
DROP TABLE IF EXISTS `group_capacity`;
CREATE TABLE `group_capacity`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL DEFAULT '' COMMENT 'Group ID，空字符表示整个集群',
  `quota` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '配额，0表示使用默认值',
  `usage` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '使用量',
  `max_size` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '单个配置大小上限，单位为字节，0表示使用默认值',
  `max_aggr_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '聚合子配置最大个数，，0表示使用默认值',
  `max_aggr_size` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '单个聚合数据的子配置大小上限，单位为字节，0表示使用默认值',
  `max_history_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '最大变更历史数量',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_group_id`(`group_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_bin COMMENT = '集群、各Group容量信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of group_capacity
-- ----------------------------

-- ----------------------------
-- Table structure for his_config_info
-- ----------------------------
DROP TABLE IF EXISTS `his_config_info`;
CREATE TABLE `his_config_info`  (
  `id` bigint UNSIGNED NOT NULL,
  `nid` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `data_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `group_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `app_name` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'app_name',
  `content` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `md5` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL,
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `src_user` text CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL,
  `src_ip` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL,
  `op_type` char(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL,
  `tenant_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT '' COMMENT '租户字段',
  `encrypted_data_key` text CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '秘钥',
  PRIMARY KEY (`nid`) USING BTREE,
  INDEX `idx_gmt_create`(`gmt_create` ASC) USING BTREE,
  INDEX `idx_gmt_modified`(`gmt_modified` ASC) USING BTREE,
  INDEX `idx_did`(`data_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 118 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_bin COMMENT = '多租户改造' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of his_config_info
-- ----------------------------
INSERT INTO `his_config_info` VALUES (59, 101, 'minio.yaml', 'alex-miaosha', '', 'minio:\r\n  url: 123.249.83.33\r\n  port: 9000  # 9001 访问minio\r\n  accessKey: b3TpHq6VLZWZHinK\r\n  secretKey: eec4E7NOILKlEFsrfKwbdFZQFo2IPFYG\r\n  bucketName: user-bucket', '284d310694cae063a950e75e6f8188b7', '2024-02-27 12:02:25', '2024-02-27 12:02:25', 'nacos', '175.160.222.200', 'U', '033377eb-973b-4dac-a0e9-e99c87325009', '');
INSERT INTO `his_config_info` VALUES (60, 102, 'minio.yaml', 'alex-miaosha-prod', '', 'minio:\n  url: minio\n  port: 9000\n  accessKey: b3TpHq6VLZWZHinK\n  secretKey: eec4E7NOILKlEFsrfKwbdFZQFo2IPFYG\n  bucketName: user-bucket', 'e6db7a7e359653414b3959252ceec206', '2024-02-27 12:05:03', '2024-02-27 12:05:04', 'nacos', '175.160.222.200', 'U', '439dbcdc-b721-4afd-abee-08184739eb75', '');
INSERT INTO `his_config_info` VALUES (63, 103, 'wechat.yaml', 'alex-miaosha', '', 'wechat:\r\n  #测试号信息中的appID\r\n  appId: wxec93a0ddb72c8cff\r\n  #测试号信息中的appsecret\r\n  secret: 1240434ae0be6dc4b0ba979d7c1f9b7a\r\n  #用户列表中的微信号id\r\n  userId: ol6WP5zaolNHhOuuouTOzq6_eNto\r\n  url: https://api.weixin.qq.com\r\n  #模板消息接口中模板ID\r\n  accountTimeOutTemplateId: D1xMFgcyw8LcREdrdukhxTU0R7H9wnSMOe8gcwQhxuY\r\n  #在一起的时间\r\n  loveDate: 2022-05-20\r\n  #生日\r\n  birthday: 1994-02-10\r\n  #彩虹屁平台apiKey\r\n  rainbowKey:\r\n\r\naccountNotice:\r\n  difDay: 2', '995acaa636907886790a27bb62d1d270', '2024-03-07 11:49:07', '2024-03-07 11:49:07', 'nacos', '116.2.213.201', 'U', '033377eb-973b-4dac-a0e9-e99c87325009', '');
INSERT INTO `his_config_info` VALUES (63, 104, 'wechat.yaml', 'alex-miaosha', '', 'wechat:\n  #测试号信息中的appID\n  appId: wxec93a0ddb72c8cff\n  #测试号信息中的appsecret\n  secret: 1240434ae0be6dc4b0ba979d7c1f9b7a\n  #用户列表中的微信号id\n  userId: ol6WP5zaolNHhOuuouTOzq6_eNto\n  url: https://api.weixin.qq.com\n  #模板消息接口中模板ID\n  accountTimeOutTemplateId: D1xMFgcyw8LcREdrdukhxTU0R7H9wnSMOe8gcwQhxuY\n  shopFinanceTemplateId：_wivkNfw4XHcuGGyY42sOi0_PUAv3QZwTDzVSjy-LYo\n  #在一起的时间\n  loveDate: 2022-05-20\n  #生日\n  birthday: 1994-02-10\n  #彩虹屁平台apiKey\n  rainbowKey:\n\naccountNotice:\n  difDay: 2', 'fbc9070e3503ed4f51146a9ece6b6af7', '2024-03-07 11:50:00', '2024-03-07 11:50:01', 'nacos', '116.2.213.201', 'U', '033377eb-973b-4dac-a0e9-e99c87325009', '');
INSERT INTO `his_config_info` VALUES (63, 105, 'wechat.yaml', 'alex-miaosha', '', 'wechat:\n  #测试号信息中的appID\n  appId: wxec93a0ddb72c8cff\n  #测试号信息中的appsecret\n  secret: 1240434ae0be6dc4b0ba979d7c1f9b7a\n  #用户列表中的微信号id\n  userId: ol6WP5zaolNHhOuuouTOzq6_eNto\n  url: https://api.weixin.qq.com\n  #模板消息接口中模板ID\n  accountTimeOutTemplateId: D1xMFgcyw8LcREdrdukhxTU0R7H9wnSMOe8gcwQhxuY\n  shopFinanceTemplateId:_wivkNfw4XHcuGGyY42sOi0_PUAv3QZwTDzVSjy-LYo\n  #在一起的时间\n  loveDate: 2022-05-20\n  #生日\n  birthday: 1994-02-10\n  #彩虹屁平台apiKey\n  rainbowKey:\n\naccountNotice:\n  difDay: 2', '4f7d90d163900c3957c2495aa983bc17', '2024-03-07 11:50:24', '2024-03-07 11:50:24', 'nacos', '116.2.213.201', 'U', '033377eb-973b-4dac-a0e9-e99c87325009', '');
INSERT INTO `his_config_info` VALUES (64, 106, 'wechat.yaml', 'alex-miaosha-prod', '', 'wechat:\r\n  #测试号信息中的appID\r\n  appId: wxec93a0ddb72c8cff\r\n  #测试号信息中的appsecret\r\n  secret: 1240434ae0be6dc4b0ba979d7c1f9b7a\r\n  #用户列表中的微信号id\r\n  userId: ol6WP5zaolNHhOuuouTOzq6_eNto\r\n  url: https://api.weixin.qq.com\r\n  #模板消息接口中模板ID\r\n  accountTimeOutTemplateId: D1xMFgcyw8LcREdrdukhxTU0R7H9wnSMOe8gcwQhxuY\r\n  #在一起的时间\r\n  loveDate: 2022-05-20\r\n  #生日\r\n  birthday: 1994-02-10\r\n  #彩虹屁平台apiKey\r\n  rainbowKey:\r\n\r\naccountNotice:\r\n  difDay: 2', '995acaa636907886790a27bb62d1d270', '2024-03-07 11:50:36', '2024-03-07 11:50:37', 'nacos', '116.2.213.201', 'U', '439dbcdc-b721-4afd-abee-08184739eb75', '');
INSERT INTO `his_config_info` VALUES (63, 107, 'wechat.yaml', 'alex-miaosha', '', 'wechat:\n  #测试号信息中的appID\n  appId: wxec93a0ddb72c8cff\n  #测试号信息中的appsecret\n  secret: 1240434ae0be6dc4b0ba979d7c1f9b7a\n  #用户列表中的微信号id\n  userId: ol6WP5zaolNHhOuuouTOzq6_eNto\n  url: https://api.weixin.qq.com\n  #模板消息接口中模板ID\n  accountTimeOutTemplateId: D1xMFgcyw8LcREdrdukhxTU0R7H9wnSMOe8gcwQhxuY\n  shopFinanceTemplateId: J8zeejiay0yP3SrzcxcDdFLKYhwn-fTHfpCXwss9zLA\n  #在一起的时间\n  loveDate: 2022-05-20\n  #生日\n  birthday: 1994-02-10\n  #彩虹屁平台apiKey\n  rainbowKey:\n\naccountNotice:\n  difDay: 2', '32da535c49edc8a6cd9f1a88ec949f9e', '2024-03-07 11:54:44', '2024-03-07 11:54:44', 'nacos', '175.160.217.20', 'U', '033377eb-973b-4dac-a0e9-e99c87325009', '');
INSERT INTO `his_config_info` VALUES (64, 108, 'wechat.yaml', 'alex-miaosha-prod', '', 'wechat:\n  #测试号信息中的appID\n  appId: wxec93a0ddb72c8cff\n  #测试号信息中的appsecret\n  secret: 1240434ae0be6dc4b0ba979d7c1f9b7a\n  #用户列表中的微信号id\n  userId: ol6WP5zaolNHhOuuouTOzq6_eNto\n  url: https://api.weixin.qq.com\n  #模板消息接口中模板ID\n  accountTimeOutTemplateId: D1xMFgcyw8LcREdrdukhxTU0R7H9wnSMOe8gcwQhxuY\n  shopFinanceTemplateId: J8zeejiay0yP3SrzcxcDdFLKYhwn-fTHfpCXwss9zLA\n  #在一起的时间\n  loveDate: 2022-05-20\n  #生日\n  birthday: 1994-02-10\n  #彩虹屁平台apiKey\n  rainbowKey:\n\naccountNotice:\n  difDay: 2', '32da535c49edc8a6cd9f1a88ec949f9e', '2024-03-07 11:55:04', '2024-03-07 11:55:04', 'nacos', '116.2.213.201', 'U', '439dbcdc-b721-4afd-abee-08184739eb75', '');
INSERT INTO `his_config_info` VALUES (76, 109, 'xxl.yaml', 'alex-miaosha', '', 'xxl:\n  job:\n    admin:\n      addresses: http://10.10.20.38:8123/xxl-job-admin', '09886c903b71022a518a8de5a510d5af', '2024-03-08 08:13:09', '2024-03-08 08:13:10', 'nacos', '112.39.98.161', 'U', '033377eb-973b-4dac-a0e9-e99c87325009', '');
INSERT INTO `his_config_info` VALUES (64, 110, 'wechat.yaml', 'alex-miaosha-prod', '', 'wechat:\n  #测试号信息中的appID\n  appId: wxec93a0ddb72c8cff\n  #测试号信息中的appsecret\n  secret: 1240434ae0be6dc4b0ba979d7c1f9b7a\n  #用户列表中的微信号id\n  userId: ol6WP5zaolNHhOuuouTOzq6_eNto\n  url: https://api.weixin.qq.com\n  #模板消息接口中模板ID\n  accountTimeOutTemplateId: D1xMFgcyw8LcREdrdukhxTU0R7H9wnSMOe8gcwQhxuY\n  shopFinanceTemplateId: J8zeejiay0yP3SrzcxcDdFLKYhwn-fTHfpCXwss9zLA\n  accountUserId: ol6WP5zaolNHhOuuouTOzq6_eNto,ol6WP5xKDRqutv_RQRFDyZdcJ3qk,\n  shopFinanceUserId: ol6WP5xKDRqutv_RQRFDyZdcJ3qk,\n  #在一起的时间\n  loveDate: 2022-05-20\n  #生日\n  birthday: 1994-02-10\n  #彩虹屁平台apiKey\n  rainbowKey:\n\naccountNotice:\n  difDay: 2', '85834c3071a4093dcd8e0cccd4c70100', '2024-03-10 08:30:15', '2024-03-10 08:30:16', 'nacos', '223.101.57.150', 'U', '439dbcdc-b721-4afd-abee-08184739eb75', '');
INSERT INTO `his_config_info` VALUES (64, 111, 'wechat.yaml', 'alex-miaosha-prod', '', 'wechat:\n  #测试号信息中的appID\n  appId: wxec93a0ddb72c8cff\n  #测试号信息中的appsecret\n  secret: 1240434ae0be6dc4b0ba979d7c1f9b7a\n  #用户列表中的微信号id\n  userId: ol6WP5zaolNHhOuuouTOzq6_eNto\n  url: https://api.weixin.qq.com\n  #模板消息接口中模板ID\n  accountTimeOutTemplateId: D1xMFgcyw8LcREdrdukhxTU0R7H9wnSMOe8gcwQhxuY\n  shopFinanceTemplateId: J8zeejiay0yP3SrzcxcDdFLKYhwn-fTHfpCXwss9zLA\n  accountUserId: ol6WP5zaolNHhOuuouTOzq6_eNto,ol6WP5xKDRqutv_RQRFDyZdcJ3qk,\n  shopFinanceUserId: ol6WP5xKDRqutv_RQRFDyZdcJ3qk,ol6WP5wb6udq22pHUHKdTOl3yJsY,ol6WP5_uVuq1QZYUJqc4W5PznV_U,\n  #在一起的时间\n  loveDate: 2022-05-20\n  #生日\n  birthday: 1994-02-10\n  #彩虹屁平台apiKey\n  rainbowKey:\n\naccountNotice:\n  difDay: 2', 'bab58fb7baf30306017a0c6976bb2cdf', '2024-03-10 08:35:13', '2024-03-10 08:35:13', 'nacos', '223.101.57.150', 'U', '439dbcdc-b721-4afd-abee-08184739eb75', '');
INSERT INTO `his_config_info` VALUES (63, 112, 'wechat.yaml', 'alex-miaosha', '', 'wechat:\n  #测试号信息中的appID\n  appId: wxec93a0ddb72c8cff\n  #测试号信息中的appsecret\n  secret: 1240434ae0be6dc4b0ba979d7c1f9b7a\n  #用户列表中的微信号id\n  userId: ol6WP5zaolNHhOuuouTOzq6_eNto\n  url: https://api.weixin.qq.com\n  #模板消息接口中模板ID\n  accountTimeOutTemplateId: D1xMFgcyw8LcREdrdukhxTU0R7H9wnSMOe8gcwQhxuY\n  shopFinanceTemplateId: J8zeejiay0yP3SrzcxcDdFLKYhwn-fTHfpCXwss9zLA\n  accountUserId: ol6WP5zaolNHhOuuouTOzq6_eNto,ol6WP5xKDRqutv_RQRFDyZdcJ3qk,\n  shopFinanceUserId: ol6WP5xKDRqutv_RQRFDyZdcJ3qk,\n  #在一起的时间\n  loveDate: 2022-05-20\n  #生日\n  birthday: 1994-02-10\n  #彩虹屁平台apiKey\n  rainbowKey:\n\naccountNotice:\n  difDay: 2', '85834c3071a4093dcd8e0cccd4c70100', '2024-03-10 21:46:01', '2024-03-10 21:46:02', 'nacos', '112.39.98.161', 'U', '033377eb-973b-4dac-a0e9-e99c87325009', '');
INSERT INTO `his_config_info` VALUES (64, 113, 'wechat.yaml', 'alex-miaosha-prod', '', 'wechat:\n  #测试号信息中的appID\n  appId: wxec93a0ddb72c8cff\n  #测试号信息中的appsecret\n  secret: 1240434ae0be6dc4b0ba979d7c1f9b7a\n  #用户列表中的微信号id\n  userId: ol6WP5zaolNHhOuuouTOzq6_eNto\n  url: https://api.weixin.qq.com\n  #模板消息接口中模板ID\n  accountTimeOutTemplateId: D1xMFgcyw8LcREdrdukhxTU0R7H9wnSMOe8gcwQhxuY\n  shopFinanceTemplateId: J8zeejiay0yP3SrzcxcDdFLKYhwn-fTHfpCXwss9zLA\n  accountUserId: ol6WP5zaolNHhOuuouTOzq6_eNto,ol6WP5xKDRqutv_RQRFDyZdcJ3qk,\n  shopFinanceUserId: ol6WP5xKDRqutv_RQRFDyZdcJ3qk,ol6WP5wb6udq22pHUHKdTOl3yJsY,ol6WP5_uVuq1QZYUJqc4W5PznV_U,\n  #在一起的时间\n  loveDate: 2022-05-20\n  #生日\n  birthday: 1994-02-10\n  #彩虹屁平台apiKey\n  rainbowKey:\n\naccountNotice:\n  difDay: 2', 'bab58fb7baf30306017a0c6976bb2cdf', '2024-03-10 21:46:18', '2024-03-10 21:46:19', 'nacos', '112.39.98.161', 'U', '439dbcdc-b721-4afd-abee-08184739eb75', '');
INSERT INTO `his_config_info` VALUES (63, 114, 'wechat.yaml', 'alex-miaosha', '', 'wechat:\n  #测试号信息中的appID\n  appId: wxec93a0ddb72c8cff\n  #测试号信息中的appsecret\n  secret: 1240434ae0be6dc4b0ba979d7c1f9b7a\n  #用户列表中的微信号id\n  userId: ol6WP5zaolNHhOuuouTOzq6_eNto\n  url: https://api.weixin.qq.com\n  #模板消息接口中模板ID\n  accountTimeOutTemplateId: D1xMFgcyw8LcREdrdukhxTU0R7H9wnSMOe8gcwQhxuY\n  shopFinanceTemplateId: 0GsIK2I0AEkB4E9UAnaSAuAzkqd-TkPRSCtoGW3ljV8\n  accountUserId: ol6WP5zaolNHhOuuouTOzq6_eNto,ol6WP5xKDRqutv_RQRFDyZdcJ3qk,\n  shopFinanceUserId: ol6WP5xKDRqutv_RQRFDyZdcJ3qk,\n  #在一起的时间\n  loveDate: 2022-05-20\n  #生日\n  birthday: 1994-02-10\n  #彩虹屁平台apiKey\n  rainbowKey:\n\naccountNotice:\n  difDay: 2', '31cebb94cec48d3e64948f647387ea71', '2024-03-10 21:48:21', '2024-03-10 21:48:22', 'nacos', '112.39.98.161', 'U', '033377eb-973b-4dac-a0e9-e99c87325009', '');
INSERT INTO `his_config_info` VALUES (64, 115, 'wechat.yaml', 'alex-miaosha-prod', '', 'wechat:\n  #测试号信息中的appID\n  appId: wxec93a0ddb72c8cff\n  #测试号信息中的appsecret\n  secret: 1240434ae0be6dc4b0ba979d7c1f9b7a\n  #用户列表中的微信号id\n  userId: ol6WP5zaolNHhOuuouTOzq6_eNto\n  url: https://api.weixin.qq.com\n  #模板消息接口中模板ID\n  accountTimeOutTemplateId: D1xMFgcyw8LcREdrdukhxTU0R7H9wnSMOe8gcwQhxuY\n  shopFinanceTemplateId: 0GsIK2I0AEkB4E9UAnaSAuAzkqd-TkPRSCtoGW3ljV8\n  accountUserId: ol6WP5zaolNHhOuuouTOzq6_eNto,ol6WP5xKDRqutv_RQRFDyZdcJ3qk,\n  shopFinanceUserId: ol6WP5xKDRqutv_RQRFDyZdcJ3qk,ol6WP5wb6udq22pHUHKdTOl3yJsY,ol6WP5_uVuq1QZYUJqc4W5PznV_U,\n  #在一起的时间\n  loveDate: 2022-05-20\n  #生日\n  birthday: 1994-02-10\n  #彩虹屁平台apiKey\n  rainbowKey:\n\naccountNotice:\n  difDay: 2', '41dc053b7f60921a0a3ef6e8f859f429', '2024-03-10 22:08:47', '2024-03-10 22:08:48', 'nacos', '112.39.98.161', 'U', '439dbcdc-b721-4afd-abee-08184739eb75', '');
INSERT INTO `his_config_info` VALUES (40, 116, 'redis.yaml', 'alex-miaosha-prod', '', '# spring:\n#   redis:\n#     cluster:\n#       # 集群节点\n#       nodes: redis-1:7001,redis-2:7002,redis-3:7003,redis-4:7004,redis-5:7005,redis-6:7006\n#       # 最大重定向次数\n#       max-redirects: 5\n#     # 密码\n#     password: 123456\n\n\nspring:\n  redis:\n    # Redis服务器地址\n    host: redis\n    # Redis服务器端口号\n    port: 6379\n    # 使用的数据库索引，默认是0\n    database: 0\n    # 连接超时时间\n    timeout: 1800000\n     # 设置密码\n    password: \"123456\"', 'f0554bcd87135b9b157849e18d5ed102', '2024-03-19 11:33:25', '2024-03-19 11:33:26', 'nacos', '116.2.220.42', 'U', '439dbcdc-b721-4afd-abee-08184739eb75', '');
INSERT INTO `his_config_info` VALUES (1, 117, 'redis.yaml', 'alex-miaosha', '', '# spring:\n#   redis:\n#     cluster:\n#       # 集群节点\n#       nodes: 123.249.83.33:7001,123.249.83.33:7002,123.249.83.33:7003,123.249.83.33:7004,123.249.83.33:7005,123.249.83.33:7006\n#       # 最大重定向次数\n#       max-redirects: 5\n#     # 密码\n#     password: 123456\n\nspring:\n  redis:\n    # Redis服务器地址\n    host: 10.10.20.38\n    # Redis服务器端口号\n    port: 6379\n    # 使用的数据库索引，默认是0\n    database: 0\n    # 连接超时时间\n    timeout: 1800000\n     # 设置密码\n    password: \"123456\"', 'f1c5b858b5a3da07e900732361cf043b', '2024-03-19 11:37:44', '2024-03-19 11:37:45', 'nacos', '116.2.220.42', 'U', '033377eb-973b-4dac-a0e9-e99c87325009', '');

-- ----------------------------
-- Table structure for permissions
-- ----------------------------
DROP TABLE IF EXISTS `permissions`;
CREATE TABLE `permissions`  (
  `role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `resource` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `action` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  UNIQUE INDEX `uk_role_permission`(`role` ASC, `resource` ASC, `action` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

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
  UNIQUE INDEX `idx_user_role`(`username` ASC, `role` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of roles
-- ----------------------------
INSERT INTO `roles` VALUES ('nacos', 'ROLE_ADMIN');

-- ----------------------------
-- Table structure for tenant_capacity
-- ----------------------------
DROP TABLE IF EXISTS `tenant_capacity`;
CREATE TABLE `tenant_capacity`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL DEFAULT '' COMMENT 'Tenant ID',
  `quota` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '配额，0表示使用默认值',
  `usage` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '使用量',
  `max_size` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '单个配置大小上限，单位为字节，0表示使用默认值',
  `max_aggr_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '聚合子配置最大个数',
  `max_aggr_size` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '单个聚合数据的子配置大小上限，单位为字节，0表示使用默认值',
  `max_history_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '最大变更历史数量',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_bin COMMENT = '租户容量信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tenant_capacity
-- ----------------------------

-- ----------------------------
-- Table structure for tenant_info
-- ----------------------------
DROP TABLE IF EXISTS `tenant_info`;
CREATE TABLE `tenant_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `kp` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'kp',
  `tenant_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT '' COMMENT 'tenant_id',
  `tenant_name` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT '' COMMENT 'tenant_name',
  `tenant_desc` varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'tenant_desc',
  `create_source` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'create_source',
  `gmt_create` bigint NOT NULL COMMENT '创建时间',
  `gmt_modified` bigint NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_info_kptenantid`(`kp` ASC, `tenant_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_bin COMMENT = 'tenant_info' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tenant_info
-- ----------------------------
INSERT INTO `tenant_info` VALUES (1, '1', '033377eb-973b-4dac-a0e9-e99c87325009', 'alex-miaosha', 'alex秒杀项目配置文件', 'nacos', 1659424590106, 1659424590106);
INSERT INTO `tenant_info` VALUES (2, '1', '439dbcdc-b721-4afd-abee-08184739eb75', 'alex-miaosha-prod', 'alex-miaosha-prod', 'nacos', 1675433842801, 1675433842801);

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `enabled` tinyint(1) NOT NULL,
  PRIMARY KEY (`username`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES ('nacos', '$2a$10$EuWPZHzz32dJN7jexM34MOeYirDdFAZm2kuWj7VEOJhhZkDrxfvUu', 1);

SET FOREIGN_KEY_CHECKS = 1;
