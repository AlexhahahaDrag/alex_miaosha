package com.alex.ai.client.deepseek.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * AI Agent：
 * DeepSeek Chat Completions 请求体（按 OpenAI 兼容格式定义）
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeepSeekChatCompletionRequest {

    /**
     * 模型名称（例如：deepseek-chat / deepseek-reasoner）
     */
    private String model;

    /**
     * 对话消息
     */
    private List<Message> messages;

    /**
     * 采样温度
     */
    private Double temperature;

    /**
     * 最大 tokens
     */
    @JsonProperty("max_tokens")
    private Integer maxTokens;

    /**
     * 是否流式返回（本项目默认 false，便于后端统一处理）
     */
    private Boolean stream = false;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Message {
        private String role;
        private String content;
    }
}


