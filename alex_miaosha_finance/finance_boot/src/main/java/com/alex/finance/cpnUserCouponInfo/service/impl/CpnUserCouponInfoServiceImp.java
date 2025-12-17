package com.alex.finance.cpnUserCouponInfo.service.impl;

import com.alex.finance.cpnUserCouponInfo.entity.CpnUserCouponInfo;
import com.alex.api.finance.cpnUserCouponInfo.vo.CpnUserCouponInfoVo;
import com.alex.finance.cpnUserCouponInfo.mapper.CpnUserCouponInfoMapper;
import com.alex.finance.cpnUserCouponInfo.service.CpnUserCouponInfoService;
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
 * @description:  用户消费券库存表 (按数量核销)服务实现类
 * @author:       alex
 * @createDate:   2025-12-17 17:55:32
 * @version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class CpnUserCouponInfoServiceImp extends ServiceImpl<CpnUserCouponInfoMapper, CpnUserCouponInfo> implements CpnUserCouponInfoService {

    private final CpnUserCouponInfoMapper cpnUserCouponInfoMapper;

    @Override
    public Page<CpnUserCouponInfoVo> getPage(Long pageNum, Long pageSize, CpnUserCouponInfoVo cpnUserCouponInfoVo) {
        Page<CpnUserCouponInfoVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return cpnUserCouponInfoMapper.getPage(page, cpnUserCouponInfoVo);
    }

    @Override
    public List<CpnUserCouponInfoVo> getList(CpnUserCouponInfoVo cpnUserCouponInfoVo) {
        return cpnUserCouponInfoMapper.getList(cpnUserCouponInfoVo);
    }

    @Override
    public CpnUserCouponInfoVo queryCpnUserCouponInfo(Long id) {
        return cpnUserCouponInfoMapper.queryCpnUserCouponInfo(id);
    }

    @Override
    public Boolean addCpnUserCouponInfo(CpnUserCouponInfoVo cpnUserCouponInfoVo) {
        CpnUserCouponInfo cpnUserCouponInfo = new CpnUserCouponInfo();
        BeanUtils.copyProperties(cpnUserCouponInfoVo, cpnUserCouponInfo);
        cpnUserCouponInfoMapper.insert(cpnUserCouponInfo);
        return true;
    }

    @Override
    public Boolean updateCpnUserCouponInfo(CpnUserCouponInfoVo cpnUserCouponInfoVo) {
        CpnUserCouponInfo cpnUserCouponInfo = new CpnUserCouponInfo();
        BeanUtils.copyProperties(cpnUserCouponInfoVo, cpnUserCouponInfo);
        cpnUserCouponInfoMapper.updateById(cpnUserCouponInfo);
        return true;
    }

    @Override
    public Boolean deleteCpnUserCouponInfo(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        cpnUserCouponInfoMapper.deleteBatchIds(idArr);
        return true;
    }
}
