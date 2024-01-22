package com.alex.common.utils.date;

import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *description:  时间工具类  DateTimeFormatter线程安全
 *author:       alex
 *createDate:   2021/7/19 7:10
 *version:      1.0.0
 */
@Slf4j
public class DateUtils {

    public static final String STARTTIME = " 00:00:00";

    public static final String ENDTIME = " 23:59:59";

    public static final String FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";

    public static final String[] REPLACE_STRING = new String[] {"GMT+0800", "GMT+08:00"};

    public static final String SPLIT_STRING = "(中国标准时间)";

    public static String YYYY = "yyyy";

    public static String YYYY_MM = "yyyy-MM";

    public static String YYYY_MM_DD = "yyyy-MM-dd";

    public static String YYYYMMDD = "yyyyMMdd";

    public static String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    public static String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    private static String[] parsePatterns = {
            "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
            "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};

    //设置私有构造器
    private DateUtils(){}

    /**
     * description:  获取当前时间 string
     * author:       alex
     * return:       java.lang.String
     */
    public static String getNowTimeStr() {
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS);
        return LocalDateTime.now().format(dateTimeFormat);
    }

    public static String getNowTimeStr(String dateTimeFormatter) {
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern(dateTimeFormatter);
        return LocalDateTime.now().format(dateTimeFormat);
    }

    /**
     * description:  获取当前时间毫秒数
     * author:       alex
     * return:       java.lang.String
     */
    public static Long getNowTimeLong() {
        return LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
    }

    /**
     * description:  获取当前时间 string
     * author:       alex
     * return:       java.lang.String
     */
    public static String getNowTimeStrStartTime() {
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS);
        return LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0).format(dateTimeFormat);
    }
    /**
     * description:  获取当前时间 string
     * author:       alex
     * return:       java.lang.String
     */
    public static String getTimeStr(LocalDateTime time) {
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS);
        return time.format(dateTimeFormat);
    }

    /**
     * description:  获取当前时间 string
     * author:       alex
     * return:       java.lang.String
     */
    public static String getTimeStr(LocalDateTime time, String formmater) {
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern(formmater);
        return time.format(dateTimeFormat);
    }

    /**
     * description:  获取当前时间 localDateTime
     * author:       alex
     * return:       java.time.LocalDateTime
     */
    public static LocalDateTime getNowDate() {
        return LocalDateTime.now();
    }

    /**
     * description:  获取当天开始时间
     * author:       alex
     * return:       java.lang.String
     */
    public static String getToDayStartTime(LocalDate startDate) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(YYYY_MM_DD);
        return dateTimeFormatter.format(startDate) + STARTTIME;
    }

    /**
     * description:  获取当天结束时间
     * author:       alex
     * return:       java.lang.String
     */
    public static String getToDayEndTime(LocalDate endDate) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(YYYY_MM_DD);
        return dateTimeFormatter.format(endDate) + ENDTIME;
    }

    /**
     * description:  获取当天开始时间
     * author:       alex
     * return:       java.lang.String
     */
    public static String getToDayStartTime(LocalDateTime startDate) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(YYYY_MM_DD);
        return dateTimeFormatter.format(startDate) + STARTTIME;
    }

    /**
     * description:  获取当天结束时间
     * author:       alex
     * return:       java.lang.String
     */
    public static String getToDayEndTime(LocalDateTime endDate) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(YYYY_MM_DD);
        return dateTimeFormatter.format(endDate) + ENDTIME;
    }

    /**
     * description:  获取相差几天的开始时间
     * author:       alex
     * return:       java.lang.String
     */
    public static String getToDayStartTimeMinusNDay(LocalDateTime startDate, int n) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(YYYY_MM_DD);
        return dateTimeFormatter.format(startDate.minusDays(n)) + STARTTIME;
    }

    /**
     * description:  获取相差几天的结束时间
     * author:       alex
     * return:       java.lang.String
     */
    public static String getToDayEndTimeMinusNDay(LocalDateTime endDate, int n) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(YYYY_MM_DD);
        return dateTimeFormatter.format(endDate.minusDays(n)) + ENDTIME;
    }

    /**
     * @param oneDay
     * description:  oneDay格式为yyyy-MM-dd
     * author:       alex
     * return:       java.lang.String
    */
    public static String getOneDayStartTime(String oneDay) {
        return getOneDayStartTime(oneDay, YYYY_MM_DD);
    }

    /**
     * @param oneDay     时间
     * @param pattern    时间格式
     * description:     将指定的时间转化为当前的开始时间
     * author:       alex
     * return:       java.lang.String
    */
    public static String getOneDayStartTime(String oneDay, String pattern) {
        return getToDayStartTime(LocalDateTime.parse(oneDay, DateTimeFormatter.ofPattern(pattern)));
    }

    /**
     * description:  获取本周周一开始时间
     * author:       alex
     * return:       java.time.LocalDateTime
    */
    public static LocalDateTime findWeekStartTime() {
        return LocalDateTime.now().with(TemporalAdjusters.previous(DayOfWeek.SUNDAY)).plusDays(1).withHour(0).withMinute(0).withSecond(0);
    }

    /**
     * description:  获取本周周日结束时间
     * author:       alex
     * return:       java.time.LocalDateTime
    */
    public static LocalDateTime findWeekEndTime() {
        return LocalDateTime.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)).minusDays(1).withHour(23).withMinute(59).withSecond(59);
    }

    /**
     * description:  获取本周周一开始时间字符串
     * author:       alex
     * return:       java.time.LocalDateTime
     */
    public static String getWeekStartTimeStr() {
        return getTimeStr(findWeekStartTime());
    }

    /**
     * description:  获取本周周日结束时间字符串
     * author:       alex
     * return:       java.time.LocalDateTime
     */
    public static String getWeekEndTimeStr() {
        return getTimeStr(findWeekEndTime());
    }

    /**
     * @param oneDay    给定时间
     * @param pattern   该时间的格式
     * description:    获取给定时间的周一的开始时间
     * author:       alex
     * return:       java.time.LocalDateTime
    */
    public static LocalDateTime findMondayStartTime(String oneDay, String pattern) {
        return parseStringToTime(oneDay, pattern).with(TemporalAdjusters.previous(DayOfWeek.SUNDAY)).plusDays(1).withHour(0).withMinute(0).withSecond(0);
    }

    /**
     * @param oneDay    给定时间
     * @param pattern   该时间的格式
     * description:    获取给定时间的周一的开始时间
     * author:       alex
     * return:       java.time.LocalDateTime
     */
    public static LocalDateTime findSundayEndTime(String oneDay, String pattern) {
        return parseStringToTime(oneDay, pattern).with(TemporalAdjusters.next(DayOfWeek.MONDAY)).minusDays(1).withHour(23).withMinute(59).withSecond(59);
    }

    /**
     * @param oneDay    时间字符串
     * @param pattern   格式化时间
     * description:    根据给定字符串格式化字符串为时间
     * author:       alex
     * return:       java.time.LocalDateTime
    */
    public static LocalDateTime parseStringToTime(String oneDay, String pattern) {
        return LocalDateTime.parse(oneDay, DateTimeFormatter.ofPattern(pattern));
    }


    /**
     * @param oneDay    时间字符串
     * description:    将字符串转化为时间 （默认 yyyy-MM-dd HH:mm:ss）
     * author:       alex
     * return:       java.time.LocalDateTime
     */
    public static LocalDateTime parseStringToTime(String oneDay) {
        return LocalDateTime.parse(oneDay, DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS));
    }

    /**
     * description:  获取本月初的开始时间
     * author:       alex
     * return:       java.time.LocalDateTime
    */
    public static LocalDateTime findMonthStartTime() {
        return LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);
    }

    /**
     * description:  获取本月末的结束时间
     * author:       alex
     * return:       java.time.LocalDateTime
     */
    public static LocalDateTime findMonthEndTime() {
        return LocalDateTime.now().with(TemporalAdjusters.firstDayOfNextMonth()).minusDays(1).withHour(23).withMinute(59).withSecond(59);
    }

    /**
     * description:  获取当前月的天数
     * author:       alex
     * return:       int
    */
    public static int getCurrentMonthDays() {
        return LocalDateTime.now().with(TemporalAdjusters.firstDayOfNextMonth()).minusDays(1).getDayOfMonth();
    }

    /**
     * description:  获取当前月的天数
     * author:       alex
     * return:       int
     */
    public static int getMonthDays(String oneDay, String pattern) {
        return parseStringToTime(oneDay, pattern).with(TemporalAdjusters.firstDayOfNextMonth()).minusDays(1).getDayOfMonth();
    }

    /**
     * description:  获取当前月的天数
     * author:       alex
     * return:       int
     */
    public static int getMonthDays(String oneDay) {
        return parseStringToTime(oneDay).with(TemporalAdjusters.firstDayOfNextMonth()).minusDays(1).getDayOfMonth();
    }

    public static int getMonthDays(int year, int month) {
        return LocalDateTime.now().withYear(year).withMonth(month).with(TemporalAdjusters.firstDayOfNextMonth()).minusDays(1).getDayOfMonth();
    }

    /**
     * @param date1  小时间
     * @param date2  大时间
     * description:  获取两个时间相差的天数
     * author:       alex
     * return:       long
    */
    public static long diffDayByTwoDays(String date1, String date2) {
        Duration diifTime = Duration.between(parseStringToTime(date1), parseStringToTime(date2));
        return diifTime.toDays();
    }

    /**
     * @param date1  小时间
     * @param date2  大时间
     * description:  获取两个时间相差的秒数
     * author:       alex
     * return:       long
     */
    public static long diffSecondByTwoDays(String date1, String date2) {
        Duration diifTime = Duration.between(parseStringToTime(date1), parseStringToTime(date2));
        return diifTime.toMillis() / 1000;
    }

    /**
     * @param date1  小时间
     * @param date2  大时间
     * description:  获取两个时间相差的秒数
     * author:       alex
     * return:       long
     */
    public static long diffSecondByTwoDays(LocalDateTime date1, LocalDateTime date2) {
        Duration diffTime = Duration.between(date1, date2);
        return diffTime.toMillis();
    }

    /**
     * @param oneDay  时间字符串
     * @param pattern 时间格式
     * description:  获取某一时间是周几
     * author:       alex
     * return:       int
    */
    public static int getDayByWeek(String oneDay, String pattern) {
        return parseStringToTime(oneDay, pattern).getDayOfWeek().getValue();
    }

    /**
     * @param oneDay  时间字符串
     * @param pattern 时间格式
     * description:  获取某一时间是几号
     * author:       alex
     * return:       int
     */
    public static int getDayByMonth(String oneDay, String pattern) {
        return parseStringToTime(oneDay, pattern).getDayOfMonth();
    }

    /**
     * @param oneDay  时间字符串
     * @param pattern 时间格式
     * description:  获取年份数据
     * author:       alex
     * return:       int
     */
    public static int getYear(String oneDay, String pattern) {
        return parseStringToTime(oneDay, pattern).getYear();
    }

    /**
     * description:  获取当前时间的年份
     * author:       alex
     * return:       int
    */
    public static int getYear() {
        return LocalDateTime.now().getYear();
    }

    /**
     * description:  获取当前月
     * author:       alex
     * return:       int
    */
    public static int getMonth() {
        return LocalDateTime.now().getMonthValue();
    }

    /**
     * description:  获取当前天
     * author:       alex
     * return:       int
    */
    public static int getDay() {
        return LocalDateTime.now().getDayOfMonth();
    }

    /*
     * @param hour    小时
     * description:  几个小时后
     * author:       alex
     * return:       java.lang.String
    */
    public static String addHour(double hour) {
        long time = (long) (hour * 60 * 60 * 1000);
        return getTimeStr(addTime(LocalDateTime.now(), time, ChronoUnit.MILLIS));
    }

    /**
     * @param oneTime   时间
     * @param time      添加的时间
     * @param unit      添加时间类型
     * description:    给定时间加减
     * author:       alex
     * return:       java.time.LocalDateTime
    */
    public static LocalDateTime addTime(LocalDateTime oneTime, long time, TemporalUnit unit) {
        return oneTime.plus(time, unit);
    }

    /**
     * @param oneTime   时间
     * @param time      添加的时间
     * @param unit      添加时间类型
     * description:    给定时间加减
     * author:       alex
     * return:       java.time.LocalDateTime
     */
    public static LocalDateTime addTime(String oneTime, long time, TemporalUnit unit) {
        return parseStringToTime(oneTime).plus(time, unit);
    }

    /**
     * @param date
     * @param day
     * description:  计算天数
     * author:       alex
     * return:       java.time.LocalDateTime
    */
    public static LocalDateTime addDay(String date, int day) {
        return addTime(date, day, ChronoUnit.DAYS);
    }

    /**
     * @param n
     * @param formatter
     * description:  获取当前日期前n天的数据集合
     * author:       alex
     * return:       java.util.List<java.lang.String>
    */
    public static List<String> getDaysByN(int n, String formatter) {
        return getDaysByN(LocalDateTime.now(), n, formatter);
    }

    /**
     * @param oneDay
     * @param n
     * @param formatter
     * description:  获取给定日期前n天的集合
     * author:       alex
     * return:       java.util.List<java.lang.String>
    */
    public static List<String> getDaysByN(String oneDay, int n, String formatter) {
        List<String> res = new ArrayList<>();
        while(--n >= 0) {
            res.add(getTimeStr(addDay(oneDay, -n), formatter));
        }
        return res;
    }

    /**
     * @param oneDay
     * @param n
     * @param formatter
     * description:  获取给定日期前n天的集合
     * author:       alex
     * return:       java.util.List<java.lang.String>
     */
    public static List<String> getDaysByN(LocalDateTime oneDay, int n, String formatter) {
        List<String> res = new ArrayList<>();
        while(--n >= 0) {
            res.add(getTimeStr(addDay(getTimeStr(oneDay), -n), formatter));
        }
        return res;
    }

    public static List<String> getDaysBetweenDates(String begin, String end) {
        return getDaysBetweenDates(parseStringToTime(begin, YYYY_MM_DD_HH_MM_SS), parseStringToTime(end, YYYY_MM_DD_HH_MM_SS));
    }

    public static List<String>  getDaysBetweenDates(LocalDateTime begin, LocalDateTime end) {
        List<String> res = new ArrayList<>();
        while(end.compareTo(begin) > 0) {
            res.add(getTimeStr(begin, YYYY_MM_DD));
            begin = begin.plusDays(1);
        }
        return res;
    }

    /**
     * description:  获取今天与周末相差的天数
     * author:       alex
     * return:       long
    */
    public static long diffDiffNowAndSunday() {
        return diffDayByTwoDays(getTimeStr(LocalDateTime.now()), getWeekEndTimeStr());
    }

    /**
     * description:  获取服务器的启动时间
     * author:       alex
     * return:       java.time.LocalDateTime
    */
    public static LocalDateTime getServerStartDate() {
        long startTime = ManagementFactory.getRuntimeMXBean().getStartTime();
        return LocalDateTime.ofEpochSecond(startTime, 0, ZoneOffset.ofHours(8));
    }

    /**
     * @param endDate
     * @param startDate
     * description:   计算两个时间差
     * author:       alex
     * return:       java.lang.String
    */
    public static String getDatePoor(LocalDateTime endDate, LocalDateTime startDate) {
        Duration diff = Duration.between(startDate, endDate);
        return diff.toDays() + "天" + diff.toHours() + "小时" + diff.toMinutes() + "分钟";
    }
}
