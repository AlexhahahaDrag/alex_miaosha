package com.alex.product.service.shopProduct.jd.impl;

import com.alex.api.product.vo.product.jd.Content;
import com.alex.product.enums.SourceType;
import com.alex.product.service.shopProduct.jd.JdProductService;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JdProductServiceImpl implements JdProductService {

    @Value("${shop.jdUrl}")
    private String jdUrl;

    public List<Content> parse(List<String> keywords, String type) throws Exception {
        if (keywords == null || keywords.isEmpty()) {
            throw new Exception("请填写关键字");
        }
        List<Content> res = Lists.newArrayList();
        for (String keyword : keywords) {
            switch (type) {
                case "jd":
                    res.addAll(parseJd(keyword));
                    break;
                case "tb":
                    res.addAll(parseTB(keyword));
                    break;
            }
            Thread.sleep(2000);
        }
        return res;
    }

    private List<Content> parseJd(String keyword) throws IOException {
        String url = jdUrl + "/Search?keyword=" + keyword;
        Document document = Jsoup.parse(new URL(new String(url.getBytes(), "utf-8")), 3000);
        Element j_goodsList = document.getElementById("J_goodsList");
        Elements lis = j_goodsList.getElementsByTag("li");
        List<Content> list = new ArrayList<>();
        for (Element element : lis) {
            String img = element.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String price = element.getElementsByClass("p-price").get(0).getElementsByTag("i").get(0).text();
            String productUrl = element.getElementsByClass("p-img").get(0).getElementsByTag("a").get(0).attr("href");
            String name = element.getElementsByClass("p-name").eq(0).text();
            String shop = element.getElementsByClass("p-shop").eq(0).text();
            String icons = element.getElementsByClass("p-icons").eq(0).text();
            String skuId = Optional.ofNullable(productUrl).map(i -> i.substring(i.indexOf("com/") + 4, i.length() - 12)).orElse(null);
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
     * description: 获取淘宝信息
     * author:      alex
     * @return:      java.util.List<com.alex.api.product.vo.product.jd.Content>
    */
    // TODO: 2023/5/25 获取淘宝信息
    private List<Content> parseTB(String keyword) throws IOException {
        return Lists.newArrayList();
    }
}
