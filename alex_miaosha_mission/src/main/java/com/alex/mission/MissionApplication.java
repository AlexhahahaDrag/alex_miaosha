package com.alex.mission;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.alex.mission", "com.alex.common"})
public class MissionApplication {

    public static void main(String[] args) {
        SpringApplication.run(MissionApplication.class, args);
    }
}
