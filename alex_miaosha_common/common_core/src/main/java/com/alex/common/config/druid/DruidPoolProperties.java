package com.alex.common.config.druid;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * description:  Druid 连接池、防泄漏与安全过滤配置属性
 * author:       alex
 * createDate:   2026/10/21
 * version:      1.0.0
 */
@Data
@ConfigurationProperties(prefix = "spring.datasource.druid")
public class DruidPoolProperties {

    /**
     * 初始化连接数
     */
    private int initialSize = 5;

    /**
     * 最小空闲连接数
     */
    private int minIdle = 5;

    /**
     * 最大活跃连接数
     */
    private int maxActive = 20;

    /**
     * 获取连接等待超时时间（毫秒）
     */
    private long maxWait = 60000L;

    /**
     * 空闲连接检测间隔时间（毫秒）
     */
    private long timeBetweenEvictionRunsMillis = 60000L;

    /**
     * 连接在池中最小生存时间（毫秒）
     */
    private long minEvictableIdleTimeMillis = 300000L;

    /**
     * 连接健康检查 SQL
     */
    private String validationQuery = "SELECT 1";

    /**
     * 申请连接时检测空闲有效性（保活关键，推荐 true）
     */
    private boolean testWhileIdle = true;

    /**
     * 申请连接时执行 validationQuery 检测（开销大，默认 false）
     */
    private boolean testOnBorrow = false;

    /**
     * 归还连接时执行 validationQuery 检测（默认 false）
     */
    private boolean testOnReturn = false;

    /**
     * 是否开启连接池防泄漏强制回收
     */
    private boolean removeAbandoned = true;

    /**
     * 泄漏超时时间（秒），默认 180 秒（3 分钟未归还的活动连接判定为泄露）
     */
    private int removeAbandonedTimeout = 180;

    /**
     * 回收超时连接时是否打印调用栈（便于排查泄漏代码源头）
     */
    private boolean logAbandoned = true;

    /**
     * 是否启用 StatFilter 慢 SQL 监控统计
     */
    private boolean statEnabled = true;

    /**
     * 慢 SQL 判定阈值（毫秒），超过此时间记录 WARN 慢查询日志
     */
    private long slowSqlMillis = 1000L;

    /**
     * 是否打印慢 SQL
     */
    private boolean logSlowSql = true;

    /**
     * 是否聚合同类 SQL
     */
    private boolean mergeSql = true;

    /**
     * 是否启用 WallFilter SQL 防火墙防注入
     */
    private boolean wallEnabled = true;

    /**
     * 防火墙是否允许执行多语句（堆叠查询，如 select 1; drop table...）
     * 生产环境强烈建议 false，杜绝堆叠注入
     */
    private boolean multiStatementAllow = false;

    /**
     * DELETE 全表无 WHERE 条件拦截（防止误清空表）
     */
    private boolean deleteWhereNoneCheck = true;

    /**
     * UPDATE 全表无 WHERE 条件拦截（防止误全量更新）
     */
    private boolean updateWhereNoneCheck = true;
}
