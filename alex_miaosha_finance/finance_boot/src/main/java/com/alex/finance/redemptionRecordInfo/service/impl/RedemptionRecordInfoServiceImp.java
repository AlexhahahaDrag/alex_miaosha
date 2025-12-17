package com.alex.finance.redemptionRecordInfo.service.impl;

import com.alex.finance.redemptionRecordInfo.entity.RedemptionRecordInfo;
import com.alex.api.finance.redemptionRecordInfo.vo.RedemptionRecordInfoVo;
import com.alex.finance.redemptionRecordInfo.mapper.RedemptionRecordInfoMapper;
import com.alex.finance.redemptionRecordInfo.service.RedemptionRecordInfoService;
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
 * @createDate:   2025-12-17 14:08:55
 * @version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class RedemptionRecordInfoServiceImp extends ServiceImpl<RedemptionRecordInfoMapper, RedemptionRecordInfo> implements RedemptionRecordInfoService {

    private final RedemptionRecordInfoMapper redemptionRecordInfoMapper;

    @Override
    public Page<RedemptionRecordInfoVo> getPage(Long pageNum, Long pageSize, RedemptionRecordInfoVo redemptionRecordInfoVo) {
        Page<RedemptionRecordInfoVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return redemptionRecordInfoMapper.getPage(page, redemptionRecordInfoVo);
    }

    @Override
    public List<RedemptionRecordInfoVo> getList(RedemptionRecordInfoVo redemptionRecordInfoVo) {
        return redemptionRecordInfoMapper.getList(redemptionRecordInfoVo);
    }

    @Override
    public RedemptionRecordInfoVo queryRedemptionRecordInfo(Long id) {
        return redemptionRecordInfoMapper.queryRedemptionRecordInfo(id);
    }

    @Override
    public Boolean addRedemptionRecordInfo(RedemptionRecordInfoVo redemptionRecordInfoVo) {
        RedemptionRecordInfo redemptionRecordInfo = new RedemptionRecordInfo();
        BeanUtils.copyProperties(redemptionRecordInfoVo, redemptionRecordInfo);
        redemptionRecordInfoMapper.insert(redemptionRecordInfo);
        return true;
    }

    @Override
    public Boolean updateRedemptionRecordInfo(RedemptionRecordInfoVo redemptionRecordInfoVo) {
        RedemptionRecordInfo redemptionRecordInfo = new RedemptionRecordInfo();
        BeanUtils.copyProperties(redemptionRecordInfoVo, redemptionRecordInfo);
        redemptionRecordInfoMapper.updateById(redemptionRecordInfo);
        return true;
    }

    @Override
    public Boolean deleteRedemptionRecordInfo(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        redemptionRecordInfoMapper.deleteBatchIds(idArr);
        return true;
    }
}
