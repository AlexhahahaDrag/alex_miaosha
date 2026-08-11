package com.alex.user.orgInfo.lookup;

import com.alex.api.user.handler.OrgSubtreeLookup;
import com.alex.user.orgInfo.entity.OrgInfo;
import com.alex.user.orgInfo.mapper.OrgInfoMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * RBAC-BE-SCOPE-002: {@link OrgSubtreeLookup} 的机构真实实现，基于全量 parent_id 关系
 * 在内存中做 BFS 展开子孙节点。查询本身不带 {@code @DataPermission}（BaseMapper 原生
 * selectList），因为计算「管理员自身机构的子孙」必须看到未被数据权限过滤的完整机构树，
 * 否则子孙节点会被反向裁剪，产生「子孙查不到自己子孙」的悖论。
 */
@Component
@RequiredArgsConstructor
public class OrgSubtreeLookupImpl implements OrgSubtreeLookup {

    private final OrgInfoMapper orgInfoMapper;

    @Override
    public List<Long> findDescendantOrgIds(Long orgId) {
        if (orgId == null) {
            return new ArrayList<>();
        }
        // 注意：不使用 .select(...) 限定列，其底层依赖 Mapper 注册后才初始化的 TableInfo
        // 缓存，纯单测（Mockito mock 掉 mapper）环境下会抛 lambda cache 未找到异常。
        List<OrgInfo> all = orgInfoMapper.selectList(Wrappers.<OrgInfo>lambdaQuery()
                .eq(OrgInfo::getIsDelete, 0));
        Map<Long, List<Long>> childrenByParent = new HashMap<>();
        for (OrgInfo org : all) {
            if (org == null || org.getId() == null || org.getParentId() == null) {
                continue;
            }
            childrenByParent.computeIfAbsent(org.getParentId(), k -> new ArrayList<>()).add(org.getId());
        }

        List<Long> descendants = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        visited.add(orgId);
        Deque<Long> queue = new ArrayDeque<>();
        queue.add(orgId);
        while (!queue.isEmpty()) {
            Long current = queue.poll();
            List<Long> children = childrenByParent.get(current);
            if (children == null) {
                continue;
            }
            for (Long childId : children) {
                if (childId != null && visited.add(childId)) {
                    descendants.add(childId);
                    queue.add(childId);
                }
            }
        }
        return descendants;
    }
}
