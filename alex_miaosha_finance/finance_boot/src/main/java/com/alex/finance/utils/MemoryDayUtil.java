package com.alex.finance.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 纪念日工具类
 * Author:木芒果
 */
public class MemoryDayUtil {
    private static final ThreadLocal<SimpleDateFormat> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 获取SimpleDateFormat
     */
    private static SimpleDateFormat get() {
        SimpleDateFormat sdf = THREAD_LOCAL.get();
        if (sdf == null) {
            sdf = new SimpleDateFormat("yyyy-MM-dd");
            THREAD_LOCAL.set(sdf);
        }
        return sdf;
    }

    /**
     * 计算两个时间差
     */
    public static long getDatePoor(Date endDate, Date nowDate) {
        long nd = 1000 * 24 * 60 * 60;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - nowDate.getTime();
        // 计算差多少天
        long day = diff / nd;
        return day;
    }

    /**
     * 计算天数
     */
    public static long calculationLianAi(String date) {
        SimpleDateFormat simpleDateFormat = get();
        Date startDate = new Date();
        try {
            startDate = simpleDateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return getDatePoor(new Date(), startDate);
    }

    /**
     * 计算生日
     */
    public static long calculationBirthday(String birthday) {
        SimpleDateFormat simpleDateFormat = get();
        Calendar cToday = Calendar.getInstance();
        Calendar cBirth = Calendar.getInstance();
        Date now = new Date();
        try {
            now = simpleDateFormat.parse(birthday);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        cBirth.setTime(now);
        cBirth.set(Calendar.YEAR, cToday.get(Calendar.YEAR));
        int days;
        if (cBirth.get(Calendar.DAY_OF_YEAR) < cToday.get(Calendar.DAY_OF_YEAR)) {
            days = cToday.getActualMaximum(Calendar.DAY_OF_YEAR) - cToday.get(Calendar.DAY_OF_YEAR);
            days += cBirth.get(Calendar.DAY_OF_YEAR);
        } else {
            days = cBirth.get(Calendar.DAY_OF_YEAR) - cToday.get(Calendar.DAY_OF_YEAR);
        }
        return days;
    }
}