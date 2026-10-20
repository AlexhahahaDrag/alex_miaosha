package com.alex.user.rbac;

import com.alex.api.user.orgUserInfo.vo.OrgUserInfoVo;
import com.alex.base.constants.SysConf;
import com.alex.common.exception.SystemException;
import com.alex.user.orgUserInfo.entity.OrgUserInfo;
import com.alex.user.orgUserInfo.mapper.OrgUserInfoMapper;
import com.alex.user.orgUserInfo.service.impl.OrgUserInfoServiceImp;
import com.alex.user.rbac.service.PermissionContextCacheService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 阶段二十九：用户中心单有效机构事务与 Redis 权限上下文联动集成测试
 */
public class OrgUserSingleActiveAssignmentIT {

    private OrgUserInfoMapper orgUserInfoMapper;
    private PermissionContextCacheService permissionContextCacheService;
    private DirectTransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        orgUserInfoMapper = mock(OrgUserInfoMapper.class);
        permissionContextCacheService = mock(PermissionContextCacheService.class);
        transactionTemplate = new DirectTransactionTemplate();
    }

    private static class DirectTransactionTemplate extends TransactionTemplate {
        @Override
        public <T> T execute(TransactionCallback<T> action) {
            return action.doInTransaction(new SimpleTransactionStatus());
        }
    }

    private static class StubOrgUserInfoService extends OrgUserInfoServiceImp {
        private final List<OrgUserInfo> activeRows = new ArrayList<>();
        private final List<OrgUserInfo> inactiveRows = new ArrayList<>();
        public final List<OrgUserInfo> updatedEntities = new ArrayList<>();
        public final List<OrgUserInfo> savedEntities = new ArrayList<>();
        public boolean saveShouldFail = false;

        public StubOrgUserInfoService(OrgUserInfoMapper mapper, TransactionTemplate tt, PermissionContextCacheService cacheService) {
            super(mapper, tt, cacheService);
        }

        public void addActive(OrgUserInfo... rows) {
            Collections.addAll(activeRows, rows);
        }

        public void addInactive(OrgUserInfo... rows) {
            Collections.addAll(inactiveRows, rows);
        }

        @Override
        public List<OrgUserInfo> list(Wrapper<OrgUserInfo> queryWrapper) {
            // listActiveAssignments 走 list(Wrappers.<OrgUserInfo>lambdaQuery().eq(userId).eq(status, 1))
            return new ArrayList<>(activeRows);
        }

        @Override
        public boolean updateById(OrgUserInfo entity) {
            updatedEntities.add(entity);
            return true;
        }

        @Override
        public boolean save(OrgUserInfo entity) {
            if (saveShouldFail) {
                return false;
            }
            savedEntities.add(entity);
            return true;
        }
    }

    @Test
    @DisplayName("assignSingleOrg: 旧有效机构置为0，新机构置为1，并主动失效该用户 Redis permission_context 缓存")
    void testAssignSingleOrgInvalidatesOldAndTriggersCacheInvalidation() {
        StubOrgUserInfoService service = new StubOrgUserInfoService(orgUserInfoMapper, transactionTemplate, permissionContextCacheService);
        OrgUserInfo oldRow = new OrgUserInfo();
        oldRow.setId(101L);
        oldRow.setUserId("888");
        oldRow.setOrgId("2001");
        oldRow.setStatus(SysConf.VALID_STATUS);
        service.addActive(oldRow);

        Boolean ok = service.assignSingleOrg(888L, 3001L);

        assertTrue(ok);
        // 验证旧记录被标记为 0
        assertEquals(SysConf.INVALID_STATUS, oldRow.getStatus());
        assertEquals(1, service.updatedEntities.size());
        assertEquals(101L, service.updatedEntities.get(0).getId());

        // 验证新记录为 1
        assertEquals(1, service.savedEntities.size());
        OrgUserInfo saved = service.savedEntities.get(0);
        assertEquals("888", saved.getUserId());
        assertEquals("3001", saved.getOrgId());
        assertEquals(SysConf.VALID_STATUS, saved.getStatus());
        assertEquals("MAIN", saved.getSummary());

        // 验证主动失效 Redis 缓存被触发
        verify(permissionContextCacheService, times(1)).invalidate(888L);
    }

    @Test
    @DisplayName("assignSingleOrg: 新关系保存失败时抛异常，且绝不提前调用 Redis 缓存失效")
    void testAssignSingleOrgFailsDoesNotInvalidateCache() {
        StubOrgUserInfoService service = new StubOrgUserInfoService(orgUserInfoMapper, transactionTemplate, permissionContextCacheService);
        service.saveShouldFail = true;

        assertThrows(SystemException.class, () -> service.assignSingleOrg(888L, 3001L));

        // 缓存失效绝不能在异常回滚分支执行
        verify(permissionContextCacheService, never()).invalidate(any());
    }

    @Test
    @DisplayName("addOrgUserInfo: 显式传入非有效状态时抛出 PARAM_ERROR 业务异常")
    void testAddOrgUserInfoRejectsInvalidStatusExplicitly() {
        StubOrgUserInfoService service = new StubOrgUserInfoService(orgUserInfoMapper, transactionTemplate, permissionContextCacheService);

        OrgUserInfoVo vo = new OrgUserInfoVo();
        vo.setUserId("888");
        vo.setOrgId("3001");
        vo.setStatus(SysConf.INVALID_STATUS); // 传入 "0"

        SystemException ex = assertThrows(SystemException.class, () -> service.addOrgUserInfo(vo));
        assertTrue(ex.getMsg().contains("新增用户机构关系仅支持有效状态"));
    }

    @Test
    @DisplayName("updateOrgUserInfo: 激活已有行时转调 activateViaAssign，物理删除历史孤儿失效行保证 (user,org) 唯一")
    void testUpdateOrgUserInfoActivateViaAssignDeletesDuplicateOrphan() {
        StubOrgUserInfoService service = new StubOrgUserInfoService(orgUserInfoMapper, transactionTemplate, permissionContextCacheService);

        // 模拟已存在一条历史失效行
        OrgUserInfo orphanRow = new OrgUserInfo();
        orphanRow.setId(555L);
        orphanRow.setUserId("888");
        orphanRow.setOrgId("3001");
        orphanRow.setStatus(SysConf.INVALID_STATUS);

        when(orgUserInfoMapper.selectById(555L)).thenReturn(orphanRow);

        OrgUserInfoVo updateVo = new OrgUserInfoVo();
        updateVo.setId(555L);
        updateVo.setUserId("888");
        updateVo.setOrgId("3001");
        updateVo.setStatus(SysConf.VALID_STATUS); // 激活
        updateVo.setSummary("ACTIVATED");

        Boolean ok = service.updateOrgUserInfo(updateVo);

        assertTrue(ok);
        // 验证物理删除了孤儿失效行
        verify(orgUserInfoMapper, times(1)).deleteById(555L);
        // 验证新有效行已保存
        assertEquals(1, service.savedEntities.size());
        assertEquals("3001", service.savedEntities.get(0).getOrgId());
        assertEquals(SysConf.VALID_STATUS, service.savedEntities.get(0).getStatus());
        // 验证缓存已被失效
        verify(permissionContextCacheService, times(1)).invalidate(888L);
    }
}
