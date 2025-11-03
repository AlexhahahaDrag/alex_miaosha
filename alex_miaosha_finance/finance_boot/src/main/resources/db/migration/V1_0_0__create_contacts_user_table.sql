-- ==========================================================
-- 联系人信息表
-- 作者: alex
-- 创建时间: 2025-11-03
-- 版本: 1.0.0
-- ==========================================================

CREATE TABLE IF NOT EXISTS `t_contacts_user` (
  `id` BIGINT NOT NULL COMMENT '主键',
  `name` VARCHAR(100) NOT NULL COMMENT '联系人姓名',
  `phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
  `relationship` VARCHAR(50) NOT NULL COMMENT '关系类型(friend:朋友,family:家人,colleague:同事,other:其他)',
  `email` VARCHAR(100) COMMENT '电子邮箱',
  `address` VARCHAR(500) COMMENT '联系地址',
  `remarks` VARCHAR(1000) COMMENT '备注信息',
  `is_favorite` TINYINT(1) DEFAULT 0 COMMENT '是否是常用联系人，0-否，1-是',
  `creator` BIGINT COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` BIGINT COMMENT '更新人',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `operator` BIGINT COMMENT '操作人',
  `operate_time` DATETIME COMMENT '操作时间',
  `deleter` BIGINT COMMENT '删除人',
  `delete_time` DATETIME COMMENT '删除时间',
  `is_delete` TINYINT(1) DEFAULT 0 COMMENT '是否删除，0-否，1-是',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '联系人信息表';

-- 创建查询索引
CREATE INDEX `idx_name` ON `t_contacts_user`(`name`) USING BTREE COMMENT '姓名索引';
CREATE INDEX `idx_phone` ON `t_contacts_user`(`phone`) USING BTREE COMMENT '电话索引';
CREATE INDEX `idx_relationship` ON `t_contacts_user`(`relationship`) USING BTREE COMMENT '关系类型索引';
CREATE INDEX `idx_is_favorite` ON `t_contacts_user`(`is_favorite`) USING BTREE COMMENT '常用联系人索引';
CREATE INDEX `idx_is_delete` ON `t_contacts_user`(`is_delete`) USING BTREE COMMENT '删除标记索引';
CREATE INDEX `idx_create_time` ON `t_contacts_user`(`create_time`) USING BTREE COMMENT '创建时间索引';
