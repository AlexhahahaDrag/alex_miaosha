package com.alex.user.rbac.service;

import java.util.Collection;

/**
 * RBAC-BE-RELATION-002 / RBAC-BE-SCOPE-004:
 * permission_context 缓存的统一主动失效入口。
 * <p>
 * 背景：{@code UserPermissionContextService.buildContext()} 构建的结果会写入 Redis
 * （key = {@code LoginKey.loginKey} + "permission_context:" + userId），TTL 固定 1 小时。
 * 机构/角色/权限的写路径（assignSingleOrg、assignRoles、assignPermissions、级联删除等）
 * 必须在“变更成功”这一侧主动调用本服务失效受影响用户的缓存——TTL 只是兜底，不能作为
 * 唯一的失效手段，否则变更后最长 1 小时内数据权限仍按旧上下文过滤（越权或看不到本该可见的数据）。
 * <p>
 * 实现要求 try/catch 不上抛：缓存清理失败只记录日志，绝不能反过来影响已经成功的主业务写操作。
 */
public interface PermissionContextCacheService {

    /**
     * 失效单个用户的 permission_context 缓存。userId 为 null 时静默跳过。
     *
     * @param userId 用户 id
     */
    void invalidate(Long userId);

    /**
     * 批量失效。userIds 为 null/空时静默跳过；内部逐个失效，单个失败不影响其它 userId。
     *
     * @param userIds 用户 id 集合
     */
    void invalidateAll(Collection<Long> userIds);
}
