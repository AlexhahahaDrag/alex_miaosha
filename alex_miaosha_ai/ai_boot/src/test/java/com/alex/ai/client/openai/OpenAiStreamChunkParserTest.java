package com.alex.ai.client.openai;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class OpenAiStreamChunkParserTest {

    @Test
    void parseDeltaText_dataPrefix_returnsContent() {
        String line = "data: {\"choices\":[{\"delta\":{\"content\":\"Hello\"}}]}";

        Optional<String> delta = OpenAiStreamChunkParser.parseDeltaText(line);

        assertTrue(delta.isPresent());
        assertEquals("Hello", delta.get());
    }

    @Test
    void parseDeltaText_bareJson_returnsContent() {
        String line = "{\"choices\":[{\"delta\":{\"content\":\"world\"}}]}";

        Optional<String> delta = OpenAiStreamChunkParser.parseDeltaText(line);

        assertTrue(delta.isPresent());
        assertEquals("world", delta.get());
    }

    @Test
    void isDoneLine_dataDone_returnsTrue() {
        assertTrue(OpenAiStreamChunkParser.isDoneLine("data: [DONE]"));
    }

    @Test
    void parseDeltaText_emptyContent_returnsEmpty() {
        String line = "data: {\"choices\":[{\"delta\":{\"content\":\"\"}}]}";

        Optional<String> delta = OpenAiStreamChunkParser.parseDeltaText(line);

        assertTrue(delta.isEmpty());
    }

    @Test
    void parseDeltaText_missingDelta_returnsEmpty() {
        String line = "data: {\"choices\":[{\"index\":0}]}";

        Optional<String> delta = OpenAiStreamChunkParser.parseDeltaText(line);

        assertTrue(delta.isEmpty());
    }

    @Test
    void parseDeltaText_malformedJson_returnsEmpty() {
        String line = "data: {\"choices\":[{\"delta\":{\"content\":";

        Optional<String> delta = OpenAiStreamChunkParser.parseDeltaText(line);

        assertTrue(delta.isEmpty());
    }

    @Test
    void parseDeltaText_blankLine_returnsEmpty() {
        assertTrue(OpenAiStreamChunkParser.parseDeltaText("").isEmpty());
        assertTrue(OpenAiStreamChunkParser.parseDeltaText("   ").isEmpty());
    }

    @Test
    void parseDeltaText_commentLine_returnsEmpty() {
        Optional<String> delta = OpenAiStreamChunkParser.parseDeltaText(": ping");

        assertTrue(delta.isEmpty());
        assertFalse(OpenAiStreamChunkParser.isDoneLine(": ping"));
    }

    @Test
    void parseDeltaText_doneLine_returnsEmpty() {
        assertTrue(OpenAiStreamChunkParser.parseDeltaText("data: [DONE]").isEmpty());
    }

    @Test
    void isDoneLine_nonDone_returnsFalse() {
        assertFalse(OpenAiStreamChunkParser.isDoneLine("data: {\"choices\":[]}"));
        assertFalse(OpenAiStreamChunkParser.isDoneLine(""));
    }
}
