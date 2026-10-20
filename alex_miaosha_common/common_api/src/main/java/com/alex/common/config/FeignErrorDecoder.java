package com.alex.common.config;

import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.SystemException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * description:  微服务间 Feign 远程调用异常解码器
 * 解析下游微服务返回的结构化 Result 错误报文，提取 code/message，
 * 避免下游业务异常被直接包装成未处理的底层 500 异常
 * author:       alex
 * createDate:   2026/10/20
 * version:      1.0.0
 */
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();
    private final ObjectMapper objectMapper;

    public FeignErrorDecoder() {
        this.objectMapper = new ObjectMapper();
    }

    public FeignErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        String body = null;
        try {
            if (response.body() != null) {
                body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            log.warn("读取 Feign 响应体失败: methodKey={}, status={}, err={}", methodKey, status, e.getMessage());
        }

        log.warn("Feign 远程调用失败: methodKey={}, status={}, body={}", methodKey, status, body);

        if (body != null && !body.trim().isEmpty()) {
            try {
                JsonNode jsonNode = objectMapper.readTree(body);
                // 尝试提取 Result 结构中的 code 和 message / msg
                JsonNode codeNode = jsonNode.get("code");
                JsonNode msgNode = jsonNode.get("message") != null ? jsonNode.get("message") : jsonNode.get("msg");
                if (codeNode != null && msgNode != null) {
                    String code = codeNode.asText();
                    String msg = msgNode.asText();
                    return new SystemException(code, "[Downstream:" + methodKey + "] " + msg);
                }
            } catch (Exception ignored) {
                // 非标准 JSON，退回按 HTTP 状态码处理
            }
        }

        if (status == 404) {
            return new SystemException(ResultEnum.SYSTEM_NO_AVAILABLE, "[Feign 404 Not Found] " + methodKey);
        } else if (status == 503 || status == 502 || status == 504) {
            return new SystemException(ResultEnum.SYSTEM_BUSY, "[Feign " + status + " Service Unavailable] " + methodKey);
        }

        return defaultDecoder.decode(methodKey, response);
    }
}
