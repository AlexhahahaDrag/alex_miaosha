package com.alex.api.user.handler;

import java.util.Collections;
import java.util.List;

/**
 * RBAC-BE-SCOPE-002: pluggable lookup for an org's descendant ids.
 * {@link DataPermissionHandlerImpl} lives in user_api and must stay DB-agnostic,
 * so the real (mapper-backed) implementation is provided as a Spring bean in
 * user_boot; tests supply a lightweight fake instead.
 */
public interface OrgSubtreeLookup {

    /**
     * @param orgId 机构 id，不含自身
     * @return 全部子孙机构 id（不含自身），无子孙或查询失败时返回空列表
     */
    List<Long> findDescendantOrgIds(Long orgId);

    /**
     * 默认空实现：不扩展任何子孙范围，用于未接入机构子树能力的微服务（product/oss/finance）。
     */
    OrgSubtreeLookup NOOP = orgId -> Collections.emptyList();
}
