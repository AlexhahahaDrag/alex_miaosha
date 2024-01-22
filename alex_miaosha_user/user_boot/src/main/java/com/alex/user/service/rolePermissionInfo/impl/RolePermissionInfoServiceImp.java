package com.alex.user.service.rolePermissionInfo.impl;

import com.alex.user.entity.rolePermissionInfo.RolePermissionInfo;
import com.alex.api.user.vo.rolePermissionInfo.RolePermissionInfoVo;
import com.alex.user.mapper.rolePermissionInfo.RolePermissionInfoMapper;
import com.alex.user.service.rolePermissionInfo.RolePermissionInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import cn.hutool.core.bean.BeanUtil;
import com.alex.common.utils.string.StringUtils;

/**
 * <p>
 * description:  角色权限信息表服务实现类
 * author:       majf
 * createDate:   2024-01-19 14:52:21
 * version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class RolePermissionInfoServiceImp extends ServiceImpl<RolePermissionInfoMapper, RolePermissionInfo> implements RolePermissionInfoService {

    private final RolePermissionInfoMapper rolePermissionInfoMapper;

    @Override
    public Page<RolePermissionInfoVo> getPage(Long pageNum, Long pageSize, RolePermissionInfoVo rolePermissionInfoVo) {
        Page<RolePermissionInfoVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return rolePermissionInfoMapper.getPage(page, rolePermissionInfoVo);
    }

    @Override
    public RolePermissionInfoVo queryRolePermissionInfo(Long id) {
        return rolePermissionInfoMapper.queryRolePermissionInfo(id);
    }

    @Override
    public Boolean addRolePermissionInfo(RolePermissionInfoVo rolePermissionInfoVo) {
        RolePermissionInfo rolePermissionInfo = new RolePermissionInfo();
        BeanUtil.copyProperties(rolePermissionInfoVo, rolePermissionInfo);
        rolePermissionInfoMapper.insert(rolePermissionInfo);
        return true;
    }

    @Override
    public Boolean updateRolePermissionInfo(RolePermissionInfoVo rolePermissionInfoVo) {
        RolePermissionInfo rolePermissionInfo = new RolePermissionInfo();
        BeanUtil.copyProperties(rolePermissionInfoVo, rolePermissionInfo);
        rolePermissionInfoMapper.updateById(rolePermissionInfo);
        return true;
    }

    @Override
    public Boolean deleteRolePermissionInfo(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        rolePermissionInfoMapper.deleteBatchIds(idArr);
        return true;
    }
}
