package com.alex.api.user.annotation;

/**
 * 数据权限范围策略。
 * <ul>
 *   <li>{@link #USER_OWNER}：超管不限；管理员按机构成员子查询；普通用户仅本人。</li>
 *   <li>{@link #ORG_SHARED}：超管不限；管理员与普通用户均按家庭组/机构共享（有 org 字段则按 org 过滤，否则按机构成员子查询）。</li>
 * </ul>
 */
public enum DataPermissionScope {

    USER_OWNER,

    ORG_SHARED
}
