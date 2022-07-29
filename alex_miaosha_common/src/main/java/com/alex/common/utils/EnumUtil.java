package com.alex.common.utils;

import com.alex.base.enums.BaseEnum;

/**
 *description:  枚举工具类
 *author:       majf
 *createDate:   2022/7/12 17:13
 *version:      1.0.0
 */
public class EnumUtil {

    public static<T extends BaseEnum> T getByCode(String code, Class<T> enumClass) {
        for (T t : enumClass.getEnumConstants()) {
            if (code.equals(t.getCode())) {
                return t;
            }
        }
        return null;
    }
}
