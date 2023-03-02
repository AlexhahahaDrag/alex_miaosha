package com.alex.product.service.pmsBrand.impl;

import com.alex.product.entity.pmsBrand.PmsBrand;
import com.alex.api.product.vo.pmsBrand.PmsBrandVo;
import com.alex.product.mapper.pmsBrand.PmsBrandMapper;
import com.alex.product.service.pmsBrand.PmsBrandService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import cn.hutool.core.bean.BeanUtil;
import com.alex.common.utils.string.StringUtils;

/**
 * <p>
 * @description:  品牌服务实现类
 * @author:       alex
 * @createDate:   2023-03-02 19:16:11
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
}
