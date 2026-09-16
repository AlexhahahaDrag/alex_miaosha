package com.alex.ai.util;

import java.util.Locale;
import java.util.Set;

public final class AiApiKeys {
    private static final Set<String> PLACEHOLDERS = Set.of(
            "sk-xxx", "changeme", "your-api-key", "todo");
    private AiApiKeys() {}
    public static boolean isConfigured(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return false;
        }
        return !PLACEHOLDERS.contains(apiKey.trim().toLowerCase(Locale.ROOT));
    }
}
