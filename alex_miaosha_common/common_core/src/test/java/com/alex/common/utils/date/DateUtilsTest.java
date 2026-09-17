package com.alex.common.utils.date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 验证 DateUtils 核心修复：
 * 1. getServerStartDate 毫秒转秒修复（避免时间穿越到公元 5 万年）
 * 2. getDatePoor 跨天跨小时取模计算（避免累计总小时总分钟错误）
 * 3. 静态 DateTimeFormatter 复用与时间解析
 */
class DateUtilsTest {

    @Test
    @DisplayName("验证服务器启动时间正常且处于当前年份区间，杜绝公元56000+年Bug")
    void testGetServerStartDate() {
        LocalDateTime serverStartDate = DateUtils.getServerStartDate();
        assertNotNull(serverStartDate, "服务器启动时间不可为空");
        // 应该处于当前年代 (>= 2024 且 <= 2030)，绝不可能大于 3000 年
        assertTrue(serverStartDate.getYear() >= 2024 && serverStartDate.getYear() <= 2030,
                "服务器启动年份必须在合规范围内，实际为: " + serverStartDate.getYear());
    }

    @Test
    @DisplayName("验证 getDatePoor 计算跨天耗时正确取模，杜绝总小时与总分钟累加Bug")
    void testGetDatePoor() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 18, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 9, 20, 13, 25, 30);

        String result = DateUtils.getDatePoor(end, start);

        // 正确结果应为 2天3小时25分钟，而非 2天51小时3085分钟
        assertEquals("2天3小时25分钟", result);
    }

    @Test
    @DisplayName("验证 getDatePoor 对同时间及 Null 安全容错")
    void testGetDatePoor_NullAndZero() {
        LocalDateTime now = LocalDateTime.now();
        assertEquals("0天0小时0分钟", DateUtils.getDatePoor(now, now));
        assertEquals("", DateUtils.getDatePoor(null, now));
        assertEquals("", DateUtils.getDatePoor(now, null));
    }

    @Test
    @DisplayName("验证静态 DateTimeFormatter 格式化与解析一致性")
    void testFormatAndParse() {
        LocalDateTime target = LocalDateTime.of(2026, 9, 18, 15, 30, 45);
        String timeStr = DateUtils.getTimeStr(target);
        assertEquals("2026-09-18 15:30:45", timeStr);

        LocalDateTime parsed = DateUtils.parseStringToTime(timeStr);
        assertEquals(target, parsed);

        LocalDate date = LocalDate.of(2026, 9, 18);
        assertEquals("2026-09-18 00:00:00", DateUtils.getToDayStartTime(date));
        assertEquals("2026-09-18 23:59:59", DateUtils.getToDayEndTime(date));
    }
}
