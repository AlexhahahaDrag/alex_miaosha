package com.alex.user.rbac;

import com.alex.user.orgInfo.entity.OrgInfo;
import com.alex.user.orgInfo.lookup.OrgSubtreeLookupImpl;
import com.alex.user.orgInfo.mapper.OrgInfoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * RBAC-BE-SCOPE-002: {@link OrgSubtreeLookupImpl} 需要基于全量 parent_id 关系
 * 在内存中做 BFS 展开，找出某机构的全部子孙 id（不含自身、不含已删除节点）。
 */
@ExtendWith(MockitoExtension.class)
public class OrgSubtreeLookupImplTest {

    @Mock
    private OrgInfoMapper orgInfoMapper;

    private OrgSubtreeLookupImpl lookup;

    @BeforeEach
    void setUp() {
        lookup = new OrgSubtreeLookupImpl(orgInfoMapper);
    }

    @Test
    void findDescendantOrgIds_returnsMultiLevelDescendants() {
        // 1(root) -> 2,3 ; 2 -> 4 ; 4 -> 5
        when(orgInfoMapper.selectList(any())).thenReturn(Arrays.asList(
                org(1L, null),
                org(2L, 1L),
                org(3L, 1L),
                org(4L, 2L),
                org(5L, 4L)
        ));

        List<Long> descendants = lookup.findDescendantOrgIds(1L);

        assertEquals(4, descendants.size());
        assertTrue(descendants.containsAll(Arrays.asList(2L, 3L, 4L, 5L)));
    }

    @Test
    void findDescendantOrgIds_returnsEmptyWhenLeafNode() {
        when(orgInfoMapper.selectList(any())).thenReturn(Arrays.asList(
                org(1L, null),
                org(2L, 1L)
        ));

        List<Long> descendants = lookup.findDescendantOrgIds(2L);

        assertTrue(descendants.isEmpty());
    }

    @Test
    void findDescendantOrgIds_returnsEmptyForNullOrgId() {
        List<Long> descendants = lookup.findDescendantOrgIds(null);

        assertTrue(descendants.isEmpty());
    }

    @Test
    void findDescendantOrgIds_toleratesCircularReferenceWithoutInfiniteLoop() {
        // 异常脏数据：1 -> 2 -> 1，不能死循环；1 的子孙只应含 2。
        when(orgInfoMapper.selectList(any())).thenReturn(Arrays.asList(
                org(1L, 2L),
                org(2L, 1L)
        ));

        List<Long> descendants = lookup.findDescendantOrgIds(1L);

        assertEquals(Collections.singletonList(2L), descendants);
    }

    private static OrgInfo org(Long id, Long parentId) {
        OrgInfo org = new OrgInfo();
        org.setId(id);
        org.setParentId(parentId);
        return org;
    }
}
