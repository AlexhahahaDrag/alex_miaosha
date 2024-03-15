package com.alex.product.service.pmsCategory.impl;

import com.alex.api.product.vo.pmsCategory.PmsCategoryVo;
import com.alex.common.utils.bean.BeanUtils;
import com.alex.common.utils.string.StringUtils;
import com.alex.product.entity.pmsCategory.PmsCategory;
import com.alex.product.mapper.pmsCategory.PmsCategoryMapper;
import com.alex.product.service.pmsCategory.PmsCategoryService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * description:  商品三级分类服务实现类
 * author:       alex
 * createDate:   2023-03-17 10:06:58
 * version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class PmsCategoryServiceImp extends ServiceImpl<PmsCategoryMapper, PmsCategory> implements PmsCategoryService {

    private final PmsCategoryMapper pmsCategoryMapper;

    @Override
    public Page<PmsCategoryVo> getPage(Long pageNum, Long pageSize, PmsCategoryVo pmsCategoryVo) {
        Page<PmsCategoryVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return pmsCategoryMapper.getPage(page, pmsCategoryVo);
    }

    @Override
    public PmsCategoryVo queryPmsCategory(String id) {
        return pmsCategoryMapper.queryPmsCategory(id);
    }

    @Override
    public Boolean addPmsCategory(PmsCategoryVo pmsCategoryVo) {
        PmsCategory pmsCategory = new PmsCategory();
        BeanUtils.copyProperties(pmsCategoryVo, pmsCategory);
        pmsCategoryMapper.insert(pmsCategory);
        return true;
    }

    @Override
    public Boolean updatePmsCategory(PmsCategoryVo pmsCategoryVo) {
        PmsCategory pmsCategory = new PmsCategory();
        BeanUtils.copyProperties(pmsCategoryVo, pmsCategory);
        pmsCategoryMapper.updateById(pmsCategory);
        return true;
    }

    @Override
    public Boolean deletePmsCategory(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        pmsCategoryMapper.deleteBatchIds(idArr);
        return true;
    }
}
