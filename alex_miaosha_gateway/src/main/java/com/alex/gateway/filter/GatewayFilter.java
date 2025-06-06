package com.alex.gateway.filter;

import com.alex.api.user.api.UserApi;
import com.alex.base.common.Result;
import com.alex.common.utils.secret.AESUtils;
import com.alex.gateway.config.GatewayAudience;
import com.alex.gateway.utils.AutowiredBean;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpMessage;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * description: 过滤器打印请求地址
 *
 * @SneakyThrows 它是lombok包下的注解 并且继承了Throwable
 * <p>
 * 作用 是为了用try{}catch{}捕捉异常
 * 添加之后会在  代码编译时 自动捕获异常
 * author: majf
 * createDate: 2022/7/29 14:57
 * version: 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class GatewayFilter implements GlobalFilter, Ordered {

    private final GatewayAudience audience;

    private static final PathMatcher antPathMatcher = new AntPathMatcher();

    @SneakyThrows
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();
        // doc直接返回
        if (audience.getDocWhiteList() != null && !audience.getDocWhiteList().isEmpty()) {
            for (String white : audience.getDocWhiteList()) {
                if (antPathMatcher.match(white, path)) {
                    return chain.filter(exchange);
                }
            }
        }
        //白名单校验路径
        if (audience.getWhiteList() != null && !audience.getWhiteList().isEmpty()) {
            for (String white : audience.getWhiteList()) {
                if (antPathMatcher.match(white, path)) {
                    return secretOut(exchange, chain);
                }
            }
        }
        log.info("当前请求地址：{}", path);
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        // 得到请求头信息authorization信息
        String token = Optional.of(request)
                .map(HttpMessage::getHeaders)
                .map(header -> header.getFirst(audience.getTokenHeader()))
                .orElse(null);
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        UserApi userApi = AutowiredBean.getBean(UserApi.class);
        CompletableFuture<Result<Boolean>> completableFuture = CompletableFuture.supplyAsync(() -> {
                    // 复制主线程的 线程共享数据
                    RequestContextHolder.setRequestAttributes(attributes);
                    return userApi.authToken(token);
                }
        ).exceptionally(e -> {
            log.info("认证失败：{}", e.getMessage());
            e.getStackTrace();
            return Result.error("403", "认证失败");
        });
        Boolean result = Optional.of(completableFuture).map(item -> {
            try {
                return item.get().getData();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }).get();
        return result ? secretOut(exchange, chain) : out(response);
    }

    /**
     * description: 拦截器的优先级，数字越小优先级越高
     * author: majf
     * return: int
     */
    @Override
    public int getOrder() {
        return -2;
    }

    private Mono<Void> out(ServerHttpResponse response) throws Exception {
        JsonObject message = new JsonObject();
        message.addProperty("success", false);
        message.addProperty("code", 403);
        message.addProperty("data", "请先登录！");
        byte[] bits = new String(AESUtils.encrypt(JSONObject.toJSONString(message.toString()), "20230610HelloDog", "1234567890123456", "PKCS5Padding").getBytes(), StandardCharsets.UTF_8).getBytes();
//        byte[] bits = JSONObject.toJSONString(message.toString()).getBytes();

        DataBuffer buffer = response.bufferFactory().wrap(bits);
        //response.setStatusCode(HttpStatus.UNAUTHORIZED);
        //指定编码，否则在浏览器中会中文乱码
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        return response.writeWith(Mono.just(buffer));
    }

    private Mono<Void> secretOut(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponse originalResponse = exchange.getResponse();
        DataBufferFactory bufferFactory = originalResponse.bufferFactory();
        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
            @NotNull
            @Override
            public Mono<Void> writeWith(@NotNull Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux<? extends DataBuffer> fluxBody) {
                    return super.writeWith(fluxBody.buffer().handle((dataBuffer, sink) -> {
                        DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
                        DataBuffer buffer = dataBufferFactory.join(dataBuffer);
                        byte[] content = new byte[buffer.readableByteCount()];
                        buffer.read(content);
                        DataBufferUtils.release(buffer);
                        String s = new String(content, StandardCharsets.UTF_8);
                        byte[] uppedContent;
                        try {
                            // TODO: 2025/5/14 测试，过后删除 
                            uppedContent = new String(AESUtils.encrypt(JSONObject.toJSONString(s),
                                    "20230610HelloDog", "1234567890123456", "PKCS5Padding").getBytes(),
                                    StandardCharsets.UTF_8).getBytes();
//                            uppedContent = JSONObject.toJSONString(s).getBytes();
                        } catch (Exception e) {
                            sink.error(new RuntimeException(e));
                            return;
                        }
                        sink.next(bufferFactory.wrap(uppedContent));
                    }));
                }
                return super.writeWith(body);
            }
        };
        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }
}
