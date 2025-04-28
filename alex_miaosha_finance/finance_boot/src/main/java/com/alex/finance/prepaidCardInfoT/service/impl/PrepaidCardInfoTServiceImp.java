package com.alex.finance.prepaidCardInfoT.service.impl;

import com.alex.finance.prepaidCardInfoT.entity.PrepaidCardInfoT;
import com.alex.api.finance.prepaidCardInfoT.vo.PrepaidCardInfoTVo;
import com.alex.finance.prepaidCardInfoT.mapper.PrepaidCardInfoTMapper;
import com.alex.finance.prepaidCardInfoT.service.PrepaidCardInfoTService;
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
 * description:  消费卡信息表服务实现类
 * author:       alex
 * createDate:   2025-04-28 20:58:16
 * version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class PrepaidCardInfoTServiceImp extends ServiceImpl<PrepaidCardInfoTMapper, PrepaidCardInfoT> implements PrepaidCardInfoTService {

    private final PrepaidCardInfoTMapper prepaidCardInfoTMapper;

    @Override
    public Page<PrepaidCardInfoTVo> getPage(Long pageNum, Long pageSize, PrepaidCardInfoTVo prepaidCardInfoTVo) {
        Page<PrepaidCardInfoTVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return prepaidCardInfoTMapper.getPage(page, prepaidCardInfoTVo);
    }

    @Override
    public List<PrepaidCardInfoTVo> getList(PrepaidCardInfoTVo prepaidCardInfoTVo) {
        return prepaidCardInfoTMapper.getList(prepaidCardInfoTVo);
    }

    @Override
    public PrepaidCardInfoTVo queryPrepaidCardInfoT(Long id) {
        return prepaidCardInfoTMapper.queryPrepaidCardInfoT(id);
    }

    @Override
    public Boolean addPrepaidCardInfoT(PrepaidCardInfoTVo prepaidCardInfoTVo) {
        PrepaidCardInfoT prepaidCardInfoT = new PrepaidCardInfoT();
        BeanUtils.copyProperties(prepaidCardInfoTVo, prepaidCardInfoT);
        prepaidCardInfoTMapper.insert(prepaidCardInfoT);
        return true;
    }

    @Override
    public Boolean updatePrepaidCardInfoT(PrepaidCardInfoTVo prepaidCardInfoTVo) {
        PrepaidCardInfoT prepaidCardInfoT = new PrepaidCardInfoT();
        BeanUtils.copyProperties(prepaidCardInfoTVo, prepaidCardInfoT);
        prepaidCardInfoTMapper.updateById(prepaidCardInfoT);
        return true;
    }

    @Override
    public Boolean deletePrepaidCardInfoT(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        prepaidCardInfoTMapper.deleteBatchIds(idArr);
        return true;
    }
}
