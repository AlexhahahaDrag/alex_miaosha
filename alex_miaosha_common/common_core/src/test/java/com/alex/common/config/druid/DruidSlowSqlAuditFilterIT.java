package com.alex.common.config.druid;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Druid 慢 SQL 审计与 StatFilter 集成测试")
class DruidSlowSqlAuditFilterIT {

    @Test
    @DisplayName("1. 验证 StatFilter 成功装配，慢查询阈值为 1000ms 且开启日志记录与 SQL 合并")
    void enhance_attachesStatFilterWithSlowSqlThreshold() {
        DruidPoolProperties properties = new DruidPoolProperties();
        DruidDataSourceEnhancer enhancer = new DruidDataSourceEnhancer(properties);

        DruidDataSource dataSource = new DruidDataSource();
        enhancer.enhance(dataSource);

        List<Filter> filters = dataSource.getProxyFilters();
        assertNotNull(filters);

        StatFilter statFilter = filters.stream()
                .filter(f -> f instanceof StatFilter)
                .map(f -> (StatFilter) f)
                .findFirst()
                .orElse(null);

        assertNotNull(statFilter, "Druid 代理 Filter 链中必须包含 StatFilter 监控");
        assertEquals(1000L, statFilter.getSlowSqlMillis(), "默认慢 SQL 阈值应为 1000ms");
        assertTrue(statFilter.isLogSlowSql(), "必须开启慢 SQL 日志打印");
        assertTrue(statFilter.isMergeSql(), "必须开启同类 SQL 合并以优化监控报告");
    }

    @Test
    @DisplayName("2. 验证关闭 statEnabled 时不装配 StatFilter")
    void enhance_whenStatDisabled_doesNotAttachStatFilter() {
        DruidPoolProperties properties = new DruidPoolProperties();
        properties.setStatEnabled(false);

        DruidDataSourceEnhancer enhancer = new DruidDataSourceEnhancer(properties);
        DruidDataSource dataSource = new DruidDataSource();
        enhancer.enhance(dataSource);

        boolean hasStat = dataSource.getProxyFilters().stream().anyMatch(f -> f instanceof StatFilter);
        assertFalse(hasStat, "当 statEnabled=false 时不应附加 StatFilter");
    }

    @Test
    @DisplayName("3. 验证多次 enhance 幂等性，不重复注入 StatFilter")
    void enhance_avoidsDuplicateStatFilter() {
        DruidDataSourceEnhancer enhancer = new DruidDataSourceEnhancer(new DruidPoolProperties());
        DruidDataSource dataSource = new DruidDataSource();

        enhancer.enhance(dataSource);
        enhancer.enhance(dataSource);

        long statFilterCount = dataSource.getProxyFilters().stream().filter(f -> f instanceof StatFilter).count();
        assertEquals(1L, statFilterCount, "StatFilter 必须保持单例，严禁重复装配");
    }
}
