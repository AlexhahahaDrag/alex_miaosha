package com.alex.finance.shop;

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
 * 店铺进销存全模块 (ShopFinance / ShopStock / ShopStockBatch / ShopOrder / ShopCart)
 * 数据权限与多租户/机构隔离 JSqlParser 拦截集成测试
 */
@ExtendWith(MockitoExtension.class)
class ShopErpDataPermissionIT {

    private static final String SHOP_FINANCE_GET_PAGE_MS =
            "com.alex.finance.shopFinance.mapper.ShopFinanceMapper.getPage";

    private static final String SHOP_STOCK_GET_PAGE_MS =
            "com.alex.finance.shopStock.mapper.ShopStockMapper.getPage";

    private static final String SHOP_STOCK_GET_SHOP_LIST_MS =
            "com.alex.finance.shopStock.mapper.ShopStockMapper.getShopList";

    private static final String SHOP_ORDER_GET_PAGE_MS =
            "com.alex.finance.shopOrder.mapper.ShopOrderMapper.getPage";

    private static final String SHOP_CART_GET_PAGE_MS =
            "com.alex.finance.shopCart.mapper.ShopCartMapper.getPage";

    private static final String SHOP_CART_LIST_MS =
            "com.alex.finance.shopCart.mapper.ShopCartMapper.list";

    private static final String SHOP_STOCK_BATCH_GET_PAGE_MS =
            "com.alex.finance.shopStockBatch.mapper.ShopStockBatchMapper.getPage";

    private static final String SHOP_STOCK_BATCH_COUNT_MS =
            "com.alex.finance.shopStockBatch.mapper.ShopStockBatchMapper.countByBatchCode";

    @Mock
    private UserUtils userUtils;

    private DataPermissionHandlerImpl handler;

    @BeforeEach
    void setUp() {
        handler = new DataPermissionHandlerImpl(userUtils, OrgSubtreeLookup.NOOP);
    }

    @Test
    @DisplayName("超管(super_admin)访问店铺财务分页应跳过数据权限SQL拼接")
    void superAdmin_should_bypass_shop_finance_page() {
        when(userUtils.getLoginUser()).thenReturn(userWithRole("super_admin"));

        Expression segment = handler.getSqlSegment(null, SHOP_FINANCE_GET_PAGE_MS);

        assertNull(segment);
    }

    @Test
    @DisplayName("超管(super_super)访问店铺库存分页应跳过数据权限SQL拼接")
    void superSuper_should_bypass_shop_stock_page() {
        when(userUtils.getLoginUser()).thenReturn(userWithRole(RbacRoleCodes.SUPER));

        Expression segment = handler.getSqlSegment(null, SHOP_STOCK_GET_PAGE_MS);

        assertNull(segment);
    }

    @Test
    @DisplayName("机构管理员(org_admin)查询店铺财务分页时应拼装机构子查询隔离条件")
    void orgAdmin_should_append_org_subquery_on_shop_finance_getPage() {
        when(userUtils.getLoginUser()).thenReturn(userWithRole("org_admin", 88L, 100L));

        Expression segment = handler.getSqlSegment(null, SHOP_FINANCE_GET_PAGE_MS);

        assertNotNull(segment);
        String sql = segment.toString();
        assertTrue(sql.contains("t_shop_finance"), () -> "sql=" + sql);
        assertTrue(sql.contains("operator"), () -> "sql=" + sql);
        assertTrue(sql.contains("t_org_user_info"), () -> "sql=" + sql);
        assertTrue(sql.contains("88"), () -> "sql=" + sql);
    }

    @Test
    @DisplayName("普通用户(rbac_user)查询店铺财务分页时应精准限制为操作人本身")
    void normalUser_should_restrict_to_belong_to_user_id_on_shop_finance_getPage() {
        when(userUtils.getLoginUser()).thenReturn(userWithRole("rbac_user", 88L, 101L));

        Expression segment = handler.getSqlSegment(null, SHOP_FINANCE_GET_PAGE_MS);

        assertNotNull(segment);
        String sql = segment.toString();
        assertTrue(sql.contains("t_shop_finance"), () -> "sql=" + sql);
        assertTrue(sql.contains("operator"), () -> "sql=" + sql);
        assertTrue(sql.contains("101"), () -> "sql=" + sql);
        assertTrue(!sql.contains("t_org_user_info"), () -> "sql=" + sql);
    }

    @Test
    @DisplayName("未关联机构的普通用户应安全降级为个人操作人范围")
    void user_without_org_should_degrade_to_user_scope_on_shop_finance_getPage() {
        TUserVo user = userWithRole("rbac_user", 88L, 102L);
        user.setOrgInfoVo(null);
        user.setOrgId(null);
        when(userUtils.getLoginUser()).thenReturn(user);

        Expression segment = handler.getSqlSegment(null, SHOP_FINANCE_GET_PAGE_MS);

        assertNotNull(segment);
        String sql = segment.toString();
        assertTrue(sql.contains("t_shop_finance"), () -> "sql=" + sql);
        assertTrue(sql.contains("operator"), () -> "sql=" + sql);
        assertTrue(sql.contains("102"), () -> "sql=" + sql);
    }

    @Test
    @DisplayName("店铺库存分页与批量列表均正确应用数据权限拦截")
    void shopStock_getPage_and_getShopList_should_enforce_data_permission() {
        when(userUtils.getLoginUser()).thenReturn(userWithRole("rbac_user", 88L, 103L));

        Expression pageSegment = handler.getSqlSegment(null, SHOP_STOCK_GET_PAGE_MS);
        assertNotNull(pageSegment);
        assertTrue(pageSegment.toString().contains("t_shop_stock"));
        assertTrue(pageSegment.toString().contains("103"));

        Expression listSegment = handler.getSqlSegment(null, SHOP_STOCK_GET_SHOP_LIST_MS);
        assertNotNull(listSegment);
        assertTrue(listSegment.toString().contains("t_shop_stock"));
        assertTrue(listSegment.toString().contains("103"));
    }

    @Test
    @DisplayName("店铺销售订单分页正确应用数据权限拦截")
    void shopOrder_getPage_should_enforce_data_permission() {
        when(userUtils.getLoginUser()).thenReturn(userWithRole("rbac_user", 88L, 104L));

        Expression segment = handler.getSqlSegment(null, SHOP_ORDER_GET_PAGE_MS);

        assertNotNull(segment);
        String sql = segment.toString();
        assertTrue(sql.contains("t_shop_order"), () -> "sql=" + sql);
        assertTrue(sql.contains("104"), () -> "sql=" + sql);
    }

    @Test
    @DisplayName("购物车分页与列表均正确应用数据权限拦截")
    void shopCart_getPage_and_list_should_enforce_data_permission() {
        when(userUtils.getLoginUser()).thenReturn(userWithRole("rbac_user", 88L, 105L));

        Expression pageSegment = handler.getSqlSegment(null, SHOP_CART_GET_PAGE_MS);
        assertNotNull(pageSegment);
        assertTrue(pageSegment.toString().contains("t_shop_cart"));
        assertTrue(pageSegment.toString().contains("105"));

        Expression listSegment = handler.getSqlSegment(null, SHOP_CART_LIST_MS);
        assertNotNull(listSegment);
        assertTrue(listSegment.toString().contains("t_shop_cart"));
        assertTrue(listSegment.toString().contains("105"));
    }

    @Test
    @DisplayName("进货批次分页与编码去重统计均正确应用数据权限拦截")
    void shopStockBatch_getPage_and_countByBatchCode_should_enforce_data_permission() {
        when(userUtils.getLoginUser()).thenReturn(userWithRole("rbac_user", 88L, 106L));

        Expression pageSegment = handler.getSqlSegment(null, SHOP_STOCK_BATCH_GET_PAGE_MS);
        assertNotNull(pageSegment);
        assertTrue(pageSegment.toString().contains("t_shop_stock_batch"));
        assertTrue(pageSegment.toString().contains("106"));

        Expression countSegment = handler.getSqlSegment(null, SHOP_STOCK_BATCH_COUNT_MS);
        assertNotNull(countSegment);
        assertTrue(countSegment.toString().contains("t_shop_stock_batch"));
        assertTrue(countSegment.toString().contains("106"));
    }

    @Test
    @DisplayName("未声明@DataPermission注解的Mapper方法不追加任何SQL过滤")
    void unannotated_method_should_not_append_filter() {
        Expression segment = handler.getSqlSegment(null,
                "com.alex.finance.shopFinance.mapper.ShopFinanceMapper.selectById");

        assertNull(segment);
    }

    @Test
    @DisplayName("无登录态上下文时不拼接任何SQL过滤条件")
    void noLoginUser_should_not_append_filter() {
        when(userUtils.getLoginUser()).thenReturn(null);

        Expression segment = handler.getSqlSegment(null, SHOP_FINANCE_GET_PAGE_MS);

        assertNull(segment);
    }

    private static TUserVo userWithRole(String roleCode) {
        return userWithRole(roleCode, 88L, 10L);
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
