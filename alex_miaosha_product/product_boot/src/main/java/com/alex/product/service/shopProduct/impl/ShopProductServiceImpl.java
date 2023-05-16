package com.alex.product.service.shopProduct.impl;

import com.alex.api.product.vo.product.jd.Content;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.ProductException;
import com.alex.product.entity.pmsShopProduct.PmsShopProduct;
import com.alex.product.enums.SourceType;
import com.alex.product.service.pmsShopProduct.PmsShopProductService;
import com.alex.product.service.shopProduct.ShopProductService;
import com.alex.product.service.shopProduct.jd.JdProductService;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopProductServiceImpl implements ShopProductService {

    private final JdProductService jdProductService;

    private final PmsShopProductService pmsShopProductService;

    @Override
    public List<Content> getShopProduct(String type) throws Exception {
        SourceType sourceType = SourceType.getSourceTypeByName(type);
        if (sourceType == null) {
            throw new ProductException(ResultEnum.GOODS_SOURCE_TYPE_NO_EXISTS);
        }
        // TODO: 2023/5/15 获取商品描述列表
        List<String> productList = Lists.newArrayList("苹果airpods二代pro", "16升燃气热水器天然气全密闭稳燃舱",
                "石头扫地机器人g20", "海尔冰箱三门风冷无霜");
        List<Content> result = Lists.newArrayList();
        // TODO: 2023/5/15 添加从淘宝获取数据
        switch (sourceType) {
            case JD -> result = jdProductService.parseJD(productList);
        }
        if (result != null && !result.isEmpty()) {
            List<PmsShopProduct> collect = result.parallelStream().map(item -> {
                PmsShopProduct pmsShopProduct = new PmsShopProduct();
                BeanUtils.copyProperties(item, pmsShopProduct);
                pmsShopProduct.setPrice(Optional.ofNullable(item).map(a -> new BigDecimal(a.getPrice())).orElse(null));
                return pmsShopProduct;
            }).collect(Collectors.toList());
            pmsShopProductService.saveBatch(collect);
        }
        return result;
    }
}
