package com.alex.product.service.pmsShopWantProduct.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alex.api.product.vo.pmsShopWantProduct.PmsShopWantProductVo;
import com.alex.common.utils.string.StringUtils;
import com.alex.product.entity.pmsShopWantProduct.PmsShopWantProduct;
import com.alex.product.mapper.pmsShopWantProduct.PmsShopWantProductMapper;
import com.alex.product.service.pmsShopWantProduct.PmsShopWantProductService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * @description:  商品想买网上商品信息服务实现类
 * @author:       alex
 * @createDate:   2023-05-25 16:18:10
 * @version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class PmsShopWantProductServiceImp extends ServiceImpl<PmsShopWantProductMapper, PmsShopWantProduct> implements PmsShopWantProductService {

    private final PmsShopWantProductMapper pmsShopWantProductMapper;

    @Override
    public Page<PmsShopWantProductVo> getPage(Long pageNum, Long pageSize, PmsShopWantProductVo pmsShopWantProductVo) {
        Page<PmsShopWantProductVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return pmsShopWantProductMapper.getPage(page, pmsShopWantProductVo);
    }

    @Override
    public List<PmsShopWantProductVo> getList(PmsShopWantProductVo pmsShopWantProductVo) {
        return pmsShopWantProductMapper.getList(pmsShopWantProductVo);
    }

    @Override
    public PmsShopWantProductVo queryPmsShopWantProduct(String id) {
        return pmsShopWantProductMapper.queryPmsShopWantProduct(id);
    }

    @Override
    public Boolean addPmsShopWantProduct(PmsShopWantProductVo pmsShopWantProductVo) {
        PmsShopWantProduct pmsShopWantProduct = new PmsShopWantProduct();
        BeanUtil.copyProperties(pmsShopWantProductVo, pmsShopWantProduct);
        pmsShopWantProductMapper.insert(pmsShopWantProduct);
        return true;
    }

    @Override
    public Boolean updatePmsShopWantProduct(PmsShopWantProductVo pmsShopWantProductVo) {
        PmsShopWantProduct pmsShopWantProduct = new PmsShopWantProduct();
        BeanUtil.copyProperties(pmsShopWantProductVo, pmsShopWantProduct);
        pmsShopWantProductMapper.updateById(pmsShopWantProduct);
        return true;
    }

    @Override
    public Boolean deletePmsShopWantProduct(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        pmsShopWantProductMapper.deleteBatchIds(idArr);
        return true;
    }
}
