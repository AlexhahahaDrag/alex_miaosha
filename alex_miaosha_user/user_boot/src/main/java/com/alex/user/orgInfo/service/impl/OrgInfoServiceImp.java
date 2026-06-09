package com.alex.user.orgInfo.service.impl;

import com.alex.api.user.orgInfo.vo.OrgInfoVo;
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

import java.util.Arrays;
import java.util.List;
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

    @Override
    public Page<OrgInfoVo> getPage(Long pageNum, Long pageSize, OrgInfoVo orgInfoVo) {
        Page<OrgInfoVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return orgInfoMapper.getPage(page, orgInfoVo);
    }

    @Override
    public OrgInfoVo queryOrgInfo(String id) {
        return orgInfoMapper.queryOrgInfo(id);
    }

    @Override
    public Boolean addOrgInfo(OrgInfoVo orgInfoVo) {
        OrgInfo orgInfo = new OrgInfo();
        BeanUtils.copyProperties(orgInfoVo, orgInfo);
        orgInfoMapper.insert(orgInfo);
        return true;
    }

    @Override
    public Boolean updateOrgInfo(OrgInfoVo orgInfoVo) {
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

    private List<Long> parseIds(String ids) {
        return Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(id -> !StringUtils.isEmpty(id))
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }
}
