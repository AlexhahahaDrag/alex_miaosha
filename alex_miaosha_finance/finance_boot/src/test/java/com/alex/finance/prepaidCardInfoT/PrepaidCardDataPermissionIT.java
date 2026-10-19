package com.alex.finance.prepaidCardInfoT;

import com.alex.api.user.handler.DataPermissionHandlerImpl;
import com.alex.api.user.handler.OrgSubtreeLookup;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.rbac.RbacRoleCodes;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.userInfo.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import net.sf.jsqlparser.expression.Expression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * 储值预付卡主数据与交易流水模块
 * 数据权限与多租户/机构隔离 JSqlParser 拦截集成测试
 */
@ExtendWith(MockitoExtension.class)
class PrepaidCardDataPermissionIT {

    private static final String PREPAID_CARD_GET_PAGE_MS =
            "com.alex.finance.prepaidCardInfoT.mapper.PrepaidCardInfoTMapper.getPage";

    private static final String PREPAID_CONSUME_RECORD_GET_PAGE_MS =
            "com.alex.finance.prepaidConsumeRecordT.mapper.PrepaidConsumeRecordTMapper.getPage";

    @Mock
    private UserUtils userUtils;

    private DataPermissionHandlerImpl handler;

    @BeforeEach
    void setUp() {
        handler = new DataPermissionHandlerImpl(userUtils, OrgSubtreeLookup.NOOP);
    }

    @Test
    @DisplayName("超级管理员(super_admin)访问预付卡与交易记录分页应跳过数据权限限制")
    void superAdmin_should_bypass_prepaid_card_and_record_page() {
        when(userUtils.getLoginUser()).thenReturn(userWithRole("super_admin"));

        Expression cardSegment = handler.getSqlSegment(null, PREPAID_CARD_GET_PAGE_MS);
        assertNull(cardSegment);

        Expression recordSegment = handler.getSqlSegment(null, PREPAID_CONSUME_RECORD_GET_PAGE_MS);
        assertNull(recordSegment);
    }

    @Test
    @DisplayName("超级租户超管(super_super)访问预付卡分页应跳过限制")
    void superSuper_should_bypass_prepaid_card_page() {
        when(userUtils.getLoginUser()).thenReturn(userWithRole(RbacRoleCodes.SUPER));

        Expression segment = handler.getSqlSegment(null, PREPAID_CARD_GET_PAGE_MS);

        assertNull(segment);
    }

    @Test
    @DisplayName("机构管理员(org_admin)查询预付卡分页时应拼装机构归属子查询过滤")
    void orgAdmin_should_append_org_subquery_on_prepaid_card() {
        when(userUtils.getLoginUser()).thenReturn(userWithRole("org_admin", 77L, 300L));

        Expression segment = handler.getSqlSegment(null, PREPAID_CARD_GET_PAGE_MS);

        assertNotNull(segment);
        String sql = segment.toString();
        assertTrue(sql.contains("prepaid_card_info_t"), () -> "sql=" + sql);
        assertTrue(sql.contains("t_org_user_info"), () -> "sql=" + sql);
        assertTrue(sql.contains("77"), () -> "sql=" + sql);
    }

    @Test
    @DisplayName("普通用户(rbac_user)查询预付卡分页时应精准限制为操作人本身")
    void normalUser_should_restrict_to_login_user_id() {
        when(userUtils.getLoginUser()).thenReturn(userWithRole("rbac_user", 77L, 301L));

        Expression cardSegment = handler.getSqlSegment(null, PREPAID_CARD_GET_PAGE_MS);
        assertNotNull(cardSegment);
        String cardSql = cardSegment.toString();
        assertTrue(cardSql.contains("prepaid_card_info_t"), () -> "sql=" + cardSql);
        assertTrue(cardSql.contains("301"), () -> "sql=" + cardSql);
        assertTrue(!cardSql.contains("t_org_user_info"), () -> "sql=" + cardSql);

        Expression recordSegment = handler.getSqlSegment(null, PREPAID_CONSUME_RECORD_GET_PAGE_MS);
        assertNotNull(recordSegment);
        String recordSql = recordSegment.toString();
        assertTrue(recordSql.contains("prepaid_consume_record_t"), () -> "sql=" + recordSql);
        assertTrue(recordSql.contains("301"), () -> "sql=" + recordSql);
    }

    @Test
    @DisplayName("未关联机构的普通用户应安全降级为个人操作人范围")
    void user_without_org_should_degrade_cleanly() {
        TUserVo user = userWithRole("rbac_user", 77L, 302L);
        user.setOrgInfoVo(null);
        user.setOrgId(null);
        when(userUtils.getLoginUser()).thenReturn(user);

        Expression segment = handler.getSqlSegment(null, PREPAID_CARD_GET_PAGE_MS);

        assertNotNull(segment);
        String sql = segment.toString();
        assertTrue(sql.contains("prepaid_card_info_t"), () -> "sql=" + sql);
        assertTrue(sql.contains("302"), () -> "sql=" + sql);
    }

    @Test
    @DisplayName("未声明注解的方法不追加过滤SQL")
    void unannotated_method_should_not_append_filter() {
        Expression segment = handler.getSqlSegment(null,
                "com.alex.finance.prepaidCardInfoT.mapper.PrepaidCardInfoTMapper.selectById");

        assertNull(segment);
    }

    @Test
    @DisplayName("无登录态时不拼接任何SQL条件")
    void no_login_user_should_return_null() {
        when(userUtils.getLoginUser()).thenReturn(null);

        Expression segment = handler.getSqlSegment(null, PREPAID_CARD_GET_PAGE_MS);

        assertNull(segment);
    }

    private static TUserVo userWithRole(String roleCode) {
        return userWithRole(roleCode, 77L, 30L);
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
