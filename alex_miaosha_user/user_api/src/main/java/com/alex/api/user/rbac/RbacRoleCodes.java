package com.alex.api.user.rbac;

/**
 * Shared RBAC role code constants and role classification helpers.
 */
public final class RbacRoleCodes {
    public static final String SUPER = "super_super";
    public static final String SUPER_ADMIN_ALIAS = "super_admin";
    public static final String ADMIN = "admin";
    public static final String USER = "user";

    /**
     * 判断是否为超级管理员角色（支持主编码 super_super 及兼容别名 super_admin）
     */
    public static boolean isSuperRole(String roleCode) {
        if (roleCode == null) {
            return false;
        }
        return SUPER.equals(roleCode) || SUPER_ADMIN_ALIAS.equalsIgnoreCase(roleCode);
    }

    /**
     * 判断是否为机构/业务管理员角色（匹配 admin 或以 _admin 结尾的垂直业务管理员，排除超级管理员）
     */
    public static boolean isAdminRole(String roleCode) {
        if (roleCode == null) {
            return false;
        }
        if (isSuperRole(roleCode)) {
            return false;
        }
        return ADMIN.equals(roleCode) || roleCode.endsWith("_admin") || "admin".equalsIgnoreCase(roleCode);
    }

    /**
     * 判断是否为普通用户角色（匹配 user 或以 _user 结尾的业务用户）
     */
    public static boolean isUserRole(String roleCode) {
        if (roleCode == null) {
            return false;
        }
        return USER.equals(roleCode) || roleCode.endsWith("_user");
    }

    private RbacRoleCodes() {}
}

