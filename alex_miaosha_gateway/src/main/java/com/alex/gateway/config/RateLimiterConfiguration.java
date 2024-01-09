package com.alex.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * @author scott
 * @date 2020/5/26
 * 路由限流配置
 */
@Configuration
@Slf4j
public class RateLimiterConfiguration {
// TODO: 2022/9/29 测试限流情况

    //    @Bean
//    @Primary
//    public KeyResolver ipKeyResolver() {
//        log.info("ip限流!!");
//        return exchange -> Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
//    }
//
//    /**
//     * 用户限流 (通过exchange对象可以获取到请求信息，获取当前请求的用户 TOKEN)
//     */
//    @Bean
//    public KeyResolver userKeyResolver() {
//        log.info("用户限流!!");
//        return exchange -> Mono.just(exchange.getRequest().getHeaders().getFirst(GatewayFilter.tokenHeader));
//    }
//
//    /**
//     * 接口限流 (获取请求地址的uri作为限流key)
//     */
//    @Bean
//    public KeyResolver apiKeyResolver() {
//        log.info("请求地址限流!!");
//        return exchange -> Mono.just(exchange.getRequest().getPath().value());
//    }
}
