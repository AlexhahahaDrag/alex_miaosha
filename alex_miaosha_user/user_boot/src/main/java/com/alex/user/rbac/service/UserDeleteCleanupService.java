package com.alex.user.rbac.service;

/**
 * Cleanup after user soft-delete: invalidate org/role bindings and best-effort Redis session kick.
 */
public interface UserDeleteCleanupService {

    /**
     * DB invalidation + redis best-effort for one userId string.
     *
     * @param userId user id as string (matches OrgUserInfo/RoleUserInfo.userId)
     */
    void cleanupAfterUserDeleted(String userId);
}
