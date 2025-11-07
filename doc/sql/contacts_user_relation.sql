-- 联系人关系分类字典表 - SQL 脚本
-- author: alex
-- createDate: 2025-11-07
-- description: 用于维护关系分类字典，包括系统公共分类和用户自定义分类

-- AI Agent
-- 创建联系人关系分类字典表
CREATE TABLE IF NOT EXISTS `contacts_user_relation_info_t` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID，为空表示公共字典，有值表示用户自定义分类',
  `relationship_tag` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '关系标签，如：重要客户、潜在客户等',
  `importance` tinyint NOT NULL COMMENT '重要程度，1-普通，2-重要，3-非常重要',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '描述信息',
  `remarks` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注信息',
  `is_enabled` tinyint NOT NULL DEFAULT '1' COMMENT '是否启用，0-禁用，1-启用',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '更新人',
  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
  `operator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作人',
  `operate_time` bigint DEFAULT NULL COMMENT '操作时间',
  `deleter` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '删除人',
  `delete_time` bigint DEFAULT NULL COMMENT '删除时间',
  `is_delete` tinyint NOT NULL DEFAULT '0' COMMENT '是否已删除，0-否，1-是',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_importance` (`importance`),
  KEY `idx_is_enabled` (`is_enabled`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='联系人关系分类字典表，维护公共和用户自定义的关系分类';

-- 创建组合唯一索引，防止用户创建重复的分类（同一用户或公共分类中）
CREATE UNIQUE INDEX `uk_user_tag` ON `contacts_user_relation_info_t` (`user_id`, `relationship_tag`, `is_delete`);

-- 插入系统默认的公共关系分类
INSERT INTO `contacts_user_relation_info_t` (`user_id`, `relationship_tag`, `importance`, `description`, `remarks`, `is_enabled`, `creator`, `create_time`, `operator`, `operate_time`, `is_delete`)
VALUES
  (NULL, '重要客户', 3, '系统默认分类', '', 1, 'system', UNIX_TIMESTAMP() * 1000, 'system', UNIX_TIMESTAMP() * 1000, 0),
  (NULL, '潜在客户', 2, '系统默认分类', '', 1, 'system', UNIX_TIMESTAMP() * 1000, 'system', UNIX_TIMESTAMP() * 1000, 0),
  (NULL, '合作伙伴', 2, '系统默认分类', '', 1, 'system', UNIX_TIMESTAMP() * 1000, 'system', UNIX_TIMESTAMP() * 1000, 0),
  (NULL, '家庭成员', 2, '系统默认分类', '', 1, 'system', UNIX_TIMESTAMP() * 1000, 'system', UNIX_TIMESTAMP() * 1000, 0),
  (NULL, '工作同事', 1, '系统默认分类', '', 1, 'system', UNIX_TIMESTAMP() * 1000, 'system', UNIX_TIMESTAMP() * 1000, 0),
  (NULL, '朋友', 1, '系统默认分类', '', 1, 'system', UNIX_TIMESTAMP() * 1000, 'system', UNIX_TIMESTAMP() * 1000, 0);

-- 修改 t_contacts_user 表的 relationship 字段类型
-- 从字符串改为关联 contacts_user_relation_info_t 的 ID
ALTER TABLE `t_contacts_user` MODIFY COLUMN `relationship` bigint DEFAULT NULL COMMENT '关系分类ID，关联 contacts_user_relation_info_t 表';

