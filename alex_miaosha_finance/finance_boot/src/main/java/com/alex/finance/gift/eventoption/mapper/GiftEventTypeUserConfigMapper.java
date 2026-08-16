package com.alex.finance.gift.eventoption.mapper;

import com.alex.finance.gift.eventoption.entity.GiftEventTypeUserConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface GiftEventTypeUserConfigMapper extends BaseMapper<GiftEventTypeUserConfig> {

    @Update("CREATE TABLE IF NOT EXISTS `gift_event_type_user_config_t` (" +
            "`id` bigint(20) NOT NULL COMMENT '主键ID'," +
            "`option_id` bigint(20) NOT NULL COMMENT '关联的事由分类选项ID'," +
            "`org_id` bigint(20) DEFAULT NULL COMMENT '机构/家庭组ID'," +
            "`user_id` bigint(20) NOT NULL COMMENT '用户ID'," +
            "`status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '启用状态(1:启用, 0:停用)'," +
            "`custom_amount` decimal(10,2) DEFAULT NULL COMMENT '个性化推荐金额'," +
            "`creator` bigint(20) DEFAULT NULL COMMENT '创建人'," +
            "`create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
            "`updater` bigint(20) DEFAULT NULL COMMENT '更新人'," +
            "`update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'," +
            "`operator` bigint(20) DEFAULT NULL COMMENT '操作人'," +
            "`operate_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '操作时间'," +
            "`deleter` bigint(20) DEFAULT NULL COMMENT '删除人'," +
            "`delete_time` datetime DEFAULT NULL COMMENT '删除时间'," +
            "`is_delete` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标识'," +
            "PRIMARY KEY (`id`)," +
            "KEY `idx_org_option` (`org_id`, `option_id`)," +
            "KEY `idx_user_id` (`user_id`)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='事由分类个性化配置表'")
    void createTableIfNotExists();

    @Update("ALTER TABLE `gift_event_type_user_config_t` " +
            "ADD COLUMN `creator` bigint(20) DEFAULT NULL COMMENT '创建人', " +
            "ADD COLUMN `updater` bigint(20) DEFAULT NULL COMMENT '更新人', " +
            "ADD COLUMN `operator` bigint(20) DEFAULT NULL COMMENT '操作人', " +
            "ADD COLUMN `operate_time` datetime DEFAULT NULL COMMENT '操作时间', " +
            "ADD COLUMN `deleter` bigint(20) DEFAULT NULL COMMENT '删除人', " +
            "ADD COLUMN `delete_time` datetime DEFAULT NULL COMMENT '删除时间'")
    void addMissingAuditColumns();
}
