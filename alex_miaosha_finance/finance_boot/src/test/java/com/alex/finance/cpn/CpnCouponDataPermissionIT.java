package com.alex.finance.cpn;

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
 * 消费券全模块 (CpnCouponInfo / CpnUserCouponInfo / CpnRedemptionRecordInfo)
 * 数据权限与多租户/机构隔离 JSqlParser 拦截集成测试
 */
@ExtendWith(MockitoExtension.class)
class CpnCouponDataPermissionIT {

    private static final String CPN_COUPON_GET_PAGE_MS =
            "com.alex.finance.cpnCouponInfo.mapper.CpnCouponInfoMapper.getPage";

    private static final String CPN_USER_COUPON_GET_PAGE_MS =
            "com.alex.finance.cpnUserCouponInfo.mapper.CpnUserCouponInfoMapper.getPage";

    private static final String CPN_REDEMPTION_RECORD_GET_PAGE_MS =
            "com.alex.finance.cpnRedemptionRecordInfo.mapper.CpnRedemptionRecordInfoMapper.getPage";

    @Mock
    private UserUtils userUtils;

    private DataPermissionHandlerImpl handler;

    @BeforeEach
    void setUp() {
        handler = new DataPermissionHandlerImpl(userUtils, OrgSubtreeLookup.NOOP);
    }

    @Test
    @DisplayName("超级管理员访问消费券分页查询跳过数据权限限制")
    void superAdmin_should_bypass_cpn_coupon_page() {
        when(userUtils.getLoginUser()).thenReturn(userWithRole("super_admin"));

        Expression segment = handler.getSqlSegment(null, CPN_COUPON_GET_PAGE_MS);

        assertNull(segment);
    }

    @Test
    @DisplayName("超级租户超管(super_super)访问消费券分页查询跳过限制")
    void superSuper_should_bypass_cpn_coupon_page() {
        when(userUtils.getLoginUser()).thenReturn(userWithRole(RbacRoleCodes.SUPER));

        Expression segment = handler.getSqlSegment(null, CPN_COUPON_GET_PAGE_MS);

        assertNull(segment);
    }

    @Test
    @DisplayName("机构管理员查询消费券分页时应拼接机构归属子查询过滤")
    void orgAdmin_should_append_org_subquery_on_coupon() {
        when(userUtils.getLoginUser()).thenReturn(userWithRole("org_admin", 66L, 200L));

        Expression segment = handler.getSqlSegment(null, CPN_COUPON_GET_PAGE_MS);

        assertNotNull(segment);
        String sql = segment.toString();
        assertTrue(sql.contains("cpn_coupon_info_t"), () -> "sql=" + sql);
        assertTrue(sql.contains("t_org_user_info"), () -> "sql=" + sql);
        assertTrue(sql.contains("66"), () -> "sql=" + sql);
    }

    @Test
    @DisplayName("普通用户查询消费券分页时应限制为本人数据")
    void normalUser_should_restrict_to_user_id() {
        when(userUtils.getLoginUser()).thenReturn(userWithRole("rbac_user", 66L, 201L));

        Expression segment = handler.getSqlSegment(null, CPN_COUPON_GET_PAGE_MS);

        assertNotNull(segment);
        String sql = segment.toString();
        assertTrue(sql.contains("cpn_coupon_info_t"), () -> "sql=" + sql);
        assertTrue(sql.contains("201"), () -> "sql=" + sql);
        assertTrue(!sql.contains("t_org_user_info"), () -> "sql=" + sql);
    }

    @Test
    @DisplayName("用户领券记录与核销流水台账均正确应用数据权限拦截")
    void userCoupon_and_redemptionRecord_should_enforce_permission() {
        when(userUtils.getLoginUser()).thenReturn(userWithRole("rbac_user", 66L, 202L));

        Expression userCouponSegment = handler.getSqlSegment(null, CPN_USER_COUPON_GET_PAGE_MS);
        assertNotNull(userCouponSegment);
        assertTrue(userCouponSegment.toString().contains("cpn_user_coupon_info_t"));
        assertTrue(userCouponSegment.toString().contains("202"));

        Expression recordSegment = handler.getSqlSegment(null, CPN_REDEMPTION_RECORD_GET_PAGE_MS);
        assertNotNull(recordSegment);
        assertTrue(recordSegment.toString().contains("cpn_redemption_record_info_t"));
        assertTrue(recordSegment.toString().contains("202"));
    }

    @Test
    @DisplayName("未配置注解的方法不追加SQL条件")
    void unannotated_method_should_not_append_filter() {
        Expression segment = handler.getSqlSegment(null,
                "com.alex.finance.cpnCouponInfo.mapper.CpnCouponInfoMapper.selectById");

        assertNull(segment);
    }

    @Test
    @DisplayName("未登录时返回null避免SQL被错误拼接")
    void no_login_user_should_return_null() {
        when(userUtils.getLoginUser()).thenReturn(null);

        Expression segment = handler.getSqlSegment(null, CPN_COUPON_GET_PAGE_MS);

        assertNull(segment);
    }

    private static TUserVo userWithRole(String roleCode) {
        return userWithRole(roleCode, 66L, 20L);
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
