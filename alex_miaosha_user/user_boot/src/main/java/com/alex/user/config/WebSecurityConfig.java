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
 * @description: security配置类
 * @author: alex
 * @createDate: 2022/9/22 22:51
 * @version: 1.0.0
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    private final UserDetailsService userDetailsService;

    private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    //白名单
    private static String[] whiteList;

    static {
        whiteList = new String[]{
                "/swagger-resources/**",
                "/doc.html",
                "/webjars/**",
                "/actuator/**",
                "/favicon.ico",
                "/api/v1/user/login",
                "/druid/**",
                "/api/v1/user/getUserInfo",
                "/api/v1/user/authToken",
                "/api/v1/user/third",
                "/api/v1/file-info/getFileInfo",
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
                        .antMatchers(whiteList)
                        .permitAll()
                        .anyRequest()
                        .authenticated()
                ).userDetailsService(userDetailsService)
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}

