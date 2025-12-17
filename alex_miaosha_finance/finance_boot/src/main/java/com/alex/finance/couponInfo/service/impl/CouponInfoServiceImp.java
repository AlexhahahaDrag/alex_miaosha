package com.alex.finance.couponInfo.service.impl;

import com.alex.finance.couponInfo.entity.CouponInfo;
import com.alex.api.finance.couponInfo.vo.CouponInfoVo;
import com.alex.finance.couponInfo.mapper.CouponInfoMapper;
import com.alex.finance.couponInfo.service.CouponInfoService;
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
 * @createDate:   2025-12-17 11:56:28
 * @version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class CouponInfoServiceImp extends ServiceImpl<CouponInfoMapper, CouponInfo> implements CouponInfoService {

    private final CouponInfoMapper couponInfoMapper;

    @Override
    public Page<CouponInfoVo> getPage(Long pageNum, Long pageSize, CouponInfoVo couponInfoVo) {
        Page<CouponInfoVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return couponInfoMapper.getPage(page, couponInfoVo);
    }

    @Override
    public Page<CouponInfoVo> getPageWithRemain(Long pageNum, Long pageSize, CouponInfoVo couponInfoVo) {
        Page<CouponInfoVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return couponInfoMapper.getPageWithRemain(page, couponInfoVo);
    }

    @Override
    public List<CouponInfoVo> getList(CouponInfoVo couponInfoVo) {
        return couponInfoMapper.getList(couponInfoVo);
    }

    @Override
    public CouponInfoVo queryCouponInfo(Long id) {
        return couponInfoMapper.queryCouponInfo(id);
    }

    @Override
    public Boolean addCouponInfo(CouponInfoVo couponInfoVo) {
        CouponInfo couponInfo = new CouponInfo();
        BeanUtils.copyProperties(couponInfoVo, couponInfo);
        couponInfoMapper.insert(couponInfo);
        return true;
    }

    @Override
    public Boolean updateCouponInfo(CouponInfoVo couponInfoVo) {
        CouponInfo couponInfo = new CouponInfo();
        BeanUtils.copyProperties(couponInfoVo, couponInfo);
        couponInfoMapper.updateById(couponInfo);
        return true;
    }

    @Override
    public Boolean deleteCouponInfo(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        couponInfoMapper.deleteBatchIds(idArr);
        return true;
    }
}
