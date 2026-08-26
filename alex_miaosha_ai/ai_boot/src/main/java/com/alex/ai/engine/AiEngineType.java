package com.alex.ai.engine;

import java.util.Locale;
import java.util.Optional;

public enum AiEngineType {
    DEEPSEEK("deepseek", "DeepSeek"),
    SENSENOVA("sensenova", "SenseNova"),
    RULE_BASED("rule-based", "规则引擎");

    private final String key;
    private final String displayName;

    AiEngineType(String key, String displayName) {
        this.key = key;
        this.displayName = displayName;
    }

    public String getKey() { return key; }
    public String getDisplayName() { return displayName; }

    public static Optional<AiEngineType> fromKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            return Optional.empty();
        }
        String normalized = key.trim().toLowerCase(Locale.ROOT);
        for (AiEngineType type : values()) {
            if (type.key.equals(normalized)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }
}
