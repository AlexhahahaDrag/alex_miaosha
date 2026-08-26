package com.alex.ai.client.openai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * OpenAI Chat Completions 请求体（兼容格式）。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenAiChatCompletionRequest {

    private String model;

    private List<Message> messages;

    private Double temperature;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    private Boolean stream = false;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Message {
        private String role;
        private String content;
    }
}
