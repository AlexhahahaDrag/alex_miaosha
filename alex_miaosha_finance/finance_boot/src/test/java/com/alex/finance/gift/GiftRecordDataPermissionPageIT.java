package com.alex.finance.gift;

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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Gift record paging via {@code getPage} triggers {@link DataPermissionHandlerImpl} with ORG_SHARED.
 */
@ExtendWith(MockitoExtension.class)
class GiftRecordDataPermissionPageIT {

    private static final String GIFT_RECORD_GET_PAGE_MS =
            "com.alex.finance.gift.record.mapper.GiftRecordInfoMapper.getPage";

    private static final String GIFT_RECORD_GET_LIST_MS =
            "com.alex.finance.gift.record.mapper.GiftRecordInfoMapper.getList";

    private static final String FINANCE_INFO_GET_PAGE_MS =
            "com.alex.finance.finance.mapper.FinanceInfoMapper.getPage";

    @Mock
    private UserUtils userUtils;

    private DataPermissionHandlerImpl handler;

    @BeforeEach
    void setUp() {
        handler = new DataPermissionHandlerImpl(userUtils, OrgSubtreeLookup.NOOP);
    }

    @Test
    void superAdmin_should_not_append_data_permission_on_getPage() {
        when(userUtils.getLoginUser()).thenReturn(userWithRole("super_admin"));

        Expression segment = handler.getSqlSegment(null, GIFT_RECORD_GET_PAGE_MS);

        assertNull(segment);
    }

    /**
     * 注意：GiftRecordInfoMapper.xml 中 getPage/getList 的 FROM 子句使用表别名 r，
     * 注解 alias="r" 时 handler 必须用别名限定列（r.org_id），否则运行时 SQL 非法。
     */
    @Test
    void orgAdmin_should_append_org_id_filter_on_gift_getPage() {
        when(userUtils.getLoginUser()).thenReturn(userWithRole("org_admin", 20L, 100L));

        Expression segment = handler.getSqlSegment(null, GIFT_RECORD_GET_PAGE_MS);

        String sql = segment.toString();
        assertTrue(sql.contains("r.org_id"), () -> "sql=" + sql);
        assertTrue(sql.contains("20"), () -> "sql=" + sql);
        assertTrue(!sql.contains("t_org_user_info"), () -> "sql=" + sql);
    }

    @Test
    void normalUser_should_append_org_id_filter_on_gift_getPage() {
        when(userUtils.getLoginUser()).thenReturn(userWithRole("rbac_user", 20L, 10L));

        Expression segment = handler.getSqlSegment(null, GIFT_RECORD_GET_PAGE_MS);

        String sql = segment.toString();
        assertTrue(sql.contains("r.org_id"), () -> "sql=" + sql);
        assertTrue(sql.contains("20"), () -> "sql=" + sql);
        assertTrue(!sql.contains("10"), () -> "sql=" + sql);
    }

    @Test
    void user_without_org_should_degrade_to_user_scope_on_gift_getPage() {
        TUserVo user = userWithRole("rbac_user", 20L, 10L);
        user.setOrgInfoVo(null);
        user.setOrgId(null);
        when(userUtils.getLoginUser()).thenReturn(user);

        Expression segment = handler.getSqlSegment(null, GIFT_RECORD_GET_PAGE_MS);

        String sql = segment.toString();
        assertTrue(sql.contains("r.user_id"), () -> "sql=" + sql);
        assertTrue(sql.contains("10"), () -> "sql=" + sql);
    }

    @Test
    void financeInfo_should_keep_user_owner_admin_subquery() {
        when(userUtils.getLoginUser()).thenReturn(userWithRole("org_admin", 20L, 100L));

        Expression segment = handler.getSqlSegment(null, FINANCE_INFO_GET_PAGE_MS);

        String sql = segment.toString();
        assertTrue(sql.contains("finance_info"), () -> "sql=" + sql);
        assertTrue(sql.contains("belong_to"), () -> "sql=" + sql);
        assertTrue(sql.contains("t_org_user_info"), () -> "sql=" + sql);
    }

    @Test
    void getList_should_also_resolve_data_permission_annotation() {
        when(userUtils.getLoginUser()).thenReturn(userWithRole("rbac_user", 20L, 10L));

        Expression segment = handler.getSqlSegment(null, GIFT_RECORD_GET_LIST_MS);

        assertTrue(segment != null && segment.toString().contains("r.org_id"));
    }

    /**
     * sumReturnAmountByRelatedRecordId 的 XML 不带表别名，注解无 alias 时按表名限定列。
     */
    @Test
    void sumReturnAmount_should_append_table_qualified_org_filter() {
        when(userUtils.getLoginUser()).thenReturn(userWithRole("rbac_user", 20L, 10L));

        Expression segment = handler.getSqlSegment(null,
                "com.alex.finance.gift.record.mapper.GiftRecordInfoMapper.sumReturnAmountByRelatedRecordId");

        String sql = segment.toString();
        assertTrue(sql.contains("gift_record_info_t.org_id"), () -> "sql=" + sql);
        assertTrue(sql.contains("20"), () -> "sql=" + sql);
    }

    @Test
    void unannotated_mappedStatement_should_not_append_filter() {
        // BaseMapper 继承的 selectById 未声明在接口上，也未挂 @DataPermission
        Expression segment = handler.getSqlSegment(null,
                "com.alex.finance.gift.record.mapper.GiftRecordInfoMapper.selectById");

        assertNull(segment);
    }

    @Test
    void noLoginUser_should_not_append_filter() {
        when(userUtils.getLoginUser()).thenReturn(null);

        Expression segment = handler.getSqlSegment(null, GIFT_RECORD_GET_PAGE_MS);

        assertNull(segment);
    }

    private static TUserVo userWithRole(String roleCode) {
        return userWithRole(roleCode, 20L, 10L);
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
