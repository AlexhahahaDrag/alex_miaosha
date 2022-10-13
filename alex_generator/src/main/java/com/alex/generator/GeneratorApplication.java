package com.alex.generator;

import com.alex.common.config.qiniu.QiNiuConfiguration;
import com.alex.common.utils.qiniu.ImageScalaKit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
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
@ComponentScan(basePackages = {"com.alex.generator", "com.alex.common"},
        excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                value = {QiNiuConfiguration.class, ImageScalaKit.class})})
public class GeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(GeneratorApplication.class, args);
    }
}
