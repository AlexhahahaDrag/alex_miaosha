package com.alex.api.user.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DataPermission {

    String table() default "t_user";

    String[] where() default {};

    String field() default "operator";

    /**
     * USER_IDS: admin uses org-member user id subquery; user uses self id.
     * ORG_ID: admin/user filter by login user's org id.
     */
    Scope scope() default Scope.USER_IDS;

    enum Scope {
        USER_IDS,
        ORG_ID
    }
}
