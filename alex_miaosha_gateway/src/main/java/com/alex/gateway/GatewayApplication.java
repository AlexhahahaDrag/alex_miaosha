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
@EnableFeignClients(basePackages = {"com.alex.api.user"})
public class GatewayApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(GatewayApplication.class, args);
        AutowiredBean.setApplicationContext(run);
    }
}
