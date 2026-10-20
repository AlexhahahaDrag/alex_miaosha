package com.alex.gateway.handler;

import com.alex.base.common.Result;
import com.alex.gateway.utils.EncryptionUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.gateway.support.TimeoutException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;

/**
 * description: 网关全局统一 JSON 异常处理器（捕获 404/503/504 等异常，统一映射为 Result 标准格式）
 * author: alex
 * createDate: 2026/10/20
 * version: 2.0.0
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class GatewayJsonErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        String path = exchange.getRequest().getPath().toString();
        log.error("网关异常捕获 [Path: {}]: {}", path, ex.getMessage(), ex);

        // 统一响应头与编码
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 加密协议协商版本回显
        String clientCryptoVersion = exchange.getRequest().getHeaders().getFirst(EncryptionUtils.HEADER_CRYPTO_VERSION);
        if (EncryptionUtils.VERSION_2_0.equalsIgnoreCase(clientCryptoVersion)) {
            response.getHeaders().set(EncryptionUtils.HEADER_CRYPTO_VERSION, EncryptionUtils.VERSION_2_0);
        }

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String code = "500";
        String message = "网关内部处理异常，请稍后重试！";

        if (ex instanceof NotFoundException || (ex instanceof ResponseStatusException rse && rse.getStatus() == HttpStatus.NOT_FOUND)) {
            status = HttpStatus.NOT_FOUND;
            code = "404";
            message = "网关未找到对应服务路由或资源不存在！";
        } else if (ex instanceof ConnectException || ex.getCause() instanceof ConnectException) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            code = "503";
            message = "目标微服务不可用或网络不可达，请稍后重试！";
        } else if (ex instanceof TimeoutException || ex.getCause() instanceof java.util.concurrent.TimeoutException) {
            status = HttpStatus.GATEWAY_TIMEOUT;
            code = "504";
            message = "下游微服务响应超时，请稍后重试！";
        } else if (ex instanceof ResponseStatusException rse) {
            status = rse.getStatus();
            code = String.valueOf(status.value());
            message = rse.getReason() != null ? rse.getReason() : status.getReasonPhrase();
        }

        response.setStatusCode(status);
        Result<Void> errorResult = Result.error(code, message);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(errorResult);
        } catch (JsonProcessingException e) {
            log.error("序列化网关错误响应 JSON 失败", e);
            bytes = ("{\"code\":\"" + code + "\",\"message\":\"" + message + "\",\"data\":null}").getBytes();
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
