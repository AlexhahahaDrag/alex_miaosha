package com.alex.finance.message.mapper;

import com.alex.api.finance.message.vo.SysMessageInfoVo;
import com.alex.finance.message.entity.SysMessageInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 系统消息与业务到期提醒 Mapper
 */
@Mapper
public interface SysMessageInfoMapper extends BaseMapper<SysMessageInfo> {

    @Update("CREATE TABLE IF NOT EXISTS `sys_message_info_t` (" +
            "`id` bigint NOT NULL," +
            "`org_id` bigint NULL DEFAULT NULL," +
            "`user_id` bigint NULL DEFAULT NULL," +
            "`msg_type` varchar(32) NOT NULL," +
            "`sub_type` varchar(32) NULL DEFAULT NULL," +
            "`priority` varchar(16) NOT NULL DEFAULT 'NORMAL'," +
            "`title` varchar(200) NOT NULL," +
            "`content` varchar(1000) NOT NULL," +
            "`biz_id` varchar(64) NULL DEFAULT NULL," +
            "`biz_type` varchar(32) NULL DEFAULT NULL," +
            "`jump_url` varchar(255) NULL DEFAULT NULL," +
            "`read_status` tinyint(1) NOT NULL DEFAULT 0," +
            "`read_time` datetime NULL DEFAULT NULL," +
            "`dedup_key` varchar(128) NULL DEFAULT NULL," +
            "`remind_date` date NULL DEFAULT NULL," +
            "`creator` bigint NULL DEFAULT NULL," +
            "`create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP," +
            "`updater` bigint NULL DEFAULT NULL," +
            "`update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP," +
            "`operator` bigint NULL DEFAULT NULL," +
            "`operate_time` datetime NULL DEFAULT CURRENT_TIMESTAMP," +
            "`deleter` bigint NULL DEFAULT NULL," +
            "`delete_time` datetime NULL DEFAULT NULL," +
            "`is_delete` tinyint(1) NOT NULL DEFAULT 0," +
            "PRIMARY KEY (`id`)," +
            "UNIQUE KEY `uk_sys_message_dedup` (`dedup_key`)" +
            ")")
    void ensureTableExists();

    Page<SysMessageInfoVo> getPage(
            Page<SysMessageInfoVo> page,
            @Param("query") SysMessageInfoVo query,
            @Param("currentUserId") Long currentUserId,
            @Param("currentOrgId") Long currentOrgId
    );

    List<SysMessageInfoVo> selectUnreadList(
            @Param("currentUserId") Long currentUserId,
            @Param("currentOrgId") Long currentOrgId
    );

    List<String> selectExistingDedupKeys(@Param("dedupKeys") Collection<String> dedupKeys);

    int markAllRead(
            @Param("currentUserId") Long currentUserId,
            @Param("currentOrgId") Long currentOrgId,
            @Param("msgType") String msgType,
            @Param("readTime") LocalDateTime readTime
    );
}
