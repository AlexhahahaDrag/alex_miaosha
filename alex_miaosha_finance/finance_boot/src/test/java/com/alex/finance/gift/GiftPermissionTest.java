package com.alex.finance.gift;

import com.alex.api.finance.gift.record.vo.GiftRecordInfoVo;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.userInfo.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.common.exception.FinanceException;
import com.alex.finance.gift.event.mapper.GiftEventInfoMapper;
import com.alex.finance.gift.person.mapper.GiftPersonInfoMapper;
import com.alex.finance.gift.record.entity.GiftRecordInfo;
import com.alex.finance.gift.record.mapper.GiftRecordInfoMapper;
import com.alex.finance.gift.record.service.impl.GiftRecordInfoServiceImp;
import com.alex.finance.gift.support.GiftDataScopeSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * 礼尚往来权限矩阵自动化测试（4 身份 × 5 操作 = 20 用例）。
 * <p>
 * 4 身份（Persona）：
 *   1. 超级管理员 (SUPER_ADMIN: role="super_super")
 *   2. 机构管理员 (ORG_ADMIN: role="admin", 同机构)
 *   3. 普通用户本人 (NORMAL_USER_SELF: role="user", 同机构且为创建人)
 *   4. 跨租户/跨机构用户 (OTHER_TENANT_USER: role="user", 异机构)
 * <p>
 * 5 核心操作（Operation）：
 *   1. queryGiftRecordInfo (单条详情查询)
 *   2. addGiftRecordInfo (还礼关联已有收礼记录)
 *   3. updateGiftRecordInfo (修改礼金记录)
 *   4. deleteGiftRecordInfo (删除礼金记录)
 *   5. markReturned (标记已还礼)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("礼尚往来权限矩阵集成测试 (4 身份 × 5 操作 = 20 Case)")
class GiftPermissionTest {

    private static final Long ORG_A = 100L;
    private static final Long ORG_B = 200L;

    private static final Long USER_SUPER = 1L;
    private static final Long USER_ADMIN_A = 2L;
    private static final Long USER_SELF_A = 3L;
    private static final Long USER_OTHER_B = 4L;

    private static final Long TARGET_RECORD_ID = 50L;

    @Mock
    private GiftRecordInfoMapper giftRecordInfoMapper;
    @Mock
    private GiftPersonInfoMapper giftPersonInfoMapper;
    @Mock
    private GiftEventInfoMapper giftEventInfoMapper;
    @Mock
    private UserUtils userUtils;

    private GiftRecordInfoServiceImp service;
    private GiftRecordInfo targetReceiveRecord;

    @BeforeEach
    void setUp() {
        GiftDataScopeSupport dataScopeSupport = new GiftDataScopeSupport(userUtils);
        GiftRecordInfoServiceImp rawService = new GiftRecordInfoServiceImp(
                dataScopeSupport,
                giftPersonInfoMapper,
                giftEventInfoMapper,
                null,
                null);
        service = spy(rawService);
        ReflectionTestUtils.setField(service, "baseMapper", giftRecordInfoMapper);

        // 目标资源：机构 A 中普通用户 USER_SELF_A 创建的收礼记录
        targetReceiveRecord = new GiftRecordInfo();
        targetReceiveRecord.setId(TARGET_RECORD_ID);
        targetReceiveRecord.setOrgId(ORG_A);
        targetReceiveRecord.setUserId(USER_SELF_A);
        targetReceiveRecord.setDirection("RECEIVE");
        targetReceiveRecord.setAmount(new BigDecimal("600"));
        targetReceiveRecord.setReturnedFlag(0);

        lenient().when(giftRecordInfoMapper.selectById(TARGET_RECORD_ID)).thenReturn(targetReceiveRecord);
        lenient().when(service.getById(TARGET_RECORD_ID)).thenReturn(targetReceiveRecord);
        lenient().when(giftRecordInfoMapper.updateById((GiftRecordInfo) any())).thenReturn(1);
        lenient().when(giftRecordInfoMapper.insert(any(GiftRecordInfo.class))).thenAnswer(inv -> {
            GiftRecordInfo r = inv.getArgument(0);
            r.setId(999L);
            return 1;
        });
        lenient().doReturn(true).when(service).removeBatchByIds(any());
    }

    private TUserVo buildUser(Long userId, Long orgId, String roleCode) {
        TUserVo user = new TUserVo();
        user.setId(userId);
        user.setOrgId(orgId);
        if (orgId != null) {
            OrgInfoVo org = new OrgInfoVo();
            org.setId(orgId);
            user.setOrgInfoVo(org);
        }
        if (roleCode != null) {
            RoleInfoVo role = new RoleInfoVo();
            role.setRoleCode(roleCode);
            user.setRoleInfoVoList(List.of(role));
        }
        return user;
    }

    private GiftRecordInfoVo buildUpdateVo() {
        GiftRecordInfoVo vo = new GiftRecordInfoVo();
        vo.setId(TARGET_RECORD_ID);
        vo.setDirection("RECEIVE");
        vo.setAmount(new BigDecimal("666"));
        return vo;
    }

    private GiftRecordInfoVo buildReturnAddVo() {
        GiftRecordInfoVo vo = new GiftRecordInfoVo();
        vo.setDirection("RETURN");
        vo.setRelatedRecordId(TARGET_RECORD_ID);
        vo.setAmount(new BigDecimal("200"));
        return vo;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 身份 1：超级管理员 (SUPER_ADMIN)
    // ──────────────────────────────────────────────────────────────────────────
        @Test
    @DisplayName("权限矩阵测试套件初始化校验")
    void testPermissionSuiteInit() {
        assertNotNull(service);
    }

@Nested
    @DisplayName("身份 1: 超级管理员 (super_super)")
    class SuperAdminTests {

        @BeforeEach
        void asSuperAdmin() {
            when(userUtils.getLoginUser()).thenReturn(buildUser(USER_SUPER, ORG_A, "super_super"));
        }

        @Test
        @DisplayName("1.1 queryGiftRecordInfo - 超管允许跨用户跨机构查详情")
        void super_can_query() {
            GiftRecordInfoVo vo = service.queryGiftRecordInfo(TARGET_RECORD_ID);
            assertNotNull(vo);
            assertEquals(TARGET_RECORD_ID, vo.getId());
        }

        @Test
        @DisplayName("1.2 addGiftRecordInfo (RETURN) - 超管允许关联任意收礼记录还礼")
        void super_can_add_return() {
            GiftRecordInfoVo saved = service.addGiftRecordInfo(buildReturnAddVo());
            assertNotNull(saved);
        }

        @Test
        @DisplayName("1.3 updateGiftRecordInfo - 超管允许修改任意礼金记录")
        void super_can_update() {
            Boolean ok = service.updateGiftRecordInfo(buildUpdateVo());
            assertTrue(ok);
        }

        @Test
        @DisplayName("1.4 deleteGiftRecordInfo - 超管允许删除任意礼金记录")
        void super_can_delete() {
            Boolean ok = service.deleteGiftRecordInfo(String.valueOf(TARGET_RECORD_ID));
            assertTrue(ok);
        }

        @Test
        @DisplayName("1.5 markReturned - 超管允许标记任意收礼记录已还")
        void super_can_mark_returned() {
            Boolean ok = service.markReturned(TARGET_RECORD_ID);
            assertTrue(ok);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 身份 2：机构管理员 (ORG_ADMIN - 同机构)
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("身份 2: 机构管理员 (admin - 机构A)")
    class OrgAdminTests {

        @BeforeEach
        void asOrgAdmin() {
            when(userUtils.getLoginUser()).thenReturn(buildUser(USER_ADMIN_A, ORG_A, "admin"));
        }

        @Test
        @DisplayName("2.1 queryGiftRecordInfo - 机构管理员允许查本机构其他成员记录")
        void org_admin_can_query() {
            GiftRecordInfoVo vo = service.queryGiftRecordInfo(TARGET_RECORD_ID);
            assertNotNull(vo);
            assertEquals(TARGET_RECORD_ID, vo.getId());
        }

        @Test
        @DisplayName("2.2 addGiftRecordInfo (RETURN) - 机构管理员允许关联本机构收礼记录还礼")
        void org_admin_can_add_return() {
            GiftRecordInfoVo saved = service.addGiftRecordInfo(buildReturnAddVo());
            assertNotNull(saved);
        }

        @Test
        @DisplayName("2.3 updateGiftRecordInfo - 机构管理员允许修改本机构其他成员记录")
        void org_admin_can_update() {
            Boolean ok = service.updateGiftRecordInfo(buildUpdateVo());
            assertTrue(ok);
        }

        @Test
        @DisplayName("2.4 deleteGiftRecordInfo - 机构管理员允许删除本机构其他成员记录")
        void org_admin_can_delete() {
            Boolean ok = service.deleteGiftRecordInfo(String.valueOf(TARGET_RECORD_ID));
            assertTrue(ok);
        }

        @Test
        @DisplayName("2.5 markReturned - 机构管理员允许标记本机构其他成员记录已还")
        void org_admin_can_mark_returned() {
            Boolean ok = service.markReturned(TARGET_RECORD_ID);
            assertTrue(ok);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 身份 3：普通用户本人 (NORMAL_USER_SELF - 机构A 且为所有者)
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("身份 3: 普通用户本人 (user - 本人创建)")
    class NormalUserSelfTests {

        @BeforeEach
        void asNormalUserSelf() {
            when(userUtils.getLoginUser()).thenReturn(buildUser(USER_SELF_A, ORG_A, "user"));
        }

        @Test
        @DisplayName("3.1 queryGiftRecordInfo - 本人允许查自己创建的记录")
        void self_can_query() {
            GiftRecordInfoVo vo = service.queryGiftRecordInfo(TARGET_RECORD_ID);
            assertNotNull(vo);
            assertEquals(TARGET_RECORD_ID, vo.getId());
        }

        @Test
        @DisplayName("3.2 addGiftRecordInfo (RETURN) - 本人允许关联自己的收礼记录还礼")
        void self_can_add_return() {
            GiftRecordInfoVo saved = service.addGiftRecordInfo(buildReturnAddVo());
            assertNotNull(saved);
        }

        @Test
        @DisplayName("3.3 updateGiftRecordInfo - 本人允许修改自己的礼金记录")
        void self_can_update() {
            Boolean ok = service.updateGiftRecordInfo(buildUpdateVo());
            assertTrue(ok);
        }

        @Test
        @DisplayName("3.4 deleteGiftRecordInfo - 本人允许删除自己的礼金记录")
        void self_can_delete() {
            Boolean ok = service.deleteGiftRecordInfo(String.valueOf(TARGET_RECORD_ID));
            assertTrue(ok);
        }

        @Test
        @DisplayName("3.5 markReturned - 本人允许标记自己的收礼记录已还")
        void self_can_mark_returned() {
            Boolean ok = service.markReturned(TARGET_RECORD_ID);
            assertTrue(ok);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 身份 4：跨租户/跨机构用户 (OTHER_TENANT_USER - 机构B 非法访问机构A数据)
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("身份 4: 跨机构非法用户 (user - 机构B越权访问机构A记录)")
    class OtherTenantUserTests {

        @BeforeEach
        void asOtherTenantUser() {
            when(userUtils.getLoginUser()).thenReturn(buildUser(USER_OTHER_B, ORG_B, "user"));
        }

        @Test
        @DisplayName("4.1 queryGiftRecordInfo - 跨机构访问详情拦截 403")
        void other_cannot_query() {
            FinanceException ex = assertThrows(FinanceException.class, () -> service.queryGiftRecordInfo(TARGET_RECORD_ID));
            assertEquals("无权访问其他机构的礼金记录", ex.getMsg());
        }

        @Test
        @DisplayName("4.2 addGiftRecordInfo (RETURN) - 跨机构关联他人收礼还礼拦截 403 (H7)")
        void other_cannot_add_return() {
            FinanceException ex = assertThrows(FinanceException.class, () -> service.addGiftRecordInfo(buildReturnAddVo()));
            assertEquals("无权访问其他机构的礼金记录", ex.getMsg());
        }

        @Test
        @DisplayName("4.3 updateGiftRecordInfo - 跨机构修改他人记录拦截 403 (H3)")
        void other_cannot_update() {
            FinanceException ex = assertThrows(FinanceException.class, () -> service.updateGiftRecordInfo(buildUpdateVo()));
            assertEquals("无权访问其他机构的礼金记录", ex.getMsg());
        }

        @Test
        @DisplayName("4.4 deleteGiftRecordInfo - 跨机构删除他人记录拦截 403 (H4)")
        void other_cannot_delete() {
            FinanceException ex = assertThrows(FinanceException.class, () -> service.deleteGiftRecordInfo(String.valueOf(TARGET_RECORD_ID)));
            assertEquals("无权访问其他机构的礼金记录", ex.getMsg());
        }

        @Test
        @DisplayName("4.5 markReturned - 跨机构标记他人收礼记录拦截 403 (H2)")
        void other_cannot_mark_returned() {
            FinanceException ex = assertThrows(FinanceException.class, () -> service.markReturned(TARGET_RECORD_ID));
            assertEquals("无权访问其他机构的礼金记录", ex.getMsg());
        }
    }
}
