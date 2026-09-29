package com.alex.user.rbac;

import com.alex.api.ai.api.AiAnalyzeApi;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.alex.api.ai.vo.AiAnalyzeResp;
import com.alex.api.user.menuInfo.vo.MenuInfoVo;
import com.alex.api.user.roleInfo.vo.RoleAiRecommendReq;
import com.alex.api.user.roleInfo.vo.RoleAiRecommendVo;
import com.alex.base.common.Result;
import com.alex.user.menuInfo.service.MenuInfoService;
import com.alex.user.roleInfo.service.RoleAiService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RoleAiServiceTest {

    private List<MenuInfoVo> buildSampleMenus() {
        List<MenuInfoVo> list = new ArrayList<>();

        MenuInfoVo rootFinance = new MenuInfoVo();
        rootFinance.setId(100L);
        rootFinance.setName("finance");
        rootFinance.setTitle("财务中心");
        rootFinance.setParentId(0L);
        list.add(rootFinance);

        MenuInfoVo giftMenu = new MenuInfoVo();
        giftMenu.setId(101L);
        giftMenu.setName("giftRecord");
        giftMenu.setTitle("礼金记账管理");
        giftMenu.setPermissionCode("finance:gift:list");
        giftMenu.setParentId(100L);
        list.add(giftMenu);

        MenuInfoVo couponMenu = new MenuInfoVo();
        couponMenu.setId(102L);
        couponMenu.setName("couponInfo");
        couponMenu.setTitle("优惠券管理");
        couponMenu.setPermissionCode("finance:coupon:list");
        couponMenu.setParentId(100L);
        list.add(couponMenu);

        MenuInfoVo rootUser = new MenuInfoVo();
        rootUser.setId(200L);
        rootUser.setName("system");
        rootUser.setTitle("系统管理");
        rootUser.setParentId(0L);
        list.add(rootUser);

        MenuInfoVo userMenu = new MenuInfoVo();
        userMenu.setId(201L);
        userMenu.setName("userInfo");
        userMenu.setTitle("用户权限管理");
        userMenu.setPermissionCode("system:user:list");
        userMenu.setParentId(200L);
        list.add(userMenu);

        return list;
    }

    @Test
    @DisplayName("测试在没有 AI 客户端时通过规则推导财务岗位权限并自动补齐父级ID")
    void testFallbackForFinanceRole() {
        MenuInfoService menuService = mock(MenuInfoService.class);
        when(menuService.getList(any())).thenReturn(buildSampleMenus());

        RoleAiService roleAiService = new RoleAiService(menuService, null);

        RoleAiRecommendReq req = new RoleAiRecommendReq()
                .setRoleName("财务出纳")
                .setRoleCode("FINANCE_CASHIER")
                .setDescription("负责日常礼金台账、红包往来与优惠券核销");

        RoleAiRecommendVo result = roleAiService.recommendPermissions(req);

        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getRecommendedMenuIds());
        // 应包含 101(礼金), 102(优惠券), 并自动补齐父节点 100(财务中心)
        Assertions.assertTrue(result.getRecommendedMenuIds().contains("101"));
        Assertions.assertTrue(result.getRecommendedMenuIds().contains("102"));
        Assertions.assertTrue(result.getRecommendedMenuIds().contains("100"));
        Assertions.assertFalse(result.getRecommendedMenuIds().contains("201"));
        Assertions.assertTrue(result.getReasoning().contains("财务出纳"));
    }

    @Test
    @DisplayName("测试当 AI 客户端返回有效 JSON 时解析成功")
    void testAiAnalyzeApiMock() {
        MenuInfoService menuService = mock(MenuInfoService.class);
        when(menuService.getList(any())).thenReturn(buildSampleMenus());

        AiAnalyzeApi aiApi = mock(AiAnalyzeApi.class);
        AiAnalyzeResp aiResp = new AiAnalyzeResp();
        aiResp.setSummary("{\"recommendedMenuIds\":[\"201\"],\"recommendedMenuNames\":[\"用户权限管理\"],\"recommendedPermissionCodes\":[\"system:user:list\"],\"reasoning\":\"该角色仅负责用户管理\"}");
        when(aiApi.chat(any(AiAnalyzeReq.class))).thenReturn(Result.success(aiResp));

        RoleAiService roleAiService = new RoleAiService(menuService, aiApi);

        RoleAiRecommendReq req = new RoleAiRecommendReq()
                .setRoleName("HR专员")
                .setDescription("只管理用户与组织");

        RoleAiRecommendVo result = roleAiService.recommendPermissions(req);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.getRecommendedMenuIds().contains("201"));
        Assertions.assertTrue(result.getRecommendedMenuIds().contains("200")); // 父节点补全
        Assertions.assertEquals("该角色仅负责用户管理", result.getReasoning());
    }
}
