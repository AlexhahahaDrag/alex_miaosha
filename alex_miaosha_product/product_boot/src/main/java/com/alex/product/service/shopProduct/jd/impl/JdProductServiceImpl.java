package com.alex.product.service.shopProduct.jd.impl;

import com.alex.api.product.vo.product.jd.Content;
import com.alex.product.enums.SourceType;
import com.alex.product.service.shopProduct.jd.JdProductService;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.SystemException;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JdProductServiceImpl implements JdProductService {

    @Value("${shop.jdUrl}")
    private String jdUrl;

    @Override
    public List<Content> parse(List<String> keywords, String type) {
        if (keywords == null || keywords.isEmpty()) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "请填写关键字");
        }
        List<Content> res = Lists.newArrayList();
        for (String keyword : keywords) {
            try {
                if ("jd".equalsIgnoreCase(type)) {
                    res.addAll(parseJd(keyword));
                } else if ("tb".equalsIgnoreCase(type)) {
                    res.addAll(parseTB(keyword));
                }
            } catch (Exception e) {
                log.error("抓取商品信息异常, keyword={}, type={}", keyword, type, e);
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("商品抓取休眠被中断: keyword={}", keyword, e);
                break;
            }
        }
        return res;
    }

    private List<Content> parseJd(String keyword) throws IOException {
        String url = jdUrl + "/Search?keyword=" + keyword;
        Document document = Jsoup.parse(new URL(new String(url.getBytes(), StandardCharsets.UTF_8)), 3000);
        Element j_goodsList = document.getElementById("J_goodsList");
        assert j_goodsList != null;
        Elements lis = j_goodsList.getElementsByTag("li");
        List<Content> list = new ArrayList<>();
        for (Element element : lis) {
            String img = element.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String price = element.getElementsByClass("p-price").get(0).getElementsByTag("i").get(0).text();
            String productUrl = element.getElementsByClass("p-img").get(0).getElementsByTag("a").get(0).attr("href");
            String name = element.getElementsByClass("p-name").eq(0).text();
            String shop = element.getElementsByClass("p-shop").eq(0).text();
            String icons = element.getElementsByClass("p-icons").eq(0).text();
            String skuId = Optional.of(productUrl).map(i -> i.substring(i.indexOf("com/") + 4, i.length() - 12))
                    .orElse(null);
            Content content = Content.builder()
                    .image(img)
                    .name(name)
                    .shop(shop)
                    .price(price)
                    .icons(icons)
                    .productUrl(productUrl)
                    .source(SourceType.JD.getCode())
                    .searchKey(keyword)
                    .skuId(skuId)
                    .build();
            list.add(content);
        }
        return list;
    }

    /**
     * @param keyword
     *                description: 获取淘宝信息
     *                author: alex
     *                return:
     *                java.util.List<com.alex.api.product.vo.product.jd.Content>
     */
    // TODO: 2023/5/25 获取淘宝信息
    private List<Content> parseTB(String keyword) throws IOException {
        return Lists.newArrayList();
    }
}
