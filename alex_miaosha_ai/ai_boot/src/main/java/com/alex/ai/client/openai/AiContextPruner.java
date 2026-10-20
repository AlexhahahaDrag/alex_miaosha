package com.alex.ai.client.openai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * AI 上下文记忆滑动窗口与 Budget 裁剪工具类
 * 职责：
 * 1. 识别多轮对话历史列表（history / messages 等）；
 * 2. 轮数限制：保留最近 N 条/轮有效交互（默认最多保留最近 6 条，即 3 轮问答）；
 * 3. 字符预算限制：总字符数超出上限（默认 3000 字符）时自旧到新依次裁剪淘汰；
 * 4. 保护业务上下文：保护除历史对话外的业务核心字段（如 userId, bizType, giftId 等）100% 完整。
 */
@Slf4j
public final class AiContextPruner {

    public static final int DEFAULT_MAX_HISTORY_ENTRIES = 6;
    public static final int DEFAULT_MAX_BUDGET_CHARS = 3000;

    private static final List<String> HISTORY_KEYS = List.of(
            "history", "messages", "chatHistory", "conversations"
    );

    private AiContextPruner() {
    }

    /**
     * 对上下文 Context 进行智能滑动窗口与预算裁剪
     *
     * @param context 原始上下文 Map
     * @param objectMapper JSON 序列化工具
     * @return 裁剪后的新 Map（原 Map 不受破坏）
     */
    public static Map<String, Object> pruneContext(Map<String, Object> context, ObjectMapper objectMapper) {
        return pruneContext(context, DEFAULT_MAX_HISTORY_ENTRIES, DEFAULT_MAX_BUDGET_CHARS, objectMapper);
    }

    public static Map<String, Object> pruneContext(Map<String, Object> context,
                                                   int maxHistoryEntries,
                                                   int maxBudgetChars,
                                                   ObjectMapper objectMapper) {
        if (context == null || context.isEmpty()) {
            return context;
        }

        Map<String, Object> pruned = new LinkedHashMap<>(context);

        // 寻找对话历史键
        for (String key : HISTORY_KEYS) {
            Object historyObj = pruned.get(key);
            if (historyObj instanceof List<?> list) {
                List<?> prunedList = pruneHistoryList(list, maxHistoryEntries, maxBudgetChars, objectMapper);
                pruned.put(key, prunedList);
            }
        }

        return pruned;
    }

    private static List<?> pruneHistoryList(List<?> rawList,
                                            int maxEntries,
                                            int maxBudgetChars,
                                            ObjectMapper objectMapper) {
        if (rawList == null || rawList.isEmpty()) {
            return rawList;
        }

        // 1. 滑动窗口按条数截断（保留最新的 maxEntries 条）
        List<?> windowedList;
        if (rawList.size() > maxEntries) {
            int fromIndex = rawList.size() - maxEntries;
            windowedList = new ArrayList<>(rawList.subList(fromIndex, rawList.size()));
            log.info("对话历史总数 {} 超过滑动窗口上限 {}，已自动淘汰最早的 {} 条",
                    rawList.size(), maxEntries, fromIndex);
        } else {
            windowedList = new ArrayList<>(rawList);
        }

        // 2. 字符预算控制（自旧到新逐一淘汰超标历史）
        if (objectMapper != null && maxBudgetChars > 0) {
            while (windowedList.size() > 1 && calculateJsonLength(windowedList, objectMapper) > maxBudgetChars) {
                // 移除最旧的一条
                windowedList.remove(0);
                log.info("对话历史总字符数超出预算 {}，已淘汰最早一条历史记录，剩余条数: {}",
                        maxBudgetChars, windowedList.size());
            }
        }

        return windowedList;
    }

    private static int calculateJsonLength(Object obj, ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(obj).length();
        } catch (Exception e) {
            return String.valueOf(obj).length();
        }
    }
}
