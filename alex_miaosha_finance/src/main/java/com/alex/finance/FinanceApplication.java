package com.alex.finance;

import com.alex.common.config.qiniu.QiNiuConfiguration;
import com.alex.common.utils.qiniu.ImageScalaKit;
import com.alex.utils.interceptor.SeckillInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * description:
 * author:       majf
 * createDate:   2022/10/10 16:56
 * version:      1.0.0
 */
@EnableDiscoveryClient
@SpringBootApplication
@ComponentScan(basePackages = {"com.alex.finance", "com.alex.common", "com.alex.utils"},
        excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                value = {QiNiuConfiguration.class, ImageScalaKit.class, SeckillInterceptor.class}
        )})
public class FinanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanceApplication.class, args);
    }
}
