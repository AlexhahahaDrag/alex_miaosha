package com.alex.common.annotations.user;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description:  限制访问注解
 * @author:       majf
 * @createDate:   2022/8/8 17:46
 * @version:      1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AccessLimit {

    int limit() default 3;

    int timeout() default 1;
}
