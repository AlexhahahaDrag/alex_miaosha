package com.alex.api.user.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DataPermission {

    String table() default "t_sys_user";

    String[] where() default {"is_deleted=0", "status=1"};

    String field() default "creator";
}
