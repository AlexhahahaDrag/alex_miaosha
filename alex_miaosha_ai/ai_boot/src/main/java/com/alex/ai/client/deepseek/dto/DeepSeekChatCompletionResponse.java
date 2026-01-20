package com.alex.ai.client.deepseek.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * AI Agent：
 * DeepSeek Chat Completions 响应体（按 OpenAI 兼容格式定义）
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeepSeekChatCompletionResponse {

    private String id;
    private String model;

    private List<Choice> choices;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        private Integer index;
        private Message message;

        @JsonProperty("finish_reason")
        private String finishReason;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        private String role;
        private String content;
    }
}


