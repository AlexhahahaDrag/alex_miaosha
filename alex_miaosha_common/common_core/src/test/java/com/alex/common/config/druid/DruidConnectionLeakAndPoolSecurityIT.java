package com.alex.common.config.druid;

import com.alibaba.druid.pool.DruidDataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Druid 连接池容量、心跳保活与防泄漏集成测试")
class DruidConnectionLeakAndPoolSecurityIT {

    @Test
    @DisplayName("1. 验证默认配置下连接池基础参数与防泄漏机制（removeAbandoned=true, timeout=180s）")
    void enhance_appliesDefaultPoolParametersAndLeakProtection() {
        DruidPoolProperties properties = new DruidPoolProperties();
        DruidDataSourceEnhancer enhancer = new DruidDataSourceEnhancer(properties);

        DruidDataSource dataSource = new DruidDataSource();
        enhancer.enhance(dataSource);

        // 验证池化容量
        assertEquals(5, dataSource.getInitialSize());
        assertEquals(5, dataSource.getMinIdle());
        assertEquals(20, dataSource.getMaxActive());
        assertEquals(60000L, dataSource.getMaxWait());

        // 验证心跳与保活
        assertTrue(dataSource.isTestWhileIdle(), "testWhileIdle 必须开启以防止连接失效");
        assertEquals("SELECT 1", dataSource.getValidationQuery());
        assertEquals(60000L, dataSource.getTimeBetweenEvictionRunsMillis());
        assertEquals(300000L, dataSource.getMinEvictableIdleTimeMillis());

        // 验证防连接泄漏机制
        assertTrue(dataSource.isRemoveAbandoned(), "必须开启 removeAbandoned 强制回收泄漏连接");
        assertEquals(180, dataSource.getRemoveAbandonedTimeout(), "默认 180 秒未归还的活动连接判定为泄漏");
        assertTrue(dataSource.isLogAbandoned(), "必须开启 logAbandoned 打印借出连接的堆栈跟踪日志");
    }

    @Test
    @DisplayName("2. 验证自定义 DruidPoolProperties 属性覆盖生效")
    void enhance_appliesCustomPoolProperties() {
        DruidPoolProperties properties = new DruidPoolProperties();
        properties.setInitialSize(10);
        properties.setMinIdle(8);
        properties.setMaxActive(50);
        properties.setMaxWait(30000L);
        properties.setRemoveAbandonedTimeout(60);
        properties.setLogAbandoned(false);

        DruidDataSourceEnhancer enhancer = new DruidDataSourceEnhancer(properties);
        DruidDataSource dataSource = new DruidDataSource();
        enhancer.enhance(dataSource);

        assertEquals(10, dataSource.getInitialSize());
        assertEquals(8, dataSource.getMinIdle());
        assertEquals(50, dataSource.getMaxActive());
        assertEquals(30000L, dataSource.getMaxWait());
        assertTrue(dataSource.isRemoveAbandoned());
        assertEquals(60, dataSource.getRemoveAbandonedTimeout());
        assertFalse(dataSource.isLogAbandoned());
    }

    @Test
    @DisplayName("3. 验证空指针防御处理")
    void enhance_handlesNullDataSourceGracefully() {
        DruidDataSourceEnhancer enhancer = new DruidDataSourceEnhancer(null);
        assertDoesNotThrow(() -> enhancer.enhance(null));
        assertNotNull(enhancer.getProperties());
    }
}
