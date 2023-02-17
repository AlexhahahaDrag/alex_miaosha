package com.alex.gateway.filter;

import com.alex.api.user.api.UserApi;
import com.alex.api.user.utils.jwt.Audience;
import com.alex.api.user.utils.jwt.JwtTokenUtils;
import com.alex.api.user.vo.user.TUserVo;
import com.alex.base.common.Result;
import com.alex.base.constants.SysConf;
import com.alex.common.redis.key.UserKey;
import com.alex.common.utils.date.DateUtils;
import com.alex.common.utils.redis.RedisUtils;
import com.alex.common.utils.string.StringUtils;
import com.alex.gateway.utils.AutowiredBean;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @description: 过滤器打印请求地址
 * @SneakyThrows 它是lombok包下的注解 并且继承了Throwable
 * <p>
 * 作用 是为了用try{}catch{}捕捉异常
 * 添加之后会在  代码编译时 自动捕获异常
 * @author: majf
 * @createDate: 2022/7/29 14:57
 * @version: 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class GatewayFilter implements GlobalFilter, Ordered {

    private final Audience audience;

    private final RedisUtils redisUtils;

    private final JwtTokenUtils jwtTokenUtils;

    private static final PathMatcher antPathMatcher = new AntPathMatcher();

    @SneakyThrows
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();
        //白名单校验路径
        if (audience.getWhiteList() != null && !audience.getWhiteList().isEmpty()) {
            for (String white : audience.getWhiteList()) {
                if (antPathMatcher.match(white, path)) {
                    return chain.filter(exchange);
                }
            }
        }
        log.info("当前请求地址：{}", path);
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        //得到请求头信息authorization信息
        String authHeader = Optional.ofNullable(request)
                .map(re -> re.getHeaders())
                .map(header -> header.getFirst(audience.getTokenHeader()))
                .orElse(null);

        if (StringUtils.isEmpty(authHeader) || !authHeader.startsWith(audience.getTokenHead())) {
            return out(response);
        }
        final String token = authHeader.substring(audience.getTokenHead().length());
        UserApi userApi = AutowiredBean.getBean(UserApi.class);
        CompletableFuture<Result<Boolean>> completableFuture = CompletableFuture.supplyAsync(() -> userApi.authToken(token));
        return chain.filter(exchange);
    }

    /**
     * @description: 拦截器的优先级，数字越小优先级越高
     * @author: majf
     * @return: int
     */
    @Override
    public int getOrder() {
        return 0;
    }

    private Mono<Void> out(ServerHttpResponse response) {
        JsonObject message = new JsonObject();
        message.addProperty("success", false);
        message.addProperty("code", 403);
        message.addProperty("data", "请先登录！");
        byte[] bits = message.toString().getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bits);
        //response.setStatusCode(HttpStatus.UNAUTHORIZED);
        //指定编码，否则在浏览器中会中文乱码
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        return response.writeWith(Mono.just(buffer));
    }
}
