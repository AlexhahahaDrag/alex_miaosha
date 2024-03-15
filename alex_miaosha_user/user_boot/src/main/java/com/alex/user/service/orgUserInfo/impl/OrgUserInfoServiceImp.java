package com.alex.user.service.orgUserInfo.impl;

import com.alex.api.user.vo.orgInfo.OrgInfoVo;
import com.alex.api.user.vo.orgUserInfo.OrgUserInfoVo;
import com.alex.common.utils.bean.BeanUtils;
import com.alex.common.utils.string.StringUtils;
import com.alex.user.entity.orgUserInfo.OrgUserInfo;
import com.alex.user.mapper.orgUserInfo.OrgUserInfoMapper;
import com.alex.user.service.orgUserInfo.OrgUserInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

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
    public List<OrgInfoVo> getOrgInfoList(Long userId) {
        return orgUserInfoMapper.getOrgInfoList(userId);
    }
}
