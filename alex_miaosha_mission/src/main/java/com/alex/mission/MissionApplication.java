package com.alex.mission;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.alex.mission", "com.alex.common", "com.alex.utils"})
public class MissionApplication {

    // TODO: 2022/8/19 消费消息后，如何回写
    // TODO: 2022/8/19 redis什么时候同步mysql
    public static void main(String[] args) {
        SpringApplication.run(MissionApplication.class, args);
    }
}
