package com.alex.gateway.filter;

import cn.hutool.json.JSONUtil;
import com.alex.api.user.api.UserApi;
import com.alex.base.common.Result;
import com.alex.common.utils.secret.AESUtils;
import com.alex.gateway.config.GatewayAudience;
import com.alex.gateway.utils.AutowiredBean;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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

    private final GatewayAudience audience;

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
        // 得到请求头信息authorization信息
        String token = Optional.ofNullable(request)
                .map(re -> re.getHeaders())
                .map(header -> header.getFirst(audience.getTokenHeader()))
                .orElse(null);
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        UserApi userApi = AutowiredBean.getBean(UserApi.class);
        CompletableFuture<Result<Boolean>> completableFuture = CompletableFuture.supplyAsync(() -> {
                    // 复制主线程的 线程共享数据
                    RequestContextHolder.setRequestAttributes(attributes);
                    Result<Boolean> res = userApi.authToken(token);
                    return res;
                }
        );
        Boolean result = Optional.ofNullable(completableFuture).map(item -> {
            try {
                return item.get().getData();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }).get();
        return result ? secretOut(exchange, chain) : out(response);
    }

    /**
     * @description: 拦截器的优先级，数字越小优先级越高
     * @author: majf
     * @return: int
     */
    @Override
    public int getOrder() {
        return -2;
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

    private Mono<Void> secretOut(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponse originalResponse = exchange.getResponse();
        DataBufferFactory bufferFactory = originalResponse.bufferFactory();
        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux) {
                    Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                    return super.writeWith(fluxBody.map(dataBuffer -> {
                        byte[] content = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(content);
                        DataBufferUtils.release(dataBuffer);
                        String s = new String(content, Charset.forName("UTF-8"));
                        System.out.println(s);
                        //TODO，s就是response的值，想修改、查看就随意而为了
                        byte[] uppedContent;
                        try {
                            uppedContent = new String(AESUtils.encrypt(JSONUtil.toJsonStr(s), "20230610HelloDog", "1234567890123456", "PKCS5Padding").getBytes(), Charset.forName("UTF-8")).getBytes();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        return bufferFactory.wrap(uppedContent);
                    }));
                }
                return super.writeWith(body);
            }
        };
        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }
}
