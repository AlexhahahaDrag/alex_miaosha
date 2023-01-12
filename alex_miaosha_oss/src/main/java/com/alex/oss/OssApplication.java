package com.alex.oss;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * @description: oss启动类
 * @author: alex
 * @createDate: 2023/1/11 22:00
 * @version: 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableEncryptableProperties
@ComponentScan(basePackages = {"com.alex.oss", "com.alex.common", "com.alex.utils"})
public class OssApplication {

    public static void main(String[] args) {
        SpringApplication.run(OssApplication.class, args);
    }

    @Bean
    MeterRegistryCustomizer<MeterRegistry> configurer(@Value("${spring.application.name}") String applicationName) {
        return (registry -> registry.config().commonTags("application", applicationName));
    }
}
