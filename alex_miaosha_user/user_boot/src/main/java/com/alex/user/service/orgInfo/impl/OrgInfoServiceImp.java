package com.alex.user.service.orgInfo.impl;

import com.alex.api.user.vo.orgInfo.OrgInfoVo;
import com.alex.common.utils.bean.BeanUtils;
import com.alex.common.utils.string.StringUtils;
import com.alex.user.entity.orgInfo.OrgInfo;
import com.alex.user.mapper.orgInfo.OrgInfoMapper;
import com.alex.user.service.orgInfo.OrgInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

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
        List<String> idArr = Arrays.asList(ids.split(","));
        orgInfoMapper.deleteBatchIds(idArr);
        return true;
    }
}
