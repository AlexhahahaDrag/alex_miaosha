package com.alex.generator;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * description:  自动生成微服务
 * author:       majf
 * createDate:   2022/10/11 14:41
 * version:      1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableEncryptableProperties
@ComponentScan(basePackages = {"com.alex.generator", "com.alex.common", "com.alex.api.user"},
        excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE)})
@EnableFeignClients(basePackages = {"com.alex.api.user"})
public class GeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(GeneratorApplication.class, args);
    }
}