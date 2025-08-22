package com.alex.common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 日志打印注解
 *
 * @author <a href="mailto:majf@emrubik.com">hongcq</a>
 * @version 1.0 $ 2024/12/11 14:40
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogRestRequest {

    /**
     * 接口名称
     *
     * @return 接口名称
     */
    String apiName();

    /**
     *  参数列表，默认为空数组
     * @return 参数列表
     */
    String[] parameters() default {};
}
