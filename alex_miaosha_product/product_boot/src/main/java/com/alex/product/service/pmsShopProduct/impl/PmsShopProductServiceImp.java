package com.alex.product.service.pmsShopProduct.impl;

import com.alex.api.product.vo.pmsShopProduct.PmsShopProductVo;
import com.alex.common.utils.date.DateUtils;
import com.alex.common.utils.string.StringUtils;
import com.alex.product.entity.pmsShopProduct.PmsShopProduct;
import com.alex.product.mapper.pmsShopProduct.PmsShopProductMapper;
import com.alex.product.service.pmsShopProduct.PmsShopProductService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * description:  商品网上商品信息服务实现类
 * author:       alex
 * createDate:   2023-05-15 14:11:10
 * version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class PmsShopProductServiceImp extends ServiceImpl<PmsShopProductMapper, PmsShopProduct> implements PmsShopProductService {

    private final PmsShopProductMapper pmsShopProductMapper;

    @Override
    public Page<PmsShopProductVo> getPage(Long pageNum, Long pageSize, PmsShopProductVo pmsShopProductVo) {
        Page<PmsShopProductVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return pmsShopProductMapper.getPage(page, pmsShopProductVo);
    }

    @Override
    public PmsShopProductVo queryPmsShopProduct(String id) {
        return pmsShopProductMapper.queryPmsShopProduct(id);
    }

    @Override
    public Boolean addPmsShopProduct(PmsShopProductVo pmsShopProductVo) {
        PmsShopProduct pmsShopProduct = new PmsShopProduct();
        BeanUtils.copyProperties(pmsShopProductVo, pmsShopProduct);
        pmsShopProductMapper.insert(pmsShopProduct);
        return true;
    }

    @Override
    public Boolean updatePmsShopProduct(PmsShopProductVo pmsShopProductVo) {
        PmsShopProduct pmsShopProduct = new PmsShopProduct();
        BeanUtils.copyProperties(pmsShopProductVo, pmsShopProduct);
        pmsShopProductMapper.updateById(pmsShopProduct);
        return true;
    }

    @Override
    public Boolean deletePmsShopProduct(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        pmsShopProductMapper.deleteBatchIds(idArr);
        return true;
    }

    @Override
    public Page<PmsShopProductVo> getNewestProductPage(Long pageNum, Long pageSize, PmsShopProductVo pmsShopProductVo) {
        Page<PmsShopProductVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
       return pmsShopProductMapper.getNewestProductPage(page, pmsShopProductVo);
    }

    @Override
    public List<PmsShopProductVo> getProductHisInfo(String skuId, String startTime) {
        // 默认取近一个月的数据
        if (StringUtils.isEmpty(startTime)) {
            startTime = DateUtils.getToDayStartTimeMinusNDay(LocalDateTime.now(), 30);
        }
        return pmsShopProductMapper.getProductHisInfo(skuId, startTime);
    }

    @Override
    public List<PmsShopProductVo> getCompareInfo(String key, String searchTime) {
        return pmsShopProductMapper.getCompareInfo(key, searchTime);
    }

    @Override
    public Boolean updateCompareInfo(String skuId, Long chooseId) {
        LambdaQueryWrapper<PmsShopProduct> query = Wrappers.<PmsShopProduct>lambdaQuery()
                .eq(PmsShopProduct::getSkuId, skuId)
                .eq(PmsShopProduct::getIsCompare, true);
        List<PmsShopProduct> list = this.list(query);
        if (list != null && !list.isEmpty()) {
            list.forEach(item -> item.setIsCompare(false));
        }
        PmsShopProduct pmsShopProduct = PmsShopProduct.builder().skuId(skuId).build();
        pmsShopProduct.setId(chooseId);
        List<PmsShopProduct> res = Lists.newArrayList(pmsShopProduct);
        res.addAll(list);
        this.updateBatchById(res);
        return true;
    }
}
