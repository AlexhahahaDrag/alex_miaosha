package com.alex.common.config.druid;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * description:  Druid 数据源安全配置与防泄漏增强器
 * 负责连接池基础容量、心跳保活检测、防连接泄露（removeAbandoned）以及慢 SQL（StatFilter）与防注入（WallFilter）安全装配。
 * author:       alex
 * createDate:   2026/10/21
 * version:      1.0.0
 */
@Slf4j
public class DruidDataSourceEnhancer {

    private final DruidPoolProperties properties;

    public DruidDataSourceEnhancer(DruidPoolProperties properties) {
        this.properties = properties != null ? properties : new DruidPoolProperties();
    }

    /**
     * 对目标 Druid 数据源执行安全与连接池防泄漏加固
     *
     * @param dataSource 待加固的 DruidDataSource
     */
    public void enhance(DruidDataSource dataSource) {
        if (dataSource == null) {
            return;
        }

        // 1. 连接池基础容量配置
        dataSource.setInitialSize(properties.getInitialSize());
        dataSource.setMinIdle(properties.getMinIdle());
        dataSource.setMaxActive(properties.getMaxActive());
        dataSource.setMaxWait(properties.getMaxWait());

        // 2. 连接健康检查与心跳保活
        dataSource.setTimeBetweenEvictionRunsMillis(properties.getTimeBetweenEvictionRunsMillis());
        dataSource.setMinEvictableIdleTimeMillis(properties.getMinEvictableIdleTimeMillis());
        dataSource.setValidationQuery(properties.getValidationQuery());
        dataSource.setTestWhileIdle(properties.isTestWhileIdle());
        dataSource.setTestOnBorrow(properties.isTestOnBorrow());
        dataSource.setTestOnReturn(properties.isTestOnReturn());

        // 3. 连接池防泄漏机制 (Remove Abandoned)
        dataSource.setRemoveAbandoned(properties.isRemoveAbandoned());
        dataSource.setRemoveAbandonedTimeout(properties.getRemoveAbandonedTimeout());
        dataSource.setLogAbandoned(properties.isLogAbandoned());

        // 4. 增量装配 Filter 链（StatFilter 慢 SQL 监控 + WallFilter 防注入）
        // 注意：DruidDataSource.setProxyFilters 内部行为是 filters.addAll()，因此仅传入需要增量添加的 Filter
        List<Filter> currentFilters = dataSource.getProxyFilters();
        boolean hasStat = currentFilters.stream().anyMatch(f -> f instanceof StatFilter);
        boolean hasWall = currentFilters.stream().anyMatch(f -> f instanceof WallFilter);

        List<Filter> filtersToAdd = new ArrayList<>();

        // 装配 StatFilter
        if (properties.isStatEnabled() && !hasStat) {
            StatFilter statFilter = new StatFilter();
            statFilter.setSlowSqlMillis(properties.getSlowSqlMillis());
            statFilter.setLogSlowSql(properties.isLogSlowSql());
            statFilter.setMergeSql(properties.isMergeSql());
            filtersToAdd.add(statFilter);
        }

        // 装配 WallFilter
        if (properties.isWallEnabled() && !hasWall) {
            WallFilter wallFilter = new WallFilter();
            wallFilter.setDbType("mysql");

            WallConfig wallConfig = new WallConfig();
            wallConfig.setMultiStatementAllow(properties.isMultiStatementAllow());
            wallConfig.setDeleteWhereNoneCheck(properties.isDeleteWhereNoneCheck());
            wallConfig.setUpdateWhereNoneCheck(properties.isUpdateWhereNoneCheck());

            wallFilter.setConfig(wallConfig);
            filtersToAdd.add(wallFilter);
        }

        if (!filtersToAdd.isEmpty()) {
            dataSource.setProxyFilters(filtersToAdd);
        }

        log.info("DruidDataSource 安全加固装配完成: maxActive={}, removeAbandoned={}, removeAbandonedTimeout={}s, slowSqlMillis={}ms, wallEnabled={}",
                properties.getMaxActive(), properties.isRemoveAbandoned(), properties.getRemoveAbandonedTimeout(), properties.getSlowSqlMillis(), properties.isWallEnabled());
    }

    public DruidPoolProperties getProperties() {
        return properties;
    }
}
