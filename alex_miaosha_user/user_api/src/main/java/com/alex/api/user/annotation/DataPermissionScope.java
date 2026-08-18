package com.alex.api.user.annotation;

/**
 * 数据权限范围策略。
 * <ul>
 *   <li>{@link #USER_OWNER} / {@link #USER_IDS}：超管不限；管理员按机构成员子查询；普通用户仅本人。</li>
 *   <li>{@link #ORG_SHARED}：超管不限；管理员与普通用户按机构共享（有 org 字段则按 org 过滤，管理员扩子孙；否则按机构成员子查询）。</li>
 *   <li>{@link #ORG_ID}：超管不限；按 {@link DataPermission#field()} 过滤登录机构，管理员扩子孙。</li>
 * </ul>
 */
public enum DataPermissionScope {

    USER_OWNER,

    /** 与 {@link #USER_OWNER} 同义，兼容 org 分支 mapper */
    USER_IDS,

    ORG_SHARED,

    ORG_ID
}
