package com.alex.finance.prepaidConsumeRecordT.service.impl;

import com.alex.finance.prepaidConsumeRecordT.entity.PrepaidConsumeRecordT;
import com.alex.api.finance.prepaidConsumeRecordT.vo.PrepaidConsumeRecordTVo;
import com.alex.finance.prepaidConsumeRecordT.mapper.PrepaidConsumeRecordTMapper;
import com.alex.finance.prepaidConsumeRecordT.service.PrepaidConsumeRecordTService;
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
 * description:  消费卡交易记录表服务实现类
 * author:       alex
 * createDate:   2025-04-28 20:58:14
 * version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class PrepaidConsumeRecordTServiceImp extends ServiceImpl<PrepaidConsumeRecordTMapper, PrepaidConsumeRecordT> implements PrepaidConsumeRecordTService {

    private final PrepaidConsumeRecordTMapper prepaidConsumeRecordTMapper;

    @Override
    public Page<PrepaidConsumeRecordTVo> getPage(Long pageNum, Long pageSize, PrepaidConsumeRecordTVo prepaidConsumeRecordTVo) {
        Page<PrepaidConsumeRecordTVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return prepaidConsumeRecordTMapper.getPage(page, prepaidConsumeRecordTVo);
    }

    @Override
    public List<PrepaidConsumeRecordTVo> getList(PrepaidConsumeRecordTVo prepaidConsumeRecordTVo) {
        return prepaidConsumeRecordTMapper.getList(prepaidConsumeRecordTVo);
    }

    @Override
    public PrepaidConsumeRecordTVo queryPrepaidConsumeRecordT(Long id) {
        return prepaidConsumeRecordTMapper.queryPrepaidConsumeRecordT(id);
    }

    @Override
    public Boolean addPrepaidConsumeRecordT(PrepaidConsumeRecordTVo prepaidConsumeRecordTVo) {
        PrepaidConsumeRecordT prepaidConsumeRecordT = new PrepaidConsumeRecordT();
        BeanUtils.copyProperties(prepaidConsumeRecordTVo, prepaidConsumeRecordT);
        prepaidConsumeRecordTMapper.insert(prepaidConsumeRecordT);
        return true;
    }

    @Override
    public Boolean updatePrepaidConsumeRecordT(PrepaidConsumeRecordTVo prepaidConsumeRecordTVo) {
        PrepaidConsumeRecordT prepaidConsumeRecordT = new PrepaidConsumeRecordT();
        BeanUtils.copyProperties(prepaidConsumeRecordTVo, prepaidConsumeRecordT);
        prepaidConsumeRecordTMapper.updateById(prepaidConsumeRecordT);
        return true;
    }

    @Override
    public Boolean deletePrepaidConsumeRecordT(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        prepaidConsumeRecordTMapper.deleteBatchIds(idArr);
        return true;
    }
}
