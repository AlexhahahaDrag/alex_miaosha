package com.alex.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @description: security配置类
 * @author: alex
 * @createDate: 2022/9/22 22:51
 * @version: 1.0.0
 */
@Configuration
public class WebSecurityConfig {

    // TODO: 2022/9/22 白名单未设置成功 
    @Bean
    public SecurityFilterChain web(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((authorize) -> authorize
                .mvcMatchers(
                        "/swagger-resources/**",
                        "/doc.html",
                        "/webjars/**",
                        "/actuator/**",
                        "/favicon.ico",
                        "/user/doLogin",
                        "/user/**",
                        "/druid/**",
                        "/error",
                        "/**").permitAll()
                .antMatchers("/user/**").permitAll()
                .anyRequest().authenticated()
        );
        return http.build();
    }
}

