package com.alex.ai.client.openai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Pattern;

/**
 * AI Prompt 安全清洗与防注入工具类
 * 职责：
 * 1. 剥离大模型特殊保留控制 Token（如 <|im_start|>, [INST] 等）；
 * 2. 检测并中性化越狱与指令覆盖注入短语（如 ignore previous instructions 等）；
 * 3. 施加 XML 数据围栏隔离不可信输入；
 * 4. 实施内容最大字符上限截断，防御拒绝服务。
 */
@Slf4j
public final class AiPromptSanitizer {

    public static final int MAX_CONTENT_LENGTH = 8000;

    // 需剥离的模型特殊控制 Token
    private static final List<String> SPECIAL_TOKENS = List.of(
            "<|im_start|>", "<|im_end|>", "<|endoftext|>", "<|extra_0|>",
            "[INST]", "[/INST]", "<<SYS>>", "<</SYS>>"
    );

    // 典型越狱与指令覆盖模式（忽略大小写）
    private static final List<Pattern> INJECTION_PATTERNS = List.of(
            Pattern.compile("(?i)ignore\\s+(all\\s+)?(previous|prior|above)\\s+instructions?"),
            Pattern.compile("(?i)disregard\\s+(all\\s+)?(previous|prior|above)\\s+instructions?"),
            Pattern.compile("(?i)system\\s*prompt\\s*(override|reset|leak|reveal)"),
            Pattern.compile("(?i)forget\\s+(all\\s+)?(rules|constraints|instructions)"),
            Pattern.compile("(?i)you\\s+are\\s+now\\s+in\\s+developer\\s+mode"),
            Pattern.compile("(?i)dan\\s*:\\s*you\\s+can\\s+do\\s+anything")
    );

    private AiPromptSanitizer() {
    }

    /**
     * 清洗用户输入内容
     *
     * @param rawContent 原始不可信输入
     * @return 安全清洗后的内容
     */
    public static String sanitizeContent(String rawContent) {
        if (!StringUtils.hasText(rawContent)) {
            return "";
        }

        String content = rawContent;

        // 1. 剥离模型保留控制 Token
        for (String token : SPECIAL_TOKENS) {
            if (content.contains(token)) {
                log.warn("检测到恶意注入的特殊模型 Token: {}, 已自动剥离", token);
                content = content.replace(token, "[BLOCKED_TOKEN]");
            }
        }

        // 2. 检测并中性化典型越狱指令
        for (Pattern pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(content).find()) {
                log.warn("检测到潜在 Prompt 越狱注入模式: {}, 已进行中性化转义", pattern.pattern());
                content = pattern.matcher(content).replaceAll("[SECURITY_NEUTRALIZED]");
            }
        }

        // 3. 长度防御截断（默认 8000 字符）
        if (content.length() > MAX_CONTENT_LENGTH) {
            log.info("用户输入字符数 {} 超过上限 {}，执行安全截断", content.length(), MAX_CONTENT_LENGTH);
            content = content.substring(0, MAX_CONTENT_LENGTH) + "\n...[内容已超过安全上限被截断]";
        }

        return content;
    }

    /**
     * 构建不可混淆的 XML 数据围栏
     *
     * @param sanitizedContent 已清洗的内容
     * @return 围栏格式化后的字符串
     */
    public static String fenceContent(String sanitizedContent) {
        return "<user_data>\n" + (sanitizedContent == null ? "" : sanitizedContent) + "\n</user_data>";
    }
}
