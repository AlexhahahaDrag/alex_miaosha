package com.alex.gateway.filter;

import com.alex.base.constants.SysConf;
import com.alex.common.redis.key.UserKey;
import com.alex.utils.date.DateUtils;
import com.alex.utils.jwt.Audience;
import com.alex.utils.jwt.JwtTokenUtils;
import com.alex.utils.redis.RedisUtils;
import com.alex.utils.string.StringUtils;
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
import org.springframework.security.core.context.SecurityContextHolder;
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
        // 私钥
        String base64Secret = audience.getBase64Secret();
        // 获取在线的管理员信息
        if (jwtTokenUtils.isExpiration(token, base64Secret)) {
            return out(response);
        }
        //得到过期时间
        Date expiration = jwtTokenUtils.getExpiration(token, base64Secret);
        Instant instant = expiration.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime expirationDate = LocalDateTime.ofInstant(instant, zoneId);
        LocalDateTime nowDate = LocalDateTime.now();
        String username = jwtTokenUtils.getUsername(token, base64Secret);
        Long adminId = jwtTokenUtils.getUserId(token, base64Secret);
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//            // 通过用户名加载SpringSecurity用户
//            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
//            // 校验Token的有效性
//            if (jwtTokenUtils.validateToken(token, userDetails, base64Secret)) {
//                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
//                        userDetails, null, userDetails.getAuthorities());
//                //以后可以security中取得SecurityUser信息
//                SecurityContextHolder.getContext().setAuthentication(authentication);
//            }
        }
        // 得到两个日期相差的间隔，秒
        long survivalSecond = DateUtils.diffSecondByTwoDays(nowDate, expirationDate) / 1000;
        // 当存活时间小于更新时间，那么将颁发新的Token到客户端，同时重置新的过期时间
        // 而旧的Token将会在不久之后从Redis中过期
        if (survivalSecond < audience.getRefreshSecond()) {
            // 生成一个新的Token
            String newToken = audience.getTokenHead() + jwtTokenUtils.refreshToken(token, base64Secret, audience.getExpiresSecond() * 1000);
            // 将新token赋值，用于后续使用
            authHeader = newToken;
            // 维护 uuid - token 互相转换的Redis集合【主要用于在线用户管理】
            redisUtils.setEx(UserKey.getById, adminId.toString(), newToken, audience.getExpiresSecond(), TimeUnit.MINUTES);
            //把adminUid存储到request中
            String finalAuthHeader = authHeader;
            Consumer<HttpHeaders> headers = httpHeaders -> {
                httpHeaders.add(SysConf.ADMIN_ID, adminId + "");
                httpHeaders.add(SysConf.ADMIN_ID, username);
                httpHeaders.add(SysConf.TOKEN, finalAuthHeader);
            };
            request.mutate().headers(headers).build();
            log.info("解析出来用户: {}", username);
            log.info("解析出来的用户Uid: {}", adminId);
        }
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
