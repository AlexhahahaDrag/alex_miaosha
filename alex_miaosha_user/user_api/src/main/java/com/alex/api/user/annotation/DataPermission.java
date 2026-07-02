package com.alex.api.user.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DataPermission {

    String table() default "t_user";

    String[] where() default {};

    /** 用户维度字段，如 user_id / operator / belong_to */
    String field() default "operator";

    /**
     * 机构维度字段；{@link DataPermissionScope#ORG_SHARED} 时优先使用。
     * 表无 org 字段时请显式置为 {@code ""}，将退化为 {@link #field()} 的机构成员子查询。
     */
    String orgField() default "org_id";

    DataPermissionScope scope() default DataPermissionScope.USER_OWNER;
}
