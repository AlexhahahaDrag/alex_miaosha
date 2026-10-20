package com.alex.user.rbac;

import com.alex.api.user.menuInfo.vo.MenuInfoVo;
import com.alex.api.user.userInfo.UserUtils;
import com.alex.common.redis.key.LoginKey;
import com.alex.common.utils.redis.RedisUtils;
import com.alex.user.menuInfo.mapper.MenuInfoMapper;
import com.alex.user.menuInfo.service.impl.MenuInfoServiceImp;
import com.alex.user.rbac.service.impl.UserPermissionContextServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 菜单树 Redis 缓存隔离与内存不变式集成测试
 * 确保全局共享缓存 menu_all_tree 绝不被管理端或非超管权限过滤污染
 */
class MenuTreeCacheAntiPollutionIT {

    @Mock
    private MenuInfoMapper menuInfoMapper;

    @Mock
    private RedisUtils redisUtils;

    @Mock
    private UserUtils userUtils;

    private MenuInfoServiceImp menuInfoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        menuInfoService = new MenuInfoServiceImp(menuInfoMapper, redisUtils, userUtils);
    }

    @Test
    @DisplayName("管理端树形接口契约：getTree 必须走带数据权限的 getList，绝不读写 menu_all_tree 缓存")
    void getTree_mustNotReadOrWriteMenuAllTreeCache() {
        MenuInfoVo query = new MenuInfoVo();
        query.setStatus("1");

        MenuInfoVo root = new MenuInfoVo();
        root.setId(1L);
        root.setParentId(0L);
        root.setName("系统管理");

        MenuInfoVo child = new MenuInfoVo();
        child.setId(2L);
        child.setParentId(1L);
        child.setName("用户管理");

        when(menuInfoMapper.getList(query)).thenReturn(List.of(root, child));

        List<MenuInfoVo> tree = menuInfoService.getTree(query);

        assertNotNull(tree);
        assertEquals(1, tree.size());
        assertEquals("系统管理", tree.get(0).getName());
        assertEquals(1, tree.get(0).getChildren().size());

        // 强断言：绝不调用 getListAll，绝不读写全局缓存
        verify(menuInfoMapper, times(1)).getList(query);
        verify(menuInfoMapper, never()).getListAll(any());
        verify(redisUtils, never()).get(any(), any(), any());
        verify(redisUtils, never()).getList(anyString(), any());
        verify(redisUtils, never()).setEx(any(), any(), any(), anyLong(), any());
        verify(redisUtils, never()).setEx(anyString(), anyString(), anyInt(), any());
    }

    @Test
    @DisplayName("全量查询契约：仅在 status=1 且无筛选条件时，getList 才触发全量共享缓存 menu_all_tree 读写")
    void isFullQuery_inGetList_readsAndWritesCache() {
        MenuInfoVo fullQuery = new MenuInfoVo();
        fullQuery.setStatus("1");

        MenuInfoVo root = new MenuInfoVo();
        root.setId(1L);
        root.setParentId(0L);
        root.setName("根菜单");

        when(redisUtils.getList(anyString(), eq(MenuInfoVo.class))).thenReturn(null);
        when(menuInfoMapper.getListAll(fullQuery)).thenReturn(List.of(root));

        List<MenuInfoVo> result = menuInfoService.getList(fullQuery);

        assertNotNull(result);
        verify(menuInfoMapper, times(1)).getListAll(fullQuery);
        verify(redisUtils, times(1)).setEx(contains("menu_all_tree"), anyString(), eq(1), any());
    }

    @Test
    @DisplayName("条件筛选隔离：带关键字过滤的 getList 严禁读写全局共享缓存")
    void filteredQuery_inGetList_isolatedFromGlobalCache() {
        MenuInfoVo query = new MenuInfoVo();
        query.setStatus("1");
        query.setName("用户");

        MenuInfoVo node = new MenuInfoVo();
        node.setId(10L);
        node.setParentId(0L);
        node.setName("用户中心");

        when(menuInfoMapper.getList(query)).thenReturn(List.of(node));

        List<MenuInfoVo> result = menuInfoService.getList(query);

        assertNotNull(result);
        verify(menuInfoMapper, times(1)).getList(query);
        verify(menuInfoMapper, never()).getListAll(any());
        verify(redisUtils, never()).getList(anyString(), any());
        verify(redisUtils, never()).setEx(anyString(), anyString(), anyInt(), any());
    }

    @Test
    @DisplayName("内存不变式防御：权限过滤生成独立节点树，绝对不篡改共享菜单树的原始 children 集合")
    void permissionFilter_mustNotMutateOriginalMenuNodes() {
        MenuInfoVo originalRoot = new MenuInfoVo();
        originalRoot.setId(100L);
        originalRoot.setName("管理台");

        MenuInfoVo childA = new MenuInfoVo();
        childA.setId(101L);
        childA.setName("用户管理");
        childA.setPermissionCode("user:view");

        MenuInfoVo childB = new MenuInfoVo();
        childB.setId(102L);
        childB.setName("财务管理");
        childB.setPermissionCode("finance:view");

        List<MenuInfoVo> originalChildren = new ArrayList<>(List.of(childA, childB));
        originalRoot.setChildren(originalChildren);

        List<MenuInfoVo> sharedTree = List.of(originalRoot);

        // 用户只拥有 user:view 权限
        List<String> userPermissions = List.of("user:view");
        List<MenuInfoVo> filteredTree = UserPermissionContextServiceImpl.filterMenusByPermissionCodes(sharedTree, userPermissions);

        // 验证过滤结果：只有用户管理
        assertNotNull(filteredTree);
        assertEquals(1, filteredTree.size());
        MenuInfoVo filteredRoot = filteredTree.get(0);
        assertEquals(1, filteredRoot.getChildren().size());
        assertEquals("用户管理", filteredRoot.getChildren().get(0).getName());

        // 核心防污染断言：原始树的 children 绝不能被原地修改！依然保留 2 个子节点
        assertEquals(2, originalRoot.getChildren().size(), "原始共享菜单树 children 受到内存污染！");
        assertEquals("财务管理", originalRoot.getChildren().get(1).getName(), "原始共享菜单树节点丢失！");
        assertNotSame(originalRoot, filteredRoot, "过滤返回的根节点必须是独立克隆对象，不能直接返回原始共享引用");
    }
}
