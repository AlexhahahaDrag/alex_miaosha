package com.alex.user.orgInfo.service.impl;

import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.rbac.RbacRoleCodes;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.api.user.userInfo.vo.UserPermissionContextVo;
import com.alex.base.constants.SysConf;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.SystemException;
import com.alex.common.utils.string.StringUtils;
import com.alex.user.orgInfo.entity.OrgInfo;
import com.alex.user.orgInfo.mapper.OrgInfoMapper;
import com.alex.user.orgInfo.service.OrgInfoService;
import com.alex.user.orgUserInfo.entity.OrgUserInfo;
import com.alex.user.orgUserInfo.service.OrgUserInfoService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * description:  机构表服务实现类
 * author:       alex
 * createDate:   2023-12-15 12:00:32
 * version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class OrgInfoServiceImp extends ServiceImpl<OrgInfoMapper, OrgInfo> implements OrgInfoService {

    private final OrgInfoMapper orgInfoMapper;

    private final OrgUserInfoService orgUserInfoService;

    private final UserUtils userUtils;

    @Override
    public Page<OrgInfoVo> getPage(Long pageNum, Long pageSize, OrgInfoVo orgInfoVo) {
        Page<OrgInfoVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return orgInfoMapper.getPage(page, orgInfoVo);
    }

    /**
     * RBAC-BE-ORG-003: load scoped flat list via annotated {@code getList}, then assemble children in memory.
     * Roots: parentId null/0, or parent not present in the scoped result (orphan promotion).
     */
    @Override
    public List<OrgInfoVo> getTree(OrgInfoVo orgInfoVo) {
        List<OrgInfoVo> list = orgInfoMapper.getList(orgInfoVo);
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        return buildOrgTree(list);
    }

    private static List<OrgInfoVo> buildOrgTree(List<OrgInfoVo> list) {
        Map<Long, OrgInfoVo> byId = new HashMap<>(list.size() * 2);
        Set<Long> ids = new HashSet<>(list.size() * 2);
        for (OrgInfoVo node : list) {
            if (node == null || node.getId() == null) {
                continue;
            }
            node.setChildren(new ArrayList<>());
            byId.put(node.getId(), node);
            ids.add(node.getId());
        }
        List<OrgInfoVo> roots = new ArrayList<>();
        for (OrgInfoVo node : list) {
            if (node == null || node.getId() == null) {
                continue;
            }
            Long parentId = node.getParentId();
            if (isRootParent(parentId) || !ids.contains(parentId)) {
                roots.add(node);
                continue;
            }
            OrgInfoVo parent = byId.get(parentId);
            if (parent != null) {
                parent.getChildren().add(node);
            } else {
                roots.add(node);
            }
        }
        return roots;
    }

    @Override
    public OrgInfoVo queryOrgInfo(String id) {
        return orgInfoMapper.queryOrgInfo(id);
    }

    @Override
    public Boolean addOrgInfo(OrgInfoVo orgInfoVo) {
        validateOrgCodeAndHierarchy(orgInfoVo);
        OrgInfo orgInfo = new OrgInfo();
        BeanUtils.copyProperties(orgInfoVo, orgInfo);
        orgInfoMapper.insert(orgInfo);
        return true;
    }

    @Override
    public Boolean updateOrgInfo(OrgInfoVo orgInfoVo) {
        assertOrgAccessible(orgInfoVo == null ? null : orgInfoVo.getId());
        validateOrgCodeAndHierarchy(orgInfoVo);
        OrgInfo orgInfo = new OrgInfo();
        BeanUtils.copyProperties(orgInfoVo, orgInfo);
        orgInfoMapper.updateById(orgInfo);
        return true;
    }

    @Override
    public Boolean deleteOrgInfo(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<Long> idArr = parseIds(ids);
        if (idArr.isEmpty()) {
            return true;
        }
        // Ownership first: deny out-of-scope orgs before child/bound-user guards.
        for (Long orgId : idArr) {
            assertOrgAccessible(orgId);
        }
        long childOrgCount = this.count(Wrappers.<OrgInfo>lambdaQuery()
                .in(OrgInfo::getParentId, idArr)
                .eq(OrgInfo::getIsDelete, 0));
        if (childOrgCount > 0) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "机构存在下级机构，不能删除:");
        }
        List<String> orgIds = idArr.stream().map(String::valueOf).collect(Collectors.toList());
        long boundUserCount = orgUserInfoService.count(Wrappers.<OrgUserInfo>lambdaQuery()
                .in(OrgUserInfo::getOrgId, orgIds)
                .eq(OrgUserInfo::getIsDelete, 0)
                .eq(OrgUserInfo::getStatus, SysConf.VALID_STATUS));
        if (boundUserCount > 0) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "机构仍绑定用户，不能删除:");
        }
        orgInfoMapper.deleteBatchIds(idArr);
        return true;
    }

    /**
     * RBAC-BE-ORG-002: orgCode uniqueness + parent existence + cycle prevention.
     * Root parent is null or 0 (aligned with PC tree: !parentId || parentId === '0').
     */
    private void validateOrgCodeAndHierarchy(OrgInfoVo orgInfoVo) {
        if (orgInfoVo == null) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "机构参数不能为空");
        }
        String orgCode = orgInfoVo.getOrgCode();
        if (StringUtils.isEmpty(orgCode)) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "机构编码不能为空");
        }
        Long selfId = orgInfoVo.getId();
        long duplicateCount = orgInfoMapper.selectCount(Wrappers.<OrgInfo>lambdaQuery()
                .eq(OrgInfo::getOrgCode, orgCode)
                .ne(selfId != null, OrgInfo::getId, selfId));
        if (duplicateCount > 0) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "机构编码已存在");
        }

        Long parentId = orgInfoVo.getParentId();
        if (isRootParent(parentId)) {
            return;
        }
        if (selfId != null && selfId.equals(parentId)) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "父级机构不能为自身");
        }
        OrgInfo parent = orgInfoMapper.selectById(parentId);
        if (parent == null || isDeleted(parent)) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "父级机构不存在");
        }
        // Walk upward from parent; if self appears in the ancestor chain, a cycle would form.
        Long cursor = parent.getParentId();
        for (int depth = 0; depth < 64; depth++) {
            if (isRootParent(cursor)) {
                return;
            }
            if (selfId != null && selfId.equals(cursor)) {
                throw new SystemException(ResultEnum.PARAM_ERROR, "机构父子关系不能成环");
            }
            OrgInfo ancestor = orgInfoMapper.selectById(cursor);
            if (ancestor == null || isDeleted(ancestor)) {
                return;
            }
            cursor = ancestor.getParentId();
        }
    }

    private static boolean isRootParent(Long parentId) {
        return parentId == null || parentId == 0L;
    }

    private static boolean isDeleted(OrgInfo orgInfo) {
        return orgInfo.getIsDelete() != null && orgInfo.getIsDelete() != 0;
    }

    /**
     * Write-path ownership guard: non-super users must pass a scoped queryOrgInfo
     * before update/delete. Null means outside data scope — never silent success.
     */
    private void assertOrgAccessible(Long id) {
        if (id == null) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "机构ID不能为空");
        }
        TUserVo loginUser = userUtils.getLoginUser();
        if (loginUser == null) {
            // I1 修复：登录上下文不可用时必须 fail-closed，不能默认放行。
            throw new SystemException(ResultEnum.PARAM_ERROR, "无权访问：登录上下文不可用");
        }
        if (isSuperAdminLogin(loginUser)) {
            return;
        }
        OrgInfoVo visible = orgInfoMapper.queryOrgInfo(String.valueOf(id));
        if (visible == null) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "无权访问其他机构");
        }
    }

    private static boolean isSuperAdminLogin(TUserVo loginUser) {
        if (loginUser == null) {
            return false;
        }
        UserPermissionContextVo context = loginUser.getPermissionContext();
        if (context != null && Boolean.TRUE.equals(context.getSuperAdmin())) {
            return true;
        }
        List<RoleInfoVo> roles = loginUser.getRoleInfoVoList();
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        for (RoleInfoVo role : roles) {
            if (role != null && RbacRoleCodes.SUPER.equals(role.getRoleCode())) {
                return true;
            }
        }
        return false;
    }

    private List<Long> parseIds(String ids) {
        return Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(id -> !StringUtils.isEmpty(id))
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }
}
