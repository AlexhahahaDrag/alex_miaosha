package com.alex.user.rbac;

import com.alex.api.user.menuInfo.vo.MenuInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.common.utils.redis.RedisUtils;
import com.alex.user.menuInfo.mapper.MenuInfoMapper;
import com.alex.user.menuInfo.service.impl.MenuInfoServiceImp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * RBAC-BE-MENU-004: admin menu tree must assemble children via scoped {@code getList}
 * ({@code @DataPermission}), never {@code getListAll}, and must never write {@code menu_all_tree}.
 */
@ExtendWith(MockitoExtension.class)
public class MenuTreeServiceTest {

    @Mock
    private MenuInfoMapper menuInfoMapper;
    @Mock
    private RedisUtils redisUtils;
    @Mock
    private UserUtils userUtils;

    private MenuInfoServiceImp service;

    @BeforeEach
    void setUp() {
        service = new MenuInfoServiceImp(menuInfoMapper, redisUtils, userUtils);
    }

    @Test
    void getTree_assemblesChildrenByParentId() {
        MenuInfoVo root = menu(1L, 0L, "ROOT");
        MenuInfoVo child = menu(2L, 1L, "CHILD");
        MenuInfoVo grand = menu(3L, 2L, "GRAND");
        when(menuInfoMapper.getList(any())).thenReturn(Arrays.asList(root, child, grand));

        List<MenuInfoVo> tree = service.getTree(null);

        assertNotNull(tree);
        assertEquals(1, tree.size(), "one root expected");
        assertEquals(1L, tree.get(0).getId());
        assertNotNull(tree.get(0).getChildren(), "root must have children");
        assertEquals(1, tree.get(0).getChildren().size());
        assertEquals(2L, tree.get(0).getChildren().get(0).getId());
        assertNotNull(tree.get(0).getChildren().get(0).getChildren());
        assertEquals(1, tree.get(0).getChildren().get(0).getChildren().size());
        assertEquals(3L, tree.get(0).getChildren().get(0).getChildren().get(0).getId());
        verify(menuInfoMapper).getList(any());
        verify(menuInfoMapper, never()).getListAll(any());
        verifyNoInteractions(redisUtils);
    }

    @Test
    void getTree_treatsNullAndZeroParentAsRoot() {
        MenuInfoVo rootNull = menu(10L, null, "NULL-ROOT");
        MenuInfoVo rootZero = menu(20L, 0L, "ZERO-ROOT");
        when(menuInfoMapper.getList(any())).thenReturn(Arrays.asList(rootNull, rootZero));

        List<MenuInfoVo> tree = service.getTree(null);

        assertEquals(2, tree.size());
        assertTrue(tree.stream().anyMatch(n -> Long.valueOf(10L).equals(n.getId())));
        assertTrue(tree.stream().anyMatch(n -> Long.valueOf(20L).equals(n.getId())));
        verify(menuInfoMapper, never()).getListAll(any());
        verifyNoInteractions(redisUtils);
    }

    @Test
    void getTree_promotesOrphanWhenParentOutsideScope() {
        // Scoped list: parent 1 not visible; node 2 (parentId=1) must become a root
        // so data-permission-filtered trees remain usable.
        MenuInfoVo visible = menu(2L, 1L, "SCOPED");
        MenuInfoVo child = menu(3L, 2L, "CHILD");
        when(menuInfoMapper.getList(any())).thenReturn(Arrays.asList(visible, child));

        List<MenuInfoVo> tree = service.getTree(null);

        assertEquals(1, tree.size());
        assertEquals(2L, tree.get(0).getId());
        assertNotNull(tree.get(0).getChildren());
        assertEquals(1, tree.get(0).getChildren().size());
        assertEquals(3L, tree.get(0).getChildren().get(0).getId());
        verify(menuInfoMapper, never()).getListAll(any());
        verifyNoInteractions(redisUtils);
    }

    @Test
    void getTree_usesScopedGetListNeverGetListAllOrCache() {
        when(menuInfoMapper.getList(any())).thenReturn(Collections.emptyList());

        // status=1 full-ish filter would trigger cache path in getList — getTree must not
        MenuInfoVo filter = new MenuInfoVo();
        filter.setStatus("1");
        List<MenuInfoVo> tree = service.getTree(filter);

        assertNotNull(tree);
        assertTrue(tree.isEmpty());
        ArgumentCaptor<MenuInfoVo> captor = ArgumentCaptor.forClass(MenuInfoVo.class);
        verify(menuInfoMapper).getList(captor.capture());
        assertNotNull(captor.getValue());
        assertEquals("1", captor.getValue().getStatus());
        verify(menuInfoMapper, never()).getListAll(any());
        verifyNoInteractions(redisUtils);
    }

    private static MenuInfoVo menu(Long id, Long parentId, String name) {
        MenuInfoVo vo = new MenuInfoVo();
        vo.setId(id);
        vo.setParentId(parentId);
        vo.setName(name);
        vo.setTitle(name);
        vo.setStatus("1");
        return vo;
    }
}
