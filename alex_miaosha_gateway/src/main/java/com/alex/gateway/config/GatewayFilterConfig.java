package com.alex.gateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @description:  过滤器打印请求地址
 *
 * @SneakyThrows
 * 它是lombok包下的注解 并且继承了Throwable
 *
 * 作用 是为了用try{}catch{}捕捉异常
 * 添加之后会在  代码编译时 自动捕获异常
 * @author:       majf
 * @createDate:   2022/7/29 14:57
 * @version:      1.0.0
 */
@Component
//@Slf4j
public class GatewayFilterConfig implements GlobalFilter, Ordered {

//    @SneakyThrows
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();
//        log.info("当前请求地址：{}", path);
        return chain.filter(exchange);
    }

    /**
     * @description: 拦截器的优先级，数字越小优先级越高
     * @author:      majf
     * @return:      int
    */
    @Override
    public int getOrder() {
        return 0;
    }
}
