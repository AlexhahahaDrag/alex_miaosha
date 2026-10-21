package com.alex.user.rbac;

import com.alex.api.user.menuInfo.vo.MenuInfoVo;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.userInfo.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.common.cache.multilevel.HotKeyDetector;
import com.alex.common.cache.multilevel.MultiLevelCacheProperties;
import com.alex.common.cache.multilevel.MultiLevelCacheService;
import com.alex.common.utils.redis.RedisUtils;
import com.alex.user.menuInfo.entity.MenuInfo;
import com.alex.user.menuInfo.mapper.MenuInfoMapper;
import com.alex.user.menuInfo.service.impl.MenuInfoServiceImp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("用户中心全量菜单树多级缓存 (Caffeine L1 + Redis L2) 接入与双写一致性测试")
class MenuMultiLevelCacheIT {

    @Mock
    private MenuInfoMapper menuInfoMapper;

    @Mock
    private RedisUtils redisUtils;

    @Mock
    private UserUtils userUtils;

    private MultiLevelCacheService multiLevelCacheService;
    private MenuInfoServiceImp menuInfoService;

    @BeforeEach
    void setUp() {
        MultiLevelCacheProperties properties = new MultiLevelCacheProperties();
        properties.setL1MaximumSize(500L);
        properties.setL1DefaultTtlSeconds(300L);
        HotKeyDetector detector = new HotKeyDetector(properties);
        multiLevelCacheService = new MultiLevelCacheService(properties, null, detector);

        menuInfoService = new MenuInfoServiceImp(menuInfoMapper, redisUtils, userUtils, multiLevelCacheService);
        ReflectionTestUtils.setField(menuInfoService, "baseMapper", menuInfoMapper);

        TUserVo superUser = new TUserVo();
        superUser.setId(1L);
        RoleInfoVo role = new RoleInfoVo();
        role.setRoleCode("super_super");
        superUser.setRoleInfoVoList(Collections.singletonList(role));
        lenient().when(userUtils.getLoginUser()).thenReturn(superUser);
    }

    @Test
    @DisplayName("1. isFullQuery 首次查库回填多级缓存，二次查询直接从多级缓存返回")
    void testFullQueryMultiLevelCache() {
        MenuInfoVo query = new MenuInfoVo();
        query.setStatus("1");

        MenuInfoVo root = new MenuInfoVo();
        root.setId(1L);
        root.setParentId(0L);
        root.setName("看板大盘");

        List<MenuInfoVo> dbList = new ArrayList<>();
        dbList.add(root);

        when(menuInfoMapper.getListAll(any())).thenReturn(dbList);

        // 首次全量查询：走 getListAll，写入多级缓存
        List<MenuInfoVo> list1 = menuInfoService.getList(query);
        assertNotNull(list1);
        assertEquals(1, list1.size());
        verify(menuInfoMapper, times(1)).getListAll(any());

        // 二次全量查询：直接命中多级缓存，不再次查库
        List<MenuInfoVo> list2 = menuInfoService.getList(query);
        assertNotNull(list2);
        assertEquals(1, list2.size());
        verify(menuInfoMapper, times(1)).getListAll(any());
    }

    @Test
    @DisplayName("2. 菜单更新操作自动驱逐多级缓存，避免陈旧菜单残留")
    void testUpdateMenuEvictsMultiLevelCache() {
        MenuInfoVo query = new MenuInfoVo();
        query.setStatus("1");

        MenuInfoVo root = new MenuInfoVo();
        root.setId(1L);
        root.setParentId(0L);
        root.setName("看板大盘");

        List<MenuInfoVo> dbList = new ArrayList<>();
        dbList.add(root);

        when(menuInfoMapper.getListAll(any())).thenReturn(dbList);

        // 首次全量查询建立缓存
        menuInfoService.getList(query);
        verify(menuInfoMapper, times(1)).getListAll(any());

        // 更新菜单触发清除缓存
        when(menuInfoMapper.updateById(any(MenuInfo.class))).thenReturn(1);
        MenuInfoVo updateParam = new MenuInfoVo();
        updateParam.setId(1L);
        updateParam.setName("看板大盘-新");
        menuInfoService.updateMenuInfo(updateParam);

        // 再次查询：由于已被 evict，必须重新触发 getListAll
        menuInfoService.getList(query);
        verify(menuInfoMapper, times(2)).getListAll(any());
    }
}
