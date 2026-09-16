package com.alex.user.rbac;

import com.alex.api.user.menuInfo.vo.MenuInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.common.utils.redis.RedisUtils;
import com.alex.user.menuInfo.mapper.MenuInfoMapper;
import com.alex.user.menuInfo.service.impl.MenuInfoServiceImp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * C1 fix (batch2 final review)：全量菜单树 Redis 缓存（全局键 menu_all_tree）不能被
 * 调用者的数据范围污染。回归机理见 .superpowers/sdd/batch2-final-review-report.md C1：
 * MenuInfoMapper.getList 挂了 @DataPermission 之后，任何非超管调用
 * {@code POST /menu-info/list} 且 body 命中 isFullQuery（status=1、name/path/title 均为空）
 * 都会把自己数据范围内的子集写进全局缓存，污染后续 1 小时内所有用户（含超管、登录构建
 * 上下文）读到的菜单树。
 *
 * 修复方式：isFullQuery 分支改用不带 @DataPermission 的 {@code MenuInfoMapper#getListAll}，
 * 非全量查询（管理端按条件筛选）继续走带注解的 {@code getList}，保留数据范围隔离。
 */
@ExtendWith(MockitoExtension.class)
public class MenuFullQueryCacheScopeTest {

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

    /**
     * isFullQuery 命中（status=1，name/path/title 均为空）且缓存未命中时：
     * 必须调用未挂 @DataPermission 的 getListAll，绝不能调用带 @DataPermission 的 getList，
     * 否则任意非超管调用者写入的子集会成为全局缓存内容。
     */
    @Test
    void getList_fullQuery_usesUnscopedGetListAll_neverScopedGetList() {
        when(redisUtils.getList(anyString(), any())).thenReturn(null);
        MenuInfoVo root = new MenuInfoVo();
        root.setId(1L);
        root.setParentId(null);
        when(menuInfoMapper.getListAll(any(MenuInfoVo.class))).thenReturn(Collections.singletonList(root));

        MenuInfoVo query = new MenuInfoVo();
        query.setStatus("1");

        List<MenuInfoVo> result = service.getList(query);

        verify(menuInfoMapper).getListAll(any(MenuInfoVo.class));
        verify(menuInfoMapper, never()).getList(any(MenuInfoVo.class));
        assertNotNull(result, "full-query result should be the tree built from getListAll");
    }

    /**
     * 非全量查询（带筛选条件，如 name 非空）必须继续走带 @DataPermission 的 getList，
     * 保持管理端列表接口的数据范围隔离不被本次修复弱化。
     */
    @Test
    void getList_scopedQuery_usesScopedGetList_neverUnscopedGetListAll() {
        MenuInfoVo query = new MenuInfoVo();
        query.setStatus("1");
        query.setName("dashboard");
        when(menuInfoMapper.getList(any(MenuInfoVo.class))).thenReturn(null);

        service.getList(query);

        verify(menuInfoMapper).getList(any(MenuInfoVo.class));
        verify(menuInfoMapper, never()).getListAll(any(MenuInfoVo.class));
        verify(redisUtils, never()).getList(anyString(), any());
    }
}
