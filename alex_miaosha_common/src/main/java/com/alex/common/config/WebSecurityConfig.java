package com.alex.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

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

//    private final SecurityUserDetailsService securityUserDetailsService;

//    @Bean
//    CorsConfigurationSource corsConfigurationSource() {
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
//        return source;
//    }

    // TODO: 2022/9/26 添加用户service 异常处理 
    @Bean
    public SecurityFilterChain web(HttpSecurity http) throws Exception {
        http// 基于 token，不需要 csrf
                .csrf().disable()
                // 基于 token，不需要 session
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeHttpRequests((authorize) -> authorize
                        .antMatchers(
                                "/swagger-resources/**",
                                "/doc.html",
                                "/webjars/**",
                                "/actuator/**",
                                "/favicon.ico",
                                "/user/doLogin",
                                "/user/**",
                                "/druid/**",
                                "/v3/api-docs",
                                "/error").permitAll()
                        .anyRequest()
                        .authenticated()
                );
        return http.build();
    }
////        return http.build();
//        return http.csrf()
//                .disable()
//                .httpBasic()
//                .disable()
//                .formLogin()
////                .successHandler(authenticationSuccessHandler)
////                .failureHandler(authenticationFailureHandler)
//                .permitAll()
//                .and()
//                .logout()
////                .logoutSuccessHandler(logoutSuccessHandler)
//                .and()
//                .authorizeRequests()
//                .antMatchers("/user/**", "/doc.html", "/swagger-resources/**")
//                .permitAll()
//                .anyRequest()
//                .authenticated()
//                .and()
//                .sessionManagement()
//                .disable()
//                .exceptionHandling()
////                .accessDeniedHandler(accessDeniedHandler)
////                .authenticationEntryPoint(authenticationEntryPoint)
//                .and()
////                .apply(httpConfiguration)
////                .and()
//                .build();
//    }

//    @Bean
//    public AuthenticationProvider daoAuthenticationProvider() {
//        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
////        daoAuthenticationProvider.setUserDetailsService(securityUserDetailsService);
//        // 这里要隐藏系统默认的提示信息，否则一直显示账户或密码错误
//        daoAuthenticationProvider.setHideUserNotFoundExceptions(false);
//        return daoAuthenticationProvider;
//    }

//    @Bean
//    public JwtAuthFilter authFilter() throws Exception {
//        return new JwtAuthFilter();
//    }

//    /**
//     * 密码明文加密方式配置
//     * @return
//     */
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    /**
//     * 获取AuthenticationManager（认证管理器），登录时认证使用
//     * @param authenticationConfiguration
//     * @return
//     * @throws Exception
//     */
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
//        return authenticationConfiguration.getAuthenticationManager();
//    }
//
//    @Bean
//    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        return http
//                // 基于 token，不需要 csrf
//                .csrf().disable()
//                // 基于 token，不需要 session
//                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
//                // 设置 jwtAuthError 处理认证失败、鉴权失败
////                .exceptionHandling()
////                .authenticationEntryPoint(jwtAuthError).
////                accessDeniedHandler(jwtAuthError)
////                .and()
//                // 下面开始设置权限
//                .authorizeRequests(authorize -> authorize
//                        // 请求放开
//                        .antMatchers("/**").permitAll()
//                        .antMatchers("/**").permitAll()
//                        // 其他地址的访问均需验证权限
//                        .anyRequest().authenticated()
//                )
//                // 添加 JWT 过滤器，JWT 过滤器在用户名密码认证过滤器之前
////                .addFilterBefore(authFilter(), UsernamePasswordAuthenticationFilter.class)
//                // 认证用户时用户信息加载配置，注入springAuthUserService
////                .userDetailsService(xxxAuthUserService)
//                .build();
//    }
//
//    /**
//     * 配置跨源访问(CORS)
//     * @return
//     */
//    @Bean
//    CorsConfigurationSource corsConfigurationSource() {
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
//        return source;
//    }
}

