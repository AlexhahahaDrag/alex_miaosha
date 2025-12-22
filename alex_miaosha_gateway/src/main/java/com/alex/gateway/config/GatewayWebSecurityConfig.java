package com.alex.gateway.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * description:
 * author:       majf
 * createDate:   2022/10/8 17:52
 * version:      1.0.0
 */
@Configuration
@EnableWebFluxSecurity
@AllArgsConstructor
public class GatewayWebSecurityConfig {

    //白名单
    private static final String[] whiteList;

    static {
        whiteList = new String[]{
                "/swagger-resources/**",
                "/doc.html",
                "/webjars/**",
                "/actuator/**",
                "/favicon.ico",
                "/druid/**",
                "/fallback",
                "/am-user/**",
                "/am-user/user/login",
                "/am-mission/**",
                "/am-finance/**",
                "/am-product/**",
                "/am-oss/**",
                "/finance-info/**",
                "/dict-info/**",
                "/v3/api-docs",
                "/error"};
    }

    @Bean
    SecurityWebFilterChain webFluxSecurityFilterChain(ServerHttpSecurity http) throws Exception {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .authorizeExchange(authorizeExchange -> 
                    authorizeExchange
                        // AI Agent：安全加固：显式拒绝 /null/** 路径，防止 CVE-2025-22235 风险
                        // 说明：当 EndpointRequest.to() 引用的端点被禁用时，可能创建 null/** 匹配器，需要显式拒绝
                        .pathMatchers("/null/**").denyAll()
                        .pathMatchers(whiteList).permitAll()
                        .anyExchange().authenticated()
                )
                .build();
    }
}