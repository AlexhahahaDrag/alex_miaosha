package com.alex.finance;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.bootstrap.BootstrapConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.io.File;

/**
 * @description:
 * @author: majf
 * @createDate: 2022/10/10 16:56
 * @version: 1.0.0
 */
@EnableDiscoveryClient
@SpringBootApplication
@ComponentScan(basePackages = {"com.alex.finance", "com.alex.common", "com.alex.api.user"})
@EnableEncryptableProperties
@BootstrapConfiguration
// AI Agent：增加 AI 服务 Feign 扫描包，后续 finance 模块可直接注入 AiAnalyzeApi 调用 AI 分析
@EnableFeignClients(basePackages = {"com.alex.api.user", "com.alex.api.ai", "com.alex.api.oss"})
public class FinanceApplication {

    public static void main(String[] args) {
        // 动态判断快照存储路径，兼容 Windows 与 Linux 容器环境，杜绝硬编码 D:\
        String snapshotPath = System.getProperty("JM.SNAPSHOT.PATH");
        if (snapshotPath == null || snapshotPath.isBlank()) {
            File dDrive = new File("D:\\");
            if (dDrive.exists() && dDrive.isDirectory()) {
                System.setProperty("JM.SNAPSHOT.PATH", "D:\\");
            } else {
                System.setProperty("JM.SNAPSHOT.PATH", System.getProperty("java.io.tmpdir", "."));
            }
        }
        SpringApplication.run(FinanceApplication.class, args);
    }

    @Bean
    MeterRegistryCustomizer<MeterRegistry> configurer(@Value("${spring.application.name}") String applicationName) {
        return (registry -> registry.config().commonTags("application", applicationName));
    }
}
