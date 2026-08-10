package com.alex.user.rbac;

import com.alex.api.user.orgUserInfo.vo.OrgUserInfoVo;
import com.alex.base.constants.SysConf;
import com.alex.common.exception.SystemException;
import com.alex.user.orgUserInfo.entity.OrgUserInfo;
import com.alex.user.orgUserInfo.mapper.OrgUserInfoMapper;
import com.alex.user.orgUserInfo.service.impl.OrgUserInfoServiceImp;
import com.alex.user.rbac.service.PermissionContextCacheService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RBAC-BE-RELATION-001: public org-user writes must go through assignSingleOrg
 * (single active org per user), not bare insert/update.
 */
@ExtendWith(MockitoExtension.class)
public class OrgUserAssignGuardTest {

    @Mock
    private OrgUserInfoMapper orgUserInfoMapper;

    @Test
    void addOrgUserInfo_delegatesToAssignSingleOrg_andDoesNotBareInsert() {
        SpyOrgUserInfoService service = new SpyOrgUserInfoService(orgUserInfoMapper);
        OrgUserInfoVo vo = new OrgUserInfoVo();
        vo.setUserId("100");
        vo.setOrgId("20");
        vo.setStatus(SysConf.VALID_STATUS);

        Boolean result = service.addOrgUserInfo(vo);

        assertTrue(Boolean.TRUE.equals(result), "addOrgUserInfo should return true");
        assertEquals(1, service.assignCalls.get(), "add must delegate to assignSingleOrg");
        assertEquals(100L, service.lastAssignUserId);
        assertEquals(20L, service.lastAssignOrgId);
        verify(orgUserInfoMapper, never()).insert(any(OrgUserInfo.class));
    }

    @Test
    void addOrgUserInfo_rejectsMissingUserOrOrg() {
        SpyOrgUserInfoService service = new SpyOrgUserInfoService(orgUserInfoMapper);
        OrgUserInfoVo vo = new OrgUserInfoVo();
        vo.setUserId("100");

        assertThrows(SystemException.class, () -> service.addOrgUserInfo(vo),
                "missing orgId must be rejected");
        assertEquals(0, service.assignCalls.get());
        verify(orgUserInfoMapper, never()).insert(any(OrgUserInfo.class));
    }

    @Test
    void addOrgUserInfo_rejectsExplicitInvalidStatus() {
        // I2: 新增机构关系仅支持有效状态；显式传入失效状态必须拒绝，不能静默按 assign 处理，
        // 否则运营选择"新增失效行"却意外把用户当前有效机构改掉了
        SpyOrgUserInfoService service = new SpyOrgUserInfoService(orgUserInfoMapper);
        OrgUserInfoVo vo = new OrgUserInfoVo();
        vo.setUserId("100");
        vo.setOrgId("20");
        vo.setStatus(SysConf.INVALID_STATUS);

        assertThrows(SystemException.class, () -> service.addOrgUserInfo(vo),
                "I2: explicit non-valid status must be rejected");
        assertEquals(0, service.assignCalls.get(), "must not silently delegate to assign");
        verify(orgUserInfoMapper, never()).insert(any(OrgUserInfo.class));
    }

    @Test
    void updateOrgUserInfo_routesToAssign_whenActivating() {
        SpyOrgUserInfoService service = new SpyOrgUserInfoService(orgUserInfoMapper);
        OrgUserInfoVo vo = new OrgUserInfoVo();
        vo.setId(9L);
        vo.setUserId("100");
        vo.setOrgId("30");
        vo.setStatus(SysConf.VALID_STATUS);

        Boolean result = service.updateOrgUserInfo(vo);

        assertTrue(Boolean.TRUE.equals(result), "updateOrgUserInfo should return true");
        assertEquals(1, service.assignCalls.get(), "activating update must route to assignSingleOrg");
        assertEquals(100L, service.lastAssignUserId);
        assertEquals(30L, service.lastAssignOrgId);
        verify(orgUserInfoMapper, never()).updateById(any(OrgUserInfo.class));
    }

    @Test
    void updateOrgUserInfo_activation_removesStaleDuplicateRow() {
        // I3: 被编辑行 vo.getId() 本身是同一 (user, org) 的历史失效行时，assign 插入新有效行后
        // 必须清理这条孤儿失效行，否则同一 (user, org) 会残留"一条失效 + 一条新有效"的重复记录
        SpyOrgUserInfoService service = new SpyOrgUserInfoService(orgUserInfoMapper);
        OrgUserInfo staleRow = new OrgUserInfo();
        staleRow.setUserId("100");
        staleRow.setOrgId("30");
        staleRow.setStatus(SysConf.INVALID_STATUS);
        when(orgUserInfoMapper.selectById(9L)).thenReturn(staleRow);

        OrgUserInfoVo vo = new OrgUserInfoVo();
        vo.setId(9L);
        vo.setUserId("100");
        vo.setOrgId("30");
        vo.setStatus(SysConf.VALID_STATUS);

        Boolean result = service.updateOrgUserInfo(vo);

        assertTrue(Boolean.TRUE.equals(result), "updateOrgUserInfo should return true");
        assertEquals(1, service.assignCalls.get());
        verify(orgUserInfoMapper).deleteById(9L);
    }

    @Test
    void updateOrgUserInfo_activation_keepsUnrelatedRow() {
        // I3 反例：被编辑行属于不同的 org（历史记录），不应被误删
        SpyOrgUserInfoService service = new SpyOrgUserInfoService(orgUserInfoMapper);
        OrgUserInfo unrelatedRow = new OrgUserInfo();
        unrelatedRow.setUserId("100");
        unrelatedRow.setOrgId("20");
        unrelatedRow.setStatus(SysConf.INVALID_STATUS);
        when(orgUserInfoMapper.selectById(9L)).thenReturn(unrelatedRow);

        OrgUserInfoVo vo = new OrgUserInfoVo();
        vo.setId(9L);
        vo.setUserId("100");
        vo.setOrgId("30");
        vo.setStatus(SysConf.VALID_STATUS);

        service.updateOrgUserInfo(vo);

        verify(orgUserInfoMapper, never()).deleteById(any());
    }

    @Test
    void updateOrgUserInfo_activation_preservesSummaryOnNewActiveRow() {
        // I3：激活走 assign 插入的新有效行须带上请求体中的 summary，不能静默丢弃
        SummarySavingOrgUserInfoService service = new SummarySavingOrgUserInfoService(orgUserInfoMapper);
        when(orgUserInfoMapper.selectById(9L)).thenReturn(null);

        OrgUserInfoVo vo = new OrgUserInfoVo();
        vo.setId(9L);
        vo.setUserId("100");
        vo.setOrgId("30");
        vo.setStatus(SysConf.VALID_STATUS);
        vo.setSummary("编辑后的岗位描述");

        Boolean result = service.updateOrgUserInfo(vo);

        assertTrue(Boolean.TRUE.equals(result));
        assertNotNull(service.savedAssignment, "assign should insert a new active row");
        assertEquals("编辑后的岗位描述", service.savedAssignment.getSummary());
    }

    /**
     * Spy subclass: counts assignSingleOrg while mapper mock catches bare insert/update.
     */
    private static class SpyOrgUserInfoService extends OrgUserInfoServiceImp {

        private final AtomicInteger assignCalls = new AtomicInteger();
        private Long lastAssignUserId;
        private Long lastAssignOrgId;

        private SpyOrgUserInfoService(OrgUserInfoMapper mapper) {
            super(mapper, noOpTransactionTemplate(), noOpCache());
        }

        @Override
        protected Boolean assignSingleOrg(Long userId, Long orgId, String summary) {
            assignCalls.incrementAndGet();
            lastAssignUserId = userId;
            lastAssignOrgId = orgId;
            return true;
        }
    }

    private static class SummarySavingOrgUserInfoService extends OrgUserInfoServiceImp {

        private OrgUserInfo savedAssignment;

        private SummarySavingOrgUserInfoService(OrgUserInfoMapper mapper) {
            super(mapper, noOpTransactionTemplate(), noOpCache());
        }

        @Override
        public List<OrgUserInfo> list(Wrapper<OrgUserInfo> queryWrapper) {
            return new ArrayList<>();
        }

        @Override
        public boolean save(OrgUserInfo entity) {
            savedAssignment = entity;
            return true;
        }

        @Override
        public boolean updateById(OrgUserInfo entity) {
            return true;
        }
    }

    private static TransactionTemplate noOpTransactionTemplate() {
        return new TransactionTemplate((PlatformTransactionManager) null) {
            @Override
            public <T> T execute(TransactionCallback<T> action) {
                return action.doInTransaction(null);
            }
        };
    }

    /**
     * This test file focuses on the assign-guard write-path routing; cache invalidation itself
     * is covered separately by PermissionContextInvalidationTest, so a no-op stub is enough here.
     */
    private static PermissionContextCacheService noOpCache() {
        return new PermissionContextCacheService() {
            @Override
            public void invalidate(Long userId) {
                // no-op for this test's scope
            }

            @Override
            public void invalidateAll(java.util.Collection<Long> userIds) {
                // no-op for this test's scope
            }
        };
    }
}
