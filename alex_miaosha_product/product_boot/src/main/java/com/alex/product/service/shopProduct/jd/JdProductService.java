package com.alex.product.service.shopProduct.jd;

import com.alex.api.product.vo.product.jd.Content;

import java.util.List;

public interface JdProductService {

    List<Content> parseJD(List<String> keywords) throws Exception;
}
