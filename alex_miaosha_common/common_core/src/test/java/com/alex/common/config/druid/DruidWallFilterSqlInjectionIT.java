package com.alex.common.config.druid;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.wall.WallCheckResult;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import com.alibaba.druid.wall.spi.MySqlWallProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Druid WallFilter SQL 防火墙与防注入集成测试")
class DruidWallFilterSqlInjectionIT {

    @Test
    @DisplayName("1. 验证 WallFilter 成功装配，禁用多语句堆叠且开启全表无条件删除/更新拦截")
    void enhance_attachesWallFilterWithAntiInjectionRules() {
        DruidPoolProperties properties = new DruidPoolProperties();
        DruidDataSourceEnhancer enhancer = new DruidDataSourceEnhancer(properties);

        DruidDataSource dataSource = new DruidDataSource();
        enhancer.enhance(dataSource);

        List<Filter> filters = dataSource.getProxyFilters();
        assertNotNull(filters);

        WallFilter wallFilter = filters.stream()
                .filter(f -> f instanceof WallFilter)
                .map(f -> (WallFilter) f)
                .findFirst()
                .orElse(null);

        assertNotNull(wallFilter, "Druid 代理 Filter 链中必须包含 WallFilter SQL 防火墙");
        assertEquals("mysql", wallFilter.getDbType());

        WallConfig wallConfig = wallFilter.getConfig();
        assertNotNull(wallConfig);
        assertFalse(wallConfig.isMultiStatementAllow(), "生产环境必须禁止多语句堆叠执行");
        assertTrue(wallConfig.isDeleteWhereNoneCheck(), "必须开启 DELETE 全表无 WHERE 校验");
        assertTrue(wallConfig.isUpdateWhereNoneCheck(), "必须开启 UPDATE 全表无 WHERE 校验");
    }

    @Test
    @DisplayName("2. 验证 WallFilter 防火墙拦截多语句堆叠注入攻击")
    void wallProvider_interceptsMultiStatementInjection() {
        WallConfig wallConfig = new WallConfig();
        wallConfig.setMultiStatementAllow(false);

        MySqlWallProvider provider = new MySqlWallProvider(wallConfig);
        String maliciousMultiSql = "SELECT * FROM t_user WHERE id = 1; DROP TABLE t_user;";

        WallCheckResult checkResult = provider.check(maliciousMultiSql);
        assertFalse(checkResult.getViolations().isEmpty(), "多语句堆叠注入必须被 WallFilter 拦截");
    }

    @Test
    @DisplayName("3. 验证 WallFilter 防火墙拦截全表无 WHERE 条件的 DELETE 操作")
    void wallProvider_interceptsDeleteWithoutWhereCondition() {
        WallConfig wallConfig = new WallConfig();
        wallConfig.setDeleteWhereNoneCheck(true);

        MySqlWallProvider provider = new MySqlWallProvider(wallConfig);
        String dangerousDeleteSql = "DELETE FROM t_user";

        WallCheckResult checkResult = provider.check(dangerousDeleteSql);
        assertFalse(checkResult.getViolations().isEmpty(), "无 WHERE 条件的 DELETE 必须被拦截防止全表清空");
    }

    @Test
    @DisplayName("4. 验证 WallFilter 放行合法合规的业务 SQL")
    void wallProvider_allowsCompliantSql() {
        WallConfig wallConfig = new WallConfig();
        wallConfig.setMultiStatementAllow(false);
        wallConfig.setDeleteWhereNoneCheck(true);
        wallConfig.setUpdateWhereNoneCheck(true);

        MySqlWallProvider provider = new MySqlWallProvider(wallConfig);

        String compliantSelect = "SELECT id, username, phone FROM t_user WHERE id = 1 AND is_delete = 0";
        WallCheckResult selectResult = provider.check(compliantSelect);
        assertTrue(selectResult.getViolations().isEmpty(), "合规 SELECT 应当放行");

        String compliantUpdate = "UPDATE t_user SET phone = '13800000000' WHERE id = 1";
        WallCheckResult updateResult = provider.check(compliantUpdate);
        assertTrue(updateResult.getViolations().isEmpty(), "带 WHERE 条件的 UPDATE 应当放行");
    }

    @Test
    @DisplayName("5. 验证关闭 wallEnabled 时不装配 WallFilter")
    void enhance_whenWallDisabled_doesNotAttachWallFilter() {
        DruidPoolProperties properties = new DruidPoolProperties();
        properties.setWallEnabled(false);

        DruidDataSourceEnhancer enhancer = new DruidDataSourceEnhancer(properties);
        DruidDataSource dataSource = new DruidDataSource();
        enhancer.enhance(dataSource);

        boolean hasWall = dataSource.getProxyFilters().stream().anyMatch(f -> f instanceof WallFilter);
        assertFalse(hasWall, "当 wallEnabled=false 时不应附加 WallFilter");
    }
}
