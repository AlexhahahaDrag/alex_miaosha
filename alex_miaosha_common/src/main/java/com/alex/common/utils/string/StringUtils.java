package com.alex.common.utils.string;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *description:  对字符串的转换的一些操作
 *author:       alex
 *createDate:   2021/7/4 20:58
 *version:      1.0.0
 */
@Slf4j
public class StringUtils {

    //集群号
    private static int machineId = 1;

    //下划线正则
    private static final Pattern CAMLE_PATTERN = Pattern.compile("_(\\w)");

    //大写字母正则
    private static final Pattern UNDER_LINE_PATTERN = Pattern.compile("[A-Z]");

    //设置最大重复数
    private static final int MAX_COUNT = 4;
    /**
     * @param str
     * description:  下划线转驼峰
     * author:       alex
     * return:       java.lang.StringBuffer
    */
    public static String camel(String str) {
        Matcher matcher = CAMLE_PATTERN.matcher(str);
        StringBuffer sb = new StringBuffer();
        while(matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * @param str
     * description:  驼峰转下划线
     * author:       alex
     * return:       java.lang.StringBuffer
    */
    public static String underline(String str) {
        Matcher matcher = UNDER_LINE_PATTERN.matcher(str);
        StringBuffer sb = new StringBuffer();
        while(matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static final long getLong(String str, Long defaultData) {
        Long lnum = defaultData;
        if (isEmpty(str)) {
            return lnum;
        }
        try {
            lnum = Long.valueOf(str.trim());
        } catch (NumberFormatException e) {
            log.error("{}转化long型错误{}", str, e.getMessage());
        }
        return lnum;
    }


    /**
     * 转换成Boolean类型
     *
     * @param str
     * @param defaultData
     * return
     */
    public static Boolean getBoolean(String str, Boolean defaultData) {
        Boolean lnum = defaultData;
        if (isEmpty(str)) {
            return lnum;
        }
        try {
            lnum = Boolean.valueOf(str.trim());
        } catch (NumberFormatException e) {
            log.error("{}转化boolean型错误{}", str, e.getMessage());
        }
        return lnum;

    }

    /**
     * 把String转换成int数据
     *
     * @param str
     * @param defaultData
     * return
     */
    public static int getInt(String str, Integer defaultData) {
        int inum = defaultData;
        if (isEmpty(str)) {
            return inum;
        }
        try {
            inum = Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            log.error("{}转化int型错误{}", str, e.getMessage());
        }
        return inum;
    }

    /**
     * 把String转换成double数据
     *
     * @param str
     * @param defaultData
     * return
     */
    public static double getDouble(String str, Double defaultData) {
        double dnum = defaultData;
        if (isEmpty(str)) {
            return dnum;
        }
        try {
            dnum = Double.parseDouble(str.trim());
        } catch (NumberFormatException e) {
            log.error("{}转化double型错误{}", str, e.getMessage());
        }
        return dnum;
    }

    /**
     * 把String转换成float数据
     *
     * @param str
     * @param defaultData
     * return
     */
    public static float getFloat(String str, Float defaultData) {
        float dnum = defaultData;
        if (isEmpty(str)) {
            return dnum;
        }
        try {
            dnum = Float.parseFloat(str.trim());
        } catch (NumberFormatException e) {
            log.error("{}转化float型错误{}", str, e.getMessage());
        }
        return dnum;
    }

    /**
     * 判断字符串是否为空
     *
     * @param s
     * return
     */
    public static Boolean isEmpty(String s) {
        if (s == null || s.length() <= 0) {
            return true;
        }
        return false;
    }
    
    /**
     * @param str
     * description:  判断非空字符串  
     * author:       alex
     * return:       boolean
    */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
    
    /**
     * @param str
     * description:  判断字符串是否为空（空格也算作空值）  
     * author:       alex
     * return:       boolean
    */
    public static boolean isBlank(String str) {
        int strLen;
        if(str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if(!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * @param str
     * description:  判断字符串不为空    
     * author:       alex
     * return:       boolean
    */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
    
    /**
     * @param str
     * description:  判断是否为数字    
     * author:       alex
     * return:       boolean
    */
    public static boolean isNumeric(String str) {
        try {
            Long.valueOf(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 某个子串是否在字符串内
     *
     * @param str
     * @param searchChar
     * return
     */
    public static boolean contains(String str, String searchChar) {
        if (isEmpty(str)) {
            return false;
        }
        return str.contains(searchChar);
    }

    /**
     * 切割字符串
     *
     * @param str
     * @param start
     * return
     */
    public static String substring(String str, int start) {
        if (str == null) {
            return null;
        }
        // handle negatives, which means last n characters
        if (start < 0) {
            start = str.length() + start;
        }
        if (start < 0) {
            start = 0;
        }
        if (start > str.length()) {
            return "";
        }
        return str.substring(start);
    }

    /**
     * @param content
     * description: 判断评论是否有效   
     * author:       alex
     * return:       boolean
    */
    // TODO: 2021/7/5  添加评论内容校验 
    public static boolean isCommentSpam(String content) {
        return content == null;
    }

    /**
     * description:  获取32位的uuid
     * author:       alex
     * return:       java.lang.String
    */
    public static String getUUID() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        log.info("获取32位的uuid: {}", uuid);
        return uuid;
    }

    /**
     * @param str
     * @param code
     * description:  根据给定字符串切割字符串
     * author:       alex
     * return:       java.util.List<java.lang.String>
    */
    public static List<String> splitStringByCode(String str, String code) {
        if (str == null) {
            return null;
        }
        return Arrays.asList(str.split(code));
    }

    /**
     * @param str
     * @param code
     * description:  根据给定字符串切割字符串
     * author:       alex
     * return:       java.util.List<java.lang.String>
     */
    public static List<Long> splitLongByCode(String str, String code) {
        if (str == null) {
            return null;
        }
        List<String> res = splitStringByCode(str, code);
        return res.stream().map(item -> Long.parseLong(item)).collect(Collectors.toList());
    }

    /**
     * @param collection
     * description:  校验ids列表不为空
     * author:       alex
     * return:       boolean
    */
    public static boolean checkIdList(Collection<String> collection) {
        return collection != null && collection.size() > 0;
    }
}
