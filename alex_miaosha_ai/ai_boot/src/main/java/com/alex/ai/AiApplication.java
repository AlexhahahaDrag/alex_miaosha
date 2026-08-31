package com.alex.ai;

import com.alex.utils.interceptor.SeckillInterceptor;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.cloud.bootstrap.BootstrapConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * AI Agent：
 * AI 服务启动类
 * - 提供统一 AI 分析能力，供其它模块通过 Feign/HTTP 调用
 * - 业务不连库；utils/common 间接引入 mybatis-plus，需排除 DataSource 自动配置，否则无 url 时启动失败
 */
@EnableDiscoveryClient
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        MybatisPlusAutoConfiguration.class
})
@ComponentScan(
        basePackages = {"com.alex.ai", "com.alex.common", "com.alex.utils", "com.alex.api.user"},
        excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = {SeckillInterceptor.class})}
)
@EnableEncryptableProperties
@BootstrapConfiguration
@EnableFeignClients(basePackages = {"com.alex.api.user"})
public class AiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiApplication.class, args);
    }

    @Bean
    MeterRegistryCustomizer<MeterRegistry> configurer(@Value("${spring.application.name}") String applicationName) {
        return (registry -> registry.config().commonTags("application", applicationName));
    }
}


