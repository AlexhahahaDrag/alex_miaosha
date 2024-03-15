package com.alex.user.service.permissionInfo.impl;

import com.alex.api.user.vo.permissionInfo.PermissionInfoVo;
import com.alex.common.utils.bean.BeanUtils;
import com.alex.common.utils.string.StringUtils;
import com.alex.user.entity.permissionInfo.PermissionInfo;
import com.alex.user.mapper.permissionInfo.PermissionInfoMapper;
import com.alex.user.service.permissionInfo.PermissionInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * description:  权限信息表服务实现类
 * author:       majf
 * createDate:   2024-01-16 15:43:56
 * version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class PermissionInfoServiceImp extends ServiceImpl<PermissionInfoMapper, PermissionInfo> implements PermissionInfoService {

    private final PermissionInfoMapper permissionInfoMapper;

    @Override
    public Page<PermissionInfoVo> getPage(Long pageNum, Long pageSize, PermissionInfoVo permissionInfoVo) {
        Page<PermissionInfoVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return permissionInfoMapper.getPage(page, permissionInfoVo);
    }

    @Override
    public PermissionInfoVo queryPermissionInfo(Long id) {
        return permissionInfoMapper.queryPermissionInfo(id);
    }

    @Override
    public PermissionInfoVo addPermissionInfo(PermissionInfoVo permissionInfoVo) {
        PermissionInfo permissionInfo = new PermissionInfo();
        BeanUtils.copyProperties(permissionInfoVo, permissionInfo);
        permissionInfoMapper.insert(permissionInfo);
        permissionInfoVo.setId(permissionInfo.getId());
        return permissionInfoVo;
    }

    @Override
    public PermissionInfoVo updatePermissionInfo(PermissionInfoVo permissionInfoVo) {
        PermissionInfo permissionInfo = new PermissionInfo();
        BeanUtils.copyProperties(permissionInfoVo, permissionInfo);
        permissionInfoMapper.updateById(permissionInfo);
        permissionInfoVo.setId(permissionInfo.getId());
        return permissionInfoVo;
    }

    @Override
    public Boolean deletePermissionInfo(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        permissionInfoMapper.deleteBatchIds(idArr);
        return true;
    }

    @Override
    public List<PermissionInfoVo> getList(PermissionInfoVo permissionInfoVo) {
        List<PermissionInfoVo> list = permissionInfoMapper.getList(permissionInfoVo);
        if (list == null || list.isEmpty()) {
            return null;
        }
        Map<Long, List<PermissionInfoVo>> menuMap = list.stream()
                .filter(item -> item.getParentId() != null)
                .collect(Collectors.groupingBy(PermissionInfoVo::getParentId));
        return list.stream().filter(item -> item.getParentId() == null)
                .peek(item -> item.setChildren(getChildren(item.getId(), menuMap)))
                .collect(Collectors.toList());
    }

    public List<PermissionInfoVo> getChildren(Long pId, Map<Long, List<PermissionInfoVo>> menuMap) {
        if (pId == null || menuMap == null || menuMap.get(pId) == null || menuMap.get(pId).isEmpty()) {
            return null;
        }
        List<PermissionInfoVo> children = menuMap.get(pId);
        children.forEach(item -> item.setChildren(getChildren(item.getId(), menuMap)));
        return children;
    }
}
