package com.alex.ai.client.openai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 集成测试套件 3:
 * AI 多轮对话上下文记忆滑动窗口与字符预算裁剪测试
 */
class AiContextMemoryPruningIT {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("滑动窗口裁剪: 历史对话超过最大条数限制时只保留最新 N 条")
    void pruneContext_keepsRecentEntries_whenExceedsMaxEntries() {
        Map<String, Object> context = new HashMap<>();
        List<Map<String, String>> history = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            history.add(Map.of("role", i % 2 == 1 ? "user" : "assistant", "content", "msg-" + i));
        }
        context.put("history", history);
        context.put("userId", "1001");

        // 限制最多保留 4 条
        Map<String, Object> pruned = AiContextPruner.pruneContext(context, 4, 10000, objectMapper);

        assertNotNull(pruned);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> prunedHistory = (List<Map<String, String>>) pruned.get("history");
        assertEquals(4, prunedHistory.size(), "应当严格只保留最新的 4 条");
        assertEquals("msg-7", prunedHistory.get(0).get("content"));
        assertEquals("msg-10", prunedHistory.get(3).get("content"));
    }

    @Test
    @DisplayName("字符预算裁剪: 字符数超出 Budget 时从最早的历史开始按序淘汰")
    void pruneContext_prunesOldestFirst_whenExceedsBudgetChars() {
        Map<String, Object> context = new HashMap<>();
        List<Map<String, String>> history = new ArrayList<>();
        history.add(Map.of("content", "A".repeat(500))); // 较旧
        history.add(Map.of("content", "B".repeat(500))); // 较旧
        history.add(Map.of("content", "C".repeat(50)));  // 最新

        context.put("messages", history);

        // 设置字符预算为 400 字符（足以容纳 C，无法容纳 A 和 B）
        Map<String, Object> pruned = AiContextPruner.pruneContext(context, 10, 400, objectMapper);

        @SuppressWarnings("unchecked")
        List<Map<String, String>> prunedHistory = (List<Map<String, String>>) pruned.get("messages");
        assertNotNull(prunedHistory);
        assertTrue(prunedHistory.size() < 3, "超预算的旧消息应当被裁剪淘汰");
        assertEquals("C".repeat(50), prunedHistory.get(prunedHistory.size() - 1).get("content"),
                "最新的消息应当被优先保留");
    }

    @Test
    @DisplayName("业务字段保护: 上下文裁剪绝不破坏非历史的业务核心属性")
    void pruneContext_preservesBusinessFields100Percent() {
        Map<String, Object> context = new HashMap<>();
        context.put("bizType", "gift_analysis");
        context.put("giftId", 998877L);
        context.put("userId", "u-3344");
        context.put("orgId", 100L);
        context.put("history", List.of("m1", "m2", "m3", "m4", "m5", "m6", "m7", "m8"));

        Map<String, Object> pruned = AiContextPruner.pruneContext(context, 2, 5000, objectMapper);

        assertEquals("gift_analysis", pruned.get("bizType"));
        assertEquals(998877L, pruned.get("giftId"));
        assertEquals("u-3344", pruned.get("userId"));
        assertEquals(100L, pruned.get("orgId"));

        @SuppressWarnings("unchecked")
        List<String> list = (List<String>) pruned.get("history");
        assertEquals(2, list.size());
        assertEquals("m7", list.get(0));
        assertEquals("m8", list.get(1));
    }

    @Test
    @DisplayName("空上下文安全: null 或空上下文时安全降级不抛异常")
    void pruneContext_nullOrEmptyContext_returnsSafely() {
        assertNull(AiContextPruner.pruneContext(null, objectMapper));
        assertTrue(AiContextPruner.pruneContext(Collections.emptyMap(), objectMapper).isEmpty());
    }
}
