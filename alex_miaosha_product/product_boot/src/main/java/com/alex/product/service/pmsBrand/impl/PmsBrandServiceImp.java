package com.alex.product.service.pmsBrand.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alex.api.product.vo.pmsBrand.PmsBrandVo;
import com.alex.common.utils.string.StringUtils;
import com.alex.product.entity.pmsBrand.PmsBrand;
import com.alex.product.mapper.pmsBrand.PmsBrandMapper;
import com.alex.product.service.pmsBrand.PmsBrandService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * @description:  品牌服务实现类
 * @author:       alex
 * @createDate:   2023-03-05 21:39:54
 * @version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class PmsBrandServiceImp extends ServiceImpl<PmsBrandMapper, PmsBrand> implements PmsBrandService {

    private final PmsBrandMapper pmsBrandMapper;

    @Override
    public Page<PmsBrandVo> getPage(Long pageNum, Long pageSize, PmsBrandVo pmsBrandVo) {
        Page<PmsBrandVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return pmsBrandMapper.getPage(page, pmsBrandVo);
    }

    @Override
    public PmsBrandVo queryPmsBrand(String id) {
        return pmsBrandMapper.queryPmsBrand(id);
    }

    @Override
    public Boolean addPmsBrand(PmsBrandVo pmsBrandVo) {
        PmsBrand pmsBrand = new PmsBrand();
        BeanUtil.copyProperties(pmsBrandVo, pmsBrand);
        pmsBrandMapper.insert(pmsBrand);
        return true;
    }

    @Override
    public Boolean updatePmsBrand(PmsBrandVo pmsBrandVo) {
        PmsBrand pmsBrand = new PmsBrand();
        BeanUtil.copyProperties(pmsBrandVo, pmsBrand);
        pmsBrandMapper.updateById(pmsBrand);
        return true;
    }

    @Override
    public Boolean deletePmsBrand(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        pmsBrandMapper.deleteBatchIds(idArr);
        return true;
    }

    @Override
    public String test() {
        return "123";
    }
}
