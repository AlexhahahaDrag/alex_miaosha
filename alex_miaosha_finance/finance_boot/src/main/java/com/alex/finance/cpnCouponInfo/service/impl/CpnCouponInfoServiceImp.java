package com.alex.finance.cpnCouponInfo.service.impl;

import com.alex.finance.cpnCouponInfo.entity.CpnCouponInfo;
import com.alex.api.finance.cpnCouponInfo.vo.CpnCouponInfoVo;
import com.alex.finance.cpnCouponInfo.mapper.CpnCouponInfoMapper;
import com.alex.finance.cpnCouponInfo.service.CpnCouponInfoService;
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
 * @description:  消费券信息表服务实现类
 * @author:       alex
 * @createDate:   2025-12-17 17:54:42
 * @version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class CpnCouponInfoServiceImp extends ServiceImpl<CpnCouponInfoMapper, CpnCouponInfo> implements CpnCouponInfoService {

    private final CpnCouponInfoMapper cpnCouponInfoMapper;

    @Override
    public Page<CpnCouponInfoVo> getPage(Long pageNum, Long pageSize, CpnCouponInfoVo cpnCouponInfoVo) {
        Page<CpnCouponInfoVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return cpnCouponInfoMapper.getPage(page, cpnCouponInfoVo);
    }

    @Override
    public List<CpnCouponInfoVo> getList(CpnCouponInfoVo cpnCouponInfoVo) {
        return cpnCouponInfoMapper.getList(cpnCouponInfoVo);
    }

    @Override
    public CpnCouponInfoVo queryCpnCouponInfo(Long id) {
        return cpnCouponInfoMapper.queryCpnCouponInfo(id);
    }

    @Override
    public Boolean addCpnCouponInfo(CpnCouponInfoVo cpnCouponInfoVo) {
        CpnCouponInfo cpnCouponInfo = new CpnCouponInfo();
        BeanUtils.copyProperties(cpnCouponInfoVo, cpnCouponInfo);
        cpnCouponInfoMapper.insert(cpnCouponInfo);
        return true;
    }

    @Override
    public Boolean updateCpnCouponInfo(CpnCouponInfoVo cpnCouponInfoVo) {
        CpnCouponInfo cpnCouponInfo = new CpnCouponInfo();
        BeanUtils.copyProperties(cpnCouponInfoVo, cpnCouponInfo);
        cpnCouponInfoMapper.updateById(cpnCouponInfo);
        return true;
    }

    @Override
    public Boolean deleteCpnCouponInfo(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        cpnCouponInfoMapper.deleteBatchIds(idArr);
        return true;
    }
}
