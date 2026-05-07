package com.alex.user.orgUserInfo.service.impl;

import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.orgUserInfo.vo.OrgUserInfoVo;
import com.alex.base.constants.SysConf;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.SystemException;
import com.alex.common.utils.string.StringUtils;
import com.alex.user.orgUserInfo.entity.OrgUserInfo;
import com.alex.user.orgUserInfo.mapper.OrgUserInfoMapper;
import com.alex.user.orgUserInfo.service.OrgUserInfoService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * description:  用户公司信息表服务实现类
 * author:       majf
 * createDate:   2024-01-15 15:12:05
 * version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class OrgUserInfoServiceImp extends ServiceImpl<OrgUserInfoMapper, OrgUserInfo> implements OrgUserInfoService {

    private final OrgUserInfoMapper orgUserInfoMapper;
    private final TransactionTemplate transactionTemplate;
    private final Map<Long, Object> assignSingleOrgLocks = new ConcurrentHashMap<>();

    @Override
    public Page<OrgUserInfoVo> getPage(Long pageNum, Long pageSize, OrgUserInfoVo orgUserInfoVo) {
        Page<OrgUserInfoVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return orgUserInfoMapper.getPage(page, orgUserInfoVo);
    }

    @Override
    public OrgUserInfoVo queryOrgUserInfo(Long id) {
        return orgUserInfoMapper.queryOrgUserInfo(id);
    }

    @Override
    public Boolean addOrgUserInfo(OrgUserInfoVo orgUserInfoVo) {
        OrgUserInfo orgUserInfo = new OrgUserInfo();
        BeanUtils.copyProperties(orgUserInfoVo, orgUserInfo);
        orgUserInfoMapper.insert(orgUserInfo);
        return true;
    }

    @Override
    public Boolean updateOrgUserInfo(OrgUserInfoVo orgUserInfoVo) {
        OrgUserInfo orgUserInfo = new OrgUserInfo();
        BeanUtils.copyProperties(orgUserInfoVo, orgUserInfo);
        orgUserInfoMapper.updateById(orgUserInfo);
        return true;
    }

    @Override
    public Boolean deleteOrgUserInfo(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        orgUserInfoMapper.deleteBatchIds(idArr);
        return true;
    }

    @Override
    public Boolean assignSingleOrg(Long userId, Long orgId) {
        if (userId == null || orgId == null) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "用户机构分配参数错误:");
        }
        synchronized (assignSingleOrgLock(userId)) {
            return transactionTemplate.execute(status -> doAssignSingleOrg(userId, orgId));
        }
    }

    protected Object assignSingleOrgLock(Long userId) {
        return assignSingleOrgLocks.computeIfAbsent(userId, key -> new Object());
    }

    private Boolean doAssignSingleOrg(Long userId, Long orgId) {
        List<OrgUserInfo> activeAssignments = list(Wrappers.<OrgUserInfo>lambdaQuery()
                .eq(OrgUserInfo::getUserId, String.valueOf(userId))
                .eq(OrgUserInfo::getStatus, SysConf.VALID_STATUS));
        for (OrgUserInfo orgUserInfo : activeAssignments) {
            orgUserInfo.setStatus(SysConf.INVALID_STATUS);
            if (!updateById(orgUserInfo)) {
                throw new SystemException(ResultEnum.SYSTEM_ERROR, "用户机构旧关系失效失败:");
            }
        }
        OrgUserInfo orgUserInfo = new OrgUserInfo();
        orgUserInfo.setUserId(String.valueOf(userId));
        orgUserInfo.setOrgId(String.valueOf(orgId));
        orgUserInfo.setStatus(SysConf.VALID_STATUS);
        if (!save(orgUserInfo)) {
            throw new SystemException(ResultEnum.SYSTEM_ERROR, "用户机构新关系保存失败:");
        }
        return true;
    }

    @Override
    public List<OrgInfoVo> getOrgInfoList(Long userId) {
        return orgUserInfoMapper.getOrgInfoList(userId);
    }
}
