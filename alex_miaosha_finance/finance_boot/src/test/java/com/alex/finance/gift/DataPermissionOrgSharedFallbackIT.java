package com.alex.finance.gift;

import com.alex.api.user.annotation.DataPermission;
import com.alex.api.user.annotation.DataPermissionScope;
import com.alex.api.user.handler.DataPermissionHandlerImpl;
import com.alex.api.user.handler.OrgSubtreeLookup;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import net.sf.jsqlparser.expression.Expression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataPermissionOrgSharedFallbackIT {

    private static final String TEST_MS =
            "com.alex.finance.gift.DataPermissionOrgSharedFallbackIT$TestMapper.probe";

    @Mock
    private UserUtils userUtils;

    private DataPermissionHandlerImpl handler;

    @BeforeEach
    void setUp() {
        handler = new DataPermissionHandlerImpl(userUtils, OrgSubtreeLookup.NOOP);
    }

    @Test
    void orgShared_without_orgField_should_use_org_member_subquery() {
        when(userUtils.getLoginUser()).thenReturn(userWithRole("rbac_user", 20L, 10L));

        Expression segment = handler.getSqlSegment(null, TEST_MS);

        String sql = segment.toString();
        assertTrue(sql.contains("finance_info"), () -> "sql=" + sql);
        assertTrue(sql.contains("belong_to"), () -> "sql=" + sql);
        assertTrue(sql.contains("t_org_user_info"), () -> "sql=" + sql);
    }

    interface TestMapper {
        @DataPermission(
                table = "finance_info",
                field = "belong_to",
                orgField = "",
                scope = DataPermissionScope.ORG_SHARED
        )
        void probe();
    }

    private static TUserVo userWithRole(String roleCode, Long orgId, Long userId) {
        RoleInfoVo role = new RoleInfoVo();
        role.setRoleCode(roleCode);
        OrgInfoVo org = new OrgInfoVo();
        org.setId(orgId);
        TUserVo user = new TUserVo();
        user.setId(userId);
        user.setOrgId(orgId);
        user.setOrgInfoVo(org);
        user.setRoleInfoVoList(List.of(role));
        return user;
    }
}
