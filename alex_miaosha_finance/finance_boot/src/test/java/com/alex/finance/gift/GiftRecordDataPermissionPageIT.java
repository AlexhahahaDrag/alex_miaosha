package com.alex.finance.gift;

import com.alex.api.user.handler.DataPermissionHandlerImpl;
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
 * H1：礼金分�?{@code getPage} �?Mapper 自定义方法时，{@link DataPermissionHandlerImpl} 必须生效�?
 * <p>
 * 本类验证拦截器对 {@code GiftRecordInfoMapper.getPage} 注入�?SQL 片段（不依赖真实 DB）�?
 */
@ExtendWith(MockitoExtension.class)
class GiftRecordDataPermissionPageIT {

    private static final String GIFT_RECORD_GET_PAGE_MS =
            "com.alex.finance.gift.record.mapper.GiftRecordInfoMapper.getPage";

    private static final String GIFT_RECORD_GET_LIST_MS =
            "com.alex.finance.gift.record.mapper.GiftRecordInfoMapper.getList";

    @Mock
    private UserUtils userUtils;

    private DataPermissionHandlerImpl handler;

    @BeforeEach
    void setUp() {
        handler = new DataPermissionHandlerImpl(userUtils);
    }

    @Test
    void superAdmin_should_not_append_data_permission_on_getPage() {
        when(userUtils.getLoginUser()).thenReturn(userWithRole("super_admin"));

        Expression segment = handler.getSqlSegment(null, GIFT_RECORD_GET_PAGE_MS);

        assertNull(segment);
    }

    @Test
    void orgAdmin_should_append_org_scope_subquery_on_getPage() {
        when(userUtils.getLoginUser()).thenReturn(userWithRole("org_admin", 20L, 100L));

        Expression segment = handler.getSqlSegment(null, GIFT_RECORD_GET_PAGE_MS);

        String sql = segment.toString();
        assertTrue(sql.contains("gift_record_info_t"), () -> "sql=" + sql);
        assertTrue(sql.contains("user_id"), () -> "sql=" + sql);
        assertTrue(sql.contains("t_org_user_info"), () -> "sql=" + sql);
    }

    @Test
    void normalUser_should_append_scope_on_getPage() {
        when(userUtils.getLoginUser()).thenReturn(userWithRole("rbac_user", 20L, 10L));

        Expression segment = handler.getSqlSegment(null, GIFT_RECORD_GET_PAGE_MS);

        String sql = segment.toString();
        assertTrue(sql.contains("gift_record_info_t"), () -> "sql=" + sql);
        assertTrue(sql.contains("user_id"), () -> "sql=" + sql);
        assertTrue(sql.contains("10"), () -> "sql=" + sql);
    }

    @Test
    void getList_should_also_resolve_data_permission_annotation() {
        when(userUtils.getLoginUser()).thenReturn(userWithRole("rbac_user", 20L, 10L));

        Expression segment = handler.getSqlSegment(null, GIFT_RECORD_GET_LIST_MS);

        assertTrue(segment != null && segment.toString().contains("gift_record_info_t"));
    }

    @Test
    void unannotated_mappedStatement_should_not_append_filter() {
        Expression segment = handler.getSqlSegment(null,
                "com.alex.finance.gift.record.mapper.GiftRecordInfoMapper.sumReturnAmountByRelatedRecordId");

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
