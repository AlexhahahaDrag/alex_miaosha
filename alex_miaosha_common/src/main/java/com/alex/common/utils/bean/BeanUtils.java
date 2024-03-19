package com.alex.common.utils.bean;

import com.alibaba.fastjson.JSONObject;

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
            return value.toString();
        } else if (String.class.equals(clazz)) {
            return (String) value;
        }
        return JSONObject.toJSONString(value);
    }

    public static<T> T stringToBean(String str, Class<T> clazz) {
        if (str == null || str.isEmpty() || clazz == null) {
            return null;
        }
        if (clazz == Integer.class || clazz == int.class) {
            return (T) Integer.valueOf(str);
        } else if (clazz == String.class) {
            return (T) str;
        } if (clazz == Long.class || clazz == long.class) {
            return (T) Long.valueOf(str);
        }
        return JSONObject.parseObject(str, clazz);
    }

    public static void copyProperties(Object source, Object target, String... ignoreProperties) {
        BeanUtils.copyProperties(source, target, ignoreProperties);
    }
}
