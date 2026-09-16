package com.alex.api.user.annotation;

/**
 * 数据权限范围策略。
 * <ul>
 *   <li>{@link #USER_OWNER} / {@link #USER_IDS}：超管不限；管理员按机构成员子查询；普通用户仅本人。</li>
 *   <li>{@link #ORG_SHARED}：超管不限；管理员与普通用户按机构共享（有 org 字段则按 org 过滤，管理员扩子孙；否则按机构成员子查询）。</li>
 *   <li>{@link #ORG_ID}：超管不限；按 {@link DataPermission#field()} 过滤登录机构，管理员扩子孙。</li>
 *   <li>{@link #ROLE_ORG_BOUND}：超管不限（含未绑定机构角色）；非超管仅可见有效绑定 org ∈ 调用方机构范围 S 的角色。</li>
 * </ul>
 */
public enum DataPermissionScope {

    USER_OWNER,

    /** 与 {@link #USER_OWNER} 同义，兼容 org 分支 mapper */
    USER_IDS,

    ORG_SHARED,

    ORG_ID,

    /**
     * 角色可见性：EXISTS 有效 {@code t_role_org_info} 且 {@code org_id ∈ S}。
     * {@link DataPermission#field()} 可忽略；过滤走绑定表而非 operator。
     */
    ROLE_ORG_BOUND
}
