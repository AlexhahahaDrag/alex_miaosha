package com.alex.finance.cpnRedemptionRecordInfo.service.impl;

import com.alex.finance.cpnRedemptionRecordInfo.entity.CpnRedemptionRecordInfo;
import com.alex.api.finance.cpnRedemptionRecordInfo.vo.CpnRedemptionRecordInfoVo;
import com.alex.finance.cpnRedemptionRecordInfo.mapper.CpnRedemptionRecordInfoMapper;
import com.alex.finance.cpnRedemptionRecordInfo.service.CpnRedemptionRecordInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import com.alex.common.utils.string.StringUtils;

/**
 * <p>
 * @description:  消费券核销记录表 (按数量核销)服务实现类
 * @author:       alex
 * @createDate:   2025-12-17 17:54:00
 * @version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class CpnRedemptionRecordInfoServiceImp extends ServiceImpl<CpnRedemptionRecordInfoMapper, CpnRedemptionRecordInfo> implements CpnRedemptionRecordInfoService {

    private final CpnRedemptionRecordInfoMapper cpnRedemptionRecordInfoMapper;

    @Override
    public Page<CpnRedemptionRecordInfoVo> getPage(Long pageNum, Long pageSize, CpnRedemptionRecordInfoVo cpnRedemptionRecordInfoVo) {
        Page<CpnRedemptionRecordInfoVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return cpnRedemptionRecordInfoMapper.getPage(page, cpnRedemptionRecordInfoVo);
    }

    @Override
    public List<CpnRedemptionRecordInfoVo> getList(CpnRedemptionRecordInfoVo cpnRedemptionRecordInfoVo) {
        return cpnRedemptionRecordInfoMapper.getList(cpnRedemptionRecordInfoVo);
    }

    @Override
    public CpnRedemptionRecordInfoVo queryCpnRedemptionRecordInfo(Long id) {
        return cpnRedemptionRecordInfoMapper.queryCpnRedemptionRecordInfo(id);
    }

    @Override
    public Boolean addCpnRedemptionRecordInfo(CpnRedemptionRecordInfoVo cpnRedemptionRecordInfoVo) {
        CpnRedemptionRecordInfo cpnRedemptionRecordInfo = new CpnRedemptionRecordInfo();
        BeanUtils.copyProperties(cpnRedemptionRecordInfoVo, cpnRedemptionRecordInfo);
        cpnRedemptionRecordInfoMapper.insert(cpnRedemptionRecordInfo);
        return true;
    }

    @Override
    public Boolean updateCpnRedemptionRecordInfo(CpnRedemptionRecordInfoVo cpnRedemptionRecordInfoVo) {
        CpnRedemptionRecordInfo cpnRedemptionRecordInfo = new CpnRedemptionRecordInfo();
        BeanUtils.copyProperties(cpnRedemptionRecordInfoVo, cpnRedemptionRecordInfo);
        cpnRedemptionRecordInfoMapper.updateById(cpnRedemptionRecordInfo);
        return true;
    }

    @Override
    public Boolean deleteCpnRedemptionRecordInfo(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        cpnRedemptionRecordInfoMapper.deleteByIds(idArr);
        return true;
    }
}
