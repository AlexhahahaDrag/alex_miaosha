package com.alex.gateway;

import com.alex.gateway.utils.AutowiredBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * description: gateway启动类
 * author: majf
 * createDate: 2022/7/29 14:18
 * version: 1.0.0
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.alex.gateway", "com.alex.common", "com.alex.api.user"})
// AI Agent：增加 AI 服务 Feign 扫描包（便于网关侧按需调用 AI 服务做统一分析/审计）
@EnableFeignClients(basePackages = {"com.alex.api.user", "com.alex.api.ai"})
public class GatewayApplication {

    public static void main(String[] args) {
        // 必须在最前面设置
        System.setProperty(
                "JM.SNAPSHOT.PATH",
                "D:\\"
        );
        ConfigurableApplicationContext run = SpringApplication.run(GatewayApplication.class, args);
        AutowiredBean.setApplicationContext(run);
    }
}
