package com.alex.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 验证 LocalDateTimeFormatConfig 定制的 Jackson 序列化与反序列化规则
 */
class LocalDateTimeFormatConfigTest {

    static class SampleTimeModel {
        private LocalDateTime dateTime;
        private LocalDate date;
        private LocalTime time;

        public SampleTimeModel() {
        }

        public SampleTimeModel(LocalDateTime dateTime, LocalDate date, LocalTime time) {
            this.dateTime = dateTime;
            this.date = date;
            this.time = time;
        }

        public LocalDateTime getDateTime() {
            return dateTime;
        }

        public void setDateTime(LocalDateTime dateTime) {
            this.dateTime = dateTime;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public LocalTime getTime() {
            return time;
        }

        public void setTime(LocalTime time) {
            this.time = time;
        }
    }

    @Test
    @DisplayName("验证 Jackson2ObjectMapperBuilderCustomizer 定制的时间格式序列化与反序列化")
    void testJacksonCustomizer() throws Exception {
        LocalDateTimeFormatConfig config = new LocalDateTimeFormatConfig();
        Jackson2ObjectMapperBuilderCustomizer customizer = config.jackson2ObjectMapperBuilderCustomizer();

        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        customizer.customize(builder);
        ObjectMapper mapper = builder.build();

        LocalDateTime dt = LocalDateTime.of(2026, 9, 18, 14, 30, 45);
        LocalDate d = LocalDate.of(2026, 9, 18);
        LocalTime t = LocalTime.of(14, 30, 45);
        SampleTimeModel model = new SampleTimeModel(dt, d, t);

        String json = mapper.writeValueAsString(model);
        assertTrue(json.contains("\"2026-09-18 14:30:45\""), "LocalDateTime 序列化格式必须为 yyyy-MM-dd HH:mm:ss");
        assertTrue(json.contains("\"2026-09-18\""), "LocalDate 序列化格式必须为 yyyy-MM-dd");
        assertTrue(json.contains("\"14:30:45\""), "LocalTime 序列化格式必须为 HH:mm:ss");

        SampleTimeModel deserialized = mapper.readValue(json, SampleTimeModel.class);
        assertEquals(dt, deserialized.getDateTime());
        assertEquals(d, deserialized.getDate());
        assertEquals(t, deserialized.getTime());
    }
}
