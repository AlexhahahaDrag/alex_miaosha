package com.alex.user.service.roleInfo.impl;

import com.alex.api.user.vo.roleInfo.RoleInfoVo;
import com.alex.common.utils.string.StringUtils;
import com.alex.user.entity.roleInfo.RoleInfo;
import com.alex.user.mapper.roleInfo.RoleInfoMapper;
import com.alex.user.service.roleInfo.RoleInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

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

    @Override
    public Page<RoleInfoVo> getPage(Long pageNum, Long pageSize, RoleInfoVo roleInfoVo) {
        Page<RoleInfoVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return roleInfoMapper.getPage(page, roleInfoVo);
    }

    @Override
    public RoleInfoVo queryRoleInfo(String id) {
        return roleInfoMapper.queryRoleInfo(id);
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
        List<String> idArr = Arrays.asList(ids.split(","));
        roleInfoMapper.deleteBatchIds(idArr);
        return true;
    }
}
