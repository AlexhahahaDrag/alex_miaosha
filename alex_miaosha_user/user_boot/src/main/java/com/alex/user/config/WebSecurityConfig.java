package com.alex.user.config;

import com.alex.user.utils.jwt.JwtAuthenticationTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * description: security配置类
 * author: alex
 * createDate: 2022/9/22 22:51
 * version: 1.0.0
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    private final UserDetailsService userDetailsService;

    private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @Value("${api.version:/api/v1}")
    private String apiVersion;

    //固定白名单（无需 api.version 前缀）
    private static final String[] STATIC_WHITE_LIST = {
            "/swagger-resources/**",
            "/doc.html",
            "/webjars/**",
            "/actuator/**",
            "/favicon.ico",
            "/druid/**",
            "/v3/api-docs",
            "/error"
    };

    @Bean
    public SecurityFilterChain web(HttpSecurity http) throws Exception {
        // 动态构建带 api 版本前缀的白名单路径
        String[] versionedPaths = {
                apiVersion + "/user/login",
                apiVersion + "/menu-info/list",
                apiVersion + "/menu-info",
                apiVersion + "/permission-info/list",
                apiVersion + "/permission-info",
                apiVersion + "/user/getUserInfo",
                apiVersion + "/user/authToken",
                apiVersion + "/user/third",
                apiVersion + "/file-info/getFileInfo"
        };
        // 合并固定白名单与带版本前缀的路径
        String[] whiteList = java.util.stream.Stream
                .concat(java.util.Arrays.stream(STATIC_WHITE_LIST), java.util.Arrays.stream(versionedPaths))
                .toArray(String[]::new);

        return http
                // 基于 token，不需要 csrf
                .csrf(AbstractHttpConfigurer::disable)
                // 基于 token，不需要 session
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // AI Agent：安全加固：显式拒绝 /null/** 路径，防止 CVE-2025-22235 风险
                        // 说明：当 EndpointRequest.to() 引用的端点被禁用时，可能创建 null/** 匹配器，需要显式拒绝
                        .antMatchers("/null/**")
                        .denyAll()
                        .antMatchers(whiteList)
                        .permitAll()
                        .anyRequest()
                        .authenticated()
                ).userDetailsService(userDetailsService)
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}

