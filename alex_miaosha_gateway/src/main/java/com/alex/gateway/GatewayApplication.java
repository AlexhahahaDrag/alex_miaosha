package com.alex.gateway;

import com.alex.common.config.WebMvcConfigurer;
import com.alex.common.config.qiniu.QiNiuConfiguration;
import com.alex.common.utils.qiniu.ImageScalaKit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * @description:  gateway启动类
 * @author:       majf
 * @createDate:   2022/7/29 14:18
 * @version:      1.0.0
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.alex.gateway", "com.alex.common", "com.alex.utils"},
        excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                value = {QiNiuConfiguration.class, ImageScalaKit.class, WebMvcConfigurer.class}
)})
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
