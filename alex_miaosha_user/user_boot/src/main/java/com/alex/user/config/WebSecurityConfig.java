package com.alex.user.config;

import com.alex.user.utils.jwt.JwtAuthenticationTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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

    //白名单
    private static final String[] whiteList;

    static {
        whiteList = new String[]{
                "/swagger-resources/**",
                "/doc.html",
                "/webjars/**",
                "/actuator/**",
                "/favicon.ico",
                "${api.version:/api/v1}/user/login",
                "${api.version:/api/v1}/menu-info/list",
                "${api.version:/api/v1}/menu-info",
                "${api.version:/api/v1}/permission-info/list",
                "${api.version:/api/v1}/permission-info",
                "/druid/**",
                "${api.version:/api/v1}/user/getUserInfo",
                "${api.version:/api/v1}/user/authToken",
                "${api.version:/api/v1}/user/third",
                "${api.version:/api/v1}/file-info/getFileInfo",
                "/v3/api-docs",
                "/error"
        };
    }

    @Bean
    public SecurityFilterChain web(HttpSecurity http) throws Exception {
        return http
                // 基于 token，不需要 csrf
                .csrf()
                .disable()
                // 基于 token，不需要 session
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeHttpRequests((authorize) -> authorize
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

