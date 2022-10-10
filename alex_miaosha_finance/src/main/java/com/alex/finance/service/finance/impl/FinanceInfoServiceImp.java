package com.alex.finance.service.finance.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alex.finance.entity.finance.FinanceInfo;
import com.alex.finance.mapper.finance.FinanceInfoMapper;
import com.alex.finance.service.finance.FinanceInfoService;
import com.alex.finance.vo.finance.FinanceInfoVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *
 * @description: 财务信息表服务实现类
 * @author: majf
 * @createDate: 2022-10-10 16:56:00
 * @version: 1.0.0
 */
@Service
@RequiredArgsConstructor
public class FinanceInfoServiceImp extends ServiceImpl<FinanceInfoMapper, FinanceInfo> implements FinanceInfoService {

    private final FinanceInfoMapper financeInfoMapper;

    @Override
    public Page<FinanceInfoVo> getPage(Long pageNum, Long pageSize, FinanceInfoVo financeInfoVo) {
        Page<FinanceInfoVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return financeInfoMapper.getPage(page, financeInfoVo);
    }

    @Override
    public List<FinanceInfoVo> getList(FinanceInfoVo financeInfoVo) {
        return financeInfoMapper.getList(financeInfoVo);
    }

    @Override
    public FinanceInfoVo queryFinanceInfo(String id) {
        return financeInfoMapper.queryFinanceInfo(id);
    }

    @Override
    public FinanceInfo addFinanceInfo(FinanceInfoVo financeInfoVo) {
        FinanceInfo financeInfo = new FinanceInfo();
        BeanUtil.copyProperties(financeInfoVo, financeInfo);
        financeInfoMapper.insert(financeInfo);
        return financeInfo;
    }

    @Override
    public FinanceInfo updateFinanceInfo(FinanceInfoVo financeInfoVo) {
        FinanceInfo financeInfo = new FinanceInfo();
        BeanUtil.copyProperties(financeInfoVo, financeInfo);
        financeInfoMapper.updateById(financeInfo);
        return financeInfo;
    }

    @Override
    public Boolean deleteFinanceInfo(List<String> ids) {
        financeInfoMapper.deleteBatchIds(ids);
        return true;
    }
}
