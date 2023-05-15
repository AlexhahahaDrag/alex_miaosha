package com.alex.product.service.shopProduct;

import com.alex.api.product.vo.product.jd.Content;

import java.util.List;

public interface ShopProductService {

    List<Content> getShopProduct(String type) throws Exception;
}
