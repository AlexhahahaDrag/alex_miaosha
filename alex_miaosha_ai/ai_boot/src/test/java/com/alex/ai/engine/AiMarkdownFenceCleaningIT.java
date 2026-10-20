package com.alex.ai.engine;

import com.alex.ai.client.openai.OpenAiCompatibleClient;
import com.alex.ai.config.AiProperties;
import com.alex.ai.config.DeepSeekProperties;
import com.alex.ai.engine.impl.AbstractOpenAiCompatibleAiEngine;
import com.alex.ai.engine.impl.DeepSeekAiEngine;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.alex.api.ai.vo.AiAnalyzeResp;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 阶段三十：AI 大模型 Markdown 代码块自适应清洗与结构化容错集成测试
 */
@ExtendWith(MockitoExtension.class)
public class AiMarkdownFenceCleaningIT {

    @Mock
    private OpenAiCompatibleClient openAiCompatibleClient;

    private DeepSeekAiEngine engine;

    @BeforeEach
    void setUp() {
        AiProperties properties = new AiProperties();
        DeepSeekProperties deepseek = new DeepSeekProperties();
        deepseek.setApiKey("sk-test-key-001");
        deepseek.setModel("deepseek-chat");
        properties.setDeepseek(deepseek);

        engine = new DeepSeekAiEngine(openAiCompatibleClient, new ObjectMapper(), properties);
    }

    @Test
    @DisplayName("工具方法验证：stripMarkdownFences 正确剥离各类 Markdown 围栏变体")
    void testStripMarkdownFencesVariants() {
        // 1. 标准 ```json 换行
        String raw1 = "```json\n{\"summary\":\"标准\"}\n```";
        assertEquals("{\"summary\":\"标准\"}", AbstractOpenAiCompatibleAiEngine.stripMarkdownFences(raw1));

        // 2. 带有首尾多余空格和 CRLF
        String raw2 = "  ```json\r\n{\"summary\":\"CRLF\"}\r\n```  ";
        assertEquals("{\"summary\":\"CRLF\"}", AbstractOpenAiCompatibleAiEngine.stripMarkdownFences(raw2));

        // 3. 无语言标识的 ``` 围栏
        String raw3 = "```\n{\"summary\":\"无语言\"}\n```";
        assertEquals("{\"summary\":\"无语言\"}", AbstractOpenAiCompatibleAiEngine.stripMarkdownFences(raw3));

        // 4. 普通纯文本无围栏
        String raw4 = "普通纯文本回复";
        assertEquals("普通纯文本回复", AbstractOpenAiCompatibleAiEngine.stripMarkdownFences(raw4));

        // 5. null 与空白
        assertNull(AbstractOpenAiCompatibleAiEngine.stripMarkdownFences(null));
        assertEquals("", AbstractOpenAiCompatibleAiEngine.stripMarkdownFences("   "));
    }

    @Test
    @DisplayName("大模型返回 ```json 包裹时：自适应清洗并成功提取结构化 summary 与 keyPoints")
    void testAnalyzeMarkdownFencedJsonSuccess() {
        String llmOutput = "```json\n" +
                "{\n" +
                "  \"summary\": \"本次财务支出环比下降 15%\",\n" +
                "  \"keyPoints\": [\"餐饮消费显著收敛\", \"交通补贴发放平稳\"]\n" +
                "}\n" +
                "```";

        when(openAiCompatibleClient.chat(any(), any(), anyString())).thenReturn(llmOutput);

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setContent("分析本月财务支出");

        AiAnalyzeResp resp = engine.analyze(req, "req-fence-001", System.currentTimeMillis());

        assertNotNull(resp);
        assertEquals("本次财务支出环比下降 15%", resp.getSummary());
        assertEquals(List.of("餐饮消费显著收敛", "交通补贴发放平稳"), resp.getKeyPoints());
        assertTrue(resp.getEngine().startsWith("deepseek:"));
        assertTrue(resp.getCostMs() >= 0);
    }

    @Test
    @DisplayName("大模型返回标准纯 JSON 时：正常反序列化结构化结果")
    void testAnalyzePlainJsonSuccess() {
        String llmOutput = "{\"summary\":\"活动策划已生成\",\"keyPoints\":[\"首发折扣\",\"限时满减\"]}";
        when(openAiCompatibleClient.chat(any(), any(), anyString())).thenReturn(llmOutput);

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setContent("生成营销活动");

        AiAnalyzeResp resp = engine.analyze(req, "req-plain-002", System.currentTimeMillis());

        assertNotNull(resp);
        assertEquals("活动策划已生成", resp.getSummary());
        assertEquals(List.of("首发折扣", "限时满减"), resp.getKeyPoints());
    }

    @Test
    @DisplayName("大模型返回非 JSON 自由文本时：平稳降级为纯文本 summary，keyPoints 为空列表")
    void testAnalyzeFreeTextDegradesGracefully() {
        String llmOutput = "您好！这是一份纯文本建议：建议下周开展促销。";
        when(openAiCompatibleClient.chat(any(), any(), anyString())).thenReturn(llmOutput);

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setContent("获取日常建议");

        AiAnalyzeResp resp = engine.analyze(req, "req-freetext-003", System.currentTimeMillis());

        assertNotNull(resp);
        assertEquals("您好！这是一份纯文本建议：建议下周开展促销。", resp.getSummary());
        assertTrue(resp.getKeyPoints().isEmpty());
    }

    @Test
    @DisplayName("大模型返回畸变或未闭合 JSON 时：捕获异常并降级为清洗后的纯文本，绝不抛出 500")
    void testAnalyzeMalformedJsonHandledGracefully() {
        String llmOutput = "```json\n{\"summary\":\"未完成的JSON断句\n```";
        when(openAiCompatibleClient.chat(any(), any(), anyString())).thenReturn(llmOutput);

        AiAnalyzeReq req = new AiAnalyzeReq();
        req.setContent("异常测试");

        assertDoesNotThrow(() -> {
            AiAnalyzeResp resp = engine.analyze(req, "req-malformed-004", System.currentTimeMillis());
            assertNotNull(resp);
            assertEquals("{\"summary\":\"未完成的JSON断句", resp.getSummary());
            assertTrue(resp.getKeyPoints().isEmpty());
        });
    }
}
