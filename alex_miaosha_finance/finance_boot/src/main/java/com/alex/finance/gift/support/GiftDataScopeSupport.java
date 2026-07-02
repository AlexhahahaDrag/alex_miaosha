package com.alex.finance.gift.support;

import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.finance.gift.event.entity.GiftEventInfo;
import com.alex.finance.gift.person.entity.GiftPersonInfo;
import com.alex.finance.gift.record.entity.GiftRecordInfo;
import com.alex.finance.gift.relation.entity.GiftRelationInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Gift 模块统一数据权限支撑类。
 * <p>
 * 角色判定规则与 DataPermissionHandlerImpl 保持一致：
 *   roleCode 含 "super" → 超管，不限范围；
 *   roleCode 含 "admin" → 机构管理员，限本机构；
 *   其他（含 "user" 或无角色）→ 普通用户，限自己。
 */
@Component
@RequiredArgsConstructor
public class GiftDataScopeSupport {

    private final UserUtils userUtils;

    // ─── 登录用户 ──────────────────────────────────────────────────────────────

    /**
     * 获取当前登录用户，未登录时抛出 {@link com.alex.common.exception.FinanceException}。
     */
    public TUserVo requireLoginUser() {
        TUserVo user = userUtils.getLoginUser();
        if (user == null) {
            throw GiftExceptions.notLogin();
        }
        return user;
    }

    // ─── accessible 断言（query / update / delete / markReturned 前调用）───────

    public void assertRecordAccessible(GiftRecordInfo entity) {
        assertEntityAccessible(
                entity == null ? null : entity.getUserId(),
                entity == null ? null : entity.getOrgId(),
                "礼金记录");
    }

    public void assertPersonAccessible(GiftPersonInfo entity) {
        assertEntityAccessible(
                entity == null ? null : entity.getUserId(),
                entity == null ? null : entity.getOrgId(),
                "亲友");
    }

    public void assertEventAccessible(GiftEventInfo entity) {
        assertEntityAccessible(
                entity == null ? null : entity.getUserId(),
                entity == null ? null : entity.getOrgId(),
                "事由");
    }

    public void assertRelationAccessible(GiftRelationInfo entity) {
        assertEntityAccessible(
                entity == null ? null : entity.getUserId(),
                entity == null ? null : entity.getOrgId(),
                "关系");
    }

    // ─── 私有辅助 ──────────────────────────────────────────────────────────────

    /**
     * 断言当前登录用户有权访问该实体。
     * <p>
     * Gift 模块 Mapper 使用 {@link com.alex.api.user.annotation.DataPermissionScope#ORG_SHARED}：
     * 超管直通；其余角色按家庭组 {@code org_id} 校验（无机构时降级为本人 {@code user_id}）。
     */
    private void assertEntityAccessible(Long entityUserId, Long entityOrgId, String resourceName) {
        if (entityUserId == null && entityOrgId == null) {
            throw GiftExceptions.param(resourceName + "不存在");
        }
        TUserVo user = requireLoginUser();
        if (isSuper(user)) {
            return;
        }
        Long myOrgId = loginOrgId(user);
        if (myOrgId != null && myOrgId.equals(entityOrgId)) {
            return;
        }
        if (myOrgId == null && user.getId().equals(entityUserId)) {
            return;
        }
        if (myOrgId != null && entityOrgId == null && user.getId().equals(entityUserId)) {
            return;
        }
        throw GiftExceptions.forbidden("无权访问其他机构的" + resourceName);
    }

    /** roleCode 含 "super" → 超管。 */
    public boolean isSuper(TUserVo user) {
        return hasRoleContaining(user, "super");
    }

    /** roleCode 含 "admin"（且不含 "super"） → 机构管理员。 */
    public boolean isAdmin(TUserVo user) {
        return !isSuper(user) && hasRoleContaining(user, "admin");
    }

    private boolean hasRoleContaining(TUserVo user, String keyword) {
        List<RoleInfoVo> roles = user.getRoleInfoVoList();
        if (roles == null) {
            return false;
        }
        return roles.stream()
                .filter(r -> r != null && r.getRoleCode() != null)
                .anyMatch(r -> r.getRoleCode().contains(keyword));
    }

    /** 取登录用户当前有效机构 ID（优先 orgInfoVo.id，其次 orgId）。 */
    public Long loginOrgId(TUserVo user) {
        OrgInfoVo orgInfoVo = user.getOrgInfoVo();
        return orgInfoVo != null ? orgInfoVo.getId() : user.getOrgId();
    }
}
