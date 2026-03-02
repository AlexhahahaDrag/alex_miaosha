package com.alex.user.roleUserInfo.service.impl;

import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.roleUserInfo.vo.RoleUserInfoVo;
import com.alex.common.utils.string.StringUtils;
import com.alex.user.roleUserInfo.entity.RoleUserInfo;
import com.alex.user.roleUserInfo.mapper.RoleUserInfoMapper;
import com.alex.user.roleUserInfo.service.RoleUserInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * description:  用户角色信息表服务实现类
 * author:       majf
 * createDate:   2024-01-15 15:12:07
 * version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class RoleUserInfoServiceImp extends ServiceImpl<RoleUserInfoMapper, RoleUserInfo> implements RoleUserInfoService {

    private final RoleUserInfoMapper roleUserInfoMapper;

    @Override
    public Page<RoleUserInfoVo> getPage(Long pageNum, Long pageSize, RoleUserInfoVo roleUserInfoVo) {
        Page<RoleUserInfoVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return roleUserInfoMapper.getPage(page, roleUserInfoVo);
    }

    @Override
    public RoleUserInfoVo queryRoleUserInfo(String id) {
        return roleUserInfoMapper.queryRoleUserInfo(id);
    }

    @Override
    public Boolean addRoleUserInfo(RoleUserInfoVo roleUserInfoVo) {
        RoleUserInfo roleUserInfo = new RoleUserInfo();
        BeanUtils.copyProperties(roleUserInfoVo, roleUserInfo);
        roleUserInfoMapper.insert(roleUserInfo);
        return true;
    }

    @Override
    public Boolean updateRoleUserInfo(RoleUserInfoVo roleUserInfoVo) {
        RoleUserInfo roleUserInfo = new RoleUserInfo();
        BeanUtils.copyProperties(roleUserInfoVo, roleUserInfo);
        roleUserInfoMapper.updateById(roleUserInfo);
        return true;
    }

    @Override
    public Boolean deleteRoleUserInfo(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        roleUserInfoMapper.deleteBatchIds(idArr);
        return true;
    }

    @Override
    public List<RoleInfoVo> getRoleInfoList(Long userId, boolean hasPermission) {
        return roleUserInfoMapper.getRoleInfoList(userId, hasPermission);
    }
}
