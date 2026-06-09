package com.alex.user.roleInfo.service.impl;

import com.alex.api.user.permissionInfo.vo.PermissionInfoVo;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.rolePermissionInfo.vo.RolePermissionInfoVo;
import com.alex.api.user.roleUserInfo.vo.RoleUserInfoVo;
import com.alex.base.constants.SysConf;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.SystemException;
import com.alex.common.utils.string.StringUtils;
import com.alex.user.permissionInfo.service.PermissionInfoService;
import com.alex.user.roleInfo.entity.RoleInfo;
import com.alex.user.roleInfo.mapper.RoleInfoMapper;
import com.alex.user.roleInfo.service.RoleInfoService;
import com.alex.user.rolePermissionInfo.service.RolePermissionInfoService;
import com.alex.user.roleUserInfo.entity.RoleUserInfo;
import com.alex.user.roleUserInfo.service.RoleUserInfoService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * description:  角色信息表服务实现类
 * author:       majf
 * createDate:   2024-01-14 21:56:18
 * version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class RoleInfoServiceImp extends ServiceImpl<RoleInfoMapper, RoleInfo> implements RoleInfoService {

    private final RoleInfoMapper roleInfoMapper;

    private final PermissionInfoService permissionInfoService;

    private final RolePermissionInfoService rolePermissionInfoService;

    private final RoleUserInfoService roleUserInfoService;

    @Override
    public Page<RoleInfoVo> getPage(Long pageNum, Long pageSize, RoleInfoVo roleInfoVo) {
        Page<RoleInfoVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return roleInfoMapper.getPage(page, roleInfoVo);
    }

    /**
     * param: id
     * description: 查询角色信息
     * author:      majf
     * return:      com.alex.api.user.roleInfo.vo.RoleInfoVo
    */
    @Override
    public RoleInfoVo queryRoleInfo(String id) {
        RoleInfoVo roleInfoVo = roleInfoMapper.queryRoleInfo(id);
        // 权限列表
        List<PermissionInfoVo> list = permissionInfoService.getList(null);
        roleInfoVo.setPermissionList(list);
        // 角色权限列表
        RolePermissionInfoVo rolePermissionInfoVo = new RolePermissionInfoVo();
        rolePermissionInfoVo.setRoleId(id);
        List<RolePermissionInfoVo> rolePermissionInfoVoList = rolePermissionInfoService.getList(rolePermissionInfoVo);
        roleInfoVo.setRolePermissionInfoVoList(rolePermissionInfoVoList);
        List<RoleUserInfoVo> roleUserInfoVoList = roleUserInfoService.list(Wrappers.<RoleUserInfo>lambdaQuery()
                        .eq(RoleUserInfo::getRoleId, id)
                        .eq(RoleUserInfo::getStatus, SysConf.VALID_STATUS))
                .stream()
                .map(item -> {
                    RoleUserInfoVo vo = new RoleUserInfoVo();
                    BeanUtils.copyProperties(item, vo);
                    return vo;
                })
                .collect(Collectors.toList());
        roleInfoVo.setRoleUserInfoVoList(roleUserInfoVoList);
        return roleInfoVo;
    }

    @Override
    public Boolean addRoleInfo(RoleInfoVo roleInfoVo) {
        RoleInfo roleInfo = new RoleInfo();
        BeanUtils.copyProperties(roleInfoVo, roleInfo);
        roleInfoMapper.insert(roleInfo);
        return true;
    }

    @Override
    public Boolean updateRoleInfo(RoleInfoVo roleInfoVo) {
        RoleInfo roleInfo = new RoleInfo();
        BeanUtils.copyProperties(roleInfoVo, roleInfo);
        roleInfoMapper.updateById(roleInfo);
        return true;
    }

    @Override
    public Boolean deleteRoleInfo(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(id -> !StringUtils.isEmpty(id))
                .collect(Collectors.toList());
        if (idArr.isEmpty()) {
            return true;
        }
        long boundUserCount = roleUserInfoService.count(Wrappers.<RoleUserInfo>lambdaQuery()
                .in(RoleUserInfo::getRoleId, idArr)
                .eq(RoleUserInfo::getIsDelete, 0)
                .eq(RoleUserInfo::getStatus, SysConf.VALID_STATUS));
        if (boundUserCount > 0) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "角色仍绑定用户，不能删除:");
        }
        roleInfoMapper.deleteBatchIds(idArr);
        return true;
    }

    @Override
    public Boolean assignUsers(Long roleId, List<Long> userIds) {
        return roleUserInfoService.assignUsersToRole(roleId, userIds);
    }
}
