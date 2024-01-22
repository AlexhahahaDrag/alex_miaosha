package com.alex.product.service.shopProduct.jd;

import com.alex.api.product.vo.product.jd.Content;

import java.util.List;

public interface JdProductService {

    List<Content> parse(List<String> keywords, String type) throws Exception;
}
