package com.alex.apidoc;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

// TODO: 2022/8/2 网关配置knife4j 
@SpringBootApplication
@EnableDiscoveryClient
public class ApiDocApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(ApiDocApplication.class)
                .properties("spring.config.additional-location:file:./common/bootstrap.yaml")
                .build().run(args);
    }
}
