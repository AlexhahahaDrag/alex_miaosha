package com.alex.common.utils;

import cn.hutool.json.JSONUtil;

/**
 *description:  bean string 转化工具
 *author:       majf
 *createDate:   2022/7/11 17:43
 *version:      1.0.0
 */
public class BeanUtils {

    public static<T> String beanToString(T value) {
        if (value == null) {
            return null;
        }
        Class<?> clazz = value.getClass();
        if (Integer.class.equals(clazz) || Long.class.equals(clazz)) {
            return "" + value;
        } else if (String.class.equals(clazz)) {
            return (String) value;
        }
        return JSONUtil.toJsonStr(value);
    }

    public static<T> T stringToBean(String str, Class<T> clazz) {
        if (str == null || str.length() == 0 || clazz == null) {
            return null;
        }
        if (clazz == Integer.class || clazz == int.class) {
            return (T) Integer.valueOf(str);
        } else if (clazz == String.class) {
            return (T) str;
        } if (clazz == Long.class || clazz == long.class) {
            return (T) Long.valueOf(str);
        }
        return JSONUtil.toBean(str, clazz);
    }
}
