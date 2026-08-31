package com.alex.ai.client.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

/**
 * OpenAI 兼容 SSE 行解析（choices[0].delta.content / [DONE]）。
 */
public final class OpenAiStreamChunkParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private OpenAiStreamChunkParser() {
    }

    /**
     * 输入一行（可含 data: 前缀或裸 JSON）；返回 delta content，无则 empty。
     */
    public static Optional<String> parseDeltaText(String line) {
        if (line == null || line.isBlank()) {
            return Optional.empty();
        }
        String trimmed = line.trim();
        if (trimmed.startsWith(":")) {
            return Optional.empty();
        }
        String payload = stripDataPrefix(trimmed);
        if (payload.isBlank() || isDonePayload(payload)) {
            return Optional.empty();
        }
        try {
            JsonNode content = MAPPER.readTree(payload)
                    .path("choices")
                    .path(0)
                    .path("delta")
                    .path("content");
            if (content.isMissingNode() || content.isNull()) {
                return Optional.empty();
            }
            String text = content.asText();
            if (text == null || text.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(text);
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    /** 是否结束标记：[DONE] */
    public static boolean isDoneLine(String line) {
        if (line == null || line.isBlank()) {
            return false;
        }
        String trimmed = line.trim();
        if (trimmed.startsWith(":")) {
            return false;
        }
        return isDonePayload(stripDataPrefix(trimmed));
    }

    private static String stripDataPrefix(String line) {
        if (line.regionMatches(true, 0, "data:", 0, 5)) {
            return line.substring(5).trim();
        }
        return line;
    }

    private static boolean isDonePayload(String payload) {
        return "[DONE]".equals(payload);
    }
}
