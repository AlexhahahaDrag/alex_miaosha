package com.alex.api.user.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DataPermission {

    String table() default "t_user";

    String[] where() default {};

    String field() default "operator";
}
