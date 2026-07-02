package com.alex.finance.gift;

import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.finance.gift.person.entity.GiftPersonInfo;
import com.alex.finance.gift.support.GiftDataScopeSupport;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GiftDataScopeOrgSharedTest {

    @Test
    void sameOrgUser_can_access_other_users_person() {
        GiftDataScopeSupport support = new GiftDataScopeSupport(loginUser(10L, 20L, "rbac_user"));
        GiftPersonInfo person = new GiftPersonInfo();
        person.setUserId(99L);
        person.setOrgId(20L);

        assertDoesNotThrow(() -> support.assertPersonAccessible(person));
    }

    @Test
    void crossOrgUser_cannot_access_person() {
        GiftDataScopeSupport support = new GiftDataScopeSupport(loginUser(10L, 20L, "rbac_user"));
        GiftPersonInfo person = new GiftPersonInfo();
        person.setUserId(99L);
        person.setOrgId(30L);

        assertThrows(Exception.class, () -> support.assertPersonAccessible(person));
    }

    @Test
    void user_without_org_can_access_own_person_only() {
        GiftDataScopeSupport support = new GiftDataScopeSupport(loginUser(10L, null, "rbac_user"));
        GiftPersonInfo own = new GiftPersonInfo();
        own.setUserId(10L);
        own.setOrgId(null);

        GiftPersonInfo other = new GiftPersonInfo();
        other.setUserId(11L);
        other.setOrgId(null);

        assertDoesNotThrow(() -> support.assertPersonAccessible(own));
        assertThrows(Exception.class, () -> support.assertPersonAccessible(other));
    }

    private UserUtils loginUser(Long userId, Long orgId, String roleCode) {
        UserUtils userUtils = mock(UserUtils.class);
        TUserVo user = new TUserVo();
        user.setId(userId);
        if (orgId != null) {
            OrgInfoVo org = new OrgInfoVo();
            org.setId(orgId);
            user.setOrgInfoVo(org);
            user.setOrgId(orgId);
        }
        RoleInfoVo role = new RoleInfoVo();
        role.setRoleCode(roleCode);
        user.setRoleInfoVoList(List.of(role));
        when(userUtils.getLoginUser()).thenReturn(user);
        return userUtils;
    }
}
