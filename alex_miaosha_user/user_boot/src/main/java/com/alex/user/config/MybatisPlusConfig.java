package com.alex.user.config;

import com.alex.api.user.handler.DataPermissionHandlerImpl;
import com.alex.api.user.handler.OrgSubtreeLookup;
import com.alex.api.user.user.UserUtils;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.aop.interceptor.PerformanceMonitorInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@MapperScan("com.alex.user.*.mapper")
public class MybatisPlusConfig {

    /**
     * OrgSubtreeLookup 必须 {@code @Lazy}：真实实现依赖 OrgInfoMapper，
     * 若在创建 sqlSessionFactory / interceptor 时强初始化，会形成循环依赖。
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(
            UserUtils userUtils,
            @Lazy OrgSubtreeLookup orgSubtreeLookup) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(
                new DataPermissionInterceptor(
                        new DataPermissionHandlerImpl(userUtils, orgSubtreeLookup)));
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        return interceptor;
    }

    @Bean
    @Profile({"dev", "test"})
    public PerformanceMonitorInterceptor performanceInterceptor() {
        return new PerformanceMonitorInterceptor();
    }
}
