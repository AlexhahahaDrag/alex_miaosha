package com.alex.product.service.shopProduct.impl;

import com.alex.api.product.vo.pmsShopWantProduct.PmsShopWantProductVo;
import com.alex.api.product.vo.product.jd.Content;
import com.alex.product.entity.pmsShopProduct.PmsShopProduct;
import com.alex.product.enums.SourceType;
import com.alex.product.service.pmsShopProduct.PmsShopProductService;
import com.alex.product.service.pmsShopWantProduct.PmsShopWantProductService;
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

    private final PmsShopWantProductService pmsShopWantProductService;

    @Override
    public List<Content> getShopProduct() throws Exception {

        PmsShopWantProductVo pmsShopWantProductVo = PmsShopWantProductVo.builder().status(1).build();
        List<PmsShopWantProductVo> productList = pmsShopWantProductService.getList(pmsShopWantProductVo);
        List<Content> result = Lists.newArrayList();
        SourceType[] sourceTypeArr = SourceType.values();
        for (SourceType sourceType : sourceTypeArr) {
            switch (sourceType) {
                case JD:
                    List<String> jdList = productList.parallelStream()
                            .filter(item -> SourceType.JD.getCode().equals(item.getSource()))
                            .map(this::getStr)
                            .collect(Collectors.toList());
                    if (jdList.isEmpty()) {
                        break;
                    }
                    result.addAll(jdProductService.parse(jdList, sourceType.getCode()));
                    break;
                case TB:
                    List<String> tbList = productList.parallelStream()
                            .filter(item -> SourceType.TB.getCode().equals(item.getSource()))
                            .map(this::getStr)
                            .collect(Collectors.toList());
                    if (tbList.isEmpty()) {
                        break;
                    }
                    result.addAll(jdProductService.parse(tbList, sourceType.getCode()));
                    break;
            }
        }
        if (!result.isEmpty()) {
            List<PmsShopProduct> collect = result.parallelStream().map(item -> {
                PmsShopProduct pmsShopProduct = new PmsShopProduct();
                BeanUtils.copyProperties(item, pmsShopProduct);
                pmsShopProduct.setPrice(Optional.of(item).map(a -> new BigDecimal(a.getPrice())).orElse(null));
                return pmsShopProduct;
            }).collect(Collectors.toList());
            pmsShopProductService.saveBatch(collect);
        }
        return result;
    }

    private String getStr(PmsShopWantProductVo pmsShopWantProductVo) {
        return Optional.ofNullable(pmsShopWantProductVo).map(PmsShopWantProductVo::getName).orElse("") +
                Optional.ofNullable(pmsShopWantProductVo).map(PmsShopWantProductVo::getShop).orElse("") +
                Optional.ofNullable(pmsShopWantProductVo).map(PmsShopWantProductVo::getIcons).orElse("");
    }
}
