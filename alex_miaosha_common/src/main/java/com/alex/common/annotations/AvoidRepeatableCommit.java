package com.alex.common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *description:  避免重复提交接口
 *author:       alex
 *createDate:   2022/12/9 21:33
 *version:      1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AvoidRepeatableCommit {

    /**
     * description: 指定时间内不能重复提交
     * author:      alex
     * return:      long
    */
    long timeout() default 1000;

    String message() default "";
}
