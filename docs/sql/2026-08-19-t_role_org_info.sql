-- Role-org binding (many-to-many). Spec: docs/superpowers/specs/2026-08-19-role-org-binding-design.md §3
-- Aligns with t_role_user_info style; no t_role_info.org_id.

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
