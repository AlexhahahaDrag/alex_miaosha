package com.alex.product.service.pmsAttr.impl;

import com.alex.api.product.vo.pmsAttr.PmsAttrVo;
import com.alex.common.utils.string.StringUtils;
import com.alex.product.entity.pmsAttr.PmsAttr;
import com.alex.product.mapper.pmsAttr.PmsAttrMapper;
import com.alex.product.service.pmsAttr.PmsAttrService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * description:  商品属性服务实现类
 * author:       alex
 * createDate:   2023-12-28 19:50:53
 * version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class PmsAttrServiceImp extends ServiceImpl<PmsAttrMapper, PmsAttr> implements PmsAttrService {

    private final PmsAttrMapper pmsAttrMapper;

    @Override
    public Page<PmsAttrVo> getPage(Long pageNum, Long pageSize, PmsAttrVo pmsAttrVo) {
        Page<PmsAttrVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return pmsAttrMapper.getPage(page, pmsAttrVo);
    }

    @Override
    public PmsAttrVo queryPmsAttr(String id) {
        return pmsAttrMapper.queryPmsAttr(id);
    }

    @Override
    public Boolean addPmsAttr(PmsAttrVo pmsAttrVo) {
        PmsAttr pmsAttr = new PmsAttr();
        BeanUtils.copyProperties(pmsAttrVo, pmsAttr);
        pmsAttrMapper.insert(pmsAttr);
        return true;
    }

    @Override
    public Boolean updatePmsAttr(PmsAttrVo pmsAttrVo) {
        PmsAttr pmsAttr = new PmsAttr();
        BeanUtils.copyProperties(pmsAttrVo, pmsAttr);
        pmsAttrMapper.updateById(pmsAttr);
        return true;
    }

    @Override
    public Boolean deletePmsAttr(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        pmsAttrMapper.deleteBatchIds(idArr);
        return true;
    }
}
