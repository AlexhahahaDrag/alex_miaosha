package com.alex.common.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 *description:  feign配置
 *author:       majf
 *createDate:   2022/12/28 11:41
 *version:      1.0.0
 */
@Configuration
public class FeignConfig implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            //添加token
            requestTemplate.header("Authorization", request.getHeader("Authorization"));
        }
    }

    @Bean
    public Logger.Level level() {
        //仅记录请求方法、URL、响应状态代码以及执行时间，生成一般用这个
        return Logger.Level.BASIC;
    }

    @Bean
    public RequestContextListener requestContextListener(){
        return new RequestContextListener();
    }
}