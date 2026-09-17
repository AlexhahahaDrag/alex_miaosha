package com.alex.common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *description:  限制用户点击秒杀接口的次数
 *author:       majf
 *createDate:   2022/7/15 9:49
 *version:      1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SeckillLimit {

    /**
     * description: 限制的有效期
     * author:      majf
     * createDate:  2022/7/15 9:52
     * return:      int
    */
    int seconds();

    /**
     * description: 限制的最大次数
     * author:      majf
     * createDate:  2022/7/15 9:52
     * return:      int
     */
    int maxCount();
}
