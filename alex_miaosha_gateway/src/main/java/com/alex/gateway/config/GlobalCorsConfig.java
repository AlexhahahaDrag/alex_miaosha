package com.alex.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * description:
 * author:       majf
 * createDate:   2022/10/8 17:05
 * version:      1.0.0
 */
@Configuration
public class GlobalCorsConfig {

    private final Long MAX_AGE = 18000l;

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //允许访问的头信息，*表示全部
        corsConfiguration.addAllowedHeader("*");
        //允许提交的请求方法类型，*表示全部
        corsConfiguration.addAllowedMethod("*");
        //允许向该服务器提交请求的uri，*表示全部允许，在springmvc中，如果设置成*，会自动转成当前请求头中的origin
        corsConfiguration.addAllowedOrigin("*");
        //这里一定要设置，因为这里要携带请求头进行凭证验证，允许cookies跨域
        corsConfiguration.setAllowCredentials(true);
        //预检请求的缓存时间（秒），即在这段时间里，对于相同的跨域请求不在预检
        corsConfiguration.setMaxAge(MAX_AGE);
        //配置前端js允许访问的自定义响应头，不能用*
        corsConfiguration.addExposedHeader(HttpHeaders.ACCEPT);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsWebFilter(source);
    }
}
