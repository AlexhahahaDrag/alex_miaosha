package com.alex.common.config.druid;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * description:  Druid 连接池安全防泄漏、慢 SQL 审计与防注入自动装配
 * author:       alex
 * createDate:   2026/10/21
 * version:      1.0.0
 */
@Configuration
@ConditionalOnClass(DruidDataSource.class)
@EnableConfigurationProperties(DruidPoolProperties.class)
public class DruidAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DruidDataSourceEnhancer druidDataSourceEnhancer(DruidPoolProperties properties) {
        return new DruidDataSourceEnhancer(properties);
    }

    @Bean
    public BeanPostProcessor druidDataSourcePostProcessor(DruidDataSourceEnhancer enhancer) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof DruidDataSource) {
                    enhancer.enhance((DruidDataSource) bean);
                }
                return bean;
            }
        };
    }
}
