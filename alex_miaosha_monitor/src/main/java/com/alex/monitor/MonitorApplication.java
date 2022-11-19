package com.alex.monitor;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @description: springboot admin监管系统
 * @author: majf
 * @createDate: 2022/8/3 11:54
 * @version: 1.0.0
 */
@EnableAdminServer
@SpringBootApplication
@EnableDiscoveryClient
public class MonitorApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(MonitorApplication.class)
                .properties("spring.config.additional-location:file:./common/bootstrap.yaml")
                .build().run(args);
    }
}
