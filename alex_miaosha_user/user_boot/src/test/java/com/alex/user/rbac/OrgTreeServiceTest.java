package com.alex.user.rbac;

import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.user.orgInfo.mapper.OrgInfoMapper;
import com.alex.user.orgInfo.service.impl.OrgInfoServiceImp;
import com.alex.user.orgUserInfo.service.OrgUserInfoService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RBAC-BE-ORG-003: org tree endpoint must assemble children by parentId
 * from a {@code @DataPermission}-scoped list source.
 */
@ExtendWith(MockitoExtension.class)
public class OrgTreeServiceTest {

    @Mock
    private OrgInfoMapper orgInfoMapper;
    @Mock
    private OrgUserInfoService orgUserInfoService;
    @Mock
    private UserUtils userUtils;

    private OrgInfoServiceImp service;

    @BeforeEach
    void setUp() {
        service = new OrgInfoServiceImp(orgInfoMapper, orgUserInfoService, userUtils);
    }

    @Test
    void getTree_assemblesChildrenByParentId() {
        OrgInfoVo root = org(1L, 0L, "ROOT");
        OrgInfoVo child = org(2L, 1L, "CHILD");
        OrgInfoVo grand = org(3L, 2L, "GRAND");
        when(orgInfoMapper.getList(any())).thenReturn(Arrays.asList(root, child, grand));

        List<OrgInfoVo> tree = service.getTree(null);

        assertNotNull(tree);
        assertEquals(1, tree.size(), "one root expected");
        assertEquals(1L, tree.get(0).getId());
        assertNotNull(tree.get(0).getChildren(), "root must have children");
        assertEquals(1, tree.get(0).getChildren().size());
        assertEquals(2L, tree.get(0).getChildren().get(0).getId());
        assertNotNull(tree.get(0).getChildren().get(0).getChildren());
        assertEquals(1, tree.get(0).getChildren().get(0).getChildren().size());
        assertEquals(3L, tree.get(0).getChildren().get(0).getChildren().get(0).getId());
        verify(orgInfoMapper).getList(any());
    }

    @Test
    void getTree_treatsNullAndZeroParentAsRoot() {
        OrgInfoVo rootNull = org(10L, null, "NULL-ROOT");
        OrgInfoVo rootZero = org(20L, 0L, "ZERO-ROOT");
        when(orgInfoMapper.getList(any())).thenReturn(Arrays.asList(rootNull, rootZero));

        List<OrgInfoVo> tree = service.getTree(null);

        assertEquals(2, tree.size());
        assertTrue(tree.stream().anyMatch(n -> Long.valueOf(10L).equals(n.getId())));
        assertTrue(tree.stream().anyMatch(n -> Long.valueOf(20L).equals(n.getId())));
    }

    @Test
    void getTree_promotesOrphanWhenParentOutsideScope() {
        // Scoped list: parent 1 not visible; node 2 (parentId=1) must become a root
        // so data-permission-filtered trees remain usable (align PC buildTree).
        OrgInfoVo visible = org(2L, 1L, "SCOPED");
        OrgInfoVo child = org(3L, 2L, "CHILD");
        when(orgInfoMapper.getList(any())).thenReturn(Arrays.asList(visible, child));

        List<OrgInfoVo> tree = service.getTree(null);

        assertEquals(1, tree.size());
        assertEquals(2L, tree.get(0).getId());
        assertNotNull(tree.get(0).getChildren());
        assertEquals(1, tree.get(0).getChildren().size());
        assertEquals(3L, tree.get(0).getChildren().get(0).getId());
    }

    @Test
    void getTree_passesStatusFilterToScopedList() {
        when(orgInfoMapper.getList(any())).thenReturn(Collections.emptyList());

        OrgInfoVo filter = new OrgInfoVo();
        filter.setStatus("1");
        service.getTree(filter);

        ArgumentCaptor<OrgInfoVo> captor = ArgumentCaptor.forClass(OrgInfoVo.class);
        verify(orgInfoMapper).getList(captor.capture());
        assertNotNull(captor.getValue());
        assertEquals("1", captor.getValue().getStatus());
    }

    private static OrgInfoVo org(Long id, Long parentId, String name) {
        OrgInfoVo vo = new OrgInfoVo();
        vo.setId(id);
        vo.setParentId(parentId);
        vo.setOrgName(name);
        vo.setOrgCode(name);
        vo.setStatus("1");
        return vo;
    }
}
