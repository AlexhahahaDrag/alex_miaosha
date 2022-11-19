package com.alex.web;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableFeignClients
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.alex.web", "com.alex.common"})
public class WebApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(WebApplication.class)
                .properties("spring.config.additional-location:file:./common/bootstrap.yaml")
                .build().run(args);
    }
}
